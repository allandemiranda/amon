package br.eti.allandemiranda.forex.controllers.chart;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.TimeFrame;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class ChartProcessor {

  private final CandlestickService candlestickService;
  private final TicketService ticketService;

  /**
   * Time frame of chart
   */
  @Value("${chart.timeframe:M15}")
  private String timeFrame;

  @Autowired
  protected ChartProcessor(final CandlestickService candlestickService, final TicketService ticketService) {
    this.candlestickService = candlestickService;
    this.ticketService = ticketService;
  }

  /**
   * Tread processor
   */
  @Synchronized
  public void run() {
    if (this.getTicketService().isReady()) {
      final Ticket ticket = this.getTicketService().getTicket();
      this.getCandlestickService().addTicket(ticket, TimeFrame.valueOf(this.getTimeFrame()));
    }
  }
}
