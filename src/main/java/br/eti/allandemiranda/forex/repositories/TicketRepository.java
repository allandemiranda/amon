package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.entities.TicketEntity;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Slf4j
public class TicketRepository {

  private final TicketEntity data = new TicketEntity();

  @PostConstruct
  private void init() {
    this.getData().setDateTime(LocalDateTime.MIN);
    this.getData().setBid(0d);
    this.getData().setAsk(0d);
  }

  @Synchronized
  public void update(final @NotNull Ticket ticket) {
    final LocalDateTime ticketDateTime = this.getData().getDateTime();
    if (ticketDateTime.isBefore(ticket.dateTime())) {
        this.getData().setDateTime(ticket.dateTime());
        if (ticket.bid() > 0d) {
          this.getData().setBid(ticket.bid());
        }
        if (ticket.ask() > 0d) {
          this.getData().setAsk(ticket.ask());
        }
    } else {
      log.warn("DataTime {} from new ticket before or equal that current DataTime {}", ticket.dateTime().format(DateTimeFormatter.ISO_DATE_TIME),
          ticketDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
    }
  }

  public @NotNull Ticket getCurrentTicket() {
    return new Ticket(this.getData().getDateTime(), this.getData().getBid(), this.getData().getAsk());
  }
}
