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
  /**
   * Number of digits of currency pairs
   */
  @Value("${ticket.digits:5}")
  @Setter(AccessLevel.NONE)
  private int digits;

  /**
   * Convert the price in points
   *
   * @param price  The price
   * @param digits The number of digits
   * @return The number of points
   */
  private static int getPoints(final @NotNull BigDecimal price, final int digits) {
    return price.multiply(BigDecimal.valueOf(Math.pow(10, digits))).intValue();
  }

  /**
   * Default values to be instanced
   */
  @PostConstruct
  private void init() {
    this.setDateTime(LocalDateTime.MIN);
    this.setBid(BigDecimal.ZERO);
    this.setAsk(BigDecimal.ZERO);
  }

  /**
   * Provide the update of ticket
   *
   * @param dateTime The new date time
   * @param bid      The new BID (upper more than zero)
   * @param ask      The new ASK (upper more than zero)
   */
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

  /**
   * Get the current ticket
   *
   * @return The current ticket
   */
  public @NotNull Ticket getCurrentTicket() {
    return new Ticket(this.getDateTime(), this.getBid(), this.getAsk(), this.getSpread(), this.getDigits());
  }
}
