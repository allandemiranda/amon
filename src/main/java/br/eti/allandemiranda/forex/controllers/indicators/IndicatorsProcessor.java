package br.eti.allandemiranda.forex.controllers.indicators;

import br.eti.allandemiranda.forex.controllers.indicators.trend.AverageDirectionalMovementIndex;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.IndicatorService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
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

  private final AverageDirectionalMovementIndex averageDirectionalMovementIndex;
  private final IndicatorService indicatorService;
  private final TicketService ticketService;

  @Value("${indicators.run.min:3}")
  private int interval;
  @Setter(AccessLevel.PRIVATE)
  private LocalDateTime lastDataTime = LocalDateTime.MIN;

  @Autowired
  private IndicatorsProcessor(final AverageDirectionalMovementIndex averageDirectionalMovementIndex, final IndicatorService indicatorService,
      final TicketService ticketService) {
    this.averageDirectionalMovementIndex = averageDirectionalMovementIndex;
    this.indicatorService = indicatorService;
    this.ticketService = ticketService;
  }

  @PostConstruct
  public void init() {
    this.getIndicatorService().addIndicator("ADX", averageDirectionalMovementIndex);
  }

  @Synchronized
  public void run() {
    final Ticket currentTicket = this.getTicketService().getCurrentTicket();
    final LocalDateTime currentDataTime = currentTicket.dateTime();
    if (lastDataTime.plusMinutes(this.getInterval()).isBefore(currentDataTime)) {
      this.setLastDataTime(currentDataTime);
      final Map<String, SignalTrend> currentSignals = this.getIndicatorService().processAndGetSignals(currentDataTime);
      this.getIndicatorService().updateDebugFile(currentSignals, currentTicket.bid());
    }
  }
}

