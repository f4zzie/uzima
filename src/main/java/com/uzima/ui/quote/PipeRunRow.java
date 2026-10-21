package com.uzima.ui.quote;

import com.uzima.domain.catalog.PipeType;
import com.uzima.domain.common.Metres;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** One pipe run being entered in itemised plumbing: pipe type, diameter and length. */
public final class PipeRunRow {

  private final ObjectProperty<PipeType> pipe = new SimpleObjectProperty<>(this, "pipe");
  private final StringProperty diameter = new SimpleStringProperty(this, "diameter", "");
  private final ObjectProperty<Metres> length = new SimpleObjectProperty<>(this, "length");

  public PipeRunRow() {}

  public PipeRunRow(PipeType pipe, int diameterMm, Metres length) {
    this.pipe.set(pipe);
    this.diameter.set(String.valueOf(diameterMm));
    this.length.set(length);
  }

  public ObjectProperty<PipeType> pipeProperty() {
    return pipe;
  }

  /** The diameter as typed, in millimetres. */
  public StringProperty diameterProperty() {
    return diameter;
  }

  public ObjectProperty<Metres> lengthProperty() {
    return length;
  }
}
