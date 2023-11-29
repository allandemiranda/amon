package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.enums.SignalTrend;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.TradingPerformanceService;
import br.eti.allandemiranda.forex.utils.Tools;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class TradingPerformance {

  private final CandlestickService candlestickService;
  private final TradingPerformanceService service;

  @Autowired
  protected TradingPerformance(final CandlestickService candlestickService, final TradingPerformanceService service) {
    this.candlestickService = candlestickService;
    this.service = service;
  }

  /**
   * Check if the signal are in the tending of graphic
   *
   * @param signalTrend The signal
   * @return If is in the correct trading
   */
  public boolean checkCompatible(final @NotNull SignalTrend signalTrend) {
    if (!signalTrend.equals(SignalTrend.NEUTRAL) && this.getService().isActive()) {
      final int simplePeriod = this.getService().getSimplePeriod();
      final BigDecimal simple = this.getCandlestickService().getCandlesticksClose(simplePeriod).map(Candlestick::close).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(simplePeriod), 10, RoundingMode.HALF_UP);
      final int exponentialPeriod = this.getService().getExponentialPeriod();
      final int memorySize = this.getService().getMemorySize();
      final BigDecimal exponential = Tools.getEMA(exponentialPeriod, this.getCandlestickService().getCandlesticksClose(memorySize - 5).map(Candlestick::close).toArray(BigDecimal[]::new))[0];

      final boolean sell = signalTrend.equals(SignalTrend.STRONG_SELL) || signalTrend.equals(SignalTrend.SELL);
      final boolean buy = signalTrend.equals(SignalTrend.STRONG_BUY) || signalTrend.equals(SignalTrend.BUY);
      if ((simple.compareTo(exponential) > 0) && sell) {
        return true;
      } else if ((simple.compareTo(exponential) > 0) && buy) {
        return false;
      }
      if ((simple.compareTo(exponential) < 0) && sell) {
        return false;
      } else if ((simple.compareTo(exponential) < 0) && buy) {
        return true;
      }
    }
    return true;
  }
}
