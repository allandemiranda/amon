package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Slf4j
public class CandlestickRepository {

  private final TreeSet<CandlestickEntity> dataBase = new TreeSet<>();

  @Value("${candlestick.repository.memory:15}")
  @Getter(AccessLevel.PUBLIC)
  private int memorySize;

  public boolean isReady() {
    return this.getDataBase().size() >= this.getMemorySize();
  }

  @Synchronized
  public void addCandlestick(final @NotNull LocalDateTime realDataTime, final @NotNull LocalDateTime dateTime, final double price) {
    if (this.getDataBase().isEmpty() || dateTime.isAfter(this.getDataBase().last().getDateTime())) {
      final CandlestickEntity entity = new CandlestickEntity();
      entity.setOpen(realDataTime, dateTime, price);
      this.getDataBase().add(entity);
      if (this.getDataBase().size() > this.getMemorySize()) {
        final CandlestickEntity older = this.getDataBase().first();
        this.getDataBase().remove(older);
      }
    } else if (dateTime.equals(this.getDataBase().last().getDateTime())) {
      this.getDataBase().last().setClose(realDataTime, price);
    } else {
      log.warn("Trying to add a old Candlestick on repository");
    }
  }

  public Candlestick @NotNull [] getCandlesticks() {
    return this.getDataBase().stream().map(this::toModel).toArray(Candlestick[]::new);
  }

  private @NotNull Candlestick toModel(final @NotNull CandlestickEntity output) {
    return new Candlestick(output.getRealDateTime(), output.getDateTime(), output.getOpen(), output.getHigh(), output.getLow(), output.getClose());
  }

  public Candlestick getLastCandlestick() {
    try {
      return this.toModel(this.getDataBase().last());
    } catch (NoSuchElementException e) {
      return null;
    }
  }
}
