package br.eti.allandemiranda.forex.controllers.indicators;

import org.jetbrains.annotations.NotNull;

public interface Indicator {
  void run();
  @NotNull SignalTrend getSignal();
}
