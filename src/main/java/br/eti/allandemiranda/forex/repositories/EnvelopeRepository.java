package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Envelopes;
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
public class EnvelopeRepository {

  private LocalDateTime dateTime;
  private BigDecimal upperBand;
  private BigDecimal lowerBand;

  @PostConstruct
  private void init() {
    this.setDateTime(LocalDateTime.MIN);
  }

  public void add(final @NotNull LocalDateTime dateTime, final @NotNull BigDecimal upperBand, final @NotNull BigDecimal lowerBand) {
    this.setDateTime(dateTime);
    this.setUpperBand(upperBand);
    this.setLowerBand(lowerBand);
  }

  public @NotNull Envelopes get() {
    return new Envelopes(this.getDateTime(), this.getUpperBand(), this.getLowerBand());
  }
}
