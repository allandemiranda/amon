package br.eti.allandemiranda.forex.dtos;

import java.time.LocalDateTime;
import lombok.With;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
@With
public record Ticket(@NotNull LocalDateTime dateTime, Double bid, Double ask) implements DefaultModel {

}
