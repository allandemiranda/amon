package br.eti.allandemiranda.forex.controllers.indicators;

import org.apache.commons.lang3.tuple.Pair;

public interface Indicator {

  void runCalculate();
  SignalTrend getSignal();
}
