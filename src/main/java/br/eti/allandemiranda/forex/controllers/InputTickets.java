package br.eti.allandemiranda.forex.controllers;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.CandlestickService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class InputTickets {

  private final CandlestickService candlestickService;

  @Autowired
  protected InputTickets(CandlestickService candlestickService) {
    this.candlestickService = candlestickService;
  }

  public void setTicket(final @NotNull Ticket ticket) {
    LocalDateTime candleDateTime = getCandleDateTime(ticket.dateTime());
    candlestickService.setTicket(ticket.withDateTime(candleDateTime), ticket.dateTime());
  }

  private @NotNull LocalDateTime getCandleDateTime(final @NotNull LocalDateTime ticketDateTime) {
    LocalDate localDate = ticketDateTime.toLocalDate();
    if (ticketDateTime.getMinute() < 15) {
      LocalTime localTime = LocalTime.of(ticketDateTime.getHour(), 0);
      return LocalDateTime.of(localDate, localTime);
    }
    if (ticketDateTime.getMinute() < 30) {
      LocalTime localTime = LocalTime.of(ticketDateTime.getHour(), 15);
      return LocalDateTime.of(localDate, localTime);
    }
    if (ticketDateTime.getMinute() < 45) {
      LocalTime localTime = LocalTime.of(ticketDateTime.getHour(), 30);
      return LocalDateTime.of(localDate, localTime);
    }
    LocalTime localTime = LocalTime.of(ticketDateTime.getHour(), 45);
    return LocalDateTime.of(localDate, localTime);
  }
}
