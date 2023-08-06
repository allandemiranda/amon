package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.services.AdxService;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class AverageDirectionalMovementIndex implements Indicator {

  private final AdxService adxService;
  private final CandlestickService candlestickService;

  @Value("${adx.parameters.period}")
  private int period;

  @Autowired
  protected AverageDirectionalMovementIndex(final AdxService adxService, final CandlestickService candlestickService) {
    this.adxService = adxService;
    this.candlestickService = candlestickService;
  }

  private static BigDecimal @NotNull [] getDx(final Candlestick @NotNull [] chart) {
    final BigDecimal tr = IntStream.range(1, chart.length).parallel().mapToObj(i -> {
      final Candlestick candlestick = chart[i];
      final Candlestick lastCandlestick = chart[i - 1];
      final BigDecimal pOne = candlestick.high().subtract(lastCandlestick.close()).abs();
      final BigDecimal pTwo = candlestick.low().subtract(lastCandlestick.close()).abs();
      final BigDecimal pThree = candlestick.high().subtract(candlestick.low());
      return pOne.max(pTwo).max(pThree);
    }).reduce(BigDecimal.valueOf(0d), BigDecimal::add);
    final BigDecimal dmPlus = IntStream.range(1, chart.length).parallel().mapToObj(i -> {
      final Candlestick candlestick = chart[i];
      final Candlestick lastCandlestick = chart[i - 1];
      final BigDecimal highValue = candlestick.high().subtract(lastCandlestick.high());
      final BigDecimal lowValue = lastCandlestick.low().subtract(candlestick.low());
      if (highValue.compareTo(lowValue) > 0) {
        return highValue.max(BigDecimal.valueOf(0d));
      } else {
        return BigDecimal.valueOf(0d);
      }
    }).reduce(BigDecimal.valueOf(0d), BigDecimal::add);
    final BigDecimal dmMinus = IntStream.range(1, chart.length).parallel().mapToObj(i -> {
      final Candlestick candlestick = chart[i];
      final Candlestick lastCandlestick = chart[i - 1];
      final BigDecimal highValue = candlestick.high().subtract(lastCandlestick.high());
      final BigDecimal lowValue = lastCandlestick.low().subtract(candlestick.low());
      if (lowValue.compareTo(highValue) > 0) {
        return lowValue.max(BigDecimal.valueOf(0d));
      } else {
        return BigDecimal.valueOf(0d);
      }
    }).reduce(BigDecimal.valueOf(0d), BigDecimal::add);
    final BigDecimal diPlus = dmPlus.divide(tr, 5, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100));
    final BigDecimal diMinus = dmMinus.divide(tr, 5, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100));
    final BigDecimal diDiff = diPlus.subtract(diMinus).abs();
    final BigDecimal diSum = diPlus.add(diMinus);
    final BigDecimal dx = diDiff.divide(diSum, 5, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100));
    return new BigDecimal[]{dx, diPlus, diMinus};
  }

  @Override
  @Synchronized
  public boolean run() {
    if (this.getCandlestickService().isReady()) {
      final Candlestick[] chart = this.getCandlestickService().getCandlesticks(this.getPeriod() * 2);
      final BigDecimal[][] adxs = IntStream.range(0, this.getPeriod()).mapToObj(i -> {
        final Candlestick[] tmp = Arrays.stream(chart, i, this.getPeriod() + i + 1).toArray(Candlestick[]::new);
        return getDx(tmp);
      }).toArray(BigDecimal[][]::new);
      final BigDecimal adxValue = Arrays.stream(adxs).map(value -> value[0]).reduce(BigDecimal.valueOf(0d), BigDecimal::add)
          .divide(BigDecimal.valueOf(adxs.length), 5, RoundingMode.DOWN);
      final BigDecimal diPlus = adxs[adxs.length - 1][1];
      final BigDecimal diMinus = adxs[adxs.length - 1][2];
      this.adxService.addAdx(this.getCandlestickService().getLastCandlestick().realDateTime(), adxValue, diPlus, diMinus);
      return true;
    } else {
      return false;
    }
  }

  @Override
  @Synchronized
  public @NotNull SignalTrend getCurrentSignal() {
    if (this.getAdxService().getAdx().dateTime().equals(LocalDateTime.MIN)) {
      return SignalTrend.OUT;
    } else {
      final LocalDateTime realTime = this.getCandlestickService().getLastCandlestick().realDateTime();
      final BigDecimal price = this.getCandlestickService().getLastCandlestick().close();
      if (this.getAdxService().getAdx().diPlus().compareTo(this.getAdxService().getAdx().diMinus()) == 0
          || this.getAdxService().getAdx().value().compareTo(BigDecimal.valueOf(50d)) < 0) {
        this.getAdxService().updateDebugFile(realTime, SignalTrend.NEUTRAL, price);
        return SignalTrend.NEUTRAL;
      } else if (this.getAdxService().getAdx().value().compareTo(BigDecimal.valueOf(75d)) < 0) {
        final SignalTrend trend = this.getAdxService().getAdx().diPlus().compareTo(this.getAdxService().getAdx().diMinus()) > 0 ? SignalTrend.BUY : SignalTrend.SELL;
        this.getAdxService().updateDebugFile(realTime, trend, price);
        return trend;
      } else {
        final SignalTrend trend =
            this.getAdxService().getAdx().diPlus().compareTo(this.getAdxService().getAdx().diMinus()) > 0 ? SignalTrend.STRONG_BUY : SignalTrend.STRONG_SELL;
        this.getAdxService().updateDebugFile(realTime, trend, price);
        return trend;
      }
    }
  }
}
