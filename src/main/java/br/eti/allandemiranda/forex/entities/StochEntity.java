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
public class StochEntity implements Serializable, Comparable<StochEntity> {

  @Serial
  private static final long serialVersionUID = 1L;

  private LocalDateTime realDateTime;
  @Id
  private LocalDateTime dateTime;
  private BigDecimal main;
  private BigDecimal signal;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final StochEntity that = (StochEntity) o;
    return Objects.equals(dateTime, that.dateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dateTime);
  }

  @Override
  public int compareTo(@NotNull final StochEntity o) {
    return o.getDateTime().compareTo(this.getDateTime());
  }
}
