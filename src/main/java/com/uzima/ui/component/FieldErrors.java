package com.uzima.ui.component;

import com.uzima.domain.common.FieldError;
import com.uzima.domain.common.ValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Puts a service's {@link ValidationException} next to the fields that caused it. Fields are
 * matched by {@link FormField#getName()}; messages for fields not on the form are returned so the
 * caller can show them in a {@link Banner}.
 */
public final class FieldErrors {

  private FieldErrors() {}

  /**
   * Clears every field, shows each error under its field, focuses the first invalid field in form
   * order, and returns the messages that matched no field.
   */
  public static List<String> show(ValidationException exception, Collection<FormField> fields) {
    clear(fields);
    List<String> unmatched = new ArrayList<>();
    for (FieldError error : exception.errors()) {
      FormField field = find(fields, error.field());
      if (field == null) {
        unmatched.add(error.message());
      } else if (!field.hasError()) {
        field.setError(error.message());
      }
    }
    fields.stream()
        .filter(FormField::hasError)
        .findFirst()
        .ifPresent(
            f -> {
              if (f.getInput() != null) {
                f.getInput().requestFocus();
              }
            });
    return unmatched;
  }

  public static List<String> show(ValidationException exception, FormField... fields) {
    return show(exception, Arrays.asList(fields));
  }

  public static void clear(Collection<FormField> fields) {
    fields.forEach(FormField::clearError);
  }

  public static void clear(FormField... fields) {
    clear(Arrays.asList(fields));
  }

  private static FormField find(Collection<FormField> fields, String name) {
    return fields.stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
  }
}
