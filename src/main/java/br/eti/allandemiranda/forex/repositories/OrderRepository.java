package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Order;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.entities.OrderEntity;
import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Slf4j
public class OrderRepository {

  private final OrderEntity data = new OrderEntity();

  @PostConstruct
  private void init() {
    this.getData().setStatus(OrderStatus.CLOSE_MANUAL);
    this.getData().setCurrentBalance(0d);
  }

  public @NotNull Order getLastOrder() {
    return new Order(this.getData().getOpenDateTime(), this.getData().getLastUpdate(), this.getData().getStatus(), this.getData().getPosition(),
        this.getData().getOpenPrice(), this.getData().getClosePrice(), this.getData().getProfit(), this.getData().getCurrentBalance());
  }

  @Synchronized
  public void updateTicket(final @NotNull Ticket ticket) {
    if (this.getData().getStatus().equals(OrderStatus.OPEN)) {
      this.getData().setLastUpdate(ticket.dateTime(), OrderStatus.OPEN, ticket.bid(), ticket.ask());
    }
  }

  @Synchronized
  public void openPosition(final @NotNull Ticket ticket, final @NotNull OrderPosition position) {
    final LocalDateTime openDateTime = ticket.dateTime();
    if (openDateTime.isBefore(this.getData().getLastUpdate())) {
      log.warn("Trying to open a position with a last update data before current one");
    } else if (this.getData().getStatus().equals(OrderStatus.OPEN)) {
      log.warn("Trying to open a position that currently is open too");
    } else {
      this.getData().setOpenDateTime(openDateTime, position, ticket.bid(), ticket.ask());
    }
  }

  @Synchronized
  public void closePosition(final @NotNull Ticket ticket, final @NotNull OrderStatus status) {
    final LocalDateTime lastUpdate = ticket.dateTime();
    if (lastUpdate.isBefore(this.getData().getLastUpdate())) {
      log.warn("Trying to close a position with a last update data before current one");
    } else if (!this.getData().getStatus().equals(OrderStatus.OPEN)) {
      log.warn("Trying to close a position that currently is closed too");
    } else if (!status.equals(OrderStatus.OPEN)) {
      this.getData().setLastUpdate(lastUpdate, status, ticket.bid(), ticket.ask());
    } else {
      log.warn("Trying to close a position with a open input");
    }
  }
}
