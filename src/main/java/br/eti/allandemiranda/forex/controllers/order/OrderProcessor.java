package br.eti.allandemiranda.forex.controllers.order;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.OrderService;
import br.eti.allandemiranda.forex.services.SignalService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class OrderProcessor {

  private final SignalService signalService;
  private final TicketService ticketService;
  private final OrderService orderService;

  @Value("${order.take-profit:0}")
  @Setter(AccessLevel.PRIVATE)
  private int takeProfit;
  @Value("${order.stop-loss:0}")
  @Setter(AccessLevel.PRIVATE)
  private int stopLoss;
  @Value("${order.open.spread.max:0}")
  @Setter(AccessLevel.PRIVATE)
  private int maxSpread;

  @Autowired
  protected OrderProcessor(final SignalService signalService, final TicketService ticketService, final OrderService orderService) {
    this.signalService = signalService;
    this.ticketService = ticketService;
    this.orderService = orderService;
  }

  @PostConstruct
  private void init() {
    if (this.getTakeProfit() <= 0) {
      this.setTakeProfit(Integer.MAX_VALUE);
    }
    if (this.getStopLoss() <= 0) {
      this.setStopLoss(Integer.MAX_VALUE);
    }
    if (this.getMaxSpread() <= 0) {
      this.setMaxSpread(Integer.MAX_VALUE);
    }
  }

  @Synchronized
  public void run() {
    if (this.getSignalService().isReady()) {
      final Ticket ticket = this.getTicketService().getTicket();
      if (OrderStatus.OPEN.equals(this.getOrderService().getLastOrder().status())) {
        operationToOpenOrder(ticket, this.getTakeProfit(), this.getStopLoss());
      } else {
        operationToCloseOrder(ticket, this.getStopLoss(), this.getMaxSpread());
      }
      this.getOrderService().updateDebugFile();
    }
  }

  private void operationToCloseOrder(final Ticket ticket, final int stopLoss, final int maxSpread) {
    if (this.getSignalService().haveValidSignal() && ticket.spread() < stopLoss && ticket.spread() <= maxSpread) {
      switch (this.getSignalService().getLastSignal().trend()) {
        case STRONG_BUY -> {
          this.getOrderService().openPosition(ticket, OrderPosition.BUY);
          this.getOrderService().updateDebugShortFile();
        }
        case STRONG_SELL -> {
          this.getOrderService().openPosition(ticket, OrderPosition.SELL);
          this.getOrderService().updateDebugShortFile();
        }
      }
    }
  }

  private void operationToOpenOrder(final Ticket ticket, final int takeProfit, final int stopLoss) {
    if (OrderStatus.OPEN.equals(this.getOrderService().updateOpenPosition(ticket, takeProfit, stopLoss))) {
      switch (this.getSignalService().getLastSignal().trend()) {
        case STRONG_BUY, BUY -> {
          if (this.getOrderService().getLastOrder().position().equals(OrderPosition.SELL)) {
            this.getOrderService().closePosition(OrderStatus.CLOSE_MANUAL);
            this.getOrderService().updateDebugShortFile();
          }
        }
        case STRONG_SELL, SELL -> {
          if (this.getOrderService().getLastOrder().position().equals(OrderPosition.BUY)) {
            this.getOrderService().closePosition(OrderStatus.CLOSE_MANUAL);
            this.getOrderService().updateDebugShortFile();
          }
        }
        case NEUTRAL -> {
          this.getOrderService().closePosition(OrderStatus.CLOSE_MANUAL);
          this.getOrderService().updateDebugShortFile();
        }
      }
    }
  }
}
