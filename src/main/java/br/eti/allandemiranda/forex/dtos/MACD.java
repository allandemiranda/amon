package br.eti.allandemiranda.forex.dtos;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public record MACD(@NotNull LocalDateTime dateTime, @NotNull BigDecimal main, @NotNull BigDecimal signal) {

}
