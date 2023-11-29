package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.enums.IndicatorTrend;
import br.eti.allandemiranda.forex.services.AcService;
import br.eti.allandemiranda.forex.services.CandlestickService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
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
  }

  @Override
  public void run() {
    final Candlestick[] candlesticks = this.getCandlestickService().getCandlesticksClose(38).toArray(Candlestick[]::new);
    final BigDecimal[] medianPrices = Arrays.stream(candlesticks)
        .map(candlestick -> (candlestick.high().add(candlestick.low())).divide(BigDecimal.TWO, 10, RoundingMode.HALF_UP)).toArray(BigDecimal[]::new);
    final BigDecimal[] smaFive = IntStream.range(0, 5).mapToObj(
            i -> IntStream.range(0, 5).mapToObj(j -> medianPrices[i + j]).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(5), 10, RoundingMode.HALF_UP))
        .toArray(BigDecimal[]::new);
    final BigDecimal[] smaThirtyFour = IntStream.range(0, 5).mapToObj(
            i -> IntStream.range(0, 34).mapToObj(j -> medianPrices[i + j]).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(34), 10, RoundingMode.HALF_UP))
        .toArray(BigDecimal[]::new);
    final BigDecimal[] aos = IntStream.range(0, 5).mapToObj(i -> smaFive[i].subtract(smaThirtyFour[i])).toArray(BigDecimal[]::new);
    final BigDecimal ac = aos[0].subtract(Arrays.stream(aos).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(5), 10, RoundingMode.HALF_UP));
    this.getAcService().addAc(candlesticks[0].dateTime(), ac);
  }
}
