package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.entities.DefaultEntity;
import br.eti.allandemiranda.forex.exceptions.LoadFileException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
public interface LoadFileRepository<T extends DefaultEntity> extends DataRepository<T> {

  @NotNull File getInputFile();

  @NotNull CSVFormat getInputCsvFormat();

  default @NotNull Collection<T> loadData() {
    try (final FileReader fileReader = new FileReader(this.getInputFile()); final CSVParser csvParser = this.getInputCsvFormat().parse(fileReader)) {
      return StreamSupport.stream(csvParser.spliterator(), true).map(this::getEntity).collect(Collectors.toCollection(HashSet::new));
    } catch (IOException e) {
      throw new LoadFileException(e);
    }
  }

  @NotNull T getEntity(@NotNull CSVRecord csvRecord);
}
