package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.RVI;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.RviService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class RelativeVigorIndex implements Indicator {

  private final RviService rviService;
  private final CandlestickService candlestickService;

  @Value("${rvi.parameters.period:10}")
  private int period;

  public RelativeVigorIndex(final RviService rviService, final CandlestickService candlestickService) {
    this.rviService = rviService;
    this.candlestickService = candlestickService;
  }

  @Override
  public boolean run() {
    if (this.getCandlestickService().isReady()) {
      final Candlestick[] candlesticks = this.getCandlestickService().getCandlesticks(this.getPeriod() + 5);
      final BigDecimal[] numerator = IntStream.range(5, candlesticks.length).mapToObj(
          i -> ((candlesticks[i].close().subtract(candlesticks[i].open())).add(BigDecimal.TWO.multiply(candlesticks[i - 1].close().subtract(candlesticks[i - 1].open())))
              .add(BigDecimal.TWO.multiply(candlesticks[i - 2].close().subtract(candlesticks[i - 2].open())))
              .add(candlesticks[i - 3].close().subtract(candlesticks[i - 3].open()))).divide(BigDecimal.valueOf(6), 5, RoundingMode.DOWN)).toArray(BigDecimal[]::new);
      final BigDecimal[] denominator = IntStream.range(5, candlesticks.length).mapToObj(
          i -> ((candlesticks[i].high().subtract(candlesticks[i].low())).add(BigDecimal.TWO.multiply(candlesticks[i - 1].high().subtract(candlesticks[i - 1].low())))
              .add(BigDecimal.TWO.multiply(candlesticks[i - 2].high().subtract(candlesticks[i - 2].low())))
              .add(candlesticks[i - 3].high().subtract(candlesticks[i - 3].low()))).divide(BigDecimal.valueOf(6), 5, RoundingMode.DOWN)).toArray(BigDecimal[]::new);
      final BigDecimal[] numeratorSMA = IntStream.range(0, 4).mapToObj(i -> Arrays.stream(numerator, i, this.getPeriod()).reduce(BigDecimal.ZERO, BigDecimal::add)
          .divide(BigDecimal.valueOf(this.getPeriod()), 5, RoundingMode.DOWN)).toArray(BigDecimal[]::new);
      final BigDecimal[] denominatorSMA = IntStream.range(0, 4).mapToObj(i -> Arrays.stream(denominator, i, this.getPeriod()).reduce(BigDecimal.ZERO, BigDecimal::add)
          .divide(BigDecimal.valueOf(this.getPeriod()), 5, RoundingMode.DOWN)).toArray(BigDecimal[]::new);
      final BigDecimal[] rvi = IntStream.range(0, numeratorSMA.length).mapToObj(i -> numeratorSMA[i].divide(denominatorSMA[i], 5, RoundingMode.DOWN))
          .toArray(BigDecimal[]::new);
      final BigDecimal signal = (rvi[rvi.length - 1].add(BigDecimal.TWO.multiply(rvi[rvi.length - 2])).add(BigDecimal.TWO.multiply(rvi[rvi.length - 3]))
          .add(rvi[rvi.length - 4])).divide(BigDecimal.valueOf(6), 5, RoundingMode.DOWN);
      this.getRviService()
          .addRvi(this.getCandlestickService().getLastCandlestick().realDateTime(), this.getCandlestickService().getLastCandlestick().dateTime(), rvi[rvi.length - 1],
              signal);
      return true;
    }
    return false;
  }

  @Override
  public @NotNull SignalTrend getCurrentSignal() {
    final RVI[] rvis = this.getRviService().getRVIs();
    if(rvis.length > 1) {
      if (rvis[0].value().compareTo(rvis[0].signal()) > 0 && rvis[1].value().compareTo(rvis[1].signal()) < 0
          || rvis[0].value().compareTo(rvis[0].signal()) < 0 && rvis[1].value().compareTo(rvis[1].signal()) > 0) {
        if (rvis[1].value().compareTo(rvis[1].signal()) < 0) {
          this.getRviService().updateDebugFile(this.getCandlestickService().getLastCandlestick().realDateTime(), SignalTrend.STRONG_SELL,
              this.getCandlestickService().getLastCandlestick().close());
          return SignalTrend.STRONG_SELL;
        } else {
          this.getRviService().updateDebugFile(this.getCandlestickService().getLastCandlestick().realDateTime(), SignalTrend.STRONG_BUY,
              this.getCandlestickService().getLastCandlestick().close());
          return SignalTrend.STRONG_BUY;
        }
      } else {
        if (rvis[1].value().compareTo(rvis[1].signal()) < 0) {
          this.getRviService().updateDebugFile(this.getCandlestickService().getLastCandlestick().realDateTime(), SignalTrend.SELL,
              this.getCandlestickService().getLastCandlestick().close());
          return SignalTrend.SELL;
        } else {
          this.getRviService().updateDebugFile(this.getCandlestickService().getLastCandlestick().realDateTime(), SignalTrend.BUY,
              this.getCandlestickService().getLastCandlestick().close());
          return SignalTrend.BUY;
        }
      }
    } else {
      return SignalTrend.OUT;
    }
  }
}
