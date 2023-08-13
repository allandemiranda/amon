package br.eti.allandemiranda.forex.controllers.order;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.OrderService;
import br.eti.allandemiranda.forex.services.SignalService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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

  @Value("${order.take-profit:0}")
  @Setter(AccessLevel.PRIVATE)
  private int takeProfit;
  @Value("${order.stop-loss:0}")
  @Setter(AccessLevel.PRIVATE)
  private int stopLoss;
  @Value("${order.open.spread.max:0}")
  @Setter(AccessLevel.PRIVATE)
  private int maxSpread;
  @Value("${order.open.onlyStrong:true}")
  private boolean isOnlyStrong;
  @Value("${order.monday.start:'00:00:00'}")
  private String mondayStart;
  @Value("${order.monday.end:'23:59:59'}")
  private String mondayEnd;
  @Value("${order.tuesday.start:'00:00:00'}")
  private String tuesdayStart;
  @Value("${order.tuesday.end:'23:59:59'}")
  private String tuesdayEnd;
  @Value("${order.wednesday.start:'00:00:00'}")
  private String wednesdayStart;
  @Value("${order.wednesday.end:'23:59:59'}")
  private String wednesdayEnd;
  @Value("${order.thursday.start:'00:00:00'}")
  private String thursdayStart;
  @Value("${order.thursday.end:'23:59:59'}")
  private String thursdayEnd;
  @Value("${order.friday.start:'00:00:00'}")
  private String fridayStart;
  @Value("${order.friday.end:'23:59:59'}")
  private String fridayEnd;

  @Autowired
  protected OrderProcessor(final SignalService signalService, final TicketService ticketService, final OrderService orderService) {
    this.signalService = signalService;
    this.ticketService = ticketService;
    this.orderService = orderService;
  }

  private static boolean getDataConfirmation(final String startTime, final String endTime, final @NotNull LocalTime localTime) {
    final LocalTime start = LocalTime.parse(startTime, DateTimeFormatter.ISO_TIME);
    final LocalTime end = LocalTime.parse(endTime, DateTimeFormatter.ISO_TIME);
    return !localTime.isBefore(start) && !localTime.isAfter(end);
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
    final Ticket ticket = this.getTicketService().getTicket();
    if (OrderStatus.OPEN.equals(this.getOrderService().getLastOrder().status())) {
      operationToOpenOrder(ticket, this.getTakeProfit(), this.getStopLoss());
    } else if (this.getSignalService().isOpenSignal()){
      operationToCloseOrder(ticket, this.getStopLoss(), this.getMaxSpread());
    }
  }

  private void operationToCloseOrder(final Ticket ticket, final int stopLoss, final int maxSpread) {
    if (this.getSignalService().isOpenSignal() && ticket.spread() < stopLoss && ticket.spread() <= maxSpread && this.checkDataTime()) {
      switch (this.getSignalService().getOpenSignal().trend()) {
        case STRONG_BUY -> {
          this.getOrderService().openPosition(ticket, OrderPosition.BUY);
          this.getOrderService().updateDebugFile();
        }
        case STRONG_SELL -> {
          this.getOrderService().openPosition(ticket, OrderPosition.SELL);
          this.getOrderService().updateDebugFile();
        }
        case BUY -> {
          if (!this.isOnlyStrong()) {
            this.getOrderService().openPosition(ticket, OrderPosition.BUY);
            this.getOrderService().updateDebugFile();
          }
        }
        case SELL -> {
          if (!this.isOnlyStrong()) {
            this.getOrderService().openPosition(ticket, OrderPosition.SELL);
            this.getOrderService().updateDebugFile();
          }
        }
      }
    }
  }

  private void operationToOpenOrder(final Ticket ticket, final int takeProfit, final int stopLoss) {
    if (OrderStatus.OPEN.equals(this.getOrderService().updateOpenPosition(ticket, takeProfit, stopLoss))) {
      switch (this.getSignalService().getOpenSignal().trend()) {
        case STRONG_BUY, BUY -> {
          if (this.getOrderService().getLastOrder().position().equals(OrderPosition.SELL)) {
            this.getOrderService().closePosition(OrderStatus.CLOSE_MANUAL);
            this.getOrderService().updateDebugFile();
          }
        }
        case STRONG_SELL, SELL -> {
          if (this.getOrderService().getLastOrder().position().equals(OrderPosition.BUY)) {
            this.getOrderService().closePosition(OrderStatus.CLOSE_MANUAL);
            this.getOrderService().updateDebugFile();
          }
        }
      }
    }
  }

  private boolean checkDataTime() {
    final LocalDateTime dateTime = this.getTicketService().getTicket().dateTime();
    final LocalTime localTime = dateTime.toLocalTime();
    return switch (dateTime.getDayOfWeek()) {
      case MONDAY -> getDataConfirmation(this.getMondayStart(), this.getMondayEnd(), localTime);
      case TUESDAY -> getDataConfirmation(this.getTuesdayStart(), this.getTuesdayEnd(), localTime);
      case WEDNESDAY -> getDataConfirmation(this.getWednesdayStart(), this.getWednesdayEnd(), localTime);
      case THURSDAY -> getDataConfirmation(this.getThursdayStart(), this.getThursdayEnd(), localTime);
      case FRIDAY -> getDataConfirmation(this.getFridayStart(), this.getFridayEnd(), localTime);
      case SUNDAY, SATURDAY -> false;
    };
  }
}
