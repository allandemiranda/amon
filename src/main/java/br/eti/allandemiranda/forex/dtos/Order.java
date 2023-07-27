package br.eti.allandemiranda.forex.dtos;

import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
public record Order(@NotNull LocalDateTime openDateTime, @NotNull LocalDateTime lastUpdate, @NotNull OrderStatus status, @NotNull OrderPosition position, double openPrice, double closePrice, double profit, double currentBalance) {

}
