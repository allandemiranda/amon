//package br.eti.allandemiranda.forex.controllers.indicators.trend;
//
//import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
//import br.eti.allandemiranda.forex.dtos.Candlestick;
//import br.eti.allandemiranda.forex.services.CandlestickService;
//import br.eti.allandemiranda.forex.services.RsiService;
//import br.eti.allandemiranda.forex.utils.IndicatorTrend;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
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
//public class RelativeStrengthIndex implements Indicator {
//
//  private final RsiService rsiService;
//  private final CandlestickService candlestickService;
//
//  @Value("${rsi.parameters.period:14}")
//  private int period;
//
//  @Autowired
//  protected RelativeStrengthIndex(final RsiService rsiService, final CandlestickService candlestickService) {
//    this.rsiService = rsiService;
//    this.candlestickService = candlestickService;
//  }
//
//  @Override
//  public @NotNull IndicatorTrend getSignal() {
//    if (this.getRsiService().getRsi().value().compareTo(BigDecimal.valueOf(70)) > 0) {
//      this.getRsiService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.SELL,
//          this.getCandlestickService().getOldestCandlestick().close());
//      return IndicatorTrend.SELL;
//    }
//    if (this.getRsiService().getRsi().value().compareTo(BigDecimal.valueOf(30)) < 0) {
//      this.getRsiService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.BUY,
//          this.getCandlestickService().getOldestCandlestick().close());
//      return IndicatorTrend.BUY;
//    }
//    this.getRsiService().updateDebugFile(this.getCandlestickService().getOldestCandlestick().realDateTime(), IndicatorTrend.NEUTRAL,
//        this.getCandlestickService().getOldestCandlestick().close());
//    return IndicatorTrend.NEUTRAL;
//  }
//
//  @Override
//  public void run() {
//    final Candlestick[] candlesticks = this.getCandlestickService().getCandlesticks(this.getPeriod() + 1).toArray(Candlestick[]::new);
//
//    final BigDecimal avGain = IntStream.rangeClosed(0, candlesticks.length - 2).mapToObj(i -> {
//      final BigDecimal subtract = candlesticks[i].close().subtract(candlesticks[i + 1].close());
//      return subtract.compareTo(BigDecimal.ZERO) > 0 ? subtract : BigDecimal.ZERO;
//    }).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(this.getPeriod()), 10, RoundingMode.HALF_UP);
//    final BigDecimal avLoss = IntStream.rangeClosed(0, candlesticks.length - 2).mapToObj(i -> {
//      final BigDecimal subtract = candlesticks[i].close().subtract(candlesticks[i + 1].close());
//      return subtract.compareTo(BigDecimal.ZERO) < 0 ? subtract.abs() : BigDecimal.ZERO;
//    }).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(this.getPeriod()), 10, RoundingMode.HALF_UP);
//
//    final BigDecimal rs = avLoss.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : avGain.divide(avLoss, 10, RoundingMode.HALF_UP);
//    final BigDecimal rsi = rs.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.valueOf(100)
//        : BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).divide((BigDecimal.ONE.add(rs)), 10, RoundingMode.HALF_UP));
//
//    this.getRsiService().addRsi(this.getCandlestickService().getOldestCandlestick().candleDateTime(), rsi);
//  }
//}
