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

  public Order getLastOrder() {
    return this.getRepository().getLastOrder();
  }

  public void updateTicket(final @NotNull Ticket ticket) {
    this.getRepository().updateTicket(ticket);
    this.updateDebugFile(this.getRepository());
  }

  public void updateTicket(final @NotNull Ticket ticket, final double takeProfit, final double stopLoss) {
    this.getRepository().updateTicket(ticket);
    if (this.getRepository().getLastOrder().profit() >= takeProfit) {
      this.closePosition(ticket, OrderStatus.CLOSE_TP);
    } else if (this.getRepository().getLastOrder().profit() <= stopLoss) {
      this.closePosition(ticket, OrderStatus.CLOSE_SL);
    } else {
      this.updateDebugFile(this.getRepository());
    }
  }

  public void openPosition(final @NotNull Ticket ticket, final @NotNull OrderPosition position) {
    this.getRepository().openPosition(ticket, position);
    this.updateDebugFile(this.getRepository());
  }

  public void closePosition(final @NotNull Ticket ticket, final @NotNull OrderStatus status) {
    this.getRepository().closePosition(ticket, status);
    this.updateDebugFile(this.getRepository());
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
  private void updateDebugFile(final @NotNull OrderRepository repository) {
    Order order = repository.getLastOrder();
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(order.openDateTime().format(DateTimeFormatter.ISO_DATE_TIME), order.lastUpdate().format(DateTimeFormatter.ISO_DATE_TIME), order.status(),
            order.position(), order.openPrice(), order.closePrice(), new DecimalFormat("#0.0000#").format(order.profit()),
            new DecimalFormat("#0.0000#").format(order.currentBalance()));
      }
    }
  }
}
