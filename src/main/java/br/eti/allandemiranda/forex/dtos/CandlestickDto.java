package br.eti.allandemiranda.forex.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class CandlestickDto {
    @NotBlank
    private LocalDateTime dateTime;
    @NotBlank
    private double open;
    @NotBlank
    private double high;
    @NotBlank
    private double low;
    @NotBlank
    private double close;
}
