package br.eti.allandemiranda.forex.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CandlestickEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private long idx;
    private LocalDateTime openTime;
    private float open;
    private float high;
    private float low;
    private float close;
}
