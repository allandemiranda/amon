package br.eti.allandemiranda.forex.dtos;

import br.eti.allandemiranda.forex.utils.SignalTrend;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public record Signal(@NotNull LocalDateTime dateTime, @NotNull SignalTrend trend, double price) {

}
