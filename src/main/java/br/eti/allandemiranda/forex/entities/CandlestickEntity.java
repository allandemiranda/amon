package br.eti.allandemiranda.forex.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class CandlestickEntity implements Serializable, DefaultEntity {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @Setter
  private LocalDateTime dateTime;
  private double open;
  private double high;
  private double low;
  private double close;

  public void setOpen(final double open) {
    this.open = open;
    this.high = open;
    this.low = open;
    this.close = open;
  }

  public void setClose(final double close) {
    this.close = close;
    if (this.getClose() > this.getHigh()) {
      this.high = close;
    } else if (this.getClose() < this.getLow()) {
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
}
