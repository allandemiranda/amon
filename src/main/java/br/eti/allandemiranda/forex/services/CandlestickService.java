package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CandlestickService implements DefaultService<CandlestickEntity, Candlestick> {

  private final CandlestickRepository candlestickRepository;

  @Value("${candlestick.repository.memory}")
  private Integer memorySize;

  @Autowired
  protected CandlestickService(CandlestickRepository candlestickRepository) {
    this.candlestickRepository = candlestickRepository;
  }

  public void setTicket(final @NotNull Ticket ticket, final @NotNull LocalDateTime realTime) {
    if (candlestickRepository.getDataBase().isEmpty()) {
      candlestickRepository.addData(toEntity(new Candlestick(ticket.dateTime(), ticket.bid(), ticket.bid(), ticket.bid(), ticket.bid())));
    } else {
      CandlestickEntity last = candlestickRepository.selectAll().last();
      Candlestick merged = mergeCandleAndTicket(toModel(last), ticket);
      candlestickRepository.updateData(toEntity(merged));
      while (candlestickRepository.getDataBase().size() > getMemorySide()) {
        CandlestickEntity first = candlestickRepository.selectAll().first();
        candlestickRepository.removeData(first);
      }
    }
    candlestickRepository.saveRunTime(candlestickRepository.selectAll().last(), realTime);
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

  private int getMemorySide() {
    return this.memorySize;
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
