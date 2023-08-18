package br.eti.allandemiranda.forex.dtos;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public record AC(@NotNull LocalDateTime realDateTime, @NotNull LocalDateTime dateTime, @NotNull BigDecimal value) {

}
