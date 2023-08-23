package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Order;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.headers.OrderHeader;
import br.eti.allandemiranda.forex.repositories.OrderRepository;
import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;
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

  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();
  private static final String OUTPUT_FILE_NAME = "orders.csv";

  private final OrderRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${order.debug:false}")
  private boolean debugActive;

  @Value("${order.open.spread.max:12}")
  @Getter(AccessLevel.PUBLIC)
  private int maxSpread;
  @Value("${order.safe:true}")
  private boolean isTakeProfit;
  @Value("${order.safe.take-profit:0}")
  @Setter(AccessLevel.PRIVATE)
  private int takeProfit;
  @Setter(AccessLevel.PRIVATE)
  private int tmpTakeProfit;
  @Value("${order.safe.stop-loss:0}")
  @Setter(AccessLevel.PRIVATE)
  private int stopLoss;
  @Setter(AccessLevel.PRIVATE)
  private int tmpStopLoss;
  @Value("${order.trading:false}")
  private boolean isGain;
  @Setter(AccessLevel.PRIVATE)
  private boolean inOnGain;
  @Value("${order.trading.gain:0}")
  private int tradingGain;
  @Value("${order.trading.loss:0}")
  private int tradingLoss;
  @Value("${order.swap.long:-5.46}")
  private double swapLong;
  @Value("${order.swap.short:0.61}")
  private double swapShort;
  @Value("${order.swap.rate.triple:WEDNESDAY}")
  private String swapRateTriple;
  @Value("${order.safe.lose:true}")
  private boolean isSafeLose;
  @Value("${order.safe.lose.sequence:1}")
  @Setter(AccessLevel.PRIVATE)
  private int loseSequence;
  @Value("${order.safe.lose.time.hours:2}")
  private int loseHours;
  private final TreeSet<LocalDateTime> badPositions = new TreeSet<>();

  @Autowired
  protected OrderService(final OrderRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getPriceNumber(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.00000#").format(value.doubleValue()).replace(".", ",");
  }

  private static @NotNull String getBalanceNumber(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.00#").format(value.doubleValue()).replace(".", ",");
  }

  @PostConstruct
  private void init() {
    this.printDebugHeader();
    if (!this.isTakeProfit() && this.isGain()) {
      this.setTakeProfit(0);
      this.setStopLoss(0);
    }
    if(this.isSafeLose() && this.getLoseSequence() <= 0) {
      this.setLoseSequence(Integer.MAX_VALUE);
    }
  }

  public @NotNull Order getLastOrder() {
    return this.getRepository().getLastOrder();
  }

  public @NotNull OrderStatus updateOpenPosition(final @NotNull Ticket ticket, final @NotNull LocalDateTime candleDataTime) {
    if (this.getRepository().getLastOrder().lastUpdate().isBefore(ticket.dateTime())) {
      this.getRepository().updateOpenPosition(ticket, candleDataTime, DayOfWeek.valueOf(this.getSwapRateTriple()), BigDecimal.valueOf(this.getSwapLong()),
          BigDecimal.valueOf(this.getSwapShort()));
      final int currentProfit = this.getRepository().getLastOrder().currentProfit();
      if (this.isTakeProfit() && !this.isGain()) {
        if (this.getTakeProfit() <= currentProfit) {
          this.closePosition(OrderStatus.CLOSE_TP);
        }
        if (this.getStopLoss() > 0 && Math.negateExact(this.getStopLoss()) >= currentProfit) {
          this.closePosition(OrderStatus.CLOSE_SL);
        }
      } else if (this.isTakeProfit() && this.isGain()) {
        if (this.isInOnGain()) {
          if (this.getTmpTakeProfit() <= currentProfit) {
            this.setTmpTakeProfit(currentProfit + this.getTradingGain());
            this.setTmpStopLoss(currentProfit - this.getTradingLoss());
          }
          if (this.getTmpStopLoss() >= currentProfit) {
            this.closePosition(OrderStatus.CLOSE_TG);
          }
        } else {
          if (this.getTakeProfit() <= currentProfit) {
            this.setTmpTakeProfit(this.getTakeProfit() + this.getTradingGain());
            this.setTmpStopLoss(this.getTakeProfit() - this.getTradingLoss());
            this.setInOnGain(true);
          }
          if (this.getStopLoss() > 0 && Math.negateExact(this.getStopLoss()) >= currentProfit) {
            this.closePosition(OrderStatus.CLOSE_SL);
          }
        }
      } else if (!this.isTakeProfit() && this.isGain()) {
        if (this.getTmpTakeProfit() == 0) {
          if (Math.negateExact(this.getTradingLoss()) >= currentProfit) {
            this.closePosition(OrderStatus.CLOSE_SL);
          } else {
            this.setTmpTakeProfit(this.getTradingGain());
            this.setTmpStopLoss(Math.negateExact(this.getTradingLoss()));
          }
        } else {
          if (this.getTmpTakeProfit() <= currentProfit) {
            this.setTmpTakeProfit(this.getTmpTakeProfit() + this.getTradingGain());
            this.setTmpStopLoss(this.getTmpTakeProfit() - this.getTradingLoss());
          }
          if (this.getTmpStopLoss() >= currentProfit) {
            this.closePosition(OrderStatus.CLOSE_TG);
          }
        }
      }
    }
    return this.getRepository().getLastOrder().status();
  }

  public void openPosition(final @NotNull Ticket ticket, final @NotNull LocalDateTime candleDataTime, final @NotNull OrderPosition position) {
    if (ticket.spread() <= this.getMaxSpread() && (!this.isTakeProfit() || this.getStopLoss() <= 0 || ticket.spread() <= this.getStopLoss()) && (!this.isGain()
        || ticket.spread() <= this.getTradingLoss()) && this.getRepository().getLastOrder().lastUpdate().isBefore(ticket.dateTime())) {
      this.getBadPositions().forEach(dateTime -> {
        if(!dateTime.toLocalDate().equals(ticket.dateTime().toLocalDate())) {
          this.getBadPositions().remove(dateTime);
        }
      });
      if(this.getBadPositions().size() < this.getLoseSequence()) {
        openPositionConfiguration(ticket, candleDataTime, position);
      } else if (this.getBadPositions().last().plusHours(this.getLoseHours()).isBefore(ticket.dateTime())) {
        openPositionConfiguration(ticket, candleDataTime, position);
        this.getBadPositions().clear();
      }
    }
  }

  private void openPositionConfiguration(final @NotNull Ticket ticket, final @NotNull LocalDateTime candleDataTime, final @NotNull OrderPosition position) {
    this.getRepository().openPosition(ticket, candleDataTime, position);
    this.setInOnGain(false);
    this.setTmpTakeProfit(0);
    this.updateDebugFile();
  }

  public void closePosition(final @NotNull OrderStatus status) {
    if (!OrderStatus.OPEN.equals(status)) {
      this.getRepository().closePosition(status);
      if(status.equals(OrderStatus.CLOSE_SL) || status.equals(OrderStatus.CLOSE_MANUAL)) {
        this.getBadPositions().add(this.getRepository().getLastOrder().lastUpdate());
      }
      this.updateDebugFile();
    }
  }

  private @NotNull File getOutputFile() {
    return new File(this.getOutputFolder(), OrderService.OUTPUT_FILE_NAME);
  }

  @SneakyThrows
  private void printDebugHeader() {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(Arrays.stream(OrderHeader.values()).map(Enum::toString).toArray());
      }
    }
  }

  private void updateDebugFile() {
    if (this.isDebugActive()) {
      this.debugUpdate(this.getOutputFile());
    }
  }

  @SneakyThrows
  private void debugUpdate(final File file) {
    Order order = this.getRepository().getLastOrder();
    if (!order.lastUpdate().equals(LocalDateTime.MIN)) {
      try (final FileWriter fileWriter = new FileWriter(file, true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(order.openDateTime().format(DateTimeFormatter.ISO_DATE_TIME), order.lastUpdate().format(DateTimeFormatter.ISO_DATE_TIME),
            order.openCandleDateTime().format(DateTimeFormatter.ISO_DATE_TIME), order.lastCandleUpdate().format(DateTimeFormatter.ISO_DATE_TIME), order.status(),
            order.position(), getPriceNumber(order.openPrice()), getPriceNumber(order.closePrice()),
            String.format("%sd %s:%s:%s", ChronoUnit.DAYS.between(order.openDateTime(), order.lastUpdate()),
                ChronoUnit.HOURS.between(order.openDateTime(), order.lastUpdate()) % 24, ChronoUnit.MINUTES.between(order.openDateTime(), order.lastUpdate()) % 60,
                ChronoUnit.SECONDS.between(order.openDateTime(), order.lastUpdate()) % 60), order.highProfit(), order.lowProfit(), order.currentProfit(),
            getBalanceNumber(order.swapBalance()), getBalanceNumber(order.currentBalance()));
      }
    }
  }
}
