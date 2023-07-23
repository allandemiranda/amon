package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.controllers.indicators.SignalTrend;
import br.eti.allandemiranda.forex.dtos.ADX;
import br.eti.allandemiranda.forex.entities.ADXEntity;
import br.eti.allandemiranda.forex.repositories.ADXRepository;
import java.time.LocalDateTime;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ADXService implements DefaultService<ADXEntity, ADX> {

  private final ADXRepository adxRepository;

  @Autowired
  protected ADXService(ADXRepository adxRepository) {
    this.adxRepository = adxRepository;
  }

  public boolean isCross() {
    if (adxRepository.getDataBase().size() == 2) {
      if (adxRepository.getLast().getDiPlus() == adxRepository.getLast().getDiMinus()) {
        return true;
      }
      if (adxRepository.getFirst().getDiPlus() > adxRepository.getFirst().getDiMinus() && adxRepository.getLast().getDiPlus() < adxRepository.getLast().getDiMinus()) {
        return true;
      }
      return adxRepository.getFirst().getDiPlus() < adxRepository.getFirst().getDiMinus() && adxRepository.getLast().getDiPlus() > adxRepository.getLast().getDiMinus();
    }
    return false;
  }

  public ADX getLast() {
    return toModel(adxRepository.getLast());
  }

  public boolean isUp() {
    return adxRepository.getLast().getDiPlus() >= adxRepository.getLast().getDiMinus();
  }

  public void add(final @NotNull ADX adx) {
    adxRepository.addData(toEntity(adx));
  }

  public boolean isReady() {
    return adxRepository.getDataBase().size() >= 2;
  }
  public void print(@NotNull SignalTrend signal, LocalDateTime realTime, ADX adx, double price) {
    adxRepository.saveRunTimeLine(realTime, toEntity(adx), signal, price);
  }

  @Override
  public @NotNull ADXEntity toEntity(@NotNull ADX model) {
    ADXEntity entity = new ADXEntity();
    entity.setDateTime(model.dateTime());
    entity.setAdx(model.adx());
    entity.setDiPlus(model.diPlus());
    entity.setDiMinus(model.diMinus());
    return entity;
  }

  @Override
  public @NotNull ADX toModel(@NotNull ADXEntity entity) {
    return new ADX(entity.getDateTime(), entity.getAdx(), entity.getDiPlus(), entity.getDiMinus());
  }
}
