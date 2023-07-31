package br.eti.allandemiranda.forex.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public record Candlestick(@NotNull LocalDateTime dateTime, double open, double high, double low, double close) implements DefaultModel {

}
