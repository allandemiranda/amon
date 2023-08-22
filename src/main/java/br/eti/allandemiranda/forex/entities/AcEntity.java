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
public class AcEntity implements Serializable, Comparable<AcEntity> {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  private LocalDateTime dateTime;
  private BigDecimal value;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AcEntity acEntity = (AcEntity) o;
    return Objects.equals(dateTime, acEntity.dateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dateTime);
  }

  @Override
  public int compareTo(@NotNull final AcEntity o) {
    return o.getDateTime().compareTo(this.getDateTime());
  }
}
