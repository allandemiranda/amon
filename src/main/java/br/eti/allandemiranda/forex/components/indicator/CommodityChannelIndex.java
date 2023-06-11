package br.eti.allandemiranda.forex.components.indicator;

import br.eti.allandemiranda.forex.components.Signal;
import br.eti.allandemiranda.forex.components.ToApply;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class CommodityChannelIndex implements TechnicalIndicator<Double>, InputsIndicator {
    @Override
    public List<Pair<Double, Signal>> get(@NotNull List<Candlestick> candlestickList, Object @NotNull ... input) {
        InputsIndicator.Input inputs = getInputs(input);

        List<Double> tp = candlestickList.stream().map(candlestick -> getApply(inputs.toApply(), candlestick)).toList();
        List<Double> smaTP = getSMA(inputs.period(), tp);
        List<Double> d = IntStream.range(0, tp.size()).mapToDouble(i -> {
            if (Objects.nonNull(tp.get(i)) && Objects.nonNull(smaTP.get(i))) {
                return tp.get(i) - smaTP.get(i);
            } else {
                return (Double) null;
            }
        }).boxed().toList();

        List<Double> m = getSMA(inputs.period(), d).stream().map(value -> {
            if (Objects.nonNull(value)) {
                return value * 0.015;
            } else {
                return null;
            }
        }).toList();

        Stream<Double> cci = IntStream.range(0, d.size()).mapToDouble(i -> {
            if (Objects.nonNull(m.get(i)) && Objects.nonNull(d.get(i))) {
                return m.get(i) / d.get(i);
            } else {
                return (Double) null;
            }
        }).boxed();

        return cci.map(value -> {
            if (Objects.nonNull(value) && value >= inputs.max()) {
                return Pair.of(value, Signal.SELL);
            }
            if (Objects.nonNull(value) && value <= inputs.min()) {
                return Pair.of(value, Signal.BUY);
            }
            return Pair.of(value, Signal.NEUTRAL);
        }).toList();
    }

//    @Override
//    public @NotNull InputsIndicator.Input getInputs(Object @NotNull [] input) {
//        int period = (int) input[0];
//        ToApply toApply = (ToApply) input[1];
//        double max = (double) input[2];
//        double min = (double) input[3];
//        return new InputsIndicator.Input(period, toApply, max, min);
//    }
}
