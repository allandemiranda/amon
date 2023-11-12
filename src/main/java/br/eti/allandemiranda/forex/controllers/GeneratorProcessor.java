package br.eti.allandemiranda.forex.controllers;

import br.eti.allandemiranda.forex.controllers.chart.ChartProcessor;
import br.eti.allandemiranda.forex.controllers.indicators.IndicatorsProcessor;
import br.eti.allandemiranda.forex.controllers.order.OrderProcessor;
import br.eti.allandemiranda.forex.services.TicketService;
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
   * @param time The time to create ticket
   * @param bid The BID price to create the ticket
   * @param ask The ASK price to create the ticket
   */
  @Synchronized
  public void webSocket(final LocalDateTime time, final Double bid, final Double ask) {
    //! TODO: Check input data handling for a real web socket with MT5
    final boolean updatedData = this.getTicketService()
        .updateData(Objects.isNull(time) ? LocalDateTime.MIN : time, Objects.isNull(bid) ? 0d : bid, Objects.isNull(ask) ? 0d : ask);
    if (updatedData) {
      this.getChartProcessor().run();
      this.getIndicatorsProcessor().run();
      // TODO VALIDANDO OS INDICADORES ANTES DE REFATORAR O ORDER PROCESS
//      this.getOrderProcessor().run();
    }
  }
}
