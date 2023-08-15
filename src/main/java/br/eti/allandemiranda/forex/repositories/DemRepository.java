package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.DeM;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class DemRepository {

  private LocalDateTime dateTime;
  private BigDecimal value;

  @PostConstruct
  private void init() {
    this.setDateTime(LocalDateTime.MIN);
  }

  public void add(final @NotNull LocalDateTime dataTime, final @NotNull BigDecimal dMark) {
    this.setDateTime(dataTime);
    this.setValue(dMark);
  }

  public @NotNull DeM get() {
    return new DeM(this.getDateTime(), this.getValue());
  }
}
