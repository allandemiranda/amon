package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.RVI;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.RviService;
import br.eti.allandemiranda.forex.utils.IndicatorTrend;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

  private static boolean isCross(final RVI @NotNull [] rvis) {
    if (rvis.length == 3 && rvis[1].value().compareTo(rvis[1].signal()) == 0) {
      return rvis[0].value().compareTo(rvis[0].signal()) > 0 && rvis[2].value().compareTo(rvis[2].signal()) < 0
          || rvis[0].value().compareTo(rvis[0].signal()) < 0 && rvis[2].value().compareTo(rvis[2].signal()) > 0;
    } else if (rvis.length >= 2) {
      return rvis[0].value().compareTo(rvis[0].signal()) > 0 && rvis[1].value().compareTo(rvis[1].signal()) < 0
          || rvis[0].value().compareTo(rvis[0].signal()) < 0 && rvis[1].value().compareTo(rvis[1].signal()) > 0;
    } else {
      return false;
    }
  }

  @Override
  public void run() {
    final BigDecimal[] closeOpens = this.getCandlestickService().getCandlesticks(this.getPeriod() + (2 * 4) - 2)
        .map(candlestick -> candlestick.close().subtract(candlestick.open())).toArray(BigDecimal[]::new);
    final BigDecimal[] highLows = this.getCandlestickService().getCandlesticks(this.getPeriod() + (2 * 4) - 2)
        .map(candlestick -> candlestick.high().subtract(candlestick.low())).toArray(BigDecimal[]::new);

    final BigDecimal[] numerators = IntStream.rangeClosed(0, closeOpens.length - 4).mapToObj(
        i -> (closeOpens[i].add(BigDecimal.TWO.multiply(closeOpens[i + 1])).add(BigDecimal.TWO.multiply(closeOpens[i + 2])).add(closeOpens[i + 3])).divide(
            BigDecimal.valueOf(6), 10, RoundingMode.HALF_UP)).toArray(BigDecimal[]::new);
    final BigDecimal[] dominators = IntStream.rangeClosed(0, closeOpens.length - 4).mapToObj(
        i -> (highLows[i].add(BigDecimal.TWO.multiply(highLows[i + 1])).add(BigDecimal.TWO.multiply(highLows[i + 2])).add(highLows[i + 3])).divide(BigDecimal.valueOf(6),
            10, RoundingMode.HALF_UP)).toArray(BigDecimal[]::new);

    final BigDecimal[] numeratorsSMA = IntStream.rangeClosed(0, 3).mapToObj(
        i -> java.util.Arrays.stream(numerators, i, i + this.getPeriod()).reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(this.getPeriod()), 10, RoundingMode.HALF_UP)).toArray(BigDecimal[]::new);
    final BigDecimal[] dominatorsSMA = IntStream.rangeClosed(0, 3).mapToObj(
        i -> java.util.Arrays.stream(dominators, i, i + this.getPeriod()).reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(this.getPeriod()), 10, RoundingMode.HALF_UP)).toArray(BigDecimal[]::new);

    final BigDecimal rvi = numeratorsSMA[0].divide(dominatorsSMA[0], 10, RoundingMode.HALF_UP);
    final BigDecimal i = numeratorsSMA[1].divide(dominatorsSMA[1], 10, RoundingMode.HALF_UP);
    final BigDecimal j = numeratorsSMA[2].divide(dominatorsSMA[2], 10, RoundingMode.HALF_UP);
    final BigDecimal k = numeratorsSMA[3].divide(dominatorsSMA[3], 10, RoundingMode.HALF_UP);
    final BigDecimal signal = (rvi.add(BigDecimal.TWO.multiply(i)).add(BigDecimal.TWO.multiply(j)).add(k)).divide(BigDecimal.valueOf(6), 10, RoundingMode.HALF_UP);
    this.getRviService()
        .addRvi(this.getCandlestickService().getOldestCandlestick().realDateTime(), this.getCandlestickService().getOldestCandlestick().dateTime(), rvi, signal);
  }

  @Override
  public @NotNull IndicatorTrend getSignal() {
    final RVI[] rvis = this.getRviService().getRVIs();
    if (rvis.length > 1) {
      if (rvis[rvis.length - 1].value().compareTo(rvis[rvis.length - 1].signal()) > 0 && rvis[rvis.length - 1].value().compareTo(BigDecimal.ZERO) < 0
          && rvis[rvis.length - 1].signal().compareTo(BigDecimal.ZERO) < 0 && isCross(rvis)) {
        this.getRviService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.BUY,
            this.getCandlestickService().getOldestCandlestick().close());
        return IndicatorTrend.BUY;
      }
      if (rvis[rvis.length - 1].value().compareTo(rvis[rvis.length - 1].signal()) < 0 && rvis[rvis.length - 1].value().compareTo(BigDecimal.ZERO) > 0
          && rvis[rvis.length - 1].signal().compareTo(BigDecimal.ZERO) > 0 && isCross(rvis)) {
        this.getRviService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.SELL,
            this.getCandlestickService().getOldestCandlestick().close());
        return IndicatorTrend.SELL;
      }
    }
    this.getRviService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.NEUTRAL,
        this.getCandlestickService().getOldestCandlestick().close());
    return IndicatorTrend.NEUTRAL;
  }
}
