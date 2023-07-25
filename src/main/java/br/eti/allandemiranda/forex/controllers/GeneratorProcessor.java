package br.eti.allandemiranda.forex.controllers;

import br.eti.allandemiranda.forex.controllers.indicators.IndicatorsProcessor;
import br.eti.allandemiranda.forex.controllers.order.OrderProcessor;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.TicketService;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class GeneratorProcessor {

  private final CandlestickService candlestickService;
  private final IndicatorsProcessor indicatorsProcessor;
  private final TicketService ticketService;
  private final OrderProcessor orderProcessor;

  @Autowired
  private GeneratorProcessor(final CandlestickService candlestickService, final IndicatorsProcessor indicatorsProcessor, final TicketService ticketService,
      final OrderProcessor orderProcessor) {
    this.candlestickService = candlestickService;
    this.indicatorsProcessor = indicatorsProcessor;
    this.ticketService = ticketService;
    this.orderProcessor = orderProcessor;
  }

  @Synchronized
  public void webSocket(LocalDateTime time, Double bid, Double ask) {
    Ticket ticket = null;
    try {
      ticketService.add(new Ticket(time, bid, ask));
      ticket = ticketService.getTicket();
      ticketService.updateOutputFile();
    } catch (Exception e) {
      log.warn(e.getMessage());
    } finally {
      if (Objects.nonNull(ticket)) {
        this.candlestickService.run();
        this.indicatorsProcessor.run();
        this.orderProcessor.run();
      }
    }
  }

}
