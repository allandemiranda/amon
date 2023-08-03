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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
public class OrderRepository {

  private final OrderEntity data = new OrderEntity();

  @PostConstruct
  private void init() {
    this.getData().setStatus(OrderStatus.CLOSE_MANUAL);
    this.getData().setOpenDateTime(LocalDateTime.MIN);
    this.getData().setLastUpdate(LocalDateTime.MIN);
    this.getData().setCurrentBalance(0d);
  }

  public @NotNull Order getLastOrder() {
    return new Order(this.getData().getOpenDateTime(), this.getData().getLastUpdate(), this.getData().getStatus(), this.getData().getPosition(),
        this.getData().getOpenPrice(), this.getData().getClosePrice(), this.getData().getProfit(), this.getData().getCurrentBalance());
  }

  @Synchronized
  public void updateOpenPosition(final @NotNull Ticket ticket) {
    this.getData().setLastUpdate(ticket.dateTime(), OrderStatus.OPEN, ticket.bid(), ticket.ask());
  }

  @Synchronized
  public void openPosition(final @NotNull Ticket ticket, final @NotNull OrderPosition position) {
    this.getData().setOpenDateTime(ticket.dateTime(), position, ticket.bid(), ticket.ask());
  }

  @Synchronized
  public void closePosition(final @NotNull OrderStatus status) {
    this.getData().setStatus(status);
  }
}
