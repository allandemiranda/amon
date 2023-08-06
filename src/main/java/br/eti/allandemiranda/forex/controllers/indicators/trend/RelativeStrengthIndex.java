package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.RsiService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
public class RelativeStrengthIndex implements Indicator {

  private final RsiService rsiService;
  private final CandlestickService candlestickService;

  @Value("${rsi.parameters.period:14}")
  private int period;

  @Autowired
  protected RelativeStrengthIndex(final RsiService rsiService, final CandlestickService candlestickService) {
    this.rsiService = rsiService;
    this.candlestickService = candlestickService;
  }

  @Override
  public boolean run() {
    if (this.getCandlestickService().isReady()) {
      final Candlestick[] candlesticks = this.getCandlestickService().getCandlesticks(this.getPeriod() + 1);
      final BigDecimal[] closes = Arrays.stream(candlesticks).map(Candlestick::close).toArray(BigDecimal[]::new);
      final BigDecimal[] diffs = IntStream.range(1, closes.length).mapToObj(i -> closes[i].subtract(closes[i - 1])).toArray(BigDecimal[]::new);
      final BigDecimal gain = Arrays.stream(diffs).filter(value -> value.compareTo(BigDecimal.ZERO) > 0).reduce(BigDecimal.ZERO, BigDecimal::add)
          .divide(BigDecimal.valueOf(this.getPeriod()), 5, RoundingMode.DOWN);
      final BigDecimal loss = Arrays.stream(diffs).filter(value -> value.compareTo(BigDecimal.ZERO) < 0).map(BigDecimal::abs).reduce(BigDecimal.ZERO, BigDecimal::add)
          .divide(BigDecimal.valueOf(this.getPeriod()), 5, RoundingMode.DOWN);
      final BigDecimal rs = loss.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : gain.divide(loss, 5, RoundingMode.DOWN);
      final BigDecimal rsi = BigDecimal.ONE.add(rs).compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.valueOf(100)
          : BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 5, RoundingMode.DOWN));
      this.getRsiService().addRsi(this.getCandlestickService().getLastCandlestick().realDateTime(), rsi);
      return true;
    }
    return false;
  }

  @Override
  public @NotNull SignalTrend getCurrentSignal() {
    final Candlestick lastCandlestick = this.getCandlestickService().getLastCandlestick();
    final BigDecimal closePrice = lastCandlestick.close();
    final LocalDateTime realTime = lastCandlestick.realDateTime();
    if (this.getRsiService().getRsi().dateTime().equals(LocalDateTime.MIN)) {
      return SignalTrend.OUT;
    } else {
      final BigDecimal value = this.getRsiService().getRsi().value();
      if (value.compareTo(BigDecimal.valueOf(70)) >= 0) {
        this.getRsiService().updateDebugFile(realTime, SignalTrend.STRONG_SELL, closePrice);
        return SignalTrend.STRONG_SELL;
      } else if (value.compareTo(BigDecimal.valueOf(30)) <= 0) {
        this.getRsiService().updateDebugFile(realTime, SignalTrend.STRONG_BUY, closePrice);
        return SignalTrend.STRONG_BUY;
      } else {
        this.getRsiService().updateDebugFile(realTime, SignalTrend.NEUTRAL, closePrice);
        return SignalTrend.NEUTRAL;
      }
    }
  }
}
