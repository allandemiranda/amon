package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.CciService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
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

  @Value("${cci.parameters.period}")
  private int period;

  @Autowired
  protected CommodityChannelIndex(final CciService cciService, final CandlestickService candlestickService) {
    this.cciService = cciService;
    this.candlestickService = candlestickService;
  }

  @Override
  public boolean run() {
    if (this.getCandlestickService().isReady()) {
      final Candlestick[] chart = this.getCandlestickService().getCandlesticks(this.getPeriod());
      final BigDecimal[] tps = Arrays.stream(chart)
          .map(candlestick -> candlestick.high().add(candlestick.low()).add(candlestick.close()).divide(BigDecimal.valueOf(3), 5, RoundingMode.DOWN))
          .toArray(BigDecimal[]::new);
      final BigDecimal smaTp = Arrays.stream(tps).reduce(BigDecimal.valueOf(0d), BigDecimal::add).divide(BigDecimal.valueOf(this.getPeriod()), 5, RoundingMode.DOWN);
      final BigDecimal dayM = Arrays.stream(tps).reduce(BigDecimal.valueOf(0d), (subtotal, element) -> smaTp.subtract(element).abs().add(subtotal))
          .divide(BigDecimal.valueOf(this.getPeriod()), 5, RoundingMode.DOWN);
      final BigDecimal cci = tps[tps.length - 1].subtract(smaTp).divide(BigDecimal.valueOf(0.015).multiply(dayM), 5, RoundingMode.DOWN);
      this.getCciService().addCci(this.getCandlestickService().getLastCandlestick().realDateTime(), cci);
      return true;
    }
    return false;
  }

  @Override
  public @NotNull SignalTrend getCurrentSignal() {
    final Candlestick lastCandlestick = this.getCandlestickService().getLastCandlestick();
    final BigDecimal closePrice = lastCandlestick.close();
    final LocalDateTime realTime = lastCandlestick.realDateTime();
    if (this.getCciService().getCci().dateTime().equals(LocalDateTime.MIN)) {
      return SignalTrend.OUT;
    } else {
      final BigDecimal value = this.getCciService().getCci().value();
      if (value.compareTo(BigDecimal.valueOf(100)) >= 0) {
        this.getCciService().updateDebugFile(realTime, SignalTrend.STRONG_SELL, closePrice);
        return SignalTrend.STRONG_SELL;
      } else if (value.compareTo(BigDecimal.valueOf(-100)) <= 0) {
        this.getCciService().updateDebugFile(realTime, SignalTrend.STRONG_BUY, closePrice);
        return SignalTrend.STRONG_BUY;
      } else {
        this.getCciService().updateDebugFile(realTime, SignalTrend.NEUTRAL, closePrice);
        return SignalTrend.NEUTRAL;
      }
    }
  }
}
