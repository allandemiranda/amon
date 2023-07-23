package br.eti.allandemiranda.forex.controllers;

import br.eti.allandemiranda.forex.controllers.indicators.trend.AverageDirectionalMovementIndex;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.CandlestickService;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TicketsProcessor {

  private final CandlestickService candlestickService;
  private final AverageDirectionalMovementIndex averageDirectionalMovementIndex;

  @Autowired
  protected TicketsProcessor(CandlestickService candlestickService, AverageDirectionalMovementIndex averageDirectionalMovementIndex) {
    this.candlestickService = candlestickService;
    this.averageDirectionalMovementIndex = averageDirectionalMovementIndex;
  }

  int hash = 0;

  @Synchronized
  public void socket(final @NotNull Ticket ticket) {
    candlestickService.addTicket(ticket);

    int hash2 = (int) (ticket.dateTime().getMinute()/15);

    if(hash2 != hash){
      averageDirectionalMovementIndex.runCalculate();
      averageDirectionalMovementIndex.getSignal();
    }

    hash = hash2;
  }

}
