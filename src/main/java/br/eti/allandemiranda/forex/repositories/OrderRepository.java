package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Order;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
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
  private LocalDateTime openCandleDateTime;
  private LocalDateTime lastCandleUpdate;
  private OrderStatus status;
  private OrderPosition position;
  private BigDecimal openPrice;
  private BigDecimal closePrice;
  private int highProfit;
  private int lowProfit;
  private int currentProfit;
  private int currentBalance;

  private static int getPoints(final @NotNull BigDecimal price, final int digits) {
    return price.multiply(BigDecimal.valueOf(Math.pow(10, digits))).intValue();
  }

  @PostConstruct
  private void init() {
    this.setStatus(OrderStatus.CLOSE_MANUAL);
    this.setOpenDateTime(LocalDateTime.MIN);
    this.setLastUpdate(LocalDateTime.MIN);
    this.setOpenCandleDateTime(LocalDateTime.MIN);
    this.setLastCandleUpdate(LocalDateTime.MIN);
  }

  public @NotNull Order getLastOrder() {
    return new Order(this.getOpenDateTime(), this.getLastUpdate(), this.getOpenCandleDateTime(), this.getLastCandleUpdate(), this.getStatus(), this.getPosition(),
        this.getOpenPrice(), this.getClosePrice(), this.getHighProfit(), this.getLowProfit(), this.getCurrentProfit(), this.getCurrentBalance());
  }

  @Synchronized
  public void updateOpenPosition(final @NotNull Ticket ticket, final @NotNull LocalDateTime candleDataTime) {
    final int digits = ticket.digits();
    this.setLastUpdate(ticket.dateTime());
    this.setLastCandleUpdate(candleDataTime);
    if (OrderPosition.BUY.equals(this.getPosition())) {
      final BigDecimal bid = ticket.bid();
      this.setCurrentBalance(this.getCurrentBalance() + getPoints(bid.subtract(this.getClosePrice()), digits));
      this.setClosePrice(bid);
      this.setCurrentProfit(getPoints(bid.subtract(this.getOpenPrice()), digits));
    } else {
      final BigDecimal ask = ticket.ask();
      this.setCurrentBalance(this.getCurrentBalance() + getPoints(this.getClosePrice().subtract(ask), digits));
      this.setClosePrice(ask);
      this.setCurrentProfit(getPoints(this.getOpenPrice().subtract(ask), digits));
    }
    if (this.getCurrentProfit() > this.getHighProfit()) {
      this.setHighProfit(this.getCurrentProfit());
    }
    if (this.getCurrentProfit() < this.getLowProfit()) {
      this.setLowProfit(this.getCurrentProfit());
    }
  }

  @Synchronized
  public void openPosition(final @NotNull Ticket ticket, final @NotNull LocalDateTime candleDataTime, final @NotNull OrderPosition position) {
    final LocalDateTime dateTime = ticket.dateTime();
    final BigDecimal ask = ticket.ask();
    final BigDecimal bid = ticket.bid();
    final int spread = ticket.spread();
    this.setOpenDateTime(dateTime);
    this.setLastUpdate(dateTime);
    this.setOpenCandleDateTime(candleDataTime);
    this.setLastCandleUpdate(candleDataTime);
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
