package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.EnvelopeService;
import br.eti.allandemiranda.forex.utils.IndicatorTrend;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
@Slf4j
public class EnvelopesTechnicalIndicator implements Indicator {

  private final CandlestickService candlestickService;
  private final EnvelopeService envelopeService;

  @Value("${envelopes.parameters.period:14}")
  private int period;
  @Value("${envelopes.parameters.deviation:0.100}")
  private double deviation;
  @Value("${envelopes.parameters.shift:0}")
  private int shift;
  @Setter(AccessLevel.PRIVATE)
  private BigDecimal percent;

  @Autowired
  protected EnvelopesTechnicalIndicator(final CandlestickService candlestickService, final EnvelopeService envelopeService) {
    this.candlestickService = candlestickService;
    this.envelopeService = envelopeService;
  }

  @PostConstruct
  private void init() {
    this.setPercent(BigDecimal.valueOf(this.getDeviation()).divide(BigDecimal.valueOf(100), 5, RoundingMode.DOWN));
  }

  @Override
  public @NotNull IndicatorTrend getSignal() {
    if (this.getCandlestickService().getOldestCandlestick().close().compareTo(this.getEnvelopeService().getEnvelopes().upperBand()) >= 0) {
      this.getEnvelopeService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.SELL, this.getCandlestickService().getOldestCandlestick().close());
      return IndicatorTrend.SELL;
    }
    if (this.getCandlestickService().getOldestCandlestick().close().compareTo(this.getEnvelopeService().getEnvelopes().lowerBand()) <= 0) {
      this.getEnvelopeService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.BUY, this.getCandlestickService().getOldestCandlestick().close());
      return IndicatorTrend.BUY;
    }
    this.getEnvelopeService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.NEUTRAL, this.getCandlestickService().getOldestCandlestick().close());
    return IndicatorTrend.NEUTRAL;
  }

  @Override
  public void run() {
    final BigDecimal sma = this.getCandlestickService().getSMA(candlesticks -> candlesticks[0].close(), 1, this.getPeriod())[0];
    final BigDecimal upper = (BigDecimal.ONE.add(this.getPercent())).multiply(sma);
    final BigDecimal lower = (BigDecimal.ONE.subtract(this.getPercent())).multiply(sma);
    this.getEnvelopeService().addEnvelopes(this.getCandlestickService().getOldestCandlestick().realDateTime(), upper, lower);
  }
}
