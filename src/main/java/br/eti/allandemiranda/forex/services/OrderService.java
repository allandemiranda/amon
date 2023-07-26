package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Order;
import br.eti.allandemiranda.forex.exceptions.WriteFileException;
import br.eti.allandemiranda.forex.headers.OrderHeaders;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  @Value("${order.output}")
  private File outputFile;

  private Order order = new Order(LocalDateTime.MIN, LocalDateTime.MIN, OrderStatus.close, 0D, 0D, 0D);

  private @NotNull File getOutputFile() {
    return this.outputFile;
  }

  @PostConstruct
  public void init() {
    printHeaders();
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(final @NotNull Order order) {
    this.order = order;
    updateFile();
  }

  private void printHeaders() {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord((Object) OrderHeaders.values());
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }

  private void updateFile() {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord(this.order.dateTime(), this.order.lastUpdate(), this.order.openPrice(), this.order.closePrice(), this.order.profit());
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }
}
