package br.eti.allandemiranda.forex.controllers;

import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.TicketService;
import com.opencsv.CSVWriter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import java.io.FileWriter;

@Controller
public class TestController {
    private final TicketService ticketService;
    private final CandlestickService candlestickService;

    @Value("classpath:files/outPutCand.txt")
    private Resource outPutCand;

    @Value("classpath:files/outPutTicket.txt")
    private Resource outPutTicket;

    @Autowired
    @Inject
    public TestController(TicketService ticketService, CandlestickService candlestickService) {
        this.ticketService = ticketService;
        this.candlestickService = candlestickService;
    }

    @SneakyThrows
    public void initJob(){
//        try (FileWriter fileWriter = new FileWriter(outPutTicket.getFile()); CSVWriter csvWriter = new CSVWriter(fileWriter)){
//            String[] header = new String[]{"idx", "dataTime", "bid", "ask"};
//            csvWriter.writeNext(header);
//            ticketService.selectAll().forEachOrdered(ticket ->
//                csvWriter.writeNext(new String[]{String.valueOf(ticket.idx()), Objects.isNull(ticket.dataTime()) ? "null" : ticket.dataTime().format(DateTimeFormatter.ISO_DATE_TIME), String.valueOf(ticket.bid()), String.valueOf(ticket.ask())}));
//        }

        try (FileWriter fileWriter = new FileWriter(outPutCand.getFile()); CSVWriter csvWriter = new CSVWriter(fileWriter)){
            String[] header = new String[]{"idx", "openTime", "open", "high", "low", "close"};
            csvWriter.writeNext(header);
            candlestickService.selectAll(ticketService.selectAll()).map(candlestick -> {
                return new String[]{String.valueOf(candlestick.idx()), candlestick.openTime().toString(), String.valueOf(candlestick.open()), String.valueOf(candlestick.high()), String.valueOf(candlestick.low()), String.valueOf(candlestick.close())};
            }).forEachOrdered(csvWriter::writeNext);
        }
    }
}
