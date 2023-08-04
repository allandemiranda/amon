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
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Getter;
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

  public static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();
  private static final String OUTPUT_FILE_NAME = "orders.csv";
  private static final String OUTPUT_FILE_NAME_SHORT = "orders_short.csv";
  private final OrderRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${order.debug:false}")
  private boolean debugActive;
  @Value("${order.debug.short:false}")
  private boolean debugActiveShort;

  @Autowired
  protected OrderService(final OrderRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getNumber(final double value) {
    return new DecimalFormat("#0.00000#").format(value).replace(".", ",");
  }

  public Order getLastOrder() {
    return this.getRepository().getLastOrder();
  }

  public void updateOpenPosition(final @NotNull Ticket ticket, final double takeProfit, final double stopLoss) {
    this.getRepository().updateOpenPosition(ticket);
    final double profit = this.getRepository().getLastOrder().profit();
    if (profit >= takeProfit) {
      this.closePosition(OrderStatus.CLOSE_TP);
      this.updateDebugShortFile();
    } else if (profit <= stopLoss) {
      this.closePosition(OrderStatus.CLOSE_SL);
      this.updateDebugShortFile();
    }
  }

  public void openPosition(final @NotNull Ticket ticket, final @NotNull OrderPosition position) {
    this.getRepository().openPosition(ticket, position);
  }

  public void closePosition(final @NotNull OrderStatus status) {
    this.getRepository().closePosition(status);
  }

  private @NotNull File getOutputFile(final String filename) {
    return new File(this.getOutputFolder(), filename);
  }

  @PostConstruct
  private void init() {
    this.printDebugHeader();
  }

  @SneakyThrows
  private void printDebugHeader() {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(OUTPUT_FILE_NAME)); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(Arrays.stream(OrderHeader.values()).map(Enum::toString).toArray());
      }
    }
    if (this.isDebugActiveShort()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(OUTPUT_FILE_NAME_SHORT)); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(Arrays.stream(OrderHeader.values()).map(Enum::toString).toArray());
      }
    }
  }

  public void updateDebugFile() {
    if (this.isDebugActive()) {
      this.debugUpdate(this.getOutputFile(OUTPUT_FILE_NAME));
    }
  }

  public void updateDebugShortFile() {
    if (this.isDebugActiveShort()) {
      this.debugUpdate(this.getOutputFile(OUTPUT_FILE_NAME_SHORT));
    }
  }

  @SneakyThrows
  private void debugUpdate(final File file) {
    Order order = this.getRepository().getLastOrder();
    if (!order.lastUpdate().equals(LocalDateTime.MIN)) {
      try (final FileWriter fileWriter = new FileWriter(file, true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(order.openDateTime().format(DateTimeFormatter.ISO_DATE_TIME), order.lastUpdate().format(DateTimeFormatter.ISO_DATE_TIME), order.status(),
            order.position(), getNumber(order.openPrice()), getNumber(order.closePrice()), getNumber(order.profit()), getNumber(order.currentBalance()));
      }
    }
  }
}
