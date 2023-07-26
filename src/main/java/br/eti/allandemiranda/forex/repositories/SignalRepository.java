package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.entities.SignalEntity;
import br.eti.allandemiranda.forex.headers.SignalHeaders;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class SignalRepository implements DataRepository<SignalEntity>, SaveRunTimeRepository {

  private final Collection<SignalEntity> collection = new ArrayList<>();

  @Value("${signal.repository.output}")
  private File ouputFile;
  @Value("${signal.repository.memory}")
  private Integer memorySize;

  @Override
  public @NotNull Collection<SignalEntity> getDataBase() {
    return this.collection;
  }

  @Override
  public long getMemorySide() {
    return this.memorySize;
  }

  @Override
  public File getOutputFile() {
    return this.ouputFile;
  }

  @Override
  public Object[] getHeaders() {
    return SignalHeaders.values();
  }

  @Override
  public Object[] getLine(Object... inputs) {
    //TODO
    return new Object[0];
  }
}
