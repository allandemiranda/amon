package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.repositories.TicketRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter(AccessLevel.PRIVATE)
@Slf4j
public class TicketService {

  private final TicketRepository repository;

  @Autowired
  protected TicketService(final TicketRepository repository) {
    this.repository = repository;
  }

  public @NotNull Ticket getTicket() {
    return this.getRepository().getCurrentTicket();
  }

  public boolean isReady() {
    return this.getRepository().getCurrentTicket().bid().compareTo(BigDecimal.ZERO) > 0 && this.getRepository().getCurrentTicket().ask().compareTo(BigDecimal.ZERO) > 0;
  }

  @Synchronized
  public void updateData(final @NotNull LocalDateTime dateTime, final double bid, final double ask) {
    if (dateTime.isAfter(this.getRepository().getCurrentTicket().dateTime())) {
      this.getRepository().update(dateTime, bid, ask);
    } else {
      log.warn("Bad input ticket dataTime={} bid={} ask={}", dateTime.format(DateTimeFormatter.ISO_DATE_TIME), bid, ask);
    }
  }
}
