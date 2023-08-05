package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Order;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class OrderRepository {

  private LocalDateTime openDateTime;
  private LocalDateTime lastUpdate;
  private OrderStatus status;
  private OrderPosition position;
  private double openPrice;
  private double closePrice;
  private int currentProfit;
  private int currentBalance;
  private int highProfit;
  private int lowProfit;

  private static int getPoints(final double price, final int digits) {
    return (int) ((price) / (1 / (Math.pow(10, digits))));
  }

  @PostConstruct
  private void init() {
    this.setStatus(OrderStatus.CLOSE_MANUAL);
    this.setOpenDateTime(LocalDateTime.MIN);
    this.setLastUpdate(LocalDateTime.MIN);
  }

  public @NotNull Order getLastOrder() {
    return new Order(this.getOpenDateTime(), this.getLastUpdate(), this.getStatus(), this.getPosition(), this.getOpenPrice(), this.getClosePrice(),
        this.getCurrentProfit(), this.getCurrentBalance(), this.getHighProfit(), this.getLowProfit());
  }

  @Synchronized
  public void updateOpenPosition(final @NotNull Ticket ticket) {
    final int digits = ticket.digits();
    this.setLastUpdate(ticket.dateTime());
    if (OrderPosition.BUY.equals(this.getPosition())) {
      final double bid = ticket.bid();
      this.setCurrentBalance(this.getCurrentBalance() + getPoints((bid - this.getClosePrice()), digits));
      this.setClosePrice(bid);
      this.setCurrentProfit(getPoints((bid - this.getOpenPrice()), digits));
    } else {
      final double ask = ticket.ask();
      this.setCurrentBalance(this.getCurrentBalance() + getPoints((this.getClosePrice() - ask), digits));
      this.setClosePrice(ask);
      this.setCurrentProfit(getPoints((this.getOpenPrice() - ask), digits));
    }
    if (this.getCurrentProfit() > this.getHighProfit()) {
      this.setHighProfit(this.getCurrentProfit());
    }
    if (this.getCurrentProfit() < this.getLowProfit()) {
      this.setLowProfit(this.getCurrentProfit());
    }
  }

  @Synchronized
  public void openPosition(final @NotNull Ticket ticket, final @NotNull OrderPosition position) {
    final LocalDateTime dateTime = ticket.dateTime();
    final double ask = ticket.ask();
    final double bid = ticket.bid();
    final int spread = ticket.spread();
    this.setOpenDateTime(dateTime);
    this.setLastUpdate(dateTime);
    this.setStatus(OrderStatus.OPEN);
    this.setPosition(position);
    if (OrderPosition.BUY.equals(position)) {
      this.setOpenPrice(ask);
      this.setClosePrice(bid);
    } else {
      this.setOpenPrice(bid);
      this.setClosePrice(ask);
    }
    this.setCurrentProfit(Math.negateExact(spread));
    this.setCurrentBalance(this.getCurrentBalance() + this.getCurrentProfit());
    this.setHighProfit(this.getCurrentProfit());
    this.setLowProfit(this.getCurrentProfit());
  }

  @Synchronized
  public void closePosition(final @NotNull OrderStatus status) {
    this.setStatus(status);
  }
}
