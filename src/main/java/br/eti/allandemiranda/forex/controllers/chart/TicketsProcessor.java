package br.eti.allandemiranda.forex.controllers.chart;

import br.eti.allandemiranda.forex.controllers.indicators.IndicatorsProcessor;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.CandlestickService;
import java.time.LocalDateTime;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
public class TicketsProcessor {

  private final CandlestickService candlestickService;
  private final IndicatorsProcessor indicatorsProcessor;

  @Value("${candlestick.timeframe}")
  private String timeFrame;
  private Ticket currentTicket = new Ticket(LocalDateTime.MIN, 0D, 0D);

  @Autowired
  private TicketsProcessor(final CandlestickService candlestickService, final IndicatorsProcessor indicatorsProcessor) {
    this.candlestickService = candlestickService;
    this.indicatorsProcessor = indicatorsProcessor;
  }

  private @NotNull TimeFrame getTimeFrame() {
    return TimeFrame.valueOf(this.timeFrame);
  }



  @Synchronized
  public void socket(final @NotNull Ticket ticket) {
    this.candlestickService.addTicket(ticket, this.getTimeFrame());
    this.indicatorsProcessor.run(ticket.dateTime());
  }

}
