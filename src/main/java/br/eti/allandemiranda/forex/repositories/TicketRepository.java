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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class TicketRepository {

  private LocalDateTime dateTime;
  private BigDecimal bid;
  private BigDecimal ask;
  private int spread;
  @Value("${ticket.digits:5}")
  @Setter(AccessLevel.NONE)
  private int digits;

  private static int getPoints(final @NotNull BigDecimal price, final int digits) {
    return price.multiply(BigDecimal.valueOf(Math.pow(10, digits))).intValue();
  }

  @PostConstruct
  private void init() {
    this.setDateTime(LocalDateTime.MIN);
    this.setBid(BigDecimal.ZERO);
    this.setAsk(BigDecimal.ZERO);
  }

  @Synchronized
  public void update(final @NotNull LocalDateTime dateTime, final double bid, final double ask) {
    this.setDateTime(dateTime);
    if (bid > 0d) {
      this.setBid(BigDecimal.valueOf(bid).setScale(this.getDigits(), RoundingMode.DOWN));
    }
    if (ask > 0d) {
      this.setAsk(BigDecimal.valueOf(ask).setScale(this.getDigits(), RoundingMode.DOWN));
    }
    this.setSpread(getPoints((this.getAsk().subtract(this.getBid())), this.getDigits()));
  }

  public @NotNull Ticket getCurrentTicket() {
    return new Ticket(this.getDateTime(), this.getBid(), this.getAsk(), this.getSpread(), this.getDigits());
  }
}
