package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.models.CandlestickModel;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.StreamSupport;

@Repository
public class HistoricRepository implements LoadFile<CandlestickModel> {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    //    @Parameter(names = "-dataMT5", description = "File in .csv with the historic", required = true)
    @Value("file:c:/Users/allan/Downloads/EURUSD_M1_198901030000_202306081435.csv")
    private File inputFile;

    @Override
    public List<CandlestickModel> load() {
        int skipHeader = 1;
        CSVParserBuilder csvParserBuilder = new CSVParserBuilder().withSeparator('\t');
        try (FileReader fileReader = new FileReader(inputFile); CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(skipHeader).withCSVParser(csvParserBuilder.build()).build()) {
            return StreamSupport.stream(csvReader.spliterator(), false)
                    .map(strings -> {
                        String dataTime = strings[0].replace(".", "-").concat("T").concat(strings[1]);
                        return new CandlestickModel(LocalDateTime.parse(dataTime, DATE_TIME_FORMATTER), Double.parseDouble(strings[2]), Double.parseDouble(strings[3]), Double.parseDouble(strings[4]), Double.parseDouble(strings[5]));
                    })
                    .toList();
        } catch (IOException e) {
            throw new LoadFileException(e);
        }
    }
}
