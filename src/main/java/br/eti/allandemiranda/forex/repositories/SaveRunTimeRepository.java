package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.exceptions.WriteFileException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public interface SaveRunTimeRepository {

  File getOutputFile();

  Object[] getHeaders();

  default void saveHeaders() {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord(this.getHeaders());
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }

  Object[] getLine(final Object... inputs);

  default void saveRunTimeLine(final Object... inputs) {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord(getLine(inputs));
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }
}
