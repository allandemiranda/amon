package br.eti.allandemiranda.forex.controllers.order;

import br.eti.allandemiranda.forex.services.IndicatorsService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class OrderProcessor {

  private final TicketService ticketService;
  private final IndicatorsService indicatorsService;

  @Autowired
  private OrderProcessor(final TicketService ticketService, final IndicatorsService indicatorsService) {
    this.ticketService = ticketService;
    this.indicatorsService = indicatorsService;
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
    SignalTrend globalSignal = getGlobalSignal();
  }

  private @NotNull SignalTrend getGlobalSignal() {
    Map<SignalTrend, Integer> signalTrendMap = this.indicatorsService.getSignals().stream().collect(Collectors.groupingBy(o -> o, Collectors.summingInt(value -> 1)));
    int powerOfSignals = Arrays.stream(SignalTrend.values()).mapToInt(signalTrend -> getPowerNumber(signalTrend, signalTrendMap.getOrDefault(signalTrend, 0))).sum();
    int numbOfSignals = signalTrendMap.values().stream().reduce(0, Integer::sum);
    return getSignalTrend(powerOfSignals, numbOfSignals);
  }
}
