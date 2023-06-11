package br.eti.allandemiranda.forex.components.indicator;

import br.eti.allandemiranda.forex.components.Method;
import br.eti.allandemiranda.forex.components.MovingAverage;
import br.eti.allandemiranda.forex.components.Signal;
import br.eti.allandemiranda.forex.components.ToApply;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TechnicalIndicator<T> extends MovingAverage {
    List<Pair<T, Signal>> get(List<Candlestick> candlestickList, Object... input);



    default double getApply(@NotNull ToApply toApply, Candlestick candlestick) {
        return switch (toApply) {
            case LOW -> candlestick.getLow();
            case CLOSE -> candlestick.getClose();
            case OPEN -> candlestick.getOpen();
            case HIGH -> candlestick.getHigh();
            case MEDIAN_PRICE -> (candlestick.getHigh() + candlestick.getLow()) / 2;
            case TYPICAL_PRICE -> (candlestick.getHigh() + candlestick.getLow() + candlestick.getClose()) / 3;
            case WEIGHTED_CLOSE -> (candlestick.getHigh() + candlestick.getLow() + (2 * candlestick.getClose())) / 4;
        };
    }

    default List<Double> getMethod(int period, @NotNull Method method, ToApply toApply, List<Candlestick> candlestickList) {
        return switch (method) {
            case SMA ->
                    getSMA(period, candlestickList.stream().map(candlestickModel -> getApply(toApply, candlestickModel)).toList());
            case EMA ->
                    getEMA(period, candlestickList.stream().map(candlestickModel -> getApply(toApply, candlestickModel)).toList());
            case SMMA ->
                    getSMMA(period, candlestickList.stream().map(candlestickModel -> getApply(toApply, candlestickModel)).toList());
            case LWMA ->
                    getLWMA(period, candlestickList.stream().map(candlestickModel -> getApply(toApply, candlestickModel)).toList());
        };
    }
}
