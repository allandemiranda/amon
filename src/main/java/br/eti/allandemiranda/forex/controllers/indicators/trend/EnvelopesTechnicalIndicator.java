package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.EnvelopeService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
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
  public boolean run() {
    if (this.getCandlestickService().isReady()) {
      if (this.getShift() == 0) {
        final BigDecimal sma = Arrays.stream(this.getCandlestickService().getCandlesticks(this.getPeriod())).map(Candlestick::close)
            .reduce(BigDecimal.valueOf(0d), BigDecimal::add).divide(BigDecimal.valueOf(this.getPeriod()), 5, RoundingMode.DOWN);
        final BigDecimal upper = (BigDecimal.valueOf(1).add(this.getPercent())).multiply(sma);
        final BigDecimal lower = (BigDecimal.valueOf(1).subtract(this.getPercent())).multiply(sma);
        this.getEnvelopeService().addEnvelopes(this.getCandlestickService().getLastCandlestick().realDateTime(), upper, lower);
        return true;
      } else {
        log.error("Can't process a Envelopes with deviation diff of zero");
      }
    }
    return false;
  }

  @Override
  public @NotNull SignalTrend getCurrentSignal() {
    if (this.getEnvelopeService().getEnvelopes().dateTime().equals(LocalDateTime.MIN)) {
      return SignalTrend.OUT;
    } else if(this.getCandlestickService().getLastCandlestick().close().compareTo(this.getEnvelopeService().getEnvelopes().upperBand()) >= 0) {
      this.getEnvelopeService().updateDebugFile(this.getCandlestickService().getLastCandlestick().realDateTime(), SignalTrend.SELL,this.getCandlestickService().getLastCandlestick().close());
      return SignalTrend.SELL;
    } else if(this.getCandlestickService().getLastCandlestick().close().compareTo(this.getEnvelopeService().getEnvelopes().lowerBand()) <= 0) {
      this.getEnvelopeService().updateDebugFile(this.getCandlestickService().getLastCandlestick().realDateTime(), SignalTrend.BUY,this.getCandlestickService().getLastCandlestick().close());
      return SignalTrend.BUY;
    } else {
      this.getEnvelopeService().updateDebugFile(this.getCandlestickService().getLastCandlestick().realDateTime(), SignalTrend.NEUTRAL,this.getCandlestickService().getLastCandlestick().close());
      return SignalTrend.NEUTRAL;
    }
  }
}
