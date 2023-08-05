package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.ADX;
import jakarta.annotation.PostConstruct;
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
  private double value;
  private double diPlus;
  private double diMinus;

  @PostConstruct
  private void init() {
    this.setDateTime(LocalDateTime.MIN);
  }

  public void add(final @NotNull LocalDateTime dateTime, final double adx, final double diPlus, final double diMinus) {
    if (dateTime.isAfter(this.getDateTime())) {
      this.setDateTime(dateTime);
      this.setValue(adx);
      this.setDiPlus(diPlus);
      this.setDiMinus(diMinus);
    }
  }

  public @NotNull ADX getADX() {
    return new ADX(this.getDateTime(), this.getValue(), this.getDiPlus(), this.getDiMinus());
  }
}
