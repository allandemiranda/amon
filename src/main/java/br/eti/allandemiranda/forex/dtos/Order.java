package br.eti.allandemiranda.forex.dtos;

import br.eti.allandemiranda.forex.utils.OrderStatus;
import java.time.LocalDateTime;
import lombok.With;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
@With
public record Order(@NotNull LocalDateTime dateTime, @NotNull LocalDateTime lastUpdate, @NotNull OrderStatus status, double openPrice, double closePrice, double profit) {

}
