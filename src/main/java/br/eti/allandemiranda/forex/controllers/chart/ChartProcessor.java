package br.eti.allandemiranda.forex.controllers.chart;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.TicketService;
import br.eti.allandemiranda.forex.utils.TimeFrame;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
public class ChartProcessor {

  private final CandlestickService candlestickService;
  private final TicketService ticketService;

  @Value("${candlestick.timeframe}")
  private String timeFrame;

  @Autowired
  private ChartProcessor(final CandlestickService candlestickService, final TicketService ticketService) {
    this.candlestickService = candlestickService;
    this.ticketService = ticketService;
  }

  private static @NotNull LocalDateTime getDateTimeToM15(final @NotNull LocalDateTime ticketDateTime, final @NotNull LocalDate localDate) {
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

  private static @NotNull LocalDateTime getInputCandleDateTime(final @NotNull LocalDateTime ticketDateTime, final @NotNull TimeFrame timeFrame) {
    LocalDate localDate = ticketDateTime.toLocalDate();
    return switch (timeFrame) {
      //TODO implement time frame for other inputs
      case M1, M5, M30, H1 -> throw new IllegalStateException("Not implemented, only M15");
      case M15 -> getDateTimeToM15(ticketDateTime, localDate);
    };
  }

  private static @NotNull Candlestick mergeCandleAndTicket(final @NotNull Candlestick candlestick, final @NotNull Ticket ticket) {
    if (candlestick.dateTime().equals(ticket.dateTime())) {
      if (ticket.bid() > candlestick.high()) {
        return candlestick.withClose(ticket.bid()).withHigh(ticket.bid());
      }
      if (ticket.bid() < candlestick.low()) {
        return candlestick.withClose(ticket.bid()).withLow(ticket.bid());
      }
      return candlestick.withClose(ticket.bid());
    } else {
      return new Candlestick(ticket.dateTime(), ticket.bid(), ticket.bid(), ticket.bid(), ticket.bid());
    }
  }

  private @NotNull TimeFrame getTimeFrame() {
    return TimeFrame.valueOf(this.timeFrame);
  }

  @Synchronized
  public void run() {
    if (this.candlestickService.getCurrentMemorySize() == 0L) {
      Candlestick model = new Candlestick(getInputCandleDateTime(this.ticketService.getLocalDateTime(), this.getTimeFrame()), this.ticketService.getBid(),
          this.ticketService.getBid(), this.ticketService.getBid(), this.ticketService.getBid());
      this.candlestickService.add(model);
    } else {
      Candlestick last = this.candlestickService.getLastCandlestick();
      Candlestick merged = mergeCandleAndTicket(last,
          this.ticketService.getTicket().withDateTime(getInputCandleDateTime(this.ticketService.getLocalDateTime(), this.getTimeFrame())));
      this.candlestickService.add(merged);
    }
    this.candlestickService.updateFile(this.ticketService.getLocalDateTime());
  }

}
