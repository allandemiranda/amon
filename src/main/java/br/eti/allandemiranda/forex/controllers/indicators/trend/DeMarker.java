//package br.eti.allandemiranda.forex.controllers.indicators.trend;
//
//import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
//import br.eti.allandemiranda.forex.dtos.Candlestick;
//import br.eti.allandemiranda.forex.services.CandlestickService;
//import br.eti.allandemiranda.forex.services.DemService;
//import br.eti.allandemiranda.forex.utils.IndicatorTrend;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.Arrays;
//import java.util.stream.IntStream;
//import lombok.AccessLevel;
//import lombok.Getter;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//
//@Controller
//@Getter(AccessLevel.PRIVATE)
//public class DeMarker implements Indicator {
//
//  private final DemService demService;
//  private final CandlestickService candlestickService;
//
//  @Value("${dem.parameters.period:14}")
//  private int period;
//
//  @Autowired
//  protected DeMarker(final DemService demService, final CandlestickService candlestickService) {
//    this.demService = demService;
//    this.candlestickService = candlestickService;
//  }
//
//  @Override
//  public @NotNull IndicatorTrend getSignal() {
//    if (this.getDemService().getDem().value().compareTo(BigDecimal.valueOf(0.7)) > 0) {
//      this.getDemService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.SELL,
//          this.getCandlestickService().getOldestCandlestick().close());
//      return IndicatorTrend.SELL;
//    }
//    if (this.getDemService().getDem().value().compareTo(BigDecimal.valueOf(0.3)) < 0) {
//      this.getDemService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.BUY,
//          this.getCandlestickService().getOldestCandlestick().close());
//      return IndicatorTrend.BUY;
//    }
//    this.getDemService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.NEUTRAL,
//        this.getCandlestickService().getOldestCandlestick().close());
//    return IndicatorTrend.NEUTRAL;
//  }
//
//  @Override
//  public void run() {
//    final Candlestick[] candlesticks = this.getCandlestickService().getCandlesticks(this.getPeriod() + 1).toArray(Candlestick[]::new);
//    final BigDecimal[] deMax = IntStream.range(0, this.getPeriod())
//        .mapToObj(i -> candlesticks[i].high().compareTo(candlesticks[i + 1].high()) > 0 ? candlesticks[i].high().subtract(candlesticks[i + 1].high()) : BigDecimal.ZERO)
//        .toArray(BigDecimal[]::new);
//    final BigDecimal[] deMin = IntStream.range(0, this.getPeriod())
//        .mapToObj(i -> candlesticks[i].low().compareTo(candlesticks[i + 1].low()) < 0 ? candlesticks[i + 1].low().subtract(candlesticks[i].low()) : BigDecimal.ZERO)
//        .toArray(BigDecimal[]::new);
//    final BigDecimal deMaxSMA = Arrays.stream(deMax).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(this.getPeriod()), 10, RoundingMode.HALF_UP);
//    final BigDecimal deMinSMA = Arrays.stream(deMin).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(this.getPeriod()), 10, RoundingMode.HALF_UP);
//    final BigDecimal dMark = deMaxSMA.divide((deMaxSMA.add(deMinSMA)), 10, RoundingMode.HALF_UP);
//    this.getDemService().addDem(this.getCandlestickService().getOldestCandlestick().candleDateTime(), dMark);
//  }
//}
