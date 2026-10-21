package com.uzima.ui.quote;

import com.uzima.app.Services;
import com.uzima.domain.catalog.CatalogItem;
import com.uzima.domain.catalog.DrillingType;
import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.catalog.PumpType;
import com.uzima.domain.catalog.TankType;
import com.uzima.domain.client.Client;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.Metres;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.job.Job;
import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.Quote;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.domain.money.Money;
import com.uzima.service.ClientSearch;
import com.uzima.service.ClientSummary;
import com.uzima.ui.Backend;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.animation.PauseTransition;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State behind the quote editor: the client, the four services and their inputs, and the live
 * quote. Every change asks the pricing engine for a fresh preview (debounced, off the FX thread,
 * newest request wins); the editor never does arithmetic itself.
 *
 * <p>While the user types, missing inputs are listed in the breakdown. After the first save attempt
 * they are also shown under their fields, and stay live from then on.
 */
public final class QuoteEditorViewModel {

  /** How plumbing is priced: per pipe run and outlet, or as one agreed amount. */
  public enum PlumbingMode {
    ITEMISED,
    MANUAL
  }

  /** Whether the editor's reference data has arrived. */
  public enum State {
    LOADING,
    READY,
    ERROR
  }

  static final String LOAD_FAILED = "The quote editor couldn't load clients and prices. Try again.";
  static final String SAVE_FAILED =
      "The quote couldn't be saved just now. Try again; your entries are still here.";

  private static final Logger log = LoggerFactory.getLogger(QuoteEditorViewModel.class);

  private final Backend backend;
  private final OptionalLong jobId;
  private final OptionalLong initialClient;
  private final PauseTransition debounce;
  private long previewSequence;
  private boolean loading;

  private final ObservableList<ClientSummary> clients = FXCollections.observableArrayList();
  private final ObservableList<DrillingType> drillingTypes = FXCollections.observableArrayList();
  private final ObservableList<PumpType> pumpTypes = FXCollections.observableArrayList();
  private final ObservableList<TankType> tanks = FXCollections.observableArrayList();
  private final ObservableList<PipeType> pipes = FXCollections.observableArrayList();

  private final ObjectProperty<ClientSummary> client = new SimpleObjectProperty<>(this, "client");
  private final BooleanProperty drillingOn = new SimpleBooleanProperty(this, "drillingOn", true);
  private final ObjectProperty<DrillingType> drillingType = new SimpleObjectProperty<>();
  private final ObjectProperty<Metres> depth = new SimpleObjectProperty<>(this, "depth");
  private final BooleanProperty pumpOn = new SimpleBooleanProperty(this, "pumpOn");
  private final ObjectProperty<PumpType> pumpType = new SimpleObjectProperty<>();
  private final ObjectProperty<Metres> lift = new SimpleObjectProperty<>(this, "lift");
  private final BooleanProperty tankOn = new SimpleBooleanProperty(this, "tankOn");
  private final ObjectProperty<TankType> tank = new SimpleObjectProperty<>(this, "tank");
  private final BooleanProperty plumbingOn = new SimpleBooleanProperty(this, "plumbingOn");
  private final ObjectProperty<PlumbingMode> plumbingMode =
      new SimpleObjectProperty<>(this, "plumbingMode", PlumbingMode.ITEMISED);
  private final ObservableList<PipeRunRow> runs =
      FXCollections.observableArrayList(
          r -> new Observable[] {r.pipeProperty(), r.diameterProperty(), r.lengthProperty()});
  private final StringProperty outlets = new SimpleStringProperty(this, "outlets", "0");
  private final ObjectProperty<Money> manualAmount = new SimpleObjectProperty<>();
  private final StringProperty manualDescription = new SimpleStringProperty(this, "desc", "");
  private final StringProperty notes = new SimpleStringProperty(this, "notes", "");

  private final ObjectProperty<State> state = new SimpleObjectProperty<>(this, "state");
  private final ObjectProperty<Quote> quote = new SimpleObjectProperty<>(this, "quote");
  private final ObservableList<String> missing = FXCollections.observableArrayList();
  private final ObservableMap<String, String> errors = FXCollections.observableHashMap();
  private final StringProperty failure = new SimpleStringProperty(this, "failure", "");
  private final BooleanProperty busy = new SimpleBooleanProperty(this, "busy");
  private final BooleanProperty dirty = new SimpleBooleanProperty(this, "dirty");
  private final ObjectProperty<Job> job = new SimpleObjectProperty<>(this, "job");
  private boolean showErrors;

  /**
   * @param jobId the pending job to edit, or empty for a new quote
   * @param clientId the client to start with for a new quote
   * @param debounceMillis delay before re-pricing after a change; 0 prices at once (tests)
   */
  public QuoteEditorViewModel(
      Backend backend, OptionalLong jobId, OptionalLong clientId, double debounceMillis) {
    this.backend = backend;
    this.jobId = jobId;
    this.initialClient = clientId;
    this.debounce = new PauseTransition(Duration.millis(debounceMillis));
    debounce.setOnFinished(e -> preview());
    Observable[] inputs = {
      client,
      drillingOn,
      drillingType,
      depth,
      pumpOn,
      pumpType,
      lift,
      tankOn,
      tank,
      plumbingOn,
      plumbingMode,
      runs,
      outlets,
      manualAmount,
      manualDescription
    };
    for (Observable input : inputs) {
      input.addListener(o -> changed());
    }
    notes.addListener(o -> markDirty());
  }

  // ------------------------------------------------------------------ loading

  /** Loads clients, active catalog items and, when editing, the job. */
  public void load() {
    state.set(State.LOADING);
    backend.run(
        this::fetch,
        this::apply,
        error -> {
          log.error("Quote editor failed to load", error);
          state.set(State.ERROR);
        });
  }

  private Loaded fetch(Services s) {
    List<ClientSummary> active =
        s.clients().search(ClientSearch.all()).stream().filter(ClientSummary::active).toList();
    Optional<Job> existing = Optional.empty();
    Optional<Client> owner = Optional.empty();
    if (jobId.isPresent()) {
      existing = s.jobs().find(jobId.getAsLong());
      owner = existing.flatMap(j -> s.clients().find(j.clientId()));
    }
    return new Loaded(
        active,
        s.catalog().activeDrillingTypes(),
        s.catalog().activePumpTypes(),
        s.catalog().activeTankTypes(),
        s.catalog().activePipeTypes(),
        existing,
        owner);
  }

  private record Loaded(
      List<ClientSummary> clients,
      List<DrillingType> drilling,
      List<PumpType> pumps,
      List<TankType> tanks,
      List<PipeType> pipes,
      Optional<Job> job,
      Optional<Client> owner) {}

  private void apply(Loaded data) {
    loading = true;
    try {
      clients.setAll(data.clients());
      drillingTypes.setAll(data.drilling());
      pumpTypes.setAll(data.pumps());
      tanks.setAll(data.tanks());
      pipes.setAll(data.pipes());
      data.job().ifPresentOrElse(j -> fill(j, data.owner()), this::startNew);
    } finally {
      loading = false;
    }
    state.set(State.READY);
    dirty.set(false);
    preview();
  }

  private void startNew() {
    initialClient.ifPresent(
        id -> clients.stream().filter(c -> c.id() == id).findFirst().ifPresent(client::set));
  }

  /** Puts a saved job's services in the form, at the prices it was quoted with. */
  private void fill(Job saved, Optional<Client> owner) {
    job.set(saved);
    owner.ifPresent(
        c -> {
          ClientSummary summary =
              new ClientSummary(
                  c.id(),
                  c.code(),
                  c.details().name(),
                  c.details().phone(),
                  c.details().category(),
                  c.details().county(),
                  c.active());
          clients.removeIf(x -> x.id() == c.id());
          clients.add(0, summary);
          client.set(summary);
        });
    QuoteSpec spec = saved.spec();
    drillingOn.set(spec.drilling().isPresent());
    spec.drilling()
        .ifPresent(
            d -> {
              drillingType.set(asQuoted(drillingTypes, d.type()));
              depth.set(d.depth());
            });
    pumpOn.set(spec.pump().isPresent());
    spec.pump()
        .ifPresent(
            p -> {
              pumpType.set(asQuoted(pumpTypes, p.type()));
              lift.set(p.liftHeight());
            });
    tankOn.set(spec.tank().isPresent());
    spec.tank().ifPresent(t -> tank.set(asQuoted(tanks, t)));
    plumbingOn.set(spec.plumbing().isPresent());
    spec.plumbing().ifPresent(this::fillPlumbing);
    notes.set(saved.notes());
  }

  private void fillPlumbing(Plumbing plumbing) {
    switch (plumbing) {
      case Plumbing.Manual m -> {
        plumbingMode.set(PlumbingMode.MANUAL);
        manualAmount.set(m.amount());
        manualDescription.set(m.description());
      }
      case Plumbing.Itemised i -> {
        plumbingMode.set(PlumbingMode.ITEMISED);
        runs.setAll(
            i.runs().stream()
                .map(r -> new PipeRunRow(asQuoted(pipes, r.pipe()), r.diameterMm(), r.length()))
                .toList());
        outlets.set(String.valueOf(i.outlets()));
      }
    }
  }

  /**
   * The saved job's own copy of a catalog item, so the picker shows the price it was quoted at.
   * Replaces the live item with the same id, or adds it if it has since been retired.
   */
  private static <T extends CatalogItem> T asQuoted(ObservableList<T> items, T quoted) {
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).id() == quoted.id()) {
        items.set(i, quoted);
        return quoted;
      }
    }
    items.add(quoted);
    return quoted;
  }

  // ------------------------------------------------------------------ preview

  private void changed() {
    if (loading) {
      return;
    }
    markDirty();
    if (debounce.getDuration().toMillis() <= 0) {
      preview();
    } else {
      debounce.playFromStart();
    }
  }

  private void markDirty() {
    if (!loading) {
      dirty.set(true);
    }
  }

  /** Prices the current inputs, or lists what's missing. Only the newest request's answer shows. */
  void preview() {
    QuoteSpecBuilder.Result result = QuoteSpecBuilder.build(this);
    missing.setAll(new java.util.LinkedHashSet<>(result.problems().values()));
    if (showErrors) {
      errors.clear();
      errors.putAll(result.problems());
    }
    if (!result.complete()) {
      previewSequence++;
      quote.set(null);
      return;
    }
    long sequence = ++previewSequence;
    QuoteSpec spec = result.spec().orElseThrow();
    long clientId = client.get().id();
    backend.run(
        s ->
            jobId.isPresent()
                ? s.jobs().previewEdit(jobId.getAsLong(), spec)
                : s.jobs().preview(clientId, spec),
        priced -> {
          if (sequence == previewSequence) {
            quote.set(priced);
          }
        },
        error -> {
          if (sequence == previewSequence) {
            quote.set(null);
            showProblem(error);
          }
        });
  }

  private void showProblem(Throwable error) {
    if (error instanceof ValidationException v) {
      v.errors().forEach(e -> missing.add(e.message()));
      if (showErrors) {
        v.errors().forEach(e -> errors.put(e.field(), e.message()));
      }
    } else if (error instanceof DomainException d) {
      missing.add(d.getMessage());
    } else {
      log.error("Quote preview failed", error);
      missing.add("Prices couldn't be worked out just now.");
    }
  }

  // ------------------------------------------------------------------ save

  /** Saves the quote; {@code onSaved} receives the saved job on the FX thread. */
  public void save(Consumer<Job> onSaved) {
    showErrors = true;
    failure.set("");
    QuoteSpecBuilder.Result result = QuoteSpecBuilder.build(this);
    errors.clear();
    errors.putAll(result.problems());
    if (!result.complete()) {
      if (result.problems().containsKey("services")) {
        failure.set(result.problems().get("services"));
      }
      return;
    }
    QuoteSpec spec = result.spec().orElseThrow();
    long clientId = client.get().id();
    String text = notes.get() == null ? "" : notes.get().strip();
    Function<Services, Job> work =
        jobId.isPresent()
            ? s -> s.jobs().editQuote(jobId.getAsLong(), spec, text)
            : s -> s.jobs().create(clientId, spec, text);
    busy.set(true);
    backend.run(
        work,
        saved -> {
          busy.set(false);
          dirty.set(false);
          job.set(saved);
          onSaved.accept(saved);
        },
        error -> {
          busy.set(false);
          saveFailed(error);
        });
  }

  private void saveFailed(Throwable error) {
    if (error instanceof ValidationException v) {
      List<String> unmatched = new ArrayList<>();
      v.errors()
          .forEach(
              e -> {
                if (FIELDS.contains(e.field())) {
                  errors.put(e.field(), e.message());
                } else {
                  unmatched.add(e.message());
                }
              });
      failure.set(String.join(" ", unmatched));
    } else if (error instanceof DomainException d) {
      failure.set(d.getMessage());
    } else {
      log.error("Saving a quote failed", error);
      failure.set(SAVE_FAILED);
    }
  }

  /** Field keys the editor shows errors under (pipe-run rows add ".index"). */
  static final java.util.Set<String> FIELDS =
      java.util.Set.of(
          "client",
          "drillingType",
          "depth",
          "pumpType",
          "lift",
          "tank",
          "plumbingAmount",
          "plumbingDescription",
          "plumbingRuns",
          "outlets");

  // ------------------------------------------------------------------ accessors

  Backend backend() {
    return backend;
  }

  public boolean isEdit() {
    return jobId.isPresent();
  }

  public OptionalLong jobId() {
    return jobId;
  }

  public ObservableList<ClientSummary> clients() {
    return clients;
  }

  public ObservableList<DrillingType> drillingTypes() {
    return drillingTypes;
  }

  public ObservableList<PumpType> pumpTypes() {
    return pumpTypes;
  }

  public ObservableList<TankType> tanks() {
    return tanks;
  }

  public ObservableList<PipeType> pipes() {
    return pipes;
  }

  public ObjectProperty<ClientSummary> clientProperty() {
    return client;
  }

  public BooleanProperty drillingOnProperty() {
    return drillingOn;
  }

  public ObjectProperty<DrillingType> drillingTypeProperty() {
    return drillingType;
  }

  public ObjectProperty<Metres> depthProperty() {
    return depth;
  }

  public BooleanProperty pumpOnProperty() {
    return pumpOn;
  }

  public ObjectProperty<PumpType> pumpTypeProperty() {
    return pumpType;
  }

  public ObjectProperty<Metres> liftProperty() {
    return lift;
  }

  public BooleanProperty tankOnProperty() {
    return tankOn;
  }

  public ObjectProperty<TankType> tankProperty() {
    return tank;
  }

  public BooleanProperty plumbingOnProperty() {
    return plumbingOn;
  }

  public ObjectProperty<PlumbingMode> plumbingModeProperty() {
    return plumbingMode;
  }

  public ObservableList<PipeRunRow> runs() {
    return runs;
  }

  public StringProperty outletsProperty() {
    return outlets;
  }

  public ObjectProperty<Money> manualAmountProperty() {
    return manualAmount;
  }

  public StringProperty manualDescriptionProperty() {
    return manualDescription;
  }

  public StringProperty notesProperty() {
    return notes;
  }

  public ObjectProperty<State> stateProperty() {
    return state;
  }

  /** The priced quote for the current inputs; null while they are incomplete. */
  public ObjectProperty<Quote> quoteProperty() {
    return quote;
  }

  /** What still has to be filled in before there is a price. */
  public ObservableList<String> missing() {
    return missing;
  }

  /** Errors to show under fields, by field key; filled once the user has tried to save. */
  public ObservableMap<String, String> errors() {
    return errors;
  }

  /** A problem that belongs to no field, for the form banner; empty when there is none. */
  public StringProperty failureProperty() {
    return failure;
  }

  public BooleanProperty busyProperty() {
    return busy;
  }

  /** True once the user has changed anything since loading or saving. */
  public BooleanProperty dirtyProperty() {
    return dirty;
  }

  /** The saved job when editing (or after saving a new one). */
  public ObjectProperty<Job> jobProperty() {
    return job;
  }

  /** Map view of the current problems, for tests. */
  Map<String, String> problems() {
    return QuoteSpecBuilder.build(this).problems();
  }
}
