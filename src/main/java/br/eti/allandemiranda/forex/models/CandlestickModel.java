package br.eti.allandemiranda.forex.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CandlestickModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private LocalDateTime dateTime;
    private double open;
    private double high;
    private double low;
    private double close;
}
