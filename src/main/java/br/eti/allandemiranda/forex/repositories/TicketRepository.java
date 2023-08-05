package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Ticket;
import jakarta.annotation.PostConstruct;
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
  private double bid;
  private double ask;
  private int spread;
  @Setter(AccessLevel.PUBLIC)
  private int digits;

  private static int getPoints(final double price, final int digits) {
    return (int) ((price) / (1 / (Math.pow(10, digits))));
  }

  @PostConstruct
  private void init() {
    this.setDateTime(LocalDateTime.MIN);
  }

  @Synchronized
  public void update(final @NotNull LocalDateTime dateTime, final double bid, final double ask) {
    this.setDateTime(dateTime);
    if (bid > 0d) {
      this.setBid(bid);
    }
    if (ask > 0d) {
      this.setAsk(ask);
    }
    this.setSpread(getPoints((this.getAsk() - this.getBid()), this.getDigits()));
  }

  public @NotNull Ticket getCurrentTicket() {
    return new Ticket(this.getDateTime(), this.getBid(), this.getAsk(), this.getSpread(), this.getDigits());
  }
}
