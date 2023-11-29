package br.eti.allandemiranda.forex.entities;

import br.eti.allandemiranda.forex.enums.OrderPosition;
import br.eti.allandemiranda.forex.enums.OrderStatus;
import br.eti.allandemiranda.forex.enums.SignalTrend;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity implements Serializable, Comparable<OrderEntity> {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  private LocalDateTime openDateTime;
  private LocalDateTime signalDateTime;
  private SignalTrend signalTrend;
  private LocalDateTime lastUpdateDateTime;
  private String timeOpen;
  private OrderStatus orderStatus;
  private OrderPosition orderPosition;
  private int tradingPerformanceDiff;
  private BigDecimal openPrice;
  private BigDecimal closePrice;
  private int highProfit;
  private int lowProfit;
  private int currentProfit;
  private BigDecimal swapProfit;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final OrderEntity that = (OrderEntity) o;
    return Objects.equals(openDateTime, that.openDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(openDateTime);
  }

  @Override
  public int compareTo(@NotNull final OrderEntity o) {
    return o.getOpenDateTime().compareTo(this.getOpenDateTime());
  }
}
