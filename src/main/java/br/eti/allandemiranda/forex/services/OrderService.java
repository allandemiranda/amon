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
  private final OrderRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${order.debug:false}")
  private boolean debugActive;

  @Autowired
  protected OrderService(final OrderRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getNumber(final double value) {
    return new DecimalFormat("#0.0000#").format(value).replace(".", ",");
  }

  public Order getLastOrder() {
    return this.getRepository().getLastOrder();
  }

  public void updateTicket(final @NotNull Ticket ticket) {
    this.getRepository().updateTicket(ticket);
  }

  public void updateTicket(final @NotNull Ticket ticket, final double takeProfit, final double stopLoss) {
    this.getRepository().updateTicket(ticket);
    final Order lastOrder = this.getRepository().getLastOrder();
    if (lastOrder.status().equals(OrderStatus.OPEN)) {
      final double profit = lastOrder.profit();
      if (profit >= takeProfit) {
        this.closePosition(ticket, OrderStatus.CLOSE_TP);
      } else if (profit <= stopLoss) {
        this.closePosition(ticket, OrderStatus.CLOSE_SL);
      }
    }
  }

  public void openPosition(final @NotNull Ticket ticket, final @NotNull OrderPosition position) {
    this.getRepository().openPosition(ticket, position);
  }

  public void closePosition(final @NotNull Ticket ticket, final @NotNull OrderStatus status) {
    this.getRepository().closePosition(ticket, status);
  }

  private @NotNull File getOutputFile() {
    return new File(this.getOutputFolder(), OUTPUT_FILE_NAME);
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

  @SneakyThrows
  public void updateDebugFile() {
    if (this.isDebugActive()) {
      Order order = this.getRepository().getLastOrder();
      if (!order.lastUpdate().equals(LocalDateTime.MIN)) {
        try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
          csvPrinter.printRecord(order.openDateTime().format(DateTimeFormatter.ISO_DATE_TIME), order.lastUpdate().format(DateTimeFormatter.ISO_DATE_TIME), order.status(),
              order.position(), getNumber(order.openPrice()), getNumber(order.closePrice()), getNumber(order.profit()), getNumber(order.currentBalance()));
        }
      }
    }
  }
}
