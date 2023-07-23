package br.eti.allandemiranda.forex.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CandlestickEntity implements Serializable, DefaultEntity {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  private LocalDateTime dateTime;
  private double open;
  private double high;
  private double low;
  private double close;
}
