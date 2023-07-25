package br.eti.allandemiranda.forex.controllers.indicators;

import org.jetbrains.annotations.NotNull;

public interface Indicator {
  boolean run();
  @NotNull SignalTrend getSignal();
}
