package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.RSI;
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
public class RsiRepository {

  private LocalDateTime dateTime;
  private BigDecimal value;

  @PostConstruct
  private void init() {
    this.setDateTime(LocalDateTime.MIN);
  }

  public void add(final @NotNull LocalDateTime dataTime, final @NotNull BigDecimal cci) {
    this.setDateTime(dataTime);
    this.setValue(cci);
  }

  public @NotNull RSI get() {
    return new RSI(this.getDateTime(), this.getValue());
  }
}
