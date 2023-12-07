package br.eti.allandemiranda.forex.controllers;

import br.eti.allandemiranda.forex.controllers.chart.ChartProcessor;
import br.eti.allandemiranda.forex.controllers.indicators.IndicatorsProcessor;
import br.eti.allandemiranda.forex.controllers.order.OrderProcessor;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.Tools;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Getter(AccessLevel.PRIVATE)
public class GeneratorProcessor {

  private final TicketService ticketService;
  private final ChartProcessor chartProcessor;
  private final IndicatorsProcessor indicatorsProcessor;
  private final OrderProcessor orderProcessor;

  @Autowired
  protected GeneratorProcessor(final TicketService ticketService, final ChartProcessor chartProcessor, final IndicatorsProcessor indicatorsProcessor,
      final OrderProcessor orderProcessor) {
    this.ticketService = ticketService;
    this.chartProcessor = chartProcessor;
    this.indicatorsProcessor = indicatorsProcessor;
    this.orderProcessor = orderProcessor;
  }

  /**
   * Web Socket to receive the ticket information
   *
   * @param time The time to create ticket
   * @param bid  The BID price to create the ticket
   * @param ask  The ASK price to create the ticket
   */
  @Synchronized
  public void webSocket(final LocalDateTime time, final Double bid, final Double ask) {
    final LocalDateTime dateTime = Objects.isNull(time) ? LocalDateTime.MIN : time;
    final double bidFixed = Objects.isNull(bid) ? 0d : bid;
    final double askFixed = Objects.isNull(ask) ? 0d : ask;
    final boolean updatedData = this.getTicketService().updateData(dateTime, bidFixed, askFixed);
    if (updatedData) {
      if(this.getTicketService().getTicket().spread() < a) {
        a = this.getTicketService().getTicket().spread();
        System.out.println(a);
      }
      this.getChartProcessor().run();
      this.getIndicatorsProcessor().run();
      this.getOrderProcessor().run();
    }
  }
  int a = 9999;
}
