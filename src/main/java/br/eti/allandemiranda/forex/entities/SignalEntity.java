package br.eti.allandemiranda.forex.entities;

import br.eti.allandemiranda.forex.utils.SignalTrend;
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
@Setter
@NoArgsConstructor
public class SignalEntity implements Serializable, DefaultEntity {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  private LocalDateTime dateTime;
  private SignalTrend trend;
  private double price;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SignalEntity entity = (SignalEntity) o;
    return Objects.equals(dateTime, entity.dateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dateTime);
  }
}
