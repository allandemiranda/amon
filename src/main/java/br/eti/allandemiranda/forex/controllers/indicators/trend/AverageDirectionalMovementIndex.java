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
    final BigDecimal price = this.getCandlestickService().getLastCandlestick().close();
    if (adx.value().compareTo(BigDecimal.valueOf(25)) > 0) {
      if (adx.diPlus().compareTo(adx.diMinus()) > 0) {
        this.getAdxService().updateDebugFile(IndicatorTrend.BUY, price);
        return IndicatorTrend.BUY;
      }
      if (adx.diPlus().compareTo(adx.diMinus()) < 0) {
        this.getAdxService().updateDebugFile(IndicatorTrend.SELL, price);
        return IndicatorTrend.SELL;
      }
    }
    this.getAdxService().updateDebugFile(IndicatorTrend.NEUTRAL, price);
    return IndicatorTrend.NEUTRAL;
  }

  @Override
  public void run() {
    final Candlestick[] candlesticks = this.candlestickService.getCandlesticks((3 * this.getPeriod()) - 2).toArray(Candlestick[]::new);

    final BigDecimal[] trRow = IntStream.rangeClosed(0, candlesticks.length - 2).mapToObj(i -> {
      final BigDecimal highCurrent = candlesticks[i].high();
      final BigDecimal lowCurrent = candlesticks[i].low();
      final BigDecimal closeLast = candlesticks[i + 1].close();
      return (highCurrent.subtract(lowCurrent)).max(highCurrent.subtract(closeLast).abs()).max(lowCurrent.subtract(closeLast).abs());
    }).toArray(BigDecimal[]::new);
    final BigDecimal[] dmPlusRow = IntStream.rangeClosed(0, candlesticks.length - 2).mapToObj(i -> {
      final BigDecimal highCurrent = candlesticks[i].high();
      final BigDecimal highLast = candlesticks[i + 1].high();
      final BigDecimal lowCurrent = candlesticks[i].low();
      final BigDecimal lowLast = candlesticks[i + 1].low();
      return (highCurrent.subtract(highLast)).compareTo(lowLast.subtract(lowCurrent)) > 0 ? (highCurrent.subtract(highLast)).max(BigDecimal.ZERO) : BigDecimal.ZERO;
    }).toArray(BigDecimal[]::new);
    final BigDecimal[] dmMinusRow = IntStream.rangeClosed(0, candlesticks.length - 2).mapToObj(i -> {
      final BigDecimal highCurrent = candlesticks[i].high();
      final BigDecimal highLast = candlesticks[i + 1].high();
      final BigDecimal lowCurrent = candlesticks[i].low();
      final BigDecimal lowLast = candlesticks[i + 1].low();
      return (lowLast.subtract(lowCurrent)).compareTo(highCurrent.subtract(highLast)) > 0 ? (lowLast.subtract(lowCurrent)).max(BigDecimal.ZERO) : BigDecimal.ZERO;
    }).toArray(BigDecimal[]::new);

    final BigDecimal[] trSum = IntStream.range(0, this.getPeriod()).mapToObj(i -> Arrays.stream(trRow, i, this.getPeriod() + i).reduce(BigDecimal.ZERO, BigDecimal::add))
        .toArray(BigDecimal[]::new);
    final BigDecimal[] dmPlusSum = IntStream.range(0, this.getPeriod())
        .mapToObj(i -> Arrays.stream(dmPlusRow, i, this.getPeriod() + i).reduce(BigDecimal.ZERO, BigDecimal::add)).toArray(BigDecimal[]::new);
    final BigDecimal[] dmMinusSum = IntStream.range(0, this.getPeriod())
        .mapToObj(i -> Arrays.stream(dmMinusRow, i, this.getPeriod() + i).reduce(BigDecimal.ZERO, BigDecimal::add)).toArray(BigDecimal[]::new);

    final BigDecimal[] diPlus = IntStream.range(0, this.getPeriod())
        .mapToObj(i -> BigDecimal.valueOf(100).multiply(dmPlusSum[i].divide(trSum[i], 10, RoundingMode.HALF_UP))).toArray(BigDecimal[]::new);
    final BigDecimal[] diMinus = IntStream.range(0, this.getPeriod())
        .mapToObj(i -> BigDecimal.valueOf(100).multiply(dmMinusSum[i].divide(trSum[i], 10, RoundingMode.HALF_UP))).toArray(BigDecimal[]::new);

    final BigDecimal[] diDiff = IntStream.range(0, this.getPeriod()).mapToObj(i -> diPlus[i].subtract(diMinus[i]).abs()).toArray(BigDecimal[]::new);
    final BigDecimal[] diSum = IntStream.range(0, this.getPeriod()).mapToObj(i -> diPlus[i].add(diMinus[i])).toArray(BigDecimal[]::new);

    final BigDecimal[] dx = IntStream.range(0, this.getPeriod()).mapToObj(i -> BigDecimal.valueOf(100).multiply(diDiff[i].divide(diSum[i], 10, RoundingMode.HALF_UP)))
        .toArray(BigDecimal[]::new);
    final BigDecimal adx = Arrays.stream(dx).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(this.getPeriod()), 10, RoundingMode.HALF_UP);

    this.getAdxService().addAdx(candlesticks[0].dateTime(), adx, diPlus[0], diMinus[0]);
  }
}
