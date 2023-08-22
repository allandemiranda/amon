package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
public class IndicatorRepository {

  private final TreeMap<String, Indicator> dataBase = new TreeMap<>();

  @Synchronized
  public void add(final @NotNull String name, final @NotNull Indicator signal) {
    this.getDataBase().put(name, signal);
  }

  @Synchronized
  public @NotNull SortedMap<String, Indicator> get() {
    return this.getDataBase();
  }
}
