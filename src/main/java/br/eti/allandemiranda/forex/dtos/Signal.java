package br.eti.allandemiranda.forex.dtos;

import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
public record Signal(@NotNull LocalDateTime dateTime, @NotNull SignalTrend trend, double price) {

}
