package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Order;
import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.headers.OrderHeader;
import br.eti.allandemiranda.forex.repositories.OrderRepository;
import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter(AccessLevel.PRIVATE)
public class OrderService {

  private static final String TIME_OPEN_FORMAT = "%sd %s:%s:%s";
  private static final String OUTPUT_FILE_NAME = "order.csv";
  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();
  public static final String TIME_OPEN = "0 00:00:00";

  @Value("${order.open.spread.max:12}")
  private int maxSpread;

  @Value("${order.open.onlyStrong:false}")
  private boolean isOpenOnlyStrong;

  @Value("${order.open.maxOpenPositions:999}")
  private int maxOpenPositions;

  @Value("${order.open.monday.start:00:00:00}")
  private String mondayStart;
  @Value("${order.open.monday.end:23:59:59}")
  private String mondayEnd;
  @Value("${order.open.tuesday.start:00:00:00}")
  private String tuesdayStart;
  @Value("${order.open.tuesday.end:23:59:59}")
  private String tuesdayEnd;
  @Value("${order.open.wednesday.start:00:00:00}")
  private String wednesdayStart;
  @Value("${order.open.wednesday.end:23:59:59}")
  private String wednesdayEnd;
  @Value("${order.open.thursday.start:00:00:00}")
  private String thursdayStart;
  @Value("${order.open.thursday.end:23:59:59}")
  private String thursdayEnd;
  @Value("${order.open.friday.start:00:00:00}")
  private String fridayStart;
  @Value("${order.open.friday.end:23:59:59}")
  private String fridayEnd;

  @Value("${order.safe.take-profit:150}")
  private int takeProfit;
  @Value("${order.safe.stop-loss:100}")
  private int stopLoss;

  @Value("${order.swap.long:-5.46}")
  private double swapLong;
  @Value("${order.swap.short:0.61}")
  private double swapShort;
  @Value("${order.swap.rate.triple:WEDNESDAY}")
  private String swapRateTriple;

  @Value("${order.debug:true}")
  private boolean debugActive;
  @Value("${config.root.folder}")
  private File outputFolder;

  @Setter(AccessLevel.PRIVATE)
  private LocalDateTime lastSignalOpenDateTime = LocalDateTime.MIN;
  @Setter(AccessLevel.PRIVATE)
  private BigDecimal currentBalance = BigDecimal.ZERO;

  private final OrderRepository repository;

  @Autowired
  protected OrderService(final OrderRepository repository) {
    this.repository = repository;
  }

  /**
   * String number format to price value
   *
   * @param value The price value
   * @return The text price value
   */
  private static @NotNull String getNumberPrice(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.00000#").format(value.doubleValue()).replace(".", ",");
  }

  /**
   * String number format to balance value
   *
   * @param value The balance value
   * @return The text balance value
   */
  private static @NotNull String getNumberBalance(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.00#").format(value.doubleValue()).replace(".", ",");
  }

  private int getPoints(final @NotNull BigDecimal price, final int digits) {
    return price.multiply(BigDecimal.valueOf(Math.pow(10, digits))).intValue();
  }

  /**
   * Add a ticket and signal to the database make the calculation and organization
   *
   * @param ticket The current ticket
   * @param signal the current signal information
   */
  public void insertTicketAndSignal(final @NotNull Ticket ticket, final @NotNull Signal signal) {
    // Update open tickets
    this.updateTicket(Arrays.stream(this.getRepository().getOrders()).toList(), ticket, this.getTakeProfit(), this.getStopLoss(), BigDecimal.valueOf(this.getSwapLong()),
        BigDecimal.valueOf(this.getSwapShort()), DayOfWeek.valueOf(this.getSwapRateTriple())).forEach(order -> this.getRepository().updateOrder(order));

    // Check to open a new order
    if (checkDataTime(ticket.dateTime())) {
      final Optional<Order> openOrder = this.openOrder(ticket, signal, this.getMaxOpenPositions());
      if (openOrder.isPresent()) {
        this.getRepository().addOrder(openOrder.get());
        this.setLastSignalOpenDateTime(signal.dataTime());
      }
    }

    // Update the current balance
    this.setCurrentBalance(getNewBalance(this.getRepository().getOrders(), this.getCurrentBalance()));

    // Print the close orders
    Arrays.stream(this.getRepository().getOrders()).filter(order -> !order.orderStatus().equals(OrderStatus.OPEN))
        .forEachOrdered(order -> updateDebugFile(order, this.getCurrentBalance()));

    // Remove che closed orders
    this.getRepository().removeCloseOrders();
  }

  /**
   * Get the curretn balance of close orders
   *
   * @param orders The full orders on this roand
   * @return The new current balance
   */
  private @NotNull BigDecimal getNewBalance(final Order @NotNull [] orders, final @NotNull BigDecimal prevBalance) {
    final BigDecimal currentCloseProfit = Arrays.stream(orders).filter(order -> !order.orderStatus().equals(OrderStatus.OPEN))
        .map(order -> order.swapProfit().add(BigDecimal.valueOf(order.currentProfit()))).reduce(BigDecimal.ZERO, BigDecimal::add);
    return prevBalance.add(currentCloseProfit);
  }

  /**
   * Update data information for positions open
   *
   * @param ticket     The current ticket
   * @param takeProfit The take profit
   * @param stopLoss   The stop loss profit
   * @param swapLong   The swap long in points
   * @param swapShort  The swap short in points
   * @return The list of orders to be updated
   */
  private @NotNull Collection<Order> updateTicket(final @NotNull Collection<Order> orders, final @NotNull Ticket ticket, final int takeProfit, final int stopLoss, final @NotNull BigDecimal swapLong,
      final @NotNull BigDecimal swapShort, final DayOfWeek swapRateTriple) {
    return orders.parallelStream().map(order -> {
      // Check if is necessary add a swap to this order
      final BigDecimal swapProfit = getSwapProfitProcess(ticket, swapLong, swapShort, swapRateTriple, order);

      // Update open time
      final String timeOpen = String.format(TIME_OPEN_FORMAT, ChronoUnit.DAYS.between(order.openDateTime(), ticket.dateTime()),
          ChronoUnit.HOURS.between(order.openDateTime(), ticket.dateTime()) % 24, ChronoUnit.MINUTES.between(order.openDateTime(), ticket.dateTime()) % 60,
          ChronoUnit.SECONDS.between(order.openDateTime(), ticket.dateTime()) % 60);

      // Update the profit to this order
      final BigDecimal closePrice = getClosePrice(ticket, order);
      final int currentProfit = getCurrentProfit(ticket, order, closePrice);
      final int highProfit = getHighProfit(order, currentProfit);
      final int lowProfit = getLowProfit(order, currentProfit);

      // Update status of this order
      final OrderStatus orderStatus = getOrderStatus(takeProfit, stopLoss, currentProfit);

      // Update order
      return new Order(order.openDateTime(), order.signalDateTime(), order.signalTrend(), ticket.dateTime(), timeOpen, orderStatus, order.orderPosition(),
          order.openPrice(), closePrice, highProfit, lowProfit, currentProfit, swapProfit);
    }).collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Get tge order status
   *
   * @param takeProfit    The take profit
   * @param stopLoss      The stop loss (positive value)
   * @param currentProfit the current profit
   * @return The new status to the order
   */
  private @NotNull OrderStatus getOrderStatus(final int takeProfit, final int stopLoss, final int currentProfit) {
    if (currentProfit >= takeProfit) {
      return OrderStatus.CLOSE_TP;
    } else if (currentProfit <= Math.negateExact(stopLoss)) {
      return OrderStatus.CLOSE_SL;
    } else {
      return OrderStatus.OPEN;
    }
  }

  /**
   * Get new low profit
   *
   * @param order         The new order
   * @param currentProfit The current profit
   * @return The low value to this order
   */
  private int getLowProfit(final @NotNull Order order, final int currentProfit) {
    return currentProfit < order.lowProfit() ? order.currentProfit() : order.lowProfit();
  }

  /**
   * Get new high profit
   *
   * @param order         The new order
   * @param currentProfit The current profit
   * @return The high value to this order
   */
  private int getHighProfit(final @NotNull Order order, final int currentProfit) {
    return currentProfit > order.highProfit() ? order.currentProfit() : order.highProfit();
  }

  /**
   * Get current profit
   *
   * @param ticket     The current ticket
   * @param order      The current order
   * @param closePrice The close price
   * @return The new current profit
   */
  private int getCurrentProfit(final @NotNull Ticket ticket, final @NotNull Order order, final @NotNull BigDecimal closePrice) {
    int currentProfit;
    if (order.orderPosition().equals(OrderPosition.BUY)) {
      currentProfit = this.getPoints(closePrice, ticket.digits()) - this.getPoints(order.openPrice(), ticket.digits());
    } else {
      currentProfit = this.getPoints(order.openPrice(), ticket.digits()) - this.getPoints(closePrice, ticket.digits());
    }
    return currentProfit;
  }

  /**
   * Get close price
   *
   * @param ticket The current ticket
   * @param order  The current order
   * @return The new close price
   */
  private @NotNull BigDecimal getClosePrice(final @NotNull Ticket ticket, final @NotNull Order order) {
    if (order.orderPosition().equals(OrderPosition.BUY)) {
      return ticket.bid();
    } else {
      return ticket.ask();
    }
  }

  /**
   * Check if is necessary add a swap to this order
   *
   * @param ticket         The current ticket
   * @param swapLong       The swap-long points
   * @param swapShort      The swap short points
   * @param swapRateTriple The day of the week to swap tripe
   * @param order          The current order to be updated
   * @return The new swap balance value
   */
  private @NotNull BigDecimal getSwapProfitProcess(final @NotNull Ticket ticket, final @NotNull BigDecimal swapLong, final @NotNull BigDecimal swapShort,
      final @NotNull DayOfWeek swapRateTriple, final @NotNull Order order) {
    if (!order.lastUpdateDateTime().getDayOfWeek().equals(ticket.dateTime().getDayOfWeek())) {
      BigDecimal points;
      if (OrderPosition.BUY.equals(order.orderPosition())) {
        points = swapRateTriple.equals(order.lastUpdateDateTime().getDayOfWeek()) ? swapLong.multiply(BigDecimal.valueOf(3)) : swapLong;
      } else {
        points = swapRateTriple.equals(order.lastUpdateDateTime().getDayOfWeek()) ? swapShort.multiply(BigDecimal.valueOf(3)) : swapShort;
      }
      return order.swapProfit().add(points);
    } else {
      return order.swapProfit();
    }
  }

  /**
   * Open a new order if possible
   *
   * @param ticket           The current ticket
   * @param signal           The current signal
   * @param maxOpenPositions The maximum number of open orders
   * @return The possible new order to open
   */
  private @NotNull Optional<Order> openOrder(final @NotNull Ticket ticket, final @NotNull Signal signal, final int maxOpenPositions) {
    final LocalDateTime signalDateTime = signal.dataTime();
    final SignalTrend trend = signal.trend();
    if (this.getRepository().numberOfOrdersOpen() < maxOpenPositions && ticket.spread() <= this.getMaxSpread() && signalDateTime.isAfter(
        this.getLastSignalOpenDateTime())) {
      if (this.isOpenOnlyStrong() && trend.equals(SignalTrend.STRONG_BUY)) {
        return Optional.of(generateOpenOrder(ticket, signalDateTime, trend, OrderPosition.BUY));
      } else if (this.isOpenOnlyStrong() && trend.equals(SignalTrend.STRONG_SELL)) {
        return Optional.of(generateOpenOrder(ticket, signalDateTime, trend, OrderPosition.SELL));
      } else if (!this.isOpenOnlyStrong() && (trend.equals(SignalTrend.STRONG_BUY) || trend.equals(SignalTrend.BUY))) {
        return Optional.of(generateOpenOrder(ticket, signalDateTime, trend, OrderPosition.BUY));
      } else if (!this.isOpenOnlyStrong() && (trend.equals(SignalTrend.STRONG_SELL) || trend.equals(SignalTrend.SELL))) {
        return Optional.of(generateOpenOrder(ticket, signalDateTime, trend, OrderPosition.SELL));
      }
    }
    return Optional.empty();
  }

  /**
   * Generate an open order to be sent
   *
   * @param ticket         The current Ticket
   * @param signalDateTime The Signal data time
   * @param trend          The trend to the new order
   * @param orderPosition  The position to the new order
   * @return The new order
   */
  private @NotNull Order generateOpenOrder(final @NotNull Ticket ticket, final @NotNull LocalDateTime signalDateTime, final @NotNull SignalTrend trend,
      final @NotNull OrderPosition orderPosition) {
    final LocalDateTime ticketDateTime = ticket.dateTime();
    final BigDecimal openPrice = orderPosition.equals(OrderPosition.BUY) ? ticket.ask() : ticket.bid();
    final BigDecimal closePrice = orderPosition.equals(OrderPosition.BUY) ? ticket.bid() : ticket.ask();
    final int spread = Math.negateExact(ticket.spread());
    return new Order(ticketDateTime, signalDateTime, trend, ticketDateTime, TIME_OPEN, OrderStatus.OPEN, orderPosition, openPrice, closePrice, spread, spread, spread,
        BigDecimal.ZERO);
  }

  /**
   * Get confirmation time
   *
   * @param startTime The start time to open an order
   * @param endTime   The end time to close an order
   * @param localTime The current time
   * @return If you can open on this day an order
   */
  private static boolean getDataConfirmation(final @NotNull String startTime, final @NotNull String endTime, final @NotNull LocalTime localTime) {
    final LocalTime start = LocalTime.parse(startTime, DateTimeFormatter.ISO_TIME);
    final LocalTime end = LocalTime.parse(endTime, DateTimeFormatter.ISO_TIME);
    return !localTime.isBefore(start) && !localTime.isAfter(end);
  }

  /**
   * Check if the day of the week and time is able to open an order
   *
   * @param dateTime The current data time
   * @return If you can open
   */
  private boolean checkDataTime(final @NotNull LocalDateTime dateTime) {
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

  @PostConstruct
  private void init() {
    this.printDebugHeader();
  }

  private @NotNull File getOutputFile() {
    return new File(this.getOutputFolder(), OUTPUT_FILE_NAME);
  }

  @SneakyThrows
  private void printDebugHeader() {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(Arrays.stream(OrderHeader.values()).map(Enum::toString).toArray());
      }
    }
  }

  @SneakyThrows
  private void updateDebugFile(final @NotNull Order order, final @NotNull BigDecimal currentBalance) {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(order.openDateTime().format(DateTimeFormatter.ISO_DATE_TIME), order.signalDateTime().format(DateTimeFormatter.ISO_DATE_TIME),
            order.signalTrend(), order.lastUpdateDateTime().format(DateTimeFormatter.ISO_DATE_TIME), order.timeOpen(), order.orderStatus(), order.orderPosition(),
            getNumberPrice(order.openPrice()), getNumberPrice(order.closePrice()), order.highProfit(), order.lowProfit(), order.currentProfit(), getNumberBalance(order.swapProfit()),
            getNumberBalance(currentBalance), order.openDateTime().getDayOfWeek(), order.openDateTime().toLocalTime().format(DateTimeFormatter.ISO_TIME));
      }
    }
  }
}
