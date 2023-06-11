package br.eti.allandemiranda.forex.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class Candlestick {
    @NotNull
    private LocalDateTime dateTime;
    @NotNull
    private double open;
    @NotNull
    private double high;
    @NotNull
    private double low;
    @NotNull
    private double close;
}
