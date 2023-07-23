package br.eti.allandemiranda.forex.entities;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;

public interface DefaultEntity {

  @NotNull LocalDateTime getDateTime();

  void setDateTime(final @NotNull LocalDateTime dateTime);
}
