package br.eti.allandemiranda.forex.dtos;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MACD(@NotNull LocalDateTime dateTime, @NotNull BigDecimal main, @NotNull BigDecimal signal) {

}
