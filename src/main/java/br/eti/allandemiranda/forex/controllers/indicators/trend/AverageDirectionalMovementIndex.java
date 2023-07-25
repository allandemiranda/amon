package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.controllers.indicators.SignalTrend;
import br.eti.allandemiranda.forex.dtos.ADX;
import br.eti.allandemiranda.forex.services.ADXService;
import br.eti.allandemiranda.forex.services.CandlestickService;
import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class AverageDirectionalMovementIndex implements Indicator {

  private final ADXService adxService;
  private final CandlestickService candlestickService;

  @Value("${adx.parameters.period}")
  private Integer period;

  @Autowired
  private AverageDirectionalMovementIndex(final ADXService adxService, final CandlestickService candlestickService) {
    this.adxService = adxService;
    this.candlestickService = candlestickService;
  }

  private static double[] getTRSum(final double[] close, final double[] high, final double[] low, final int period) {
    return IntStream.range(0, period).mapToDouble(
        j -> IntStream.range(j, period + j).mapToDouble(i -> DoubleStream.of(high[i] - low[i], Math.abs(high[i] - close[i]), Math.abs(low[i] - close[i + 1])).max().getAsDouble())
            .sum()).toArray();
  }

  private static double[] getDMPlusSum(final double[] high, final double[] low, final int period) {
    return IntStream.range(0, period).mapToDouble(
        j -> IntStream.range(j, period + j).mapToDouble(i -> high[i] - high[i + 1] > low[i + 1] - low[i] ? DoubleStream.of(high[i] - high[i + 1], 0D).max().getAsDouble() : 0D)
            .sum()).toArray();
  }

  private static double[] getDMMinusSum(final double[] high, final double[] low, final int period) {
    return IntStream.range(0, period).mapToDouble(
            j -> IntStream.range(j, period + j).mapToDouble(i -> low[i + 1] - low[i] > high[i] - high[1] ? DoubleStream.of(low[i + 1] - low[i], 0D).max().getAsDouble() : 0D).sum())
        .toArray();
  }

  private static double[] getDIPlus(final double @NotNull [] dmPlus, final double[] trSum) {
    return IntStream.range(0, dmPlus.length).mapToDouble(i -> 100 * (dmPlus[i] / trSum[i])).toArray();
  }

  private static double[] getDIMinus(final double @NotNull [] dmMinus, final double[] trSum) {
    return IntStream.range(0, dmMinus.length).mapToDouble(i -> 100 * (dmMinus[i] / trSum[i])).toArray();
  }

  private static double[] getDIDiff(final double @NotNull [] diPlus, final double[] diMinus) {
    return IntStream.range(0, diPlus.length).mapToDouble(i -> Math.abs(diPlus[i] - diMinus[i])).toArray();
  }

  private static double[] getDiSum(final double @NotNull [] diPlus, final double[] diMinus) {
    return IntStream.range(0, diPlus.length).mapToDouble(i -> diPlus[i] + diMinus[i]).toArray();
  }

  private static double[] getDX(final double @NotNull [] diDiff, final double[] diSum) {
    return IntStream.range(0, diDiff.length).mapToDouble(i -> 100 * (diDiff[i] / diSum[i])).toArray();
  }

  private int getPeriod() {
    return this.period;
  }

  @Override
  @Synchronized
  public boolean run() {
    if (this.candlestickService.getCurrentMemorySize() >= (this.getPeriod() * 2L)) {
      double[] close = this.candlestickService.getCloseReversed((this.getPeriod() * 2));
      double[] high = this.candlestickService.getHighReversed((this.getPeriod() * 2));
      double[] low = this.candlestickService.getLowReversed((this.getPeriod() * 2));

      double[] trSum = getTRSum(close, high, low, this.getPeriod());
      double[] dmPlusSum = getDMPlusSum(high, low, this.getPeriod());
      double[] dmMinusSum = getDMMinusSum(high, low, this.getPeriod());

      double[] diPlus = getDIPlus(dmPlusSum, trSum);
      double[] diMinus = getDIMinus(dmMinusSum, trSum);

      double[] diDiff = getDIDiff(diPlus, diMinus);
      double[] diSum = getDiSum(diPlus, diMinus);

      double[] dx = getDX(diDiff, diSum);

      double adxIndex = Arrays.stream(dx).sum() / this.getPeriod();

      this.adxService.add(new ADX(this.candlestickService.getCandlestickDataTime(), adxIndex, diPlus[0], diMinus[0]));
      return true;
    } else {
      log.debug("Can't get ADX, low memory side on Candlestick");
      return false;
    }
  }

  @Override
  @Synchronized
  public @NotNull SignalTrend getSignal() {
    // TODO ajeitar esse getSinal
    SignalTrend signalTrend = adxService.isDiPlusUpThanDiMinus() ? SignalTrend.buy : SignalTrend.sell;
    SignalTrend trend = adxService.getLast().adx() >= 75D ? signalTrend : SignalTrend.neutral;

    adxService.print(trend, candlestickService.getRealDataTime(), adxService.getLast(), candlestickService.getLastCloseValue());
    return trend;
  }
}
