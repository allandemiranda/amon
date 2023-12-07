package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.ADX;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.enums.IndicatorTrend;
import br.eti.allandemiranda.forex.services.AdxService;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.utils.Tools;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class AverageDirectionalMovementIndex implements Indicator {

  private final AdxService adxService;
  private final CandlestickService candlestickService;

  @Autowired
  protected AverageDirectionalMovementIndex(final AdxService adxService, final CandlestickService candlestickService) {
    this.adxService = adxService;
    this.candlestickService = candlestickService;
  }

  @Override
  public @NotNull IndicatorTrend getSignal() {
    try {
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
    } catch (NullPointerException e) {
      log.warn("Forcing set ADX indicator NEUTRAL");
      return IndicatorTrend.NEUTRAL;
    }
  }

  @Override
  public void run() {
    try {
      final Candlestick[] candlesticks = this.candlestickService.getCandlesticksClose(2 * this.getAdxService().getPeriod()).toArray(Candlestick[]::new);

      final BigDecimal[] trRow = new BigDecimal[candlesticks.length - 1];
      final BigDecimal[] dmPlusRow = new BigDecimal[candlesticks.length - 1];
      final BigDecimal[] dmMinusRow = new BigDecimal[candlesticks.length - 1];
      final Thread trRowThread = Thread.ofVirtual().unstarted(() -> IntStream.range(0, candlesticks.length - 1).parallel().forEach(i -> {
        final BigDecimal highCurrent = candlesticks[i].high();
        final BigDecimal lowCurrent = candlesticks[i].low();
        final BigDecimal closeLast = candlesticks[i + 1].close();
        trRow[i] = (highCurrent.subtract(lowCurrent)).max((highCurrent.subtract(closeLast)).abs()).max((lowCurrent.subtract(closeLast)).abs());
      }));
      final Thread dmPlusRowThread = Thread.ofVirtual().unstarted(() -> IntStream.range(0, candlesticks.length - 1).parallel().forEach(i -> {
        final BigDecimal highCurrent = candlesticks[i].high();
        final BigDecimal highLast = candlesticks[i + 1].high();
        final BigDecimal lowCurrent = candlesticks[i].low();
        final BigDecimal lowLast = candlesticks[i + 1].low();
        dmPlusRow[i] =
            (highCurrent.subtract(highLast)).compareTo(lowLast.subtract(lowCurrent)) > 0 ? (highCurrent.subtract(highLast)).max(BigDecimal.ZERO) : BigDecimal.ZERO;
      }));
      final Thread dmMinusThread = Thread.ofVirtual().unstarted(() -> IntStream.range(0, candlesticks.length - 1).parallel().forEach(i -> {
        final BigDecimal highCurrent = candlesticks[i].high();
        final BigDecimal highLast = candlesticks[i + 1].high();
        final BigDecimal lowCurrent = candlesticks[i].low();
        final BigDecimal lowLast = candlesticks[i + 1].low();
        dmMinusRow[i] =
            (lowLast.subtract(lowCurrent)).compareTo(highCurrent.subtract(highLast)) > 0 ? (lowLast.subtract(lowCurrent)).max(BigDecimal.ZERO) : BigDecimal.ZERO;
      }));
      Tools.startThreadsUnstated(trRowThread, dmPlusRowThread, dmMinusThread);

      final BigDecimal[] trSum = new BigDecimal[this.getAdxService().getPeriod()];
      final BigDecimal[] dmPlusSum = new BigDecimal[this.getAdxService().getPeriod()];
      final BigDecimal[] dmMinusSum = new BigDecimal[this.getAdxService().getPeriod()];
      final Thread trSumThread = Thread.ofVirtual().unstarted(() -> IntStream.range(0, this.getAdxService().getPeriod()).parallel()
          .forEach(i -> trSum[i] = Arrays.stream(trRow, i, this.getAdxService().getPeriod() + i).reduce(BigDecimal.ZERO, BigDecimal::add)));
      final Thread dmPlusSumThread = Thread.ofVirtual().unstarted(() -> IntStream.range(0, this.getAdxService().getPeriod()).parallel()
          .forEach(i -> dmPlusSum[i] = Arrays.stream(dmPlusRow, i, this.getAdxService().getPeriod() + i).reduce(BigDecimal.ZERO, BigDecimal::add)));
      final Thread dmMinusSumThread = Thread.ofVirtual().unstarted(() -> IntStream.range(0, this.getAdxService().getPeriod()).parallel()
          .forEach(i -> dmMinusSum[i] = Arrays.stream(dmMinusRow, i, this.getAdxService().getPeriod() + i).reduce(BigDecimal.ZERO, BigDecimal::add)));
      Tools.startThreadsUnstated(trSumThread, dmPlusSumThread, dmMinusSumThread);

      final BigDecimal[] diPlus = new BigDecimal[this.getAdxService().getPeriod()];
      final BigDecimal[] diMinus = new BigDecimal[this.getAdxService().getPeriod()];
      final Thread diPlusThread = Thread.ofVirtual().unstarted(() -> IntStream.range(0, this.getAdxService().getPeriod()).parallel()
          .forEach(i -> diPlus[i] = BigDecimal.valueOf(100).multiply(dmPlusSum[i].divide(trSum[i], 10, RoundingMode.HALF_UP))));
      final Thread diMinusThread = Thread.ofVirtual().unstarted(() -> IntStream.range(0, this.getAdxService().getPeriod()).parallel()
          .forEach(i -> diMinus[i] = BigDecimal.valueOf(100).multiply(dmMinusSum[i].divide(trSum[i], 10, RoundingMode.HALF_UP))));
      Tools.startThreadsUnstated(diPlusThread, diMinusThread);

      final BigDecimal[] diDiff = new BigDecimal[this.getAdxService().getPeriod()];
      final BigDecimal[] diSum = new BigDecimal[this.getAdxService().getPeriod()];
      final Thread diDiffThread = Thread.ofVirtual()
          .unstarted(() -> IntStream.range(0, this.getAdxService().getPeriod()).parallel().forEach(i -> diDiff[i] = diPlus[i].subtract(diMinus[i]).abs()));
      final Thread diSumThread = Thread.ofVirtual()
          .unstarted(() -> IntStream.range(0, this.getAdxService().getPeriod()).parallel().forEach(i -> diSum[i] = diPlus[i].add(diMinus[i])));
      Tools.startThreadsUnstated(diDiffThread, diSumThread);

      final BigDecimal[] dx = new BigDecimal[this.getAdxService().getPeriod()];
      IntStream.range(0, this.getAdxService().getPeriod()).parallel()
          .forEach(i -> dx[i] = BigDecimal.valueOf(100).multiply(diDiff[i].divide(diSum[i], 10, RoundingMode.HALF_UP)));
      final BigDecimal adx = Arrays.stream(dx).reduce(BigDecimal.ZERO, BigDecimal::add)
          .divide(BigDecimal.valueOf(this.getAdxService().getPeriod()), 10, RoundingMode.HALF_UP);

      this.getAdxService().addAdx(candlesticks[0].dateTime(), adx, diPlus[0], diMinus[0]);
    } catch (Exception e) {
      log.warn("Can't generate ADX indicator: {}", e.getMessage());
    }
  }
}
