package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.MACD;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.MacdService;
import br.eti.allandemiranda.forex.enums.IndicatorTrend;
import br.eti.allandemiranda.forex.utils.Tools;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
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
    final BigDecimal[] closes = this.getCandlestickService().getCandlesticksClose(Math.max(this.getMacdService().getSlowPeriod(), this.getMacdService().getFastPeriod()) + this.getMacdService().getMacdPeriod() - 1).map(Candlestick::close)
        .toArray(BigDecimal[]::new);
    final BigDecimal[] fasts = Tools.getEMA(this.getMacdService().getFastPeriod(), closes);
    final BigDecimal[] slows = Tools.getEMA(this.getMacdService().getSlowPeriod(), closes);
    final BigDecimal[] macds = IntStream.range(0, this.getMacdService().getMacdPeriod()).mapToObj(i -> fasts[i].subtract(slows[i])).toArray(BigDecimal[]::new);
    final BigDecimal signal = Arrays.stream(macds).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(this.getMacdService().getMacdPeriod()), 10, RoundingMode.HALF_UP);
    this.getMacdService().addMacd(this.getCandlestickService().getLastCandlestick().dateTime(), macds[0], signal);
  }
}
