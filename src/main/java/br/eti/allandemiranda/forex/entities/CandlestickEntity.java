package br.eti.allandemiranda.forex.entities;

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
@NoArgsConstructor
public class CandlestickEntity implements Serializable, Comparable<CandlestickEntity> {

  @Serial
  private static final long serialVersionUID = 1L;

  private LocalDateTime realDateTime;
  @Id
  @Setter
  private LocalDateTime dateTime;
  private BigDecimal open;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal close;

  public void setOpen(final @NotNull LocalDateTime realDateTime, final @NotNull LocalDateTime dateTime, final @NotNull BigDecimal open) {
    this.realDateTime = realDateTime;
    this.dateTime = dateTime;
    this.open = open;
    this.high = open;
    this.low = open;
    this.close = open;
  }

  public void setClose(final @NotNull LocalDateTime realDateTime, final @NotNull BigDecimal close) {
    this.realDateTime = realDateTime;
    this.close = close;
    if (this.getClose().compareTo(this.getHigh()) > 0) {
      this.high = close;
    } else if (this.getClose().compareTo(this.getLow()) < 0) {
      this.low = close;
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final CandlestickEntity entity = (CandlestickEntity) o;
    return Objects.equals(dateTime, entity.dateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dateTime);
  }

  @Override
  public int compareTo(@NotNull final CandlestickEntity o) {
    return this.getDateTime().compareTo(o.getDateTime());
  }
}
