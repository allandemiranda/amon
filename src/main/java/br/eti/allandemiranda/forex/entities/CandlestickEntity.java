package br.eti.allandemiranda.forex.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CandlestickEntity implements Serializable, DefaultEntity<CandlestickEntity> {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  private LocalDateTime dateTime;
  private double open;
  private double high;
  private double low;
  private double close;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CandlestickEntity entity = (CandlestickEntity) o;
    return Objects.equals(dateTime, entity.dateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dateTime);
  }
}
