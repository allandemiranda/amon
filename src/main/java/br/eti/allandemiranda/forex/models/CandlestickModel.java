package br.eti.allandemiranda.forex.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
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
