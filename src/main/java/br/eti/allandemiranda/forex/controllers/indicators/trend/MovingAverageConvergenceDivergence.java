package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.MACD;
import br.eti.allandemiranda.forex.enums.IndicatorTrend;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.MacdService;
import br.eti.allandemiranda.forex.utils.Tools;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class MovingAverageConvergenceDivergence implements Indicator {

  private final MacdService macdService;
  private final CandlestickService candlestickService;

  @Autowired
  protected MovingAverageConvergenceDivergence(final MacdService macdService, final CandlestickService candlestickService) {
    this.macdService = macdService;
    this.candlestickService = candlestickService;
  }

  private boolean isCross(final MACD @NotNull [] macds) {
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
    final int slowPeriod = this.getMacdService().getSlowPeriod();
    final int fastPeriod = this.getMacdService().getFastPeriod();
    final int macdPeriod = this.getMacdService().getMacdPeriod();
    final Candlestick[] candlesticks = this.getCandlestickService().getCandlesticksClose(Math.max(slowPeriod, fastPeriod) + macdPeriod - 1).toArray(Candlestick[]::new);
    final BigDecimal[] closes = new BigDecimal[candlesticks.length];
    IntStream.range(0, closes.length).parallel().forEach(i -> closes[i] = candlesticks[i].close());
    final BigDecimal[][] fasts = new BigDecimal[1][1];
    final BigDecimal[][] slows = new BigDecimal[1][1];
    final Thread fastPeriodThread = Thread.ofVirtual().unstarted(() -> fasts[0] = Tools.getEMA(fastPeriod, closes));
    final Thread slowPeriodThread = Thread.ofVirtual().unstarted(() -> slows[0] = Tools.getEMA(slowPeriod, closes));
    Tools.startThreadsUnstated(fastPeriodThread, slowPeriodThread);
    final BigDecimal[] macds = new BigDecimal[macdPeriod];
    IntStream.range(0, macdPeriod).parallel().forEach(i -> macds[i] = fasts[0][i].subtract(slows[0][i]));
    final BigDecimal signal = Arrays.stream(macds).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(macdPeriod), 10, RoundingMode.HALF_UP);
    final LocalDateTime candlestickTime = this.getCandlestickService().getLastCandlestick().dateTime();
    this.getMacdService().addMacd(candlestickTime, macds[0], signal);
  }
}
