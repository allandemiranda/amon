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
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
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
  private Double stopLoss;
  @Value("${order.profit}")
  private Double profit;

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
    if (this.order.status().equals(OrderStatus.open)) {
      if (this.order.position().equals(OrderPosition.buy)) {
        if ((this.order.openPrice() - this.stopLoss) >= bid) {
          this.closeOrderLose(time, ask, bid);
        } else {
          if ((this.profit + this.order.openPrice()) <= bid) {
            this.closeOrderWin(time, ask, bid);
          }
        }
      } else {
        if ((this.order.openPrice() + this.stopLoss) <= ask) {
          this.closeOrderLose(time, ask, bid);
        } else {
          if ((this.order.openPrice() - this.profit) >= ask) {
            this.closeOrderWin(time, ask, bid);
          }
        }
      }
    }
  }

  private void updateProfit(double ask, double bid) {
    if (this.order.status().equals(OrderStatus.open)) {
      if (this.order.position().equals(OrderPosition.buy)) {
        this.order = this.order.withClosePrice(bid);
        this.order = this.order.withProfit(this.order.closePrice() - this.order.openPrice());
      } else {
        this.order = this.order.withClosePrice(ask);
        this.order = this.order.withProfit(this.order.openPrice() - this.order.closePrice());
      }
    }
  }

  public void closeOrder(final @NotNull LocalDateTime time, final double ask, final double bid) {
    if (this.order.status().equals(OrderStatus.open)) {
      this.updateProfit(ask, bid);
      this.order = this.order.withStatus(OrderStatus.close).withLastUpdate(time);
      this.balance += this.order.profit();
      this.updateFile("NORMAL");
    }
  }

  public void closeOrderLose(final @NotNull LocalDateTime time, final double ask, final double bid) {
    if (this.order.status().equals(OrderStatus.open)) {
      this.updateProfit(ask, bid);
      this.order = this.order.withStatus(OrderStatus.close).withLastUpdate(time);
      this.balance += this.order.profit();
      this.updateFile("LOSE");
    }
  }

  public void closeOrderWin(final @NotNull LocalDateTime time, final double ask, final double bid) {
    if (this.order.status().equals(OrderStatus.open)) {
      this.updateProfit(ask, bid);
      this.order = this.order.withStatus(OrderStatus.close).withLastUpdate(time);
      this.balance += this.order.profit();
      this.updateFile("WIN");
    }
  }

  public Order getOrder() {
    return order;
  }

  public void openOrder(final @NotNull LocalDateTime time, @NotNull OrderPosition position, final double ask, final double bid) {
    if (this.order.status().equals(OrderStatus.close)) {
      this.order = this.order.withDateTime(time).withLastUpdate(time).withPosition(position).withStatus(OrderStatus.open);
      if (OrderPosition.buy.equals(position)) {
        this.order = this.order.withOpenPrice(ask).withClosePrice(bid).withProfit(bid-ask);
      } else {
        this.order = this.order.withOpenPrice(bid).withClosePrice(ask).withProfit(bid-ask);
      }
      this.updateFile("OPEN");
    } else {
      log.warn("Can't open a order operation");
    }
  }

  private void printHeaders() {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord(Arrays.stream(OrderHeaders.values()).map(Enum::toString).toArray());
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }

  private void updateFile(String a) {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord(this.order.dateTime(), this.order.lastUpdate(), this.order.status(), this.order.position(), this.order.openPrice(), this.order.closePrice(),
          new DecimalFormat("#0.0000#").format(this.order.profit()), new DecimalFormat("#0.0000#").format(this.balance), a);
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }
}
