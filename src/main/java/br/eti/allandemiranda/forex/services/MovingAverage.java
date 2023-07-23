package br.eti.allandemiranda.forex.services;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public interface MovingAverage {

  //! Simple Moving Average (SMA)
  default Double[] getSMA(final int period, final double @NotNull [] list) {
    return IntStream.rangeClosed(1, list.length).mapToObj(p -> (p >= period) ? (Arrays.stream(list, p - period, p).sum() / period) : null).toArray(Double[]::new);
  }

  //! Exponential Moving Average (EMA)
  default double[] getEMA(final int period, final double @NotNull [] list) {
    double a = 2D / (period + 1);
    AtomicReference<Double> prevEMA = new AtomicReference<>(list[0]);
    return IntStream.range(0, list.length).mapToDouble(index -> {
      if (index == 0) {
        return prevEMA.get();
      } else {
        double ema = (a * list[index]) + (1 - a) * prevEMA.get();
        prevEMA.set(ema);
        return ema;
      }
    }).toArray();
  }

  //! Smoothed Moving Average (SMMA)
  default Double[] getSMMA(final int period, final double @NotNull [] list) {
    AtomicReference<Double> prevSMMA = new AtomicReference<>(null);
    AtomicInteger index = new AtomicInteger(-1);
    AtomicBoolean second = new AtomicBoolean(true);
    return Arrays.stream(getSMA(period, list)).map(sma -> {
      index.getAndIncrement();
      if (Objects.isNull(sma)) {
        return null;
      } else {
        if (Objects.isNull(prevSMMA.get())) {
          prevSMMA.set(sma);
          return sma;
        } else {
          if (second.get()) {
            second.set(false);
            double smma = ((prevSMMA.get() * (period - 1)) + list[index.get()]) / period;
            prevSMMA.set(smma);
            return smma;
          } else {
            double smma = ((prevSMMA.get() * period) - prevSMMA.get() + list[index.get()]) / period;
            prevSMMA.set(smma);
            return smma;
          }
        }
      }
    }).toArray(Double[]::new);
  }
}
