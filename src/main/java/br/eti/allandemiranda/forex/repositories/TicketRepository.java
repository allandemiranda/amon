package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.entities.TicketEntity;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class TicketRepository {

    private static final int DATE_COLUMN = 0;
    private static final int TIME_COLUMN = 1;
    private static final int BID_COLUMN = 2;
    private static final int ASK_COLUMN = 3;
    private static final float DEFAULT_PRICE = 0F;
    private static final int LEST_BID_COLUMN = 0;
    private static final int LEST_ASK_COLUMN = 1;
    private static final String DATE_TIME_PATTERN = "uuuu-MM-dd'T'HH:mm:ss.SSS";

    @Value("classpath:files/EURUSD_202301020007_202307072357.csv")
    private Resource resource;

    private void setBidPrice(String @NotNull [] row, TicketEntity entity, float[] lestValues) {
        if (row[BID_COLUMN].isBlank() || row[BID_COLUMN].isEmpty()) {
            entity.setBid(lestValues[LEST_BID_COLUMN]);
        } else {
            entity.setBid(Float.parseFloat(row[BID_COLUMN]));
            lestValues[LEST_BID_COLUMN] = entity.getBid();
        }
    }

    private void setAskPrice(String @NotNull [] row, TicketEntity entity, float[] lestValues) {
        if (row[ASK_COLUMN].isBlank() || row[ASK_COLUMN].isEmpty()) {
            entity.setAsk(lestValues[LEST_ASK_COLUMN]);
        } else {
            entity.setAsk(Float.parseFloat(row[ASK_COLUMN]));
            lestValues[LEST_ASK_COLUMN] = entity.getBid();
        }
    }

    @NotNull
    private LocalDateTime getDateTime(String @NotNull [] row) {
        String date = row[DATE_COLUMN].replace(".", "-");
        String time = row[TIME_COLUMN];
        String dateTime = date.concat("T").concat(time);
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    }

    @NotNull
    private Function<String[], TicketEntity> getTicket(float[] lestValues, AtomicLong idx) {
        return row -> {
            TicketEntity entity = new TicketEntity();
            entity.setIdx(idx.getAndIncrement());
            entity.setDataTime(getDateTime(row));

            setBidPrice(row, entity, lestValues);
            setAskPrice(row, entity, lestValues);

            return entity;
        };
    }

    @SneakyThrows
    @NotNull
    private File getInputFile() {
        return resource.getFile();
    }

    @SneakyThrows
    @NotNull
    public Collection<TicketEntity> getDataBase() {
        try (FileReader fileReader = new FileReader(getInputFile()); BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            float[] lestValues = new float[]{DEFAULT_PRICE, DEFAULT_PRICE};
            AtomicLong idx = new AtomicLong(1L);
            return bufferedReader.lines().skip(1).map(s -> s.split("\t")).map(getTicket(lestValues, idx)).collect(Collectors.toCollection(ArrayList::new));
        }
    }
}
