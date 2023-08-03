package br.eti.allandemiranda.forex.controllers.order;

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

  @Value("${order.take-profit:0.0}")
  @Setter(AccessLevel.PRIVATE)
  private double takeProfit;
  @Value("${order.stop-loss:0.0}")
  @Setter(AccessLevel.PRIVATE)
  private double stopLoss;

  @Autowired
  protected OrderProcessor(final SignalService signalService, final TicketService ticketService, final OrderService orderService) {
    this.signalService = signalService;
    this.ticketService = ticketService;
    this.orderService = orderService;
  }

  @PostConstruct
  private void init() {
    if (this.getTakeProfit() <= 0d) {
      this.setTakeProfit(Double.MAX_VALUE);
    }
    if (this.getStopLoss() <= 0d) {
      this.setStopLoss(Double.MAX_VALUE);
    }
  }

  @Synchronized
  public void run() {
    if (this.getSignalService().isReady()) {
      if (OrderStatus.OPEN.equals(this.getOrderService().getLastOrder().status())) {
        this.getOrderService().updateOpenPosition(this.getTicketService().getTicket(), this.getTakeProfit(), this.getStopLoss());
        if (OrderStatus.OPEN.equals(this.getOrderService().getLastOrder().status())) {
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
      } else {
        if (this.getSignalService().getValidation() && this.getTicketService().getCurrentSpread() < this.getStopLoss()) {
          switch (this.getSignalService().getLastSignal().trend()) {
            case STRONG_BUY -> {
              this.getOrderService().openPosition(this.getTicketService().getTicket(), OrderPosition.BUY);
              this.getOrderService().updateDebugShortFile();
            }
            case STRONG_SELL -> {
              this.getOrderService().openPosition(this.getTicketService().getTicket(), OrderPosition.SELL);
              this.getOrderService().updateDebugShortFile();
            }
          }
        }
      }
      this.getOrderService().updateDebugFile();
    }
  }
}
