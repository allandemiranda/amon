package br.eti.allandemiranda.forex.entities;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;

public interface DefaultEntity<T extends DefaultEntity> extends Comparable<T>{

  @NotNull LocalDateTime getDateTime();

  void setDateTime(final @NotNull LocalDateTime dateTime);

  @Override
  default int compareTo(@NotNull T o) {
    return this.getDateTime().compareTo(o.getDateTime());
  }
}
