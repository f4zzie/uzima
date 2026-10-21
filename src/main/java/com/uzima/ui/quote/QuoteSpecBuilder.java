package com.uzima.ui.quote;

import com.uzima.domain.catalog.TankType;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.FieldError;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.job.DrillingSpec;
import com.uzima.domain.job.PipeRun;
import com.uzima.domain.job.Plumbing;
import com.uzima.domain.job.PumpSpec;
import com.uzima.domain.job.QuoteSpec;
import com.uzima.ui.quote.QuoteEditorViewModel.PlumbingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Turns the editor's inputs into a {@link QuoteSpec}, or says what is missing. Each problem is
 * keyed by the field it belongs to ({@code depth}, {@code pipe.0} …), so the editor can list it in
 * the breakdown while the user is typing and show it under the field once they try to save. Builds
 * domain objects only; prices come from the pricing engine.
 */
final class QuoteSpecBuilder {

  /** The spec when the inputs are complete, otherwise the problems by field, in form order. */
  record Result(Optional<QuoteSpec> spec, Map<String, String> problems) {

    boolean complete() {
      return spec.isPresent();
    }
  }

  private final QuoteEditorViewModel form;
  private final Map<String, String> problems = new LinkedHashMap<>();

  private QuoteSpecBuilder(QuoteEditorViewModel form) {
    this.form = form;
  }

  static Result build(QuoteEditorViewModel form) {
    return new QuoteSpecBuilder(form).build();
  }

  private Result build() {
    if (form.clientProperty().get() == null) {
      problems.put("client", "Choose a client");
    }
    Optional<DrillingSpec> drilling =
        form.drillingOnProperty().get() ? drilling() : Optional.empty();
    Optional<PumpSpec> pump = form.pumpOnProperty().get() ? pump() : Optional.empty();
    Optional<TankType> tank = Optional.empty();
    if (form.tankOnProperty().get()) {
      tank = Optional.ofNullable(form.tankProperty().get());
      if (tank.isEmpty()) {
        problems.put("tank", "Choose a tank size");
      }
    }
    Optional<Plumbing> plumbing = form.plumbingOnProperty().get() ? plumbing() : Optional.empty();
    if (!form.drillingOnProperty().get()
        && !form.pumpOnProperty().get()
        && !form.tankOnProperty().get()
        && !form.plumbingOnProperty().get()) {
      problems.put("services", "Turn on at least one service: drilling, pump, tank or plumbing");
    }
    if (!problems.isEmpty()) {
      return new Result(Optional.empty(), Map.copyOf(problems));
    }
    try {
      return new Result(Optional.of(new QuoteSpec(drilling, pump, tank, plumbing)), Map.of());
    } catch (ValidationException e) {
      e.errors().forEach(err -> problems.putIfAbsent(err.field(), err.message()));
      return new Result(Optional.empty(), Map.copyOf(problems));
    }
  }

  private Optional<DrillingSpec> drilling() {
    if (form.drillingTypeProperty().get() == null) {
      problems.put("drillingType", "Choose a drilling type");
    }
    var depth = form.depthProperty().get();
    if (depth == null) {
      problems.put("depth", "Enter the depth to drill");
    } else if (depth.isZero()) {
      problems.put("depth", "Enter more than 0 m");
    }
    return problems.containsKey("drillingType") || problems.containsKey("depth")
        ? Optional.empty()
        : Optional.of(new DrillingSpec(form.drillingTypeProperty().get(), depth));
  }

  private Optional<PumpSpec> pump() {
    if (form.pumpTypeProperty().get() == null) {
      problems.put("pumpType", "Choose a pump type");
    }
    if (form.liftProperty().get() == null) {
      problems.put("lift", "Enter the lift height (0 if none)");
    }
    return problems.containsKey("pumpType") || problems.containsKey("lift")
        ? Optional.empty()
        : Optional.of(new PumpSpec(form.pumpTypeProperty().get(), form.liftProperty().get()));
  }

  private Optional<Plumbing> plumbing() {
    if (form.plumbingModeProperty().get() == PlumbingMode.MANUAL) {
      var amount = form.manualAmountProperty().get();
      String description = form.manualDescriptionProperty().get();
      if (amount == null) {
        problems.put("plumbingAmount", "Enter the plumbing amount");
      }
      if (description == null || description.isBlank()) {
        problems.put("plumbingDescription", "Describe the plumbing work");
      }
      return attempt(() -> new Plumbing.Manual(amount, description));
    }
    List<PipeRun> runs = new ArrayList<>();
    List<PipeRunRow> rows = form.runs();
    if (rows.isEmpty()) {
      problems.put("plumbingRuns", "Add at least one pipe run");
    }
    for (int i = 0; i < rows.size(); i++) {
      PipeRunRow row = rows.get(i);
      Integer diameter = positiveInt(row.diameterProperty().get());
      if (row.pipeProperty().get() == null) {
        problems.put("pipe." + i, "Choose a pipe type");
      }
      if (diameter == null) {
        problems.put("diameter." + i, "Enter the diameter in mm");
      }
      if (row.lengthProperty().get() == null || row.lengthProperty().get().isZero()) {
        problems.put("length." + i, "Enter the length");
      }
      if (row.pipeProperty().get() != null
          && diameter != null
          && row.lengthProperty().get() != null
          && !row.lengthProperty().get().isZero()) {
        runs.add(new PipeRun(row.pipeProperty().get(), diameter, row.lengthProperty().get()));
      }
    }
    Integer outlets = nonNegativeInt(form.outletsProperty().get());
    if (outlets == null) {
      problems.put("outlets", "Enter a whole number of outlets (0 or more)");
    }
    return runs.size() == rows.size() && outlets != null && !rows.isEmpty()
        ? attempt(() -> new Plumbing.Itemised(runs, outlets))
        : Optional.empty();
  }

  private <T> Optional<T> attempt(java.util.function.Supplier<T> build) {
    if (!problems.isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.of(build.get());
    } catch (ValidationException e) {
      for (FieldError err : e.errors()) {
        problems.putIfAbsent(err.field(), err.message());
      }
      return Optional.empty();
    } catch (DomainException e) {
      problems.putIfAbsent("plumbing", e.getMessage());
      return Optional.empty();
    }
  }

  private static Integer positiveInt(String text) {
    Integer n = parse(text);
    return n == null || n <= 0 ? null : n;
  }

  private static Integer nonNegativeInt(String text) {
    if (text == null || text.isBlank()) {
      return 0;
    }
    Integer n = parse(text);
    return n == null || n < 0 ? null : n;
  }

  private static Integer parse(String text) {
    try {
      return text == null ? null : Integer.valueOf(text.strip());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
