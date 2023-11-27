package br.eti.allandemiranda.forex.services;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter(AccessLevel.PRIVATE)
public class TradingPerformanceService {

  @Getter(AccessLevel.PUBLIC)
  @Value("${trading.parameters.active:true}")
  private boolean active;
  @Getter(AccessLevel.PUBLIC)
  @Value("${trading.parameters.exponential.period:9}")
  private int exponentialPeriod;
  @Getter(AccessLevel.PUBLIC)
  @Value("${trading.parameters.simple.period:21}")
  private int simplePeriod;
  @Getter(AccessLevel.PUBLIC)
  @Value("${candlestick.repository.memory:41}")
  private int memorySize;

}
