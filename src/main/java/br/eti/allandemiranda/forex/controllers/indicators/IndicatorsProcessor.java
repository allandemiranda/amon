package br.eti.allandemiranda.forex.controllers.indicators;

import br.eti.allandemiranda.forex.controllers.indicators.trend.AverageDirectionalMovementIndex;
import br.eti.allandemiranda.forex.controllers.indicators.trend.AverageTrueRange;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.IndicatorsService;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
public class IndicatorsProcessor {

  private final AverageDirectionalMovementIndex averageDirectionalMovementIndex;
  private final AverageTrueRange averageTrueRange;
  private final Map<String, Indicator> indicators = new HashMap<>();
  private final IndicatorsService indicatorsService;
  private final CandlestickService candlestickService;

  @Value("${indicators.run.min}")
  private Integer interval;
  private LocalDateTime lastTime = LocalDateTime.MIN;

  @Autowired
  private IndicatorsProcessor(final AverageDirectionalMovementIndex averageDirectionalMovementIndex, final AverageTrueRange averageTrueRange,
      final IndicatorsService indicatorsService, final CandlestickService candlestickService) {
    this.averageDirectionalMovementIndex = averageDirectionalMovementIndex;
    this.averageTrueRange = averageTrueRange;
    this.indicatorsService = indicatorsService;
    this.candlestickService = candlestickService;
  }

  @PostConstruct
  public void init() {
    this.indicators.put("ADX", averageDirectionalMovementIndex);
    this.indicators.put("TEST", averageTrueRange);
    this.indicatorsService.printHeaders(Stream.concat(Stream.of("dataTime", "price"), this.indicators.keySet().stream()).toArray());
  }

  private @NotNull LocalDateTime getLastTime() {
    return this.lastTime;
  }

  private void setLastTime(final @NotNull LocalDateTime newLastTime) {
    this.lastTime = newLastTime;
  }

  @Synchronized
  public void run(final @NotNull Ticket ticket) {
    LocalDateTime ticketDataTime = ticket.dateTime();
    if (ChronoUnit.MINUTES.between(this.getLastTime(), ticketDataTime) >= interval) {
      this.setLastTime(ticketDataTime);
      indicators.entrySet().parallelStream().map(entry -> {
        if (entry.getValue().run()) {
          return entry;
        } else {
          return null;
        }
      }).filter(Objects::nonNull).forEach(entry -> this.indicatorsService.add(entry.getKey(), entry.getValue().getSignal()));
      this.indicatorsService.updateFile(ticketDataTime, this.candlestickService.getLastCloseValue());
    }
  }
}
