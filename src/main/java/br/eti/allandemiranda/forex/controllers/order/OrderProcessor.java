package br.eti.allandemiranda.forex.controllers.order;

import br.eti.allandemiranda.forex.services.OrderService;
import br.eti.allandemiranda.forex.services.SignalService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.services.TradingPerformanceService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class OrderProcessor {

  private final SignalService signalService;
  private final TicketService ticketService;
  private final OrderService orderService;
  private final TradingPerformanceService tradingPerformanceService;

  @Autowired
  protected OrderProcessor(final SignalService signalService, final TicketService ticketService, final OrderService orderService,
      final TradingPerformanceService tradingPerformanceService) {
    this.signalService = signalService;
    this.ticketService = ticketService;
    this.orderService = orderService;
    this.tradingPerformanceService = tradingPerformanceService;
  }

  @Synchronized
  public void run() {
    this.getOrderService().insertTicketAndSignal(this.getTicketService().getTicket(), this.getSignalService().getLastSignal(), this.getTradingPerformanceService().getDiff(this.getTicketService().getTicket().digits()));
  }
}
