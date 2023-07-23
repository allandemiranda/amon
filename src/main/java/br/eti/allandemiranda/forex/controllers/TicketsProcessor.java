package br.eti.allandemiranda.forex.controllers;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.CandlestickService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TicketsProcessor {

  private final CandlestickService candlestickService;

  @Autowired
  protected TicketsProcessor(CandlestickService candlestickService) {
    this.candlestickService = candlestickService;
  }

  public void socket(final @NotNull Ticket ticket) {
    candlestickService.setTicket(ticket);
  }

}
