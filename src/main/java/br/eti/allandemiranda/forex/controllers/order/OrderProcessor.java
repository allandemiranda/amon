package br.eti.allandemiranda.forex.controllers.order;

import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.services.IndicatorsService;
import br.eti.allandemiranda.forex.services.SignalService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
public class OrderProcessor {

  @Value("${order.signaltoopen}")
  private Integer openSequence;

  private final TicketService ticketService;
  private final IndicatorsService indicatorsService;
  private final SignalService signalService;

  @Autowired
  private OrderProcessor(final TicketService ticketService, final IndicatorsService indicatorsService, final SignalService signalService) {
    this.ticketService = ticketService;
    this.indicatorsService = indicatorsService;
    this.signalService = signalService;
  }

  private static int getPowerNumber(final @NotNull SignalTrend signal, final int quantity) {
    return switch (signal) {
      case strongSell -> quantity * (-2);
      case sell -> quantity * (-1);
      case neutral, out -> 0;
      case buy -> quantity;
      case strongBuy -> quantity * 2;
    };
  }

  @NotNull
  private static SignalTrend getSignalTrend(int powerOfSignals, int numbOfSignals) {
    if (powerOfSignals == 0) {
      return SignalTrend.neutral;
    }
    if (powerOfSignals < (numbOfSignals * (-1))) {
      return SignalTrend.strongSell;
    }
    if (powerOfSignals < 0) {
      return SignalTrend.sell;
    }
    if (powerOfSignals > numbOfSignals) {
      return SignalTrend.strongBuy;
    }
    return SignalTrend.buy;
  }

  @Synchronized
  public void run() {
    this.updateSignal();
    this.updatePositionAble();
  }
  public void updateSignal() {
    SignalTrend globalSignal = getGlobalSignal();
    this.signalService.add(new Signal(this.ticketService.getLocalDateTime(), globalSignal, this.ticketService.getBid()));



  }

  private void updatePositionAble() {
    SignalTrend[] lastSignals = this.signalService.getLastSequence(openSequence);
    long numFalse = IntStream.range(1, lastSignals.length).mapToObj(i -> lastSignals[0].equals(lastSignals[i])).filter(aBoolean -> !aBoolean).count();
    if(numFalse  == 0L) {
      this.openPosition(lastSignals[0]);
    }
  }

  private void openPosition(final @NotNull SignalTrend signalTrend) {

  }

  private @NotNull SignalTrend getGlobalSignal() {
    Map<SignalTrend, Integer> signalTrendMap = this.indicatorsService.getSignals().stream().collect(Collectors.groupingBy(o -> o, Collectors.summingInt(value -> 1)));
    int powerOfSignals = Arrays.stream(SignalTrend.values()).mapToInt(signalTrend -> getPowerNumber(signalTrend, signalTrendMap.getOrDefault(signalTrend, 0))).sum();
    int numbOfSignals = signalTrendMap.values().stream().reduce(0, Integer::sum);
    return getSignalTrend(powerOfSignals, numbOfSignals);
  }
}
