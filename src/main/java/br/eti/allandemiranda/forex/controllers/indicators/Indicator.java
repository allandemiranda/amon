package br.eti.allandemiranda.forex.controllers.indicators;

import br.eti.allandemiranda.forex.utils.SignalTrend;
import org.jetbrains.annotations.NotNull;

public interface Indicator {

  boolean run();

  @NotNull SignalTrend getSignal();
}
