package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.models.CandlestickModel;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandlestickService {

    final CandlestickRepository candlestickRepository;

    @Autowired
    public CandlestickService(CandlestickRepository candlestickRepository) {
        this.candlestickRepository = candlestickRepository;
    }

    public List<CandlestickModel> getAll() {
        return candlestickRepository.load();
    }
}
