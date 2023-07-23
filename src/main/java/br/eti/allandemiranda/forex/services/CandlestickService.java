package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CandlestickService {

  final CandlestickRepository candlestickRepository;

  @Autowired
  public CandlestickService(CandlestickRepository candlestickRepository) {
    this.candlestickRepository = candlestickRepository;
  }

  public double[] getCloseValues() {
    return candlestickRepository.selectAll().stream().mapToDouble(CandlestickEntity::getClose).limit(10).toArray();
  }
}
