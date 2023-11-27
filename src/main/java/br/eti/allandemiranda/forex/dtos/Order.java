package br.eti.allandemiranda.forex.dtos;

import br.eti.allandemiranda.forex.enums.OrderPosition;
import br.eti.allandemiranda.forex.enums.OrderStatus;
import br.eti.allandemiranda.forex.enums.SignalTrend;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public record Order(@NotNull LocalDateTime openDateTime, @NotNull LocalDateTime signalDateTime, @NotNull SignalTrend signalTrend,
                    @NotNull LocalDateTime lastUpdateDateTime, @NotNull String timeOpen, @NotNull OrderStatus orderStatus, @NotNull OrderPosition orderPosition,
                    @NotNull BigDecimal openPrice, @NotNull BigDecimal closePrice, int highProfit, int lowProfit, int currentProfit, @NotNull BigDecimal swapProfit) {

}
