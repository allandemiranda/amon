package br.eti.allandemiranda.forex.controllers.indicators;

import br.eti.allandemiranda.forex.controllers.indicators.trend.AverageDirectionalMovementIndex;
import br.eti.allandemiranda.forex.controllers.indicators.trend.CommodityChannelIndex;
import br.eti.allandemiranda.forex.controllers.indicators.trend.EnvelopesTechnicalIndicator;
import br.eti.allandemiranda.forex.controllers.indicators.trend.RelativeStrengthIndex;
import br.eti.allandemiranda.forex.controllers.indicators.trend.RelativeVigorIndex;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.IndicatorService;
import br.eti.allandemiranda.forex.services.SignalService;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class IndicatorsProcessor {

  private static final String ADX = "ADX";
  private static final String ENVELOPES = "Envelopes";
  private static final String CCI = "CCI";
  private static final String RSI = "RSI";
  private static final String RVI = "RVI";

  private final AverageDirectionalMovementIndex averageDirectionalMovementIndex;
  private final EnvelopesTechnicalIndicator envelopesTechnicalIndicator;
  private final CommodityChannelIndex commodityChannelIndex;
  private final RelativeStrengthIndex relativeStrengthIndex;
  private final RelativeVigorIndex relativeVigorIndex;
  private final IndicatorService indicatorService;
  private final SignalService signalService;
  private final CandlestickService candlestickService;

  @Value("${indicators.run.min:3}")
  private int interval;
  @Setter(AccessLevel.PRIVATE)
  private LocalDateTime lastDataTime = LocalDateTime.MIN;

  @Autowired
  protected IndicatorsProcessor(final AverageDirectionalMovementIndex averageDirectionalMovementIndex, final EnvelopesTechnicalIndicator envelopesTechnicalIndicator,
      final CommodityChannelIndex commodityChannelIndex, final RelativeStrengthIndex relativeStrengthIndex, final IndicatorService indicatorService,
      final SignalService signalService, final CandlestickService candlestickService, final RelativeVigorIndex relativeVigorIndex) {
    this.averageDirectionalMovementIndex = averageDirectionalMovementIndex;
    this.envelopesTechnicalIndicator = envelopesTechnicalIndicator;
    this.commodityChannelIndex = commodityChannelIndex;
    this.relativeStrengthIndex = relativeStrengthIndex;
    this.indicatorService = indicatorService;
    this.signalService = signalService;
    this.candlestickService = candlestickService;
    this.relativeVigorIndex = relativeVigorIndex;
  }

  @PostConstruct
  public void init() {
    this.getIndicatorService().addIndicator(ADX, this.getAverageDirectionalMovementIndex());
    this.getIndicatorService().addIndicator(ENVELOPES, this.getEnvelopesTechnicalIndicator());
    this.getIndicatorService().addIndicator(CCI, this.getCommodityChannelIndex());
    this.getIndicatorService().addIndicator(RSI, this.getRelativeStrengthIndex());
    this.getIndicatorService().addIndicator(RVI, this.getRelativeVigorIndex());
  }

  @Synchronized
  public void run() {
    final Candlestick lastCandlestick = this.getCandlestickService().getLastCandlestick();
    final LocalDateTime currentDataTime = lastCandlestick.realDateTime();
    if (this.getCandlestickService().isReady() && this.getLastDataTime().plusMinutes(this.getInterval()).isBefore(currentDataTime)) {
      this.setLastDataTime(currentDataTime);
      final Map<String, SignalTrend> currentSignals = this.getIndicatorService().processAndGetSignals();
      if (!currentSignals.isEmpty()) {
        final BigDecimal price = lastCandlestick.close();
        this.getIndicatorService().updateDebugFile(currentSignals, currentDataTime, price);
        final List<SignalTrend> collect = currentSignals.values().stream().filter(signalTrend -> !SignalTrend.OUT.equals(signalTrend)).toList();
        final double signalsPower =
            collect.stream().collect(Collectors.groupingBy(signalTrend -> signalTrend, Collectors.summingInt(signalTrend -> 1))).entrySet().parallelStream()
                .mapToInt(entry -> entry.getKey().power * entry.getValue()).sum() / (double) collect.size();
        if (signalsPower < SignalTrend.SELL.power) {
          this.getSignalService().addGlobalSignal(new Signal(currentDataTime, SignalTrend.STRONG_SELL, price));
        } else if (signalsPower == SignalTrend.SELL.power) {
          this.getSignalService().addGlobalSignal(new Signal(currentDataTime, SignalTrend.SELL, price));
        } else if (signalsPower == SignalTrend.BUY.power) {
          this.getSignalService().addGlobalSignal(new Signal(currentDataTime, SignalTrend.BUY, price));
        } else if (signalsPower > SignalTrend.BUY.power) {
          this.getSignalService().addGlobalSignal(new Signal(currentDataTime, SignalTrend.STRONG_BUY, price));
        } else {
          this.getSignalService().addGlobalSignal(new Signal(currentDataTime, SignalTrend.NEUTRAL, price));
        }
      }
    }
  }
}

