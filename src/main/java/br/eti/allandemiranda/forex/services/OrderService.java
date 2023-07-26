package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Order;
import br.eti.allandemiranda.forex.exceptions.WriteFileException;
import br.eti.allandemiranda.forex.headers.OrderHeaders;
import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService {

  @Value("${order.output}")
  private File outputFile;
  @Value("${order.stop-loss}")
  private Integer stopLoss;
  @Value("${order.profit}")
  private Integer profit;

  private Order order = new Order(LocalDateTime.MIN, LocalDateTime.MIN, OrderStatus.close, OrderPosition.buy, 0D, 0D, 0D);
  private double balance = 0D;

  private @NotNull File getOutputFile() {
    return this.outputFile;
  }

  @PostConstruct
  public void init() {
    printHeaders();
  }

  public void updateOrder(final @NotNull LocalDateTime time, final double ask, final double bid) {
    if (this.order.status().equals(OrderStatus.open)) {
      this.order = this.order.withLastUpdate(time);
      this.updateProfit(ask, bid);
      this.checkProfitAndStopLoss(time, ask, bid);
    }
  }

  private void checkProfitAndStopLoss(@NotNull LocalDateTime time, double ask, double bid) {
    if (this.profit <= this.order.profit()) {
      this.closeOrder(time, ask, bid);
    } else {
      if (this.order.position().equals(OrderPosition.buy)) {
        if (this.stopLoss >= bid) {
          this.closeOrder(time, ask, bid);
        }
      } else {
        if (this.stopLoss <= ask) {
          this.closeOrder(time, ask, bid);
        }
      }
    }
  }

  private void updateProfit(double ask, double bid) {
    if (this.order.position().equals(OrderPosition.buy)) {
      this.order = this.order.withClosePrice(bid);
      this.order = this.order.withProfit(this.order.closePrice() - this.order.openPrice());
    } else {
      this.order = this.order.withClosePrice(ask);
      this.order = this.order.withProfit(this.order.openPrice() - this.order.closePrice());
    }
  }

  public void closeOrder(final @NotNull LocalDateTime time, final double ask, final double bid) {
    this.updateProfit(ask, bid);
    this.order = this.order.withStatus(OrderStatus.close).withLastUpdate(time);
    this.balance += this.order.profit();
    this.updateFile();
  }

  public Order getOrder() {
    return order;
  }

  public void openOrder(final @NotNull LocalDateTime time, @NotNull OrderPosition position, final double ask, final double bid) {
    if (this.order.status().equals(OrderStatus.close)) {
      this.order = this.order.withDateTime(time).withLastUpdate(time).withPosition(position);
      if (OrderPosition.buy.equals(position)) {
        this.order = this.order.withOpenPrice(ask).withClosePrice(bid);
      } else {
        this.order = this.order.withOpenPrice(bid).withClosePrice(ask);
      }
      this.updateFile();
    } else {
      log.warn("Can't open a order operation");
    }
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
      csvPrinter.printRecord(this.order.dateTime(), this.order.lastUpdate(), this.order.status(), this.order.position(), this.order.openPrice(), this.order.closePrice(),
          this.order.profit(), this.balance);
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }
}
