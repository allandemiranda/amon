package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.STOCH;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.StochService;
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
public class StochasticOscillator implements Indicator {

  private final StochService stochService;
  private final CandlestickService candlestickService;

  @Value("${stoch.parameters.period.k:5}")
  private int periodK;
  @Value("${stoch.parameters.period.d:3}")
  private int periodD;

  @Autowired
  protected StochasticOscillator(final StochService stochService, final CandlestickService candlestickService) {
    this.stochService = stochService;
    this.candlestickService = candlestickService;
  }

  private static boolean isCross(final STOCH @NotNull [] stoches) {
    if (stoches.length == 3 && stoches[1].main().compareTo(stoches[1].signal()) == 0) {
      return stoches[0].main().compareTo(stoches[0].signal()) > 0 && stoches[2].main().compareTo(stoches[2].signal()) < 0
          || stoches[0].main().compareTo(stoches[0].signal()) < 0 && stoches[2].main().compareTo(stoches[2].signal()) > 0;
    } else if (stoches.length >= 2) {
      return stoches[0].main().compareTo(stoches[0].signal()) > 0 && stoches[1].main().compareTo(stoches[1].signal()) < 0
          || stoches[0].main().compareTo(stoches[0].signal()) < 0 && stoches[1].main().compareTo(stoches[1].signal()) > 0;
    } else {
      return false;
    }
  }

  @Override
  public @NotNull IndicatorTrend getSignal() {
    final STOCH[] stoches = this.getStochService().getStoch();
    if (stoches.length > 1) {
      if (stoches[0].main().compareTo(stoches[0].signal()) > 0 && stoches[0].main().compareTo(BigDecimal.valueOf(20)) < 0
          && stoches[0].signal().compareTo(BigDecimal.valueOf(20)) < 0 && isCross(stoches)) {
        this.getStochService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.BUY,
            this.getCandlestickService().getOldestCandlestick().close());
        return IndicatorTrend.BUY;
      }
      if (stoches[0].main().compareTo(stoches[0].signal()) < 0 && stoches[0].main().compareTo(BigDecimal.valueOf(80)) > 0
          && stoches[0].signal().compareTo(BigDecimal.valueOf(80)) > 0 && isCross(stoches)) {
        this.getStochService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.SELL,
            this.getCandlestickService().getOldestCandlestick().close());
        return IndicatorTrend.SELL;
      }
    }
    this.getStochService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.NEUTRAL,
        this.getCandlestickService().getOldestCandlestick().close());
    return IndicatorTrend.NEUTRAL;
  }

  @Override
  public void run() {
    final Candlestick[] candlesticks = this.getCandlestickService().getCandlesticks(this.getPeriodK() + this.getPeriodD() - 1).toArray(Candlestick[]::new);
    final BigDecimal[] highestHighs = IntStream.range(0, this.getPeriodD())
        .mapToObj(i -> IntStream.range(i, this.getPeriodK() + i).mapToObj(j -> candlesticks[j].high()).reduce(BigDecimal.ZERO, BigDecimal::max))
        .toArray(BigDecimal[]::new);
    final BigDecimal[] lowestLows = IntStream.range(0, this.getPeriodD())
        .mapToObj(i -> IntStream.range(i, this.getPeriodK() + i).mapToObj(j -> candlesticks[j].low()).reduce(BigDecimal.valueOf(Double.MAX_VALUE), BigDecimal::min))
        .toArray(BigDecimal[]::new);
    final BigDecimal[] ks = IntStream.range(0, this.getPeriodD()).mapToObj(
        i -> ((highestHighs[i].subtract(candlesticks[i].close())).divide((highestHighs[i].subtract(lowestLows[i])), 10, RoundingMode.HALF_UP)).multiply(
            BigDecimal.valueOf(100))).toArray(BigDecimal[]::new);
    final BigDecimal d = Arrays.stream(ks).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(this.getPeriodD()), 10, RoundingMode.HALF_UP);

    this.getStochService()
        .addStoch(this.getCandlestickService().getOldestCandlestick().realDateTime(), this.getCandlestickService().getOldestCandlestick().dateTime(), ks[0], d);
  }
}
