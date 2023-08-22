package br.eti.allandemiranda.forex.controllers.indicators;

import br.eti.allandemiranda.forex.controllers.indicators.trend.AceleradorOscilador;
import br.eti.allandemiranda.forex.controllers.indicators.trend.AverageDirectionalMovementIndex;
import br.eti.allandemiranda.forex.controllers.indicators.trend.MovingAverageConvergenceDivergence;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.exceptions.IndicatorsException;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.IndicatorService;
import br.eti.allandemiranda.forex.services.SignalService;
import br.eti.allandemiranda.forex.utils.IndicatorTrend;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class IndicatorsProcessor {

  private static final String ADX = "ADX";
  private static final String AC = "AC";
  private static final String MACD = "MACD";

  private final AverageDirectionalMovementIndex averageDirectionalMovementIndex;
  private final AceleradorOscilador aceleradorOscilador;
  private final MovingAverageConvergenceDivergence movingAverageConvergenceDivergence;
  private final IndicatorService indicatorService;
  private final SignalService signalService;
  private final CandlestickService candlestickService;

  @Autowired
  protected IndicatorsProcessor(final AverageDirectionalMovementIndex averageDirectionalMovementIndex, final AceleradorOscilador aceleradorOscilador,
      final MovingAverageConvergenceDivergence movingAverageConvergenceDivergence, final IndicatorService indicatorService, final SignalService signalService,
      final CandlestickService candlestickService) {
    this.averageDirectionalMovementIndex = averageDirectionalMovementIndex;
    this.aceleradorOscilador = aceleradorOscilador;
    this.movingAverageConvergenceDivergence = movingAverageConvergenceDivergence;
    this.indicatorService = indicatorService;
    this.signalService = signalService;
    this.candlestickService = candlestickService;
  }

  @PostConstruct
  public void init() {
    this.getIndicatorService().addIndicator(ADX, this.getAverageDirectionalMovementIndex());
    this.getIndicatorService().addIndicator(AC, this.getAceleradorOscilador());
    this.getIndicatorService().addIndicator(MACD, this.getMovingAverageConvergenceDivergence());
  }

  @Synchronized
  public void run() {
    final LocalDateTime candleDataTime = this.getCandlestickService().getCandlesticks(1).toArray(Candlestick[]::new)[0].candleDateTime();
    if (this.getCandlestickService().isReady() && this.getSignalService().getLastSignal().candleDataTime().isBefore(candleDataTime)) {
      this.getIndicatorService().getIndicators().entrySet().parallelStream().map(entry -> {
        Thread thread = new Thread(entry.getValue(), entry.getKey());
        thread.start();
        return thread;
      }).forEachOrdered(thread -> {
        try {
          thread.join();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IndicatorsException(e);
        }
      });
      final TreeMap<String, IndicatorTrend> trendSortedMap = this.getIndicatorService().getIndicators().entrySet().stream()
          .collect(Collectors.toMap(Entry::getKey, o -> o.getValue().getSignal(), (o1, o2) -> o1, TreeMap::new));
      final BigDecimal average = BigDecimal.valueOf(trendSortedMap.values().stream().mapToInt(indicator -> switch (indicator) {
        case SELL -> -1;
        case BUY -> 1;
        case NEUTRAL -> 0;
      }).sum());
      if (average.compareTo(BigDecimal.valueOf(-3)) == 0) {
        this.getSignalService().addGlobalSignal(candleDataTime, SignalTrend.STRONG_SELL);
      } else if (average.compareTo(BigDecimal.valueOf(3)) == 0) {
        this.getSignalService().addGlobalSignal(candleDataTime, SignalTrend.STRONG_BUY);
      } else {
        this.getSignalService().addGlobalSignal(candleDataTime, SignalTrend.NEUTRAL);
      }
      this.getSignalService().updateDebugFile(this.getCandlestickService().getCandlesticks(1).toArray(Candlestick[]::new)[0], trendSortedMap);
    }
  }
}

