package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.utils.SignalTrend;
import br.eti.allandemiranda.forex.dtos.ADX;
import br.eti.allandemiranda.forex.entities.ADXEntity;
import br.eti.allandemiranda.forex.repositories.ADXBase;
import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ADXService {

  private final ADXBase adxRepository;

  @Autowired
  protected ADXService(final ADXBase adxRepository) {
    this.adxRepository = adxRepository;
  }

  public @NotNull ADX getLast() {
    return toModel(this.adxRepository.getLast());
  }

  public boolean isDiPlusUpThanDiMinus() {
    return this.adxRepository.getLast().getDiPlus() >= this.adxRepository.getLast().getDiMinus();
  }

  public void add(final @NotNull ADX adx) {
    this.adxRepository.addData(toEntity(adx));
  }

  public void updateFile(final @NotNull SignalTrend signal, final @NotNull LocalDateTime realTime, final @NotNull ADX adx, final double price) {
    this.adxRepository.saveRunTimeLine(realTime, toEntity(adx), signal, price);
  }

  private static @NotNull ADXEntity toEntity(@NotNull ADX model) {
    ADXEntity entity = new ADXEntity();
    entity.setDateTime(model.dateTime());
    entity.setAdx(model.adx());
    entity.setDiPlus(model.diPlus());
    entity.setDiMinus(model.diMinus());
    return entity;
  }

  private static @NotNull ADX toModel(@NotNull ADXEntity entity) {
    return new ADX(entity.getDateTime(), entity.getAdx(), entity.getDiPlus(), entity.getDiMinus());
  }
}
