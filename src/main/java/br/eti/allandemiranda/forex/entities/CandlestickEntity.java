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
@Setter
@NoArgsConstructor
public class CandlestickEntity implements Serializable, Comparable<CandlestickEntity> {

  @Serial
  private static final long serialVersionUID = 1L;

  private LocalDateTime realDateTime;
  @Id
  private LocalDateTime candleDateTime;
  private BigDecimal open;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal close;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final CandlestickEntity entity = (CandlestickEntity) o;
    return Objects.equals(candleDateTime, entity.candleDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(candleDateTime);
  }

  @Override
  public int compareTo(@NotNull final CandlestickEntity o) {
    return o.getCandleDateTime().compareTo(this.getCandleDateTime());
  }
}
