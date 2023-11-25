package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Order;
import br.eti.allandemiranda.forex.entities.OrderEntity;
import br.eti.allandemiranda.forex.utils.OrderStatus;
import java.util.TreeSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@Slf4j
public class OrderRepository {

  private final TreeSet<OrderEntity> dataBase = new TreeSet<>();

  public Order @NotNull [] getOrders() {
    return this.getDataBase().parallelStream().map(this::toModel).toArray(Order[]::new);
  }

  /**
   * Number of orders open on the database
   *
   * @return The number of orders open on the database
   */
  public long numberOfOrdersOpen() {
    return this.getDataBase().stream().filter(orderEntity -> orderEntity.getOrderStatus().equals(OrderStatus.OPEN)).count();
  }

  /**
   * Update a existent order
   *
   * @param order The order updated
   */
  @Synchronized
  public void updateOrder(final @NotNull Order order) {
    OrderEntity entity = this.toEntity(order);
    if (!this.getDataBase().remove(entity)) {
      log.warn("Trying to update a {} that not exist on database", order);
    }
    this.getDataBase().add(entity);
  }

  /**
   * Add a new order to the database
   *
   * @param order The Order to add
   */
  @Synchronized
  public void addOrder(final @NotNull Order order) {
    OrderEntity entity = this.toEntity(order);
    if (!this.getDataBase().add(entity)) {
      log.warn("Trying to add a {} that exist on database", order);
      this.updateOrder(order);
    }
  }

  /**
   * Remove all close orders on the database
   */
  @Synchronized
  public void removeCloseOrders() {
    this.getDataBase().removeIf(orderEntity -> orderEntity.getOrderStatus().equals(OrderStatus.CLOSE_SL) || orderEntity.getOrderStatus().equals(OrderStatus.CLOSE_TP));
  }

  private @NotNull Order toModel(final @NotNull OrderEntity entity) {
    return new Order(entity.getOpenDateTime(), entity.getSignalDateTime(), entity.getSignalTrend(), entity.getLastUpdateDateTime(), entity.getTimeOpen(),
        entity.getOrderStatus(), entity.getOrderPosition(), entity.getOpenPrice(), entity.getClosePrice(), entity.getHighProfit(), entity.getLowProfit(),
        entity.getCurrentProfit(), entity.getSwapProfit());
  }

  private @NotNull OrderEntity toEntity(final @NotNull Order order) {
    OrderEntity entity = new OrderEntity();
    entity.setOpenDateTime(order.openDateTime());
    entity.setSignalDateTime(order.signalDateTime());
    entity.setSignalTrend(order.signalTrend());
    entity.setLastUpdateDateTime(order.lastUpdateDateTime());
    entity.setTimeOpen(order.timeOpen());
    entity.setOrderStatus(order.orderStatus());
    entity.setOrderPosition(order.orderPosition());
    entity.setOpenPrice(order.openPrice());
    entity.setClosePrice(order.closePrice());
    entity.setHighProfit(order.highProfit());
    entity.setLowProfit(order.lowProfit());
    entity.setCurrentProfit(order.currentProfit());
    entity.setSwapProfit(order.swapProfit());
    return entity;
  }
}
