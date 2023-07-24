package br.eti.allandemiranda.forex.controllers.indicators;

import br.eti.allandemiranda.forex.controllers.indicators.trend.AverageDirectionalMovementIndex;
import br.eti.allandemiranda.forex.controllers.indicators.trend.AverageTrueRange;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
public class IndicatorsProcessor {

  private final AverageDirectionalMovementIndex averageDirectionalMovementIndex;
  private final AverageTrueRange averageTrueRange;
  private final Map<String, Indicator> indicators = new HashMap<>();
  @Value("${indicators.run.min}")
  private Integer interval;
  private LocalDateTime lastTime = LocalDateTime.MIN;

  @Autowired
  private IndicatorsProcessor(final AverageDirectionalMovementIndex averageDirectionalMovementIndex, final AverageTrueRange averageTrueRange) {
    this.averageDirectionalMovementIndex = averageDirectionalMovementIndex;
    this.averageTrueRange = averageTrueRange;
  }

  @PostConstruct
  public void init() {
    this.indicators.put("ADX", averageDirectionalMovementIndex);
    this.indicators.put("TEST", averageTrueRange);
  }

  private @NotNull LocalDateTime getLastTime() {
    return this.lastTime;
  }

  private void setLastTime(final @NotNull LocalDateTime newLastTime) {
    this.lastTime = newLastTime;
  }

  public void run(final @NotNull LocalDateTime realTime) {
    if (ChronoUnit.MINUTES.between(this.getLastTime(), realTime) >= interval) {
      this.setLastTime(realTime);
      indicators.values().parallelStream().forEach(Indicator::run);
    }
  }
}
