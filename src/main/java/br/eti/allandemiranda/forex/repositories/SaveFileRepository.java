package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.entities.DefaultEntity;
import br.eti.allandemiranda.forex.exceptions.WriteFileException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;

public interface SaveFileRepository<T extends DefaultEntity> extends DataRepository<T> {

  @NotNull File getOutputFile();

  @NotNull CSVFormat getOutputCsvFormat();

  @NotNull Collection<T> getDataBase();

  @NotNull Consumer<T> getConsumer(@NotNull CSVPrinter csvPrinter);

  default void saveData() {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = new CSVPrinter(fileWriter, getOutputCsvFormat())) {
      this.getDataBase().forEach(this.getConsumer(csvPrinter));
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }
}
