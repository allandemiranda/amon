package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.ADX;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.services.AdxService;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.utils.IndicatorTrend;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class AverageDirectionalMovementIndex implements Indicator {

  private final AdxService adxService;
  private final CandlestickService candlestickService;

  @Value("${adx.parameters.period:14}")
  private int period;

  @Autowired
  protected AverageDirectionalMovementIndex(final AdxService adxService, final CandlestickService candlestickService) {
    this.adxService = adxService;
    this.candlestickService = candlestickService;
  }

  @Override
  public @NotNull IndicatorTrend getSignal() {
    final ADX adx = this.getAdxService().getAdx();
    if (adx.value().compareTo(BigDecimal.valueOf(50)) >= 0) {
      if (adx.diPlus().compareTo(adx.diMinus()) > 0) {
        this.getAdxService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.BUY, this.getCandlestickService().getOldestCandlestick().close());
        return IndicatorTrend.BUY;
      } else if (adx.diPlus().compareTo(adx.diMinus()) < 0) {
        this.getAdxService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.SELL, this.getCandlestickService().getOldestCandlestick().close());
        return IndicatorTrend.SELL;
      }
    }
    this.getAdxService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.NEUTRAL, this.getCandlestickService().getOldestCandlestick().close());
    return IndicatorTrend.NEUTRAL;
  }

  @Override
  public void run() {
    final Function<Candlestick[], BigDecimal> trSmoothing = candlesticks -> {
      final BigDecimal highCurrent = candlesticks[0].high();
      final BigDecimal lowCurrent = candlesticks[0].low();
      final BigDecimal closeLast = candlesticks[1].close();
      return (highCurrent.subtract(lowCurrent)).max(highCurrent.subtract(closeLast).abs()).max(lowCurrent.subtract(closeLast).abs());
    };
    final Function<Candlestick[], BigDecimal> dmPlusSmoothing = candlesticks -> {
      final BigDecimal highCurrent = candlesticks[0].high();
      final BigDecimal highLast = candlesticks[1].high();
      final BigDecimal lowCurrent = candlesticks[0].low();
      final BigDecimal lowLast = candlesticks[0].low();
      return (highCurrent.subtract(highLast)).compareTo(lowLast.subtract(lowCurrent)) > 0 ? (highCurrent.subtract(highLast)).max(BigDecimal.ZERO) : BigDecimal.ZERO;
    };
    final Function<Candlestick[], BigDecimal> dmMinusSmoothing = candlesticks -> {
      final BigDecimal highCurrent = candlesticks[0].high();
      final BigDecimal highLast = candlesticks[1].high();
      final BigDecimal lowCurrent = candlesticks[0].low();
      final BigDecimal lowLast = candlesticks[0].low();
      return (lowLast.subtract(lowCurrent)).compareTo(highCurrent.subtract(highLast)) > 0 ? (lowLast.subtract(lowCurrent)).max(BigDecimal.ZERO) : BigDecimal.ZERO;
    };
    final BigDecimal[] trArray = this.getCandlestickService().getSMA(trSmoothing, 2, this.getPeriod());
    final BigDecimal[] dmPlusArray = this.getCandlestickService().getSMA(dmPlusSmoothing, 2, this.getPeriod());
    final BigDecimal[] dmMinusArray = this.getCandlestickService().getSMA(dmMinusSmoothing, 2, this.getPeriod());

    final BigDecimal[] trP = IntStream.rangeClosed(0, trArray.length - this.getPeriod())
        .mapToObj(i -> Arrays.stream(trArray, i, i + this.getPeriod()).reduce(BigDecimal.ZERO, BigDecimal::add)).toArray(BigDecimal[]::new);
    final BigDecimal[] dmPlusP = IntStream.rangeClosed(0, dmPlusArray.length - this.getPeriod())
        .mapToObj(i -> Arrays.stream(dmPlusArray, i, i + this.getPeriod()).reduce(BigDecimal.ZERO, BigDecimal::add)).toArray(BigDecimal[]::new);
    final BigDecimal[] dmMinusP = IntStream.rangeClosed(0, dmMinusArray.length - this.getPeriod())
        .mapToObj(i -> Arrays.stream(dmMinusArray, i, i + this.getPeriod()).reduce(BigDecimal.ZERO, BigDecimal::add)).toArray(BigDecimal[]::new);

    final BigDecimal[] diPlus = IntStream.range(0, trP.length).mapToObj(i -> BigDecimal.valueOf(100).multiply(dmPlusP[i].divide(trP[i], 10, RoundingMode.HALF_UP)))
        .toArray(BigDecimal[]::new);
    final BigDecimal[] diMinus = IntStream.range(0, trP.length).mapToObj(i -> BigDecimal.valueOf(100).multiply(dmMinusP[i].divide(trP[i], 10, RoundingMode.HALF_UP)))
        .toArray(BigDecimal[]::new);

    final BigDecimal[] dx = IntStream.range(0, diPlus.length).mapToObj(i -> {
      final BigDecimal diDiff = diPlus[i].subtract(diMinus[i]).abs();
      final BigDecimal diSum = diPlus[i].add(diMinus[i]);
      return BigDecimal.valueOf(100).multiply(diDiff.divide(diSum, 10, RoundingMode.HALF_UP));
    }).toArray(BigDecimal[]::new);
    final BigDecimal dxMed = Arrays.stream(dx).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(this.getPeriod()), 10, RoundingMode.HALF_UP);

    this.getAdxService().addAdx(this.getCandlestickService().getOldestCandlestick().realDateTime(), dxMed, diPlus[0], diMinus[0]);
  }
}
