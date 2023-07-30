package br.eti.allandemiranda.forex.utils;

public enum SignalTrend {
  STRONG_SELL(-2), SELL(-1), NEUTRAL(0), BUY(1), STRONG_BUY(2), OUT(0);

  public final int power;

  SignalTrend(final int power) {
    this.power = power;
  }
}
