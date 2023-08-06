package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.ADX;
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
public class ADXRepository {

  private LocalDateTime dateTime;
  private BigDecimal value;
  private BigDecimal diPlus;
  private BigDecimal diMinus;

  @PostConstruct
  private void init() {
    this.setDateTime(LocalDateTime.MIN);
  }

  public void add(final @NotNull LocalDateTime dateTime, final @NotNull BigDecimal adx, final @NotNull BigDecimal diPlus, final @NotNull BigDecimal diMinus) {
    if (dateTime.isAfter(this.getDateTime())) {
      this.setDateTime(dateTime);
      this.setValue(adx);
      this.setDiPlus(diPlus);
      this.setDiMinus(diMinus);
    }
  }

  public @NotNull ADX get() {
    return new ADX(this.getDateTime(), this.getValue(), this.getDiPlus(), this.getDiMinus());
  }
}
