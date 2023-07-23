package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.entities.DefaultEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
public interface DataRepository<T extends DefaultEntity>  {

  @NotNull Collection<T> getDataBase();

  void initDataBase();

  default @NotNull Collection<T> selectAll() {
    return this.getDataBase();
  }

  default void addData(@NotNull T entity) {
    this.getDataBase().add(entity);
  }

  default void removeData(@NotNull LocalDateTime dateTime) {
    this.getDataBase().removeIf(entity -> entity.getDateTime().equals(dateTime));
  }

  default void removeData(@NotNull T entity) {
    this.removeData(entity.getDateTime());
  }

  default void updateData(@NotNull T entity) {
    this.removeData(entity);
    this.addData(entity);
  }
}
