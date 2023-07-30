package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.ADX;
import br.eti.allandemiranda.forex.entities.ADXEntity;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
public class ADXRepository {

  private final ADXEntity data = new ADXEntity();

  @PostConstruct
  private void init() {
    this.getData().setDateTime(LocalDateTime.MIN);
  }

  public void add(final @NotNull LocalDateTime dateTime, final double adx, final double diPlus, final double diMinus) {
    if (dateTime.isAfter(this.getData().getDateTime())) {
      this.getData().setDateTime(dateTime);
      this.getData().setAdx(adx);
      this.getData().setDiPlus(diPlus);
      this.getData().setDiMinus(diMinus);
    }
  }

  public @NotNull ADX getADX() {
    return new ADX(this.getData().getDateTime(), this.getData().getAdx(), this.getData().getDiPlus(), this.getData().getDiMinus());
  }
}
