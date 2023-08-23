package br.eti.allandemiranda.forex.controllers.order;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.OrderService;
import br.eti.allandemiranda.forex.services.SignalService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.apache.commons.csv.CSVFormat;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class OrderProcessor {

  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();
  private static final String OUTPUT_FILE_NAME = "ordersFull.csv";

  private final SignalService signalService;
  private final TicketService ticketService;
  private final OrderService orderService;
  private final CandlestickService candlestickService;

  @Value("${order.open.onlyStrong:true}")
  private boolean isOpenOnlyStrong;
  @Value("${order.closeManual:true}")
  private boolean isCloseManual;
  @Value("${order.closeManual.onlyStrong:true}")
  private boolean isCloseManualOnlyStrong;
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
  protected OrderProcessor(final SignalService signalService, final TicketService ticketService, final OrderService orderService,
      final CandlestickService candlestickService) {
    this.signalService = signalService;
    this.ticketService = ticketService;
    this.orderService = orderService;
    this.candlestickService = candlestickService;
  }

  private static boolean getDataConfirmation(final String startTime, final String endTime, final @NotNull LocalTime localTime) {
    final LocalTime start = LocalTime.parse(startTime, DateTimeFormatter.ISO_TIME);
    final LocalTime end = LocalTime.parse(endTime, DateTimeFormatter.ISO_TIME);
    return !localTime.isBefore(start) && !localTime.isAfter(end);
  }

  @Synchronized
  public void run() {
    if (OrderStatus.OPEN.equals(this.getOrderService().getLastOrder().status())) {
      operationToOpenOrder();
    }
    if (!OrderStatus.OPEN.equals(this.getOrderService().getLastOrder().status()) && !this.getSignalService().getLastSignal().trend().equals(SignalTrend.NEUTRAL)
        && this.getOrderService().getLastOrder().openCandleDateTime().isBefore(this.getSignalService().getLastSignal().dataTime())) {
      operationToCloseOrder();
    }
  }

  private void operationToCloseOrder() {
    final Ticket ticket = this.getTicketService().getTicket();
    if (this.checkDataTime()) {
      final LocalDateTime candleDataTime = this.getCandlestickService().getLastCandlestick().dateTime();
      switch (this.getSignalService().getLastSignal().trend()) {
        case STRONG_BUY -> this.getOrderService().openPosition(ticket, candleDataTime, OrderPosition.BUY);
        case STRONG_SELL -> this.getOrderService().openPosition(ticket, candleDataTime, OrderPosition.SELL);
        case BUY -> {
          if (!this.isOpenOnlyStrong()) {
            this.getOrderService().openPosition(ticket, candleDataTime, OrderPosition.BUY);
          }
        }
        case SELL -> {
          if (!this.isOpenOnlyStrong()) {
            this.getOrderService().openPosition(ticket, candleDataTime, OrderPosition.SELL);
          }
        }
      }
    }
  }

  private void operationToOpenOrder() {
    final Ticket ticket = this.getTicketService().getTicket();
    if (OrderStatus.OPEN.equals(this.getOrderService().updateOpenPosition(ticket, this.getCandlestickService().getLastCandlestick().dateTime()))
        && this.isCloseManual()) {
      switch (this.getSignalService().getLastSignal().trend()) {
        case STRONG_BUY -> {
          if (this.getOrderService().getLastOrder().position().equals(OrderPosition.SELL)) {
            this.getOrderService().closePosition(OrderStatus.CLOSE_MANUAL);
          }
        }
        case STRONG_SELL -> {
          if (this.getOrderService().getLastOrder().position().equals(OrderPosition.BUY)) {
            this.getOrderService().closePosition(OrderStatus.CLOSE_MANUAL);
          }
        }
        case BUY -> {
          if (!this.isCloseManualOnlyStrong() && this.getOrderService().getLastOrder().position().equals(OrderPosition.SELL)) {
            this.getOrderService().closePosition(OrderStatus.CLOSE_MANUAL);

          }
        }
        case SELL -> {
          if (!this.isCloseManualOnlyStrong() && this.getOrderService().getLastOrder().position().equals(OrderPosition.BUY)) {
            this.getOrderService().closePosition(OrderStatus.CLOSE_MANUAL);
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
