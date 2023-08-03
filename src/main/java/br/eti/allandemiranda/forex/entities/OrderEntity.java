package br.eti.allandemiranda.forex.entities;

import br.eti.allandemiranda.forex.utils.OrderPosition;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private LocalDateTime openDateTime;
  @Id
  private LocalDateTime lastUpdate;
  private OrderStatus status;
  private OrderPosition position;
  private double openPrice;
  private double closePrice;
  private double profit;
  private double currentBalance;

  public void setLastUpdate(final @NotNull LocalDateTime lastUpdate, final @NotNull OrderStatus status, final double bid, final double ask) {
    this.lastUpdate = lastUpdate;
    this.status = status;
    if (this.position.equals(OrderPosition.BUY)) {
      this.currentBalance += bid - this.closePrice;
      this.closePrice = bid;
      this.profit = this.closePrice - this.openPrice;
    } else {
      this.currentBalance += this.closePrice - ask;
      this.closePrice = ask;
      this.profit = this.openPrice - this.closePrice;
    }
  }

  public void setOpenDateTime(final @NotNull LocalDateTime openDateTime, final @NotNull OrderPosition position, final double bid, final double ask) {
    this.openDateTime = openDateTime;
    this.lastUpdate = openDateTime;
    this.status = OrderStatus.OPEN;
    this.position = position;
    if (this.position.equals(OrderPosition.BUY)) {
      this.openPrice = ask;
      this.closePrice = bid;
      this.profit = this.closePrice - this.openPrice;
    } else {
      this.openPrice = bid;
      this.closePrice = ask;
      this.profit = this.openPrice - this.closePrice;
    }
    this.currentBalance += this.profit;
  }
}
