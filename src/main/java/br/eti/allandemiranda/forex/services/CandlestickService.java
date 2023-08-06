package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.exceptions.ServiceException;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter(AccessLevel.PRIVATE)
public class CandlestickService {

  private final CandlestickRepository repository;

  @Autowired
  protected CandlestickService(final CandlestickRepository repository) {
    this.repository = repository;
  }

  public void addTicket(final @NotNull Ticket ticket, final @NotNull LocalDateTime candlestickDateTime) {
    this.getRepository().addCandlestick(ticket.dateTime(), candlestickDateTime, ticket.bid());
  }

  public Candlestick @NotNull [] getCandlesticks(final int periodNum) {
    if (this.getRepository().getMemorySize() >= periodNum) {
      return Arrays.stream(this.getRepository().getCandlesticks(), this.getRepository().getMemorySize() - periodNum, this.getRepository().getMemorySize())
          .toArray(Candlestick[]::new);
    }
    throw new ServiceException("Can't provide Candlesticks to the period requested");
  }

  public boolean isReady() {
    return this.getRepository().isReady();
  }

  public @NotNull Candlestick getLastCandlestick() {
    return this.getRepository().getLastCandlestick();
  }
}
