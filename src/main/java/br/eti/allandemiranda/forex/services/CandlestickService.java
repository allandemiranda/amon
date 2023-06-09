package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.models.CandlestickModel;
import br.eti.allandemiranda.forex.repositories.HistoricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandlestickService {

    final HistoricRepository historicRepository;

    @Autowired
    public CandlestickService(HistoricRepository historicRepository) {
        this.historicRepository = historicRepository;
    }

    public List<CandlestickModel> getAll() {
        return historicRepository.load();
    }
}
