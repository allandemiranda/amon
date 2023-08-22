package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.MACD;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.MacdService;
import br.eti.allandemiranda.forex.utils.IndicatorTrend;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class MovingAverageConvergenceDivergence implements Indicator {

  private final MacdService macdService;
  private final CandlestickService candlestickService;

  @Value("${macd.parameters.fast.period:12}")
  private int fastPeriod;
  @Value("${macd.parameters.slow.period:26}")
  private int slowPeriod;
  @Value("${macd.parameters.macd.period:9}")
  private int macdPeriod;

  @Autowired
  protected MovingAverageConvergenceDivergence(final MacdService macdService, final CandlestickService candlestickService) {
    this.macdService = macdService;
    this.candlestickService = candlestickService;
  }

  private static BigDecimal @NotNull [] invertArray(BigDecimal @NotNull [] array) {
    return IntStream.rangeClosed(1, array.length).mapToObj(i -> array[array.length - i]).toArray(BigDecimal[]::new);
  }

  private static BigDecimal @NotNull [] getEMA(final int period, final BigDecimal @NotNull [] closes) {
    final BigDecimal a = BigDecimal.TWO.divide(BigDecimal.valueOf(period + 1L), 10, RoundingMode.HALF_UP);
    final BigDecimal[] list = invertArray(closes);
    AtomicReference<BigDecimal> prevEMA = new AtomicReference<>(list[0]);
    final BigDecimal[] emaList = IntStream.range(0, list.length).mapToObj(index -> {
      if (index == 0) {
        return prevEMA.get();
      } else {
        BigDecimal ema = (a.multiply(list[index])).add(BigDecimal.ONE.subtract(a).multiply(prevEMA.get()));
        prevEMA.set(ema);
        return ema;
      }
    }).toArray(BigDecimal[]::new);
    return invertArray(emaList);
  }

  private static boolean isCross(final MACD @NotNull [] macds) {
    if (macds.length == 3 && macds[1].main().compareTo(macds[1].signal()) == 0) {
      return macds[0].main().compareTo(macds[0].signal()) > 0 && macds[2].main().compareTo(macds[2].signal()) < 0
          || macds[0].main().compareTo(macds[0].signal()) < 0 && macds[2].main().compareTo(macds[2].signal()) > 0;
    } else if (macds.length >= 2) {
      return macds[0].main().compareTo(macds[0].signal()) > 0 && macds[1].main().compareTo(macds[1].signal()) < 0
          || macds[0].main().compareTo(macds[0].signal()) < 0 && macds[1].main().compareTo(macds[1].signal()) > 0;
    } else {
      return false;
    }
  }

  @Override
  public @NotNull IndicatorTrend getSignal() {
    final MACD[] macds = this.getMacdService().getMacd();
    final BigDecimal price = this.getCandlestickService().getLastCandlestick().close();
    if (macds.length > 1) {
      if (macds[0].main().compareTo(macds[0].signal()) > 0 && isCross(macds)) {
        this.getMacdService().updateDebugFile(IndicatorTrend.BUY, price);
        return IndicatorTrend.BUY;
      }
      if (macds[0].main().compareTo(macds[0].signal()) < 0 && isCross(macds)) {
        this.getMacdService().updateDebugFile(IndicatorTrend.SELL, price);
        return IndicatorTrend.SELL;
      }
    }
    this.getMacdService().updateDebugFile(IndicatorTrend.NEUTRAL, price);
    return IndicatorTrend.NEUTRAL;
  }

  @Override
  public void run() {
    final BigDecimal[] closes = this.getCandlestickService().getCandlesticks(Math.max(slowPeriod, fastPeriod) + macdPeriod - 1).map(Candlestick::close)
        .toArray(BigDecimal[]::new);
    final BigDecimal[] fasts = getEMA(fastPeriod, closes);
    final BigDecimal[] slows = getEMA(slowPeriod, closes);
    final BigDecimal[] macds = IntStream.range(0, macdPeriod).mapToObj(i -> fasts[i].subtract(slows[i])).toArray(BigDecimal[]::new);
    final BigDecimal signal = Arrays.stream(macds).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(macdPeriod), 10, RoundingMode.HALF_UP);
    this.getMacdService().addMacd(this.getCandlestickService().getLastCandlestick().dateTime(), macds[0], signal);
  }
}
