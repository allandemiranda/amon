package br.eti.allandemiranda.forex.dtos;

import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public record Order(@NotNull LocalDateTime openDateTime, @NotNull LocalDateTime lastUpdate, @NotNull LocalDateTime openCandleDateTime,
                    @NotNull LocalDateTime lastCandleUpdate, @NotNull OrderStatus status, @NotNull OrderPosition position, @NotNull BigDecimal openPrice,
                    @NotNull BigDecimal closePrice, int highProfit, int lowProfit, int currentProfit, int currentBalance) {

}
