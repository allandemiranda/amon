package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.utils.SignalTrend;
import br.eti.allandemiranda.forex.repositories.IndicatorsRepository;
import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndicatorsService {

  private final IndicatorsRepository repository;

  @Autowired
  private IndicatorsService(final IndicatorsRepository repository) {
    this.repository = repository;
  }

  public void add(final @NotNull String name, final @NotNull SignalTrend signal) {
    this.repository.add(name, signal);
  }

  public void printHeaders(final @NotNull Object... inputs) {
    this.repository.printHeaders(inputs);
  }

  public void updateFile(final @NotNull LocalDateTime dataTime, final double price) {
    this.repository.updateFile(dataTime, price);
  }
}
