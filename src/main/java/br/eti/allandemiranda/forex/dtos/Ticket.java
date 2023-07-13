package br.eti.allandemiranda.forex.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
@ToString
public class Ticket {
    @NotNull private final long idx;
    @NotNull private final LocalDateTime dataTime;
    @NotNull private final float bid;
    @NotNull private final float ask;
}
