package br.eti.allandemiranda.forex.controllers.order;

import br.eti.allandemiranda.forex.dtos.Order;
import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.OrderService;
import br.eti.allandemiranda.forex.services.SignalService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
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

  @Value("${order.take-profit:0.001}")
  private double takeProfit;
  @Value("${order.stop-loss:0.001}")
  private double stopLoss;
  @Value("${order.security:false}")
  private boolean isSecurity;
  @Value("${order.open}")
  private int openWith;

  @Autowired
  protected OrderProcessor(final SignalService signalService, final TicketService ticketService, final OrderService orderService) {
    this.signalService = signalService;
    this.ticketService = ticketService;
    this.orderService = orderService;
  }

  @Synchronized
  public void run() {
    final Ticket currentTicket = this.getTicketService().getCurrentTicket();
    final SignalTrend[] signals = Arrays.stream(this.getSignalService().getSignals()).sorted(Comparator.comparing(Signal::dateTime).reversed()).limit(this.getOpenWith())
        .map(Signal::trend).toArray(SignalTrend[]::new);
    if (currentTicket.dateTime().equals(this.getSignalService().getLastUpdateTime()) && signals.length >= this.getOpenWith()) {
      updateOnTime(currentTicket, signals);
    } else {
      updateWithOutIndicators(currentTicket);
    }
  }

  private void updateOnTime(final Ticket currentTicket, final SignalTrend[] signals) {
    final Order lastOrder = this.getOrderService().getLastOrder();
    if (lastOrder.status().equals(OrderStatus.OPEN)) {
      OrderPosition orderPositionIndicator = null;
      final SignalTrend lastSignal = signals[signals.length - 1];
      if (lastSignal.equals(SignalTrend.STRONG_BUY) || lastSignal.equals(SignalTrend.BUY)) {
        orderPositionIndicator = OrderPosition.BUY;
      } else if (lastSignal.equals(SignalTrend.STRONG_SELL) || lastSignal.equals(SignalTrend.SELL)) {
        orderPositionIndicator = OrderPosition.SELL;
      }
      if (!lastOrder.position().equals(orderPositionIndicator)) {
        this.getOrderService().closePosition(currentTicket, OrderStatus.CLOSE_MANUAL);
      }
    } else {
      final ArrayList<SignalTrend> signalTrendSet = Arrays.stream(signals).distinct().collect(Collectors.toCollection(ArrayList::new));
      if (signalTrendSet.size() == 1) {
        if (signalTrendSet.get(0).equals(SignalTrend.STRONG_BUY)) {
          this.getOrderService().openPosition(currentTicket, OrderPosition.BUY);
        } else if (signalTrendSet.get(0).equals(SignalTrend.STRONG_SELL)) {
          this.getOrderService().openPosition(currentTicket, OrderPosition.SELL);
        }
      }
    }
  }

  private void updateWithOutIndicators(final Ticket currentTicket) {
    if (this.isSecurity()) {
      this.getOrderService().updateTicket(currentTicket, this.getTakeProfit(), this.getStopLoss());
    } else {
      this.getOrderService().updateTicket(currentTicket);
    }
  }
}
