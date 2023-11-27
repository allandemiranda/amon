package br.eti.allandemiranda.forex.controllers.indicators;

import br.eti.allandemiranda.forex.enums.IndicatorTrend;
import org.jetbrains.annotations.NotNull;

public interface Indicator extends Runnable {

  @NotNull IndicatorTrend getSignal();
}
