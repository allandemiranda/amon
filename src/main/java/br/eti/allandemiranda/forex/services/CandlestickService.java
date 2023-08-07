package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
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
    final LocalDateTime realDataTime = ticket.dateTime();
    this.getRepository().add(realDataTime, candlestickDateTime, price);
  }

  public Candlestick @NotNull [] getCandlesticks() {
    return this.getRepository().get();
  }

  public boolean isReady() {
    return this.getRepository().isReady();
  }

  public @NotNull Candlestick getOldestCandlestick() {
    return this.getRepository().getLastUpdate();
  }

  public BigDecimal @NotNull [] getSMA(final Function<Candlestick[], BigDecimal> function, final int inputSize, final int period) {
    final Candlestick[] candlesticks = this.getRepository().get();
    return IntStream.range(0, candlesticks.length).mapToObj(i -> {
      try {
        return IntStream.range(i, period + i).mapToObj(j -> {
          try {
            final Candlestick[] tmp = Arrays.stream(candlesticks, j, j + inputSize).toArray(Candlestick[]::new);
            return function.apply(tmp);
          } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            throw new IllegalStateException();
          }
        }).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(period), 10, RoundingMode.HALF_UP);
      } catch (IllegalStateException e) {
        return null;
      }
    }).filter(Objects::nonNull).toArray(BigDecimal[]::new);
  }
}
