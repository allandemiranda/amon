package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.repositories.IndicatorRepository;
import java.util.SortedMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter(AccessLevel.PRIVATE)
public class IndicatorService {

  private final IndicatorRepository repository;

  @Autowired
  protected IndicatorService(final IndicatorRepository repository) {
    this.repository = repository;
  }

  public void addIndicator(final @NotNull String name, final Indicator indicator) {
    this.getRepository().add(name, indicator);
  }

  public SortedMap<String, Indicator> getIndicators() {
    return this.getRepository().get();
  }
}
