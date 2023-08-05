package br.eti.allandemiranda.forex.dtos;

import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public record Order(@NotNull LocalDateTime openDateTime, @NotNull LocalDateTime lastUpdate, @NotNull OrderStatus status, @NotNull OrderPosition position,
                    double openPrice, double closePrice, int currentProfit, int currentBalance, int highProfit, int lowProfit) {

}
