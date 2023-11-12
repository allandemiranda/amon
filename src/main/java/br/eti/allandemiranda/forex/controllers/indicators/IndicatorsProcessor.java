package br.eti.allandemiranda.forex.controllers.indicators;

import br.eti.allandemiranda.forex.controllers.indicators.trend.AceleradorOscilador;
import br.eti.allandemiranda.forex.controllers.indicators.trend.AverageDirectionalMovementIndex;
import br.eti.allandemiranda.forex.controllers.indicators.trend.MovingAverageConvergenceDivergence;
import br.eti.allandemiranda.forex.exceptions.IndicatorsException;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.IndicatorService;
import br.eti.allandemiranda.forex.services.SignalService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
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

  /**
   * Default init values
   */
  @PostConstruct
  public void init() {
    this.getIndicatorService().addIndicator(ADX, this.getAverageDirectionalMovementIndex());
    this.getIndicatorService().addIndicator(AC, this.getAceleradorOscilador());
    this.getIndicatorService().addIndicator(MACD, this.getMovingAverageConvergenceDivergence());
  }

  /**
   * Tread processor
   */
  @Synchronized
  public void run() {
    if (this.getCandlestickService().isReady()) {
      final LocalDateTime lastCandleDataTime = this.getCandlestickService().getLastCloseCandlestick().dateTime();
      if (this.getSignalService().getLastSignal().dataTime().isBefore(lastCandleDataTime)) {
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
        final int signalSum = this.getIndicatorService().getIndicators().values().stream().map(Indicator::getSignal).mapToInt(indicatorTrend -> switch (indicatorTrend) {
          case SELL -> -1;
          case BUY -> 1;
          case NEUTRAL -> 0;
        }).sum();
        if (signalSum == -3) {
          this.getSignalService().addGlobalSignal(lastCandleDataTime, SignalTrend.STRONG_SELL);
        } else if (signalSum == 3) {
          this.getSignalService().addGlobalSignal(lastCandleDataTime, SignalTrend.STRONG_BUY);
        } else if (signalSum == 0) {
          this.getSignalService().addGlobalSignal(lastCandleDataTime, SignalTrend.NEUTRAL);
        } else if (signalSum > 0) {
          this.getSignalService().addGlobalSignal(lastCandleDataTime, SignalTrend.BUY);
        } else {
          this.getSignalService().addGlobalSignal(lastCandleDataTime, SignalTrend.SELL);
        }
      }
    }
  }
}

