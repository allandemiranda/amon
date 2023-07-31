package br.eti.allandemiranda.forex.controllers.indicators;

import br.eti.allandemiranda.forex.controllers.indicators.trend.AverageDirectionalMovementIndex;
import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.services.IndicatorService;
import br.eti.allandemiranda.forex.services.SignalService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class IndicatorsProcessor {

  private static final String ADX = "ADX";

  private final AverageDirectionalMovementIndex averageDirectionalMovementIndex;
  private final IndicatorService indicatorService;
  private final TicketService ticketService;
  private final SignalService signalService;

  @Value("${indicators.run.min:3}")
  private int interval;
  @Setter(AccessLevel.PRIVATE)
  private LocalDateTime lastDataTime = LocalDateTime.MIN;

  @Autowired
  protected IndicatorsProcessor(final AverageDirectionalMovementIndex averageDirectionalMovementIndex, final IndicatorService indicatorService,
      final TicketService ticketService, final SignalService signalService) {
    this.averageDirectionalMovementIndex = averageDirectionalMovementIndex;
    this.indicatorService = indicatorService;
    this.ticketService = ticketService;
    this.signalService = signalService;
  }

  @PostConstruct
  public void init() {
    this.getIndicatorService().addIndicator(ADX, this.getAverageDirectionalMovementIndex());
  }

  @Synchronized
  public void run() {
    final LocalDateTime currentTicketDataTime = this.getTicketService().getCurrentTicket().dateTime();
    if (this.getLastDataTime().plusMinutes(this.getInterval()).isBefore(currentTicketDataTime)) {
      this.setLastDataTime(currentTicketDataTime);
      final Map<String, SignalTrend> currentSignals = this.getIndicatorService().processAndGetSignals(currentTicketDataTime);
      if(!currentSignals.isEmpty()) {
        final double bid = this.getTicketService().getCurrentTicket().bid();
        this.getIndicatorService().updateDebugFile(currentSignals, bid);
        final double signalsPower = currentSignals.values().stream().filter(signalTrend -> !SignalTrend.OUT.equals(signalTrend))
            .collect(Collectors.groupingBy(signalTrend -> signalTrend, Collectors.summingInt(signalTrend -> 1))).entrySet().parallelStream()
            .mapToInt(entry -> entry.getKey().power * entry.getValue()).sum() / (double) currentSignals.size();
        if (signalsPower < SignalTrend.SELL.power) {
          this.getSignalService().addGlobalSignal(new Signal(currentTicketDataTime, SignalTrend.STRONG_SELL, bid));
        } else if (signalsPower == SignalTrend.SELL.power) {
          this.getSignalService().addGlobalSignal(new Signal(currentTicketDataTime, SignalTrend.SELL, bid));
        } else if (signalsPower == SignalTrend.BUY.power) {
          this.getSignalService().addGlobalSignal(new Signal(currentTicketDataTime, SignalTrend.BUY, bid));
        } else if (signalsPower > SignalTrend.BUY.power) {
          this.getSignalService().addGlobalSignal(new Signal(currentTicketDataTime, SignalTrend.STRONG_BUY, bid));
        } else {
          this.getSignalService().addGlobalSignal(new Signal(currentTicketDataTime, SignalTrend.NEUTRAL, bid));
        }
      }
    }
  }
}

