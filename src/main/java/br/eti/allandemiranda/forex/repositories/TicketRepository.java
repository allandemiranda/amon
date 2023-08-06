package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Ticket;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class TicketRepository {

  private LocalDateTime dateTime;
  private BigDecimal bid;
  private BigDecimal ask;
  private int spread;
  @Setter(AccessLevel.PUBLIC)
  private int digits;

  private static int getPoints(final @NotNull BigDecimal price, final int digits) {
    return price.multiply(BigDecimal.valueOf(Math.pow(10, digits))).intValue();
  }

  @PostConstruct
  private void init() {
    this.setDateTime(LocalDateTime.MIN);
    this.setBid(BigDecimal.valueOf(Double.MIN_VALUE).setScale(this.getDigits(), RoundingMode.DOWN));
    this.setAsk(BigDecimal.valueOf(Double.MIN_VALUE).setScale(this.getDigits(), RoundingMode.DOWN));
  }

  @Synchronized
  public void update(final @NotNull LocalDateTime dateTime, final @NotNull BigDecimal bid, final @NotNull BigDecimal ask) {
    this.setDateTime(dateTime);
    if (bid.compareTo(BigDecimal.ZERO) > 0) {
      this.setBid(bid.setScale(this.getDigits(), RoundingMode.DOWN));
    }
    if (ask.compareTo(BigDecimal.ZERO) > 0) {
      this.setAsk(ask.setScale(this.getDigits(), RoundingMode.DOWN));
    }
    this.setSpread(getPoints((this.getAsk().subtract(this.getBid())), this.getDigits()));
  }

  public @NotNull Ticket getCurrentTicket() {
    return new Ticket(this.getDateTime(), this.getBid(), this.getAsk(), this.getSpread(), this.getDigits());
  }
}
