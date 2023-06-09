package br.eti.allandemiranda.forex.repositories;

import java.util.List;

public interface LoadFile<T> {
    List<T> load();
}
