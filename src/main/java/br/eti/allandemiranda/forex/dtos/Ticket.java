package br.eti.allandemiranda.forex.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public record Ticket(@NotNull LocalDateTime dateTime, double bid, double ask) implements DefaultModel {

}
