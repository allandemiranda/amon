package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Objects;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CandlestickService implements DefaultService<CandlestickEntity, Candlestick> {

  private final CandlestickRepository candlestickRepository;
  private LocalDateTime realDataTime = LocalDateTime.MIN;

  @Autowired
  protected CandlestickService(CandlestickRepository candlestickRepository) {
    this.candlestickRepository = candlestickRepository;
  }

  @Synchronized
  public void addTicket(final @NotNull Ticket ticket) {
    if (ticket.dateTime().isAfter(getRealDataTime())) {
      setRealDataTime(ticket.dateTime());
      if (candlestickRepository.getDataBase().isEmpty()) {
        candlestickRepository.addData(toEntity(new Candlestick(getCandleDateTime(ticket.dateTime()), ticket.bid(), ticket.bid(), ticket.bid(), ticket.bid())));
      } else {
        CandlestickEntity last = candlestickRepository.getLast();
        Candlestick merged = mergeCandleAndTicket(toModel(last), ticket.withDateTime(getCandleDateTime(ticket.dateTime())));
        candlestickRepository.updateData(toEntity(merged));
      }
//      candlestickRepository.saveRunTimeLine(candlestickRepository.getLast(), getRealDataTime());
    } else {
      log.warn("Ticket {} with a data before current {}", ticket, realDataTime.format(DateTimeFormatter.ISO_DATE_TIME));
    }
  }

  public long getCurrentMemorySize() {
    return candlestickRepository.selectAll().size();
  }

  public LocalDateTime getRealDataTime() {
    return this.realDataTime;
  }

  private void setRealDataTime(final @NotNull LocalDateTime realDataTime) {
    this.realDataTime = realDataTime;
  }

  public LocalDateTime getCurrentDataTime() {
    return candlestickRepository.getLast().getDateTime();
  }

  public Candlestick getLast() {
    return toModel(candlestickRepository.getLast());
  }

  public double[] getCloseReversed(int period) {
    return candlestickRepository.selectAll().stream().sorted(Comparator.comparing(CandlestickEntity::getDateTime).reversed()).limit(period).mapToDouble(CandlestickEntity::getClose)
        .toArray();
  }

  public double[] getHighReversed(int period) {
    return candlestickRepository.selectAll().stream().sorted(Comparator.comparing(CandlestickEntity::getDateTime).reversed()).limit(period).mapToDouble(CandlestickEntity::getHigh)
        .toArray();
  }

  public double[] getLowReversed(int period) {
    return candlestickRepository.selectAll().stream().sorted(Comparator.comparing(CandlestickEntity::getDateTime).reversed()).limit(period).mapToDouble(CandlestickEntity::getLow)
        .toArray();
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
