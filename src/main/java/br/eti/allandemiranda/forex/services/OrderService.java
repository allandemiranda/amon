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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class OrderService {

  private static final String OUTPUT_FILE_NAME = "orders.csv";

  private final OrderRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${order.debug:false}")
  private boolean debugActive;

  @Autowired
  private OrderService(final OrderRepository repository) {
    this.repository = repository;
  }

  public void updateTicket(final @NotNull Ticket ticket) {
    this.getRepository().updateTicket(ticket);
    this.updateDebugFile();
  }

  public void openPosition(final @NotNull Ticket ticket, final @NotNull OrderPosition position) {
    this.getRepository().openPosition(ticket, position);
    this.updateDebugFile();
  }

  public void closePosition(final @NotNull Ticket ticket, final @NotNull OrderStatus status) {
    this.getRepository().closePosition(ticket, status);
    this.updateDebugFile();
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
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
        csvPrinter.printRecord(Arrays.stream(OrderHeader.values()).map(Enum::toString).toArray());
      }
    }
  }

  @SneakyThrows
  public void updateDebugFile() {
    Order order = this.getRepository().getLastOrder();
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
        csvPrinter.printRecord(order.openDateTime().format(DateTimeFormatter.ISO_DATE_TIME), order.lastUpdate().format(DateTimeFormatter.ISO_DATE_TIME), order.status(),
            order.position(), order.openPrice(), order.closePrice(), new DecimalFormat("#0.0000#").format(order.profit()),
            new DecimalFormat("#0.0000#").format(order.currentBalance()));
      }
    }
  }
}
