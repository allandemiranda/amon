package br.eti.allandemiranda.forex.dtos;

import java.time.LocalDateTime;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
public record ADX(@NotNull LocalDateTime dateTime, double adx, double diPlus, double diMinus) implements DefaultModel {

}
