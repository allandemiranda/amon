package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.entities.SignalEntity;
import br.eti.allandemiranda.forex.repositories.SignalRepository;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignalService {

  private final SignalRepository repository;

  @Autowired
  public SignalService(SignalRepository repository) {
    this.repository = repository;
  }

  private static Signal toModel(SignalEntity entity) {
    return new Signal(entity.getDateTime(), entity.getTrend(), entity.getPrice());
  }

  private static SignalEntity toEntity(Signal model) {
    SignalEntity entity = new SignalEntity();
    entity.setDateTime(model.dateTime());
    entity.setTrend(model.trend());
    entity.setPrice(model.price());
    return entity;
  }

  public void add(Signal signal) {
    this.repository.addData(toEntity(signal));
  }

  public SignalTrend[] getLastSequence(final int size) {
    return this.repository.selectAll().stream().sorted(Comparator.comparing(SignalEntity::getDateTime).reversed()).limit(size)
        .sorted(Comparator.comparing(SignalEntity::getDateTime)).map(SignalService::toModel).map(Signal::trend).toArray(SignalTrend[]::new);
  }
}
