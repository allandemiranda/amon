package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.enums.IndicatorTrend;
import br.eti.allandemiranda.forex.services.AcService;
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
public class AceleradorOscilador implements Indicator {

  private final AcService acService;
  private final CandlestickService candlestickService;

  @Autowired
  protected AceleradorOscilador(final AcService acService, final CandlestickService candlestickService) {
    this.acService = acService;
    this.candlestickService = candlestickService;
  }

  @Override
  public @NotNull IndicatorTrend getSignal() {
    try {
      final BigDecimal price = this.getCandlestickService().getLastCandlestick().close();
      if (this.getAcService().getAc().length > 1) {
        if (this.getAcService().getAc()[0].value().compareTo(this.getAcService().getAc()[1].value()) > 0) {
          this.getAcService().updateDebugFile(price, IndicatorTrend.BUY);
          return IndicatorTrend.BUY;
        }
        if (this.getAcService().getAc()[0].value().compareTo(this.getAcService().getAc()[1].value()) < 0) {
          this.getAcService().updateDebugFile(price, IndicatorTrend.SELL);
          return IndicatorTrend.SELL;
        }
      }
      this.getAcService().updateDebugFile(price, IndicatorTrend.NEUTRAL);
      return IndicatorTrend.NEUTRAL;
    } catch (NullPointerException e) {
      log.warn("Forcing set AC indicator NEUTRAL");
      return IndicatorTrend.NEUTRAL;
    }
  }

  @Override
  public void run() {
    try {
      final Candlestick[] candlesticks = this.getCandlestickService().getCandlesticksClose(38).toArray(Candlestick[]::new);
      final BigDecimal[] medianPrices = new BigDecimal[candlesticks.length];
      IntStream.range(0, medianPrices.length).parallel()
          .forEach(i -> medianPrices[i] = (candlesticks[i].high().add(candlesticks[i].low())).divide(BigDecimal.TWO, 10, RoundingMode.HALF_UP));

      final BigDecimal[] smaFive = new BigDecimal[5];
      final BigDecimal[] smaThirtyFour = new BigDecimal[5];
      final Thread smaFiveThread = Thread.ofVirtual().unstarted(() -> IntStream.range(0, 5).parallel().forEach(
          i -> smaFive[i] = IntStream.range(0, 5).mapToObj(j -> medianPrices[i + j]).reduce(BigDecimal.ZERO, BigDecimal::add)
              .divide(BigDecimal.valueOf(5), 10, RoundingMode.HALF_UP)));
      final Thread smaThirtyFourThread = Thread.ofVirtual().unstarted(() -> IntStream.range(0, 5).parallel().forEach(
          i -> smaThirtyFour[i] = IntStream.range(0, 34).mapToObj(j -> medianPrices[i + j]).reduce(BigDecimal.ZERO, BigDecimal::add)
              .divide(BigDecimal.valueOf(34), 10, RoundingMode.HALF_UP)));
      Tools.startThreadsUnstated(smaFiveThread, smaThirtyFourThread);

      final BigDecimal[] aos = new BigDecimal[5];
      IntStream.range(0, 5).parallel().forEach(i -> aos[i] = smaFive[i].subtract(smaThirtyFour[i]));
      final BigDecimal ac = aos[0].subtract(Arrays.stream(aos).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(5), 10, RoundingMode.HALF_UP));
      this.getAcService().addAc(candlesticks[0].dateTime(), ac);
    } catch (Exception e) {
      log.warn("Can't generate AC indicator: {}", e.getMessage());
    }
  }
}
