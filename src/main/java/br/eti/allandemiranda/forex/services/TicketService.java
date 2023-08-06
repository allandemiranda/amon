package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.repositories.TicketRepository;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter(AccessLevel.PRIVATE)
public class TicketService {

  private final TicketRepository repository;

  @Value("${ticket.digits:5}")
  private int digits;

  @Autowired
  protected TicketService(final TicketRepository repository) {
    this.repository = repository;
  }

  @PostConstruct
  private void init() {
    this.getRepository().setDigits(this.getDigits());
  }

  public @NotNull Ticket getTicket() {
    return this.getRepository().getCurrentTicket();
  }

  public boolean isReady() {
    return this.getRepository().getCurrentTicket().bid().compareTo(BigDecimal.valueOf(0d)) > 0
        && this.getRepository().getCurrentTicket().ask().compareTo(BigDecimal.valueOf(0d)) > 0;
  }

  @Synchronized
  public void updateData(final @NotNull LocalDateTime dateTime, final @NotNull BigDecimal bid, final @NotNull BigDecimal ask) {
    if (dateTime.isAfter(this.getRepository().getCurrentTicket().dateTime())) {
      this.getRepository().update(dateTime, bid, ask);
    }
  }
}
