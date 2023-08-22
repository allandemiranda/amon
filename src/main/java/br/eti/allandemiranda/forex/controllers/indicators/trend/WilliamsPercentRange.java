//package br.eti.allandemiranda.forex.controllers.indicators.trend;
//
//import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
//import br.eti.allandemiranda.forex.dtos.Candlestick;
//import br.eti.allandemiranda.forex.services.CandlestickService;
//import br.eti.allandemiranda.forex.services.RService;
//import br.eti.allandemiranda.forex.utils.IndicatorTrend;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import lombok.AccessLevel;
//import lombok.Getter;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//
//@Controller
//@Getter(AccessLevel.PRIVATE)
//public class WilliamsPercentRange implements Indicator {
//
//  private final RService rService;
//  private final CandlestickService candlestickService;
//
//  @Value("${r.parameters.period:14}")
//  private int period;
//
//  @Autowired
//  protected WilliamsPercentRange(final RService rService, final CandlestickService candlestickService) {
//    this.rService = rService;
//    this.candlestickService = candlestickService;
//  }
//
//  @Override
//  public @NotNull IndicatorTrend getSignal() {
//    if (this.getRService().getR().value().compareTo(BigDecimal.valueOf(-20)) > 0) {
//      this.getRService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.SELL,
//          this.getCandlestickService().getOldestCandlestick().close());
//      return IndicatorTrend.SELL;
//    }
//    if (this.getRService().getR().value().compareTo(BigDecimal.valueOf(-80)) < 0) {
//      this.getRService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.BUY,
//          this.getCandlestickService().getOldestCandlestick().close());
//      return IndicatorTrend.BUY;
//    }
//    this.getRService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.NEUTRAL,
//        this.getCandlestickService().getOldestCandlestick().close());
//    return IndicatorTrend.NEUTRAL;
//  }
//
//  @Override
//  public void run() {
//    final BigDecimal highestHigh = this.getCandlestickService().getCandlesticks(this.getPeriod()).map(Candlestick::high).reduce(BigDecimal.ZERO, BigDecimal::max);
//    final BigDecimal lowestLow = this.getCandlestickService().getCandlesticks(this.getPeriod()).map(Candlestick::low)
//        .reduce(BigDecimal.valueOf(Double.MAX_VALUE), BigDecimal::min);
//    final Candlestick oldestCandlestick = this.getCandlestickService().getOldestCandlestick();
//    final BigDecimal k = ((highestHigh.subtract(oldestCandlestick.close())).divide((highestHigh.subtract(lowestLow)), 10, RoundingMode.HALF_UP)).multiply(
//        BigDecimal.valueOf(100));
//    final BigDecimal r = (BigDecimal.valueOf(-1)).multiply(k);
//    this.getRService().addR(oldestCandlestick.candleDateTime(), r);
//  }
//}
