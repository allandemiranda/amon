package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
public class IndicatorRepository {

  @Getter(AccessLevel.PRIVATE)
  private final HashMap<String, Indicator> indicators = new HashMap<>();

  @Synchronized
  public void add(final @NotNull String name, final @NotNull Indicator signal) {
    this.getIndicators().put(name, signal);
  }

  @Synchronized
  public @NotNull Map<String, SignalTrend> processAndGetSignals() {
    return this.getIndicators().entrySet().parallelStream().filter(entry -> entry.getValue().run())
        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getCurrentSignal()));
  }

  public @NotNull Collection<String> getNames() {
    return this.getIndicators().keySet();
  }
}
