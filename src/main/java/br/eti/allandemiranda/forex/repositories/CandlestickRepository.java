package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TreeSet;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
public class CandlestickRepository {

  private final TreeSet<CandlestickEntity> dataBase = new TreeSet<>();
  @Getter(AccessLevel.PUBLIC)
  private long numberOfBar = 0L;

  /**
   * Number of candlesticks necessary to start the data process
   */
  @Value("${candlestick.repository.memory:41}")
  @Getter(AccessLevel.PUBLIC)
  private int memorySize;

  /**
   * Check if the database contains the minimal number of candlesticks necessary to start the data use.
   *
   * @return If is ready to use the database
   */
  public boolean isReady() {
    return this.getDataBase().size() >= this.getMemorySize();
  }

  /**
   * To add new candlestick data on the database by DateTime.
   * If the DateTime exists, it means that the candlestick exists, so we will update the price on candlestick.
   * If not, we will create a new candlestick.
   *
   * @param candlestickDateTime Current time of candlestick
   * @param price               Current price of candlestick
   */
  @Synchronized
  public void add(final @NotNull LocalDateTime candlestickDateTime, final @NotNull BigDecimal price) {
    final CandlestickEntity entity = new CandlestickEntity();
    entity.setDateTime(candlestickDateTime);
    if (this.getDataBase().contains(entity)) {
      final CandlestickEntity older = this.getDataBase().first();
      older.setClose(price);
      if (older.getClose().compareTo(older.getHigh()) > 0) {
        older.setHigh(price);
      } else if (older.getClose().compareTo(older.getLow()) < 0) {
        older.setLow(price);
      }
    } else {
      entity.setHigh(price);
      entity.setLow(price);
      entity.setOpen(price);
      entity.setClose(price);
      this.getDataBase().add(entity);
      if (this.getDataBase().size() > this.getMemorySize()) {
        this.getDataBase().pollLast();
      }
      numberOfBar++;
    }
  }

  /**
   * Method to return the last candlesticks on DataBase with limit data
   *
   * @param size number of candlesticks
   * @return A Stream of candlesticks
   */
  public Stream<Candlestick> get(final int size) {
    return this.getDataBase().stream().limit(size).map(this::toModel);
  }

  /**
   * Method to return the last candlesticks on DataBase
   *
   * @return A Stream of candlesticks (the chart)
   */
  public Stream<Candlestick> get() {
    return this.getDataBase().stream().map(this::toModel);
  }

  /**
   * Convert an entity in a model
   *
   * @param entity The entity to be converted
   * @return The model
   */
  private @NotNull Candlestick toModel(final @NotNull CandlestickEntity entity) {
    return new Candlestick(entity.getDateTime(), entity.getOpen(), entity.getHigh(), entity.getLow(), entity.getClose());
  }

  /**
   * Get the last candlestick on the database (candlestick not close)
   *
   * @return The last candlestick
   */
  public @NotNull Candlestick getLastUpdate() {
    return this.toModel(this.getDataBase().first());
  }
}
