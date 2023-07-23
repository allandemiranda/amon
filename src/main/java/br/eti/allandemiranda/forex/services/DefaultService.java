package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.DefaultModel;
import br.eti.allandemiranda.forex.entities.DefaultEntity;
import org.jetbrains.annotations.NotNull;

public interface DefaultService <T extends DefaultEntity, U extends DefaultModel> {

  @NotNull T toEntity(@NotNull U model);
  @NotNull U toModel(@NotNull T entity);
}
