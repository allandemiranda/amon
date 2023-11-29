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
public class AdxRepository {

  private LocalDateTime dateTime;
  private BigDecimal value;
  private BigDecimal diPlus;
  private BigDecimal diMinus;

  @PostConstruct
  private void init() {
    this.setDateTime(LocalDateTime.MIN);
  }

  /**
   * Add a ADX indicator to the data base
   *
   * @param dateTime The date time
   * @param adx      The ADX value
   * @param diPlus   The di+ value
   * @param diMinus  The di- value
   */
  public void add(final @NotNull LocalDateTime dateTime, final @NotNull BigDecimal adx, final @NotNull BigDecimal diPlus, final @NotNull BigDecimal diMinus) {
    this.setDateTime(dateTime);
    this.setValue(adx);
    this.setDiPlus(diPlus);
    this.setDiMinus(diMinus);
  }

  /**
   * Get last ADX value on the database
   *
   * @return The last ADX value
   */
  public @NotNull ADX get() {
    return new ADX(this.getDateTime(), this.getValue(), this.getDiPlus(), this.getDiMinus());
  }
}
