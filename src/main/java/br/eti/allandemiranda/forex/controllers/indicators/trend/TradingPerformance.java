package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.enums.SignalTrend;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.TradingPerformanceService;
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
    if (!signalTrend.equals(SignalTrend.NEUTRAL)) {
      final int memorySize = this.getService().getMemorySize();
      final Candlestick[] candlesticks = this.getCandlestickService().getCandlesticksClose(memorySize - 5).toArray(Candlestick[]::new);
      final BigDecimal[] closeValues = new BigDecimal[candlesticks.length];
      IntStream.range(0, candlesticks.length).parallel().forEach(i -> closeValues[i] = candlesticks[i].close());

      final int simplePeriod = this.getService().getSimplePeriod();
      final AtomicReference<BigDecimal> simple = new AtomicReference<>();
      final Thread simpleThread = Thread.ofVirtual().unstarted(() -> simple.set(
          Arrays.stream(closeValues, 0, simplePeriod).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(simplePeriod), 10, RoundingMode.HALF_UP)));

      final int exponentialPeriod = this.getService().getExponentialPeriod();
      final AtomicReference<BigDecimal> exponential = new AtomicReference<>();
      final Thread exponentialThread = Thread.ofVirtual().unstarted(() -> exponential.set(Tools.getEMA(exponentialPeriod, closeValues)[0]));

      Tools.startThreadsUnstated(simpleThread, exponentialThread);

      final boolean sell = signalTrend.equals(SignalTrend.STRONG_SELL) || signalTrend.equals(SignalTrend.SELL);
      final boolean buy = signalTrend.equals(SignalTrend.STRONG_BUY) || signalTrend.equals(SignalTrend.BUY);

      if (simple.get().compareTo(exponential.get()) == 0) {
        final BigDecimal lastSimple = this.getService().getTradingPerformance().getKey();
        final BigDecimal lastExponential = this.getService().getTradingPerformance().getValue();
        this.getService().addTradingPerformance(simple.get(), exponential.get());
        return (lastSimple.compareTo(lastExponential) > 0 && buy) || (lastSimple.compareTo(lastExponential) < 0 && sell);
      } else {
        this.getService().addTradingPerformance(simple.get(), exponential.get());
        return ((simple.get().compareTo(exponential.get()) > 0) && sell) || ((simple.get().compareTo(exponential.get()) < 0) && buy);
      }
    }
    return true;
  }
}
