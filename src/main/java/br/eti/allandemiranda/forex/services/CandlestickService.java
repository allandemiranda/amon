package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.exceptions.ServiceException;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;
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
    final BigDecimal price = ticket.bid();
    this.getRepository().add(candlestickDateTime, price);
  }

  public Stream<Candlestick> getCandlesticks(final int period) {
    if (this.getRepository().getMemorySize() >= period) {
      return this.getRepository().get(period);
    } else {
      throw new ServiceException("Can't get a Candlesticks period mode high that the memory");
    }
  }

  public boolean isReady() {
    return this.getRepository().isReady();
  }

  //TODO: remove this method
  public @NotNull Candlestick getCurrentCandlestick() {
    return this.getRepository().getLastUpdate();
  }

  public @NotNull Candlestick getLastCandlestick() {
    return this.getCandlesticks(1).toArray(Candlestick[]::new)[0];
  }
}
