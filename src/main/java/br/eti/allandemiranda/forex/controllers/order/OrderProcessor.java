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
import org.jetbrains.annotations.NotNull;
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
  @Value("${order.stop-loss:100.0}")
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

  private static void updateClosePosition(final @NotNull Ticket currentTicket, final SignalTrend[] signals, final OrderService service, final double stopLoss) {
    final ArrayList<SignalTrend> signalTrendSet = Arrays.stream(signals).distinct().collect(Collectors.toCollection(ArrayList::new));
    if (signalTrendSet.size() == 1 && ((currentTicket.ask() - currentTicket.bid()) < stopLoss)) {
      switch (signalTrendSet.get(0)) {
        case STRONG_BUY -> service.openPosition(currentTicket, OrderPosition.BUY);
        case STRONG_SELL -> service.openPosition(currentTicket, OrderPosition.SELL);
      }
    }
  }

  private static void updateOpenPosition(final Ticket currentTicket, final SignalTrend @NotNull [] signals, final OrderService service, final Order lastOrder) {
    final SignalTrend lastSignal = signals[0];
    if (SignalTrend.NEUTRAL.equals(lastSignal)) {
      service.closePosition(currentTicket, OrderStatus.CLOSE_MANUAL);
    } else {
      OrderPosition orderPositionIndicator = null;
      if (lastSignal.equals(SignalTrend.STRONG_BUY) || lastSignal.equals(SignalTrend.BUY)) {
        orderPositionIndicator = OrderPosition.BUY;
      } else if (lastSignal.equals(SignalTrend.STRONG_SELL) || lastSignal.equals(SignalTrend.SELL)) {
        orderPositionIndicator = OrderPosition.SELL;
      }
      if (!lastOrder.position().equals(orderPositionIndicator)) {
        service.closePosition(currentTicket, OrderStatus.CLOSE_MANUAL);
      }
    }
  }

  @Synchronized
  public void run() {
    final Ticket currentTicket = this.getTicketService().getCurrentTicket();
    final SignalTrend[] signals = Arrays.stream(this.getSignalService().getSignals()).sorted(Comparator.comparing(Signal::dateTime).reversed()).limit(this.getOpenWith())
        .map(Signal::trend).toArray(SignalTrend[]::new);
    if (signals.length == this.getOpenWith()) {
      this.updateOnTime(currentTicket, signals);
    } else {
      this.updateWithOutIndicators(currentTicket);
    }
    this.getOrderService().updateDebugFile();
  }

  private void updateOnTime(final Ticket currentTicket, final SignalTrend[] signals) {
    final OrderService service = this.getOrderService();
    if (service.getLastOrder().status().equals(OrderStatus.OPEN)) {
      updateOpenPosition(currentTicket, signals, service, service.getLastOrder());
      if(service.getLastOrder().status().equals(OrderStatus.OPEN)) {
        this.updateWithOutIndicators(currentTicket);
      }
    } else {
      updateClosePosition(currentTicket, signals, service, this.getStopLoss());
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
