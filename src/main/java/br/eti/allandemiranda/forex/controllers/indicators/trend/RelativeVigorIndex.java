package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.RVI;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.RviService;
import br.eti.allandemiranda.forex.utils.IndicatorTrend;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
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
    final Function<Candlestick[], BigDecimal> smoothingNumerator = candlesticks -> {
      final BigDecimal a = candlesticks[0].close().subtract(candlesticks[0].open());
      final BigDecimal b = candlesticks[1].close().subtract(candlesticks[1].open());
      final BigDecimal c = candlesticks[2].close().subtract(candlesticks[2].open());
      final BigDecimal d = candlesticks[3].close().subtract(candlesticks[3].open());
      return (a.add(BigDecimal.TWO.multiply(b)).add(BigDecimal.TWO.multiply(c)).add(d)).divide(BigDecimal.valueOf(6), 10, RoundingMode.HALF_UP);
    };
    final BigDecimal[] numeratorSMAs = this.getCandlestickService().getSMA(smoothingNumerator, 4, this.getPeriod());
    final Function<Candlestick[], BigDecimal> smoothingDenominator = candlesticks -> {
      final BigDecimal a = candlesticks[0].high().subtract(candlesticks[0].low());
      final BigDecimal b = candlesticks[1].high().subtract(candlesticks[1].low());
      final BigDecimal c = candlesticks[2].high().subtract(candlesticks[2].low());
      final BigDecimal d = candlesticks[3].high().subtract(candlesticks[3].low());
      return (a.add(BigDecimal.TWO.multiply(b)).add(BigDecimal.TWO.multiply(c)).add(d)).divide(BigDecimal.valueOf(6), 10, RoundingMode.HALF_UP);
    };
    final BigDecimal[] denominatorSMAs = this.getCandlestickService().getSMA(smoothingDenominator, 4, this.getPeriod());
    final BigDecimal rvi = numeratorSMAs[0].divide(denominatorSMAs[0], 10, RoundingMode.HALF_UP);
    final BigDecimal i = numeratorSMAs[1].divide(denominatorSMAs[1], 10, RoundingMode.HALF_UP);
    final BigDecimal j = numeratorSMAs[2].divide(denominatorSMAs[2], 10, RoundingMode.HALF_UP);
    final BigDecimal k = numeratorSMAs[3].divide(denominatorSMAs[3], 10, RoundingMode.HALF_UP);
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
        this.getRviService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.BUY, this.getCandlestickService().getOldestCandlestick().close());
        return IndicatorTrend.BUY;
      }
      if (rvis[rvis.length - 1].value().compareTo(rvis[rvis.length - 1].signal()) < 0 && rvis[rvis.length - 1].value().compareTo(BigDecimal.ZERO) > 0
          && rvis[rvis.length - 1].signal().compareTo(BigDecimal.ZERO) > 0 && isCross(rvis)) {
        this.getRviService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.SELL, this.getCandlestickService().getOldestCandlestick().close());
        return IndicatorTrend.SELL;
      }
    }
    this.getRviService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.NEUTRAL, this.getCandlestickService().getOldestCandlestick().close());
    return IndicatorTrend.NEUTRAL;
  }
}
