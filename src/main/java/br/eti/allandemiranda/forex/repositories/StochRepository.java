//package br.eti.allandemiranda.forex.repositories;
//
//import br.eti.allandemiranda.forex.dtos.STOCH;
//import br.eti.allandemiranda.forex.entities.StochEntity;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.TreeSet;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.Setter;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.stereotype.Repository;
//
//@Repository
//@Getter(AccessLevel.PRIVATE)
//@Setter(AccessLevel.PRIVATE)
//public class StochRepository {
//
//  private static final int MEMORY_SIZE = 3;
//  private final TreeSet<StochEntity> dataBase = new TreeSet<>();
//
//  public void add(final @NotNull LocalDateTime realDataTime, final @NotNull LocalDateTime dateTime, final @NotNull BigDecimal k, final @NotNull BigDecimal d) {
//    if (this.getDataBase().isEmpty() || dateTime.isAfter(this.getDataBase().first().getDateTime())) {
//      final StochEntity entity = new StochEntity();
//      entity.setRealDateTime(realDataTime);
//      entity.setDateTime(dateTime);
//      entity.setMain(k);
//      entity.setSignal(d);
//      if(!this.getDataBase().add(entity)) {
//        this.getDataBase().remove(entity);
//        this.getDataBase().add(entity);
//      }
//      if (this.getDataBase().size() > MEMORY_SIZE) {
//        this.getDataBase().pollLast();
//      }
//    } else {
//      this.getDataBase().first().setRealDateTime(realDataTime);
//      this.getDataBase().first().setMain(k);
//      this.getDataBase().first().setSignal(d);
//    }
//  }
//
//  public STOCH @NotNull [] get() {
//    return this.getDataBase().stream().map(this::toModel).toArray(STOCH[]::new);
//  }
//
//  private @NotNull STOCH toModel(final @NotNull StochEntity entity) {
//    return new STOCH(entity.getRealDateTime(), entity.getDateTime(), entity.getMain(), entity.getSignal());
//  }
//}
