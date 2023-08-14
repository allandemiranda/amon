package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.CciService;
import br.eti.allandemiranda.forex.utils.IndicatorTrend;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class CommodityChannelIndex implements Indicator {

  private final CciService cciService;
  private final CandlestickService candlestickService;

  @Value("${cci.parameters.period:14}")
  private int period;

  @Autowired
  protected CommodityChannelIndex(final CciService cciService, final CandlestickService candlestickService) {
    this.cciService = cciService;
    this.candlestickService = candlestickService;
  }

  @Override
  public @NotNull IndicatorTrend getSignal() {
    if (this.getCciService().getCci().value().compareTo(BigDecimal.valueOf(100)) > 0) {
      this.getCciService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.SELL,
          this.getCandlestickService().getOldestCandlestick().close());
      return IndicatorTrend.SELL;
    }
    if (this.getCciService().getCci().value().compareTo(BigDecimal.valueOf(-100)) < 0) {
      this.getCciService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.BUY,
          this.getCandlestickService().getOldestCandlestick().close());
      return IndicatorTrend.BUY;
    }
    this.getCciService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.NEUTRAL,
        this.getCandlestickService().getOldestCandlestick().close());
    return IndicatorTrend.NEUTRAL;
  }

  @Override
  public void run() {
    final Function<Candlestick, BigDecimal> getTp = candlestick -> (candlestick.close().add(candlestick.high()).add(candlestick.low())).divide(BigDecimal.valueOf(3), 10,
        RoundingMode.HALF_UP);
    final BigDecimal[] tps = this.getCandlestickService().getCandlesticks(this.getPeriod()).map(getTp).toArray(BigDecimal[]::new);
    final BigDecimal tpSMA = Arrays.stream(tps).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(this.getPeriod()), 10, RoundingMode.HALF_UP);
    final BigDecimal deviation = Arrays.stream(tps).map(tp -> tpSMA.subtract(tp).abs()).reduce(BigDecimal.ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(this.getPeriod()), 10, RoundingMode.HALF_UP);
    final BigDecimal cci = (tps[0].subtract(tpSMA)).divide((BigDecimal.valueOf(0.015).multiply(deviation)), 10, RoundingMode.HALF_UP);
    this.getCciService().addCci(this.getCandlestickService().getOldestCandlestick().dateTime(), cci);
  }
}
