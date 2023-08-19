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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
  @Setter(AccessLevel.PRIVATE)
  private int newMultiplication = 1;

  @Autowired
  protected OrderService(final OrderRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getNumber(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.00000#").format(value.doubleValue()).replace(".", ",");
  }

  public @NotNull Order getLastOrder() {
    return this.getRepository().getLastOrder();
  }

  public @NotNull OrderStatus updateOpenPosition(final @NotNull Ticket ticket, final int takeProfit, final int stopLoss, final int tradingGain, final int tradingLoss) {
    if (this.getRepository().getLastOrder().lastUpdate().isBefore(ticket.dateTime())) {
      this.getRepository().updateOpenPosition(ticket);
      final int currentProfit = this.getRepository().getLastOrder().currentProfit();
      if (tradingGain == 0) {
        if (currentProfit >= takeProfit) {
          this.closePosition(OrderStatus.CLOSE_TP);
          this.updateDebugFile();
        } else if (currentProfit <= Math.negateExact(stopLoss)) {
          this.closePosition(OrderStatus.CLOSE_SL);
          this.updateDebugFile();
        }
      } else {
        if (currentProfit >= (tradingGain * this.getNewMultiplication())) {
          this.setNewMultiplication(this.getNewMultiplication() + 1);
        } else if (currentProfit <= Math.negateExact(stopLoss)) {
          this.closePosition(OrderStatus.CLOSE_SL);
          this.updateDebugFile();
        } else if (this.getNewMultiplication() > 1 && currentProfit <= ((tradingGain * (this.getNewMultiplication() - 1)) - tradingLoss)) {
          this.closePosition(OrderStatus.CLOSE_TG);
          this.updateDebugFile();
        }
      }
    }
    return this.getRepository().getLastOrder().status();
  }

  public void openPosition(final @NotNull Ticket ticket, final @NotNull OrderPosition position) {
    if (this.getRepository().getLastOrder().lastUpdate().isBefore(ticket.dateTime())) {
      this.getRepository().openPosition(ticket, position);
    }
  }

  public void closePosition(final @NotNull OrderStatus status) {
    if (!OrderStatus.OPEN.equals(status)) {
      this.setNewMultiplication(1);
      this.getRepository().closePosition(status);
    }
  }

  private @NotNull File getOutputFile() {
    return new File(this.getOutputFolder(), OrderService.OUTPUT_FILE_NAME);
  }

  @PostConstruct
  private void init() {
    this.printDebugHeader();
  }

  @SneakyThrows
  private void printDebugHeader() {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(Arrays.stream(OrderHeader.values()).map(Enum::toString).toArray());
      }
    }
  }

  public void updateDebugFile() {
    if (this.isDebugActive()) {
      this.debugUpdate(this.getOutputFile());
    }
  }

  @SneakyThrows
  private void debugUpdate(final File file) {
    Order order = this.getRepository().getLastOrder();
    if (!order.lastUpdate().equals(LocalDateTime.MIN)) {
      try (final FileWriter fileWriter = new FileWriter(file, true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(order.openDateTime().format(DateTimeFormatter.ISO_DATE_TIME), order.lastUpdate().format(DateTimeFormatter.ISO_DATE_TIME), order.status(),
            order.position(), getNumber(order.openPrice()), getNumber(order.closePrice()), order.highProfit(), order.lowProfit(), order.currentProfit(),
            order.currentBalance());
      }
    }
  }
}
