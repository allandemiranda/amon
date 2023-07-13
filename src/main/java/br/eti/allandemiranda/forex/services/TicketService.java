package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.entities.TicketEntity;
import br.eti.allandemiranda.forex.repositories.TicketRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class TicketService {
    private final TicketRepository repository;

    @Autowired
    @Inject
    protected TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    @NotNull
    private Function<TicketEntity, Ticket> getTicketEntityTicketFunction() {
        return ticketEntity -> new Ticket(ticketEntity.getIdx(), ticketEntity.getDataTime(), ticketEntity.getBid(), ticketEntity.getAsk());
    }

    @NotNull
    public Stream<Ticket> selectAll() {
        return repository.getDataBase().stream().map(getTicketEntityTicketFunction());
    }

}
