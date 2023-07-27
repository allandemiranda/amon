package br.eti.allandemiranda.forex.entities;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;

public interface DefaultEntity extends Comparable<LocalDateTime> {

  @NotNull LocalDateTime getDateTime();

  void setDateTime(final @NotNull LocalDateTime dateTime);

  @Override
  default int compareTo(@NotNull LocalDateTime o) {
    return this.getDateTime().compareTo(o);
  }
}
