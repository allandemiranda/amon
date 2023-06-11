package br.eti.allandemiranda.forex.components;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface MovingAverage {
    default List<Double> getSMA(int period, @NotNull List<Double> list) {
        return IntStream.range(0, list.size()).mapToDouble(i -> {
            if ((i + 1) >= period) {
                return IntStream.range(i - period, period).mapToDouble(list::get).sum() / (double) period;
            } else {
                return (Double) null;
            }
        }).boxed().toList();
    }

    default List<Double> getEMA(int period, @NotNull List<Double> list) {
        return Stream.concat(Stream.of(list.get(0)), IntStream.range(1, list.size()).mapToDouble(i -> {
            double previousEMA = list.get(i - 1);
            double k = 2.0 / (period - 1.0);
            double currentPrice = list.get(i);
            return (k * (currentPrice - previousEMA)) + previousEMA;
        }).boxed()).toList();
    }

    default List<Double> getSMMA(int period, List<Double> list) {
        throw new NotImplementedException("The SMMA not implemented");
    }

    default List<Double> getLWMA(int period, List<Double> list) {
        throw new NotImplementedException("The LWMA not implemented");
    }
}
