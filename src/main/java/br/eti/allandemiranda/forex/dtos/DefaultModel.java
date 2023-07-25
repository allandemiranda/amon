package br.eti.allandemiranda.forex.dtos;

import java.time.LocalDateTime;

@FunctionalInterface
public interface DefaultModel {
  LocalDateTime dateTime();
}
