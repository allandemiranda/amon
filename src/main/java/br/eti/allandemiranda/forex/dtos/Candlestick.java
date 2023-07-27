package br.eti.allandemiranda.forex.dtos;

import java.time.LocalDateTime;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
public record Candlestick(@NotNull LocalDateTime dateTime, double open, double high, double low, double close) implements DefaultModel {

}
