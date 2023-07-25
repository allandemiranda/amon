package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.entities.DefaultEntity;
import br.eti.allandemiranda.forex.exceptions.DataRepositoryException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import org.jetbrains.annotations.NotNull;

public interface DataRepository<T extends DefaultEntity> {

  @NotNull Collection<T> getDataBase();

  default @NotNull Collection<T> selectAll() {
    return this.getDataBase();
  }

  default void addData(final @NotNull T entity) {
    this.removeData(entity);
    this.getDataBase().add(entity);
    this.checkMemory();
  }

  default void removeData(final @NotNull LocalDateTime dateTime) {
    this.getDataBase().removeIf(entity -> entity.getDateTime().equals(dateTime));
  }

  default void removeData(final @NotNull T entity) {
    this.removeData(entity.getDateTime());
  }

  default void updateData(final @NotNull T entity) {
    this.addData(entity);
  }

  default long getMemorySide() {
    return Long.MAX_VALUE;
  }

  default void checkMemory() {
    while (this.getDataBase().size() > this.getMemorySide()) {
      this.removeData(this.getFirst());
    }
  }

  default T getFirst() {
    return this.selectAll().stream().min(Comparator.comparing(DefaultEntity::getDateTime)).orElseThrow(DataRepositoryException::new);
  }

  default T getLast() {
    return this.selectAll().stream().max(Comparator.comparing(DefaultEntity::getDateTime)).orElseThrow(DataRepositoryException::new);
  }
}
