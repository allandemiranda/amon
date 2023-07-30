package br.eti.allandemiranda.forex.controllers.indicators.trend;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.services.ADXService;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class AverageDirectionalMovementIndex implements Indicator {

  private final ADXService adxService;
  private final CandlestickService candlestickService;
  private final TicketService ticketService;

  @Value("${adx.parameters.period}")
  private int period;

  @Autowired
  protected AverageDirectionalMovementIndex(final ADXService adxService, final CandlestickService candlestickService, final TicketService ticketService) {
    this.adxService = adxService;
    this.candlestickService = candlestickService;
    this.ticketService = ticketService;
  }

  private static double @NotNull [] getDx(final Candlestick @NotNull [] chart) {
    final double tr = IntStream.range(1, chart.length).parallel().mapToDouble(i -> {
      final Candlestick candlestick = chart[i];
      final Candlestick lastCandlestick = chart[i - 1];
      final double pOne = Math.abs(candlestick.high() - lastCandlestick.close());
      final double pTwo = Math.abs(candlestick.low() - lastCandlestick.close());
      final double pThree = candlestick.high() - candlestick.low();
      return Math.max(Math.max(pOne, pTwo), pThree);
    }).sum();
    final double dmPlus = IntStream.range(1, chart.length).parallel().mapToDouble(i -> {
      final Candlestick candlestick = chart[i];
      final Candlestick lastCandlestick = chart[i - 1];
      final double highValue = candlestick.high() - lastCandlestick.high();
      final double lowValue = lastCandlestick.low() - candlestick.low();
      if (highValue > lowValue) {
        return Math.max(highValue, 0d);
      } else {
        return 0d;
      }
    }).sum();
    final double dmMinus = IntStream.range(1, chart.length).parallel().mapToDouble(i -> {
      final Candlestick candlestick = chart[i];
      final Candlestick lastCandlestick = chart[i - 1];
      final double highValue = candlestick.high() - lastCandlestick.high();
      final double lowValue = lastCandlestick.low() - candlestick.low();
      if (lowValue > highValue) {
        return Math.max(lowValue, 0d);
      } else {
        return 0d;
      }
    }).sum();
    final double diPlus = 100 * (dmPlus / tr);
    final double diMinus = 100 * (dmMinus / tr);
    final double diDiff = Math.abs(diPlus - diMinus);
    final double diSum = diPlus + diMinus;
    final double dx = 100 * (diDiff / diSum);
    return new double[]{dx, diPlus, diMinus};
  }

  @Override
  @Synchronized
  public boolean run() {
    if (this.getCandlestickService().getCacheMemorySize() >= (this.getPeriod() * 2)) {
      final Candlestick[] chart = this.getCandlestickService().getCandlesticks(this.getPeriod() * 2);
      final double[][] adxs = IntStream.range(0, this.getPeriod()).mapToObj(i -> {
        final Candlestick[] tmp = Arrays.stream(chart, i, this.getPeriod() + i + 1).toArray(Candlestick[]::new);
        return getDx(tmp);
      }).toArray(double[][]::new);
      final double adxValue = Arrays.stream(adxs).mapToDouble(value -> value[0]).sum() / adxs.length;
      final double diPlus = adxs[adxs.length - 1][1];
      final double diMinus = adxs[adxs.length - 1][2];
      this.adxService.addADX(this.getCandlestickService().getLastDataTime(), adxValue, diPlus, diMinus);
      return true;
    } else {
      return false;
    }
  }

  @Override
  @Synchronized
  public @NotNull SignalTrend getCurrentSignal() {
    if (this.getAdxService().getADX().dateTime().equals(LocalDateTime.MIN)) {
      return SignalTrend.OUT;
    } else {
      final LocalDateTime realTime = this.getTicketService().getCurrentTicket().dateTime();
      final double price = this.getTicketService().getCurrentTicket().bid();
      if (this.getAdxService().getADX().diPlus() == this.getAdxService().getADX().diMinus() || this.getAdxService().getADX().adx() < 50d) {
        this.getAdxService().updateDebugFile(realTime, SignalTrend.NEUTRAL, price);
        return SignalTrend.NEUTRAL;
      } else if (this.getAdxService().getADX().adx() < 75) {
        final SignalTrend trend = this.getAdxService().getADX().diPlus() > this.getAdxService().getADX().diMinus() ? SignalTrend.BUY : SignalTrend.SELL;
        this.getAdxService().updateDebugFile(realTime, trend, price);
        return trend;
      } else {
        final SignalTrend trend = this.getAdxService().getADX().diPlus() > this.getAdxService().getADX().diMinus() ? SignalTrend.STRONG_BUY : SignalTrend.STRONG_SELL;
        this.getAdxService().updateDebugFile(realTime, trend, price);
        return trend;
      }
    }
  }
}
