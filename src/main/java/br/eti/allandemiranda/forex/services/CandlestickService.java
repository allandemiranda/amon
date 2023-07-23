package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CandlestickService implements DefaultService<CandlestickEntity, Candlestick> {

  private final CandlestickRepository candlestickRepository;

  @Autowired
  protected CandlestickService(CandlestickRepository candlestickRepository) {
    this.candlestickRepository = candlestickRepository;
  }

  @Synchronized
  public void setTicket(final @NotNull Ticket ticket) {
    if (candlestickRepository.getDataBase().isEmpty()) {
      candlestickRepository.addData(toEntity(new Candlestick(getCandleDateTime(ticket.dateTime()), ticket.bid(), ticket.bid(), ticket.bid(), ticket.bid())));
    } else {
      CandlestickEntity last = candlestickRepository.getLast();
      Candlestick merged = mergeCandleAndTicket(toModel(last), ticket.withDateTime(getCandleDateTime(ticket.dateTime())));
      candlestickRepository.updateData(toEntity(merged));
    }
    candlestickRepository.saveRunTime(candlestickRepository.getLast(), ticket.dateTime());
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

  private Candlestick mergeCandleAndTicket(final @NotNull Candlestick candlestick, final @NotNull Ticket ticket) {
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

  @Override
  public @NotNull CandlestickEntity toEntity(@NotNull Candlestick model) {
    CandlestickEntity entity = new CandlestickEntity();
    entity.setDateTime(model.dateTime());
    entity.setLow(model.low());
    entity.setOpen(model.open());
    entity.setHigh(model.high());
    entity.setClose(model.close());
    return entity;
  }

  @Override
  public @NotNull Candlestick toModel(@NotNull CandlestickEntity entity) {
    return new Candlestick(entity.getDateTime(), entity.getOpen(), entity.getHigh(), entity.getLow(), entity.getClose());
  }
}
