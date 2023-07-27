package br.eti.allandemiranda.forex.controllers.order;

import br.eti.allandemiranda.forex.services.IndicatorService;
import br.eti.allandemiranda.forex.services.SignalService;
import br.eti.allandemiranda.forex.services.TicketService;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class OrderProcessor {

  private final SignalService signalService;
  private final IndicatorService indicatorService;
  private final TicketService ticketService;

  @Autowired
  private OrderProcessor(final SignalService signalService, final IndicatorService indicatorService, final TicketService ticketService) {
    this.signalService = signalService;
    this.indicatorService = indicatorService;
    this.ticketService = ticketService;
  }

  @Synchronized
  public void run() {
    final LocalDateTime lastIndicatorUpdate = this.getIndicatorService().getLastUpdate();
    final LocalDateTime lastTicketUpdate = this.getTicketService().getCurrentTicket().dateTime();
//TODO amanhã
  }
}
