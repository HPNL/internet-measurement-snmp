package com.intenert_measurement.snmp.metric;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricUtil {

    public static List<Metric> computeAggregation(List<Metric> rawValues, Function<Metric, Number> valueFunc, Aggregator aggregation) {
        List<Metric> aggregatedValues = new ArrayList<>();

        for (int i = 0; i < rawValues.size(); i++) {
            switch (aggregation) {
                case AVERAGE:
                    Metric metric = rawValues.get(i).clone();
                    metric.setValue(rawValues.subList(0, i + 1).stream().collect(Collectors.averagingDouble(x -> toDouble(valueFunc.apply(x)))));
                    aggregatedValues.add(metric);
            }
        }
        return aggregatedValues;
    }

    public static List<Metric> computeMetricStaticTypes(List<Metric> rawValues, Function<Metric, Number> valueFunc, MetricType type) {
        List<Metric> aggregatedValues = new ArrayList<>();
        for (int i = 0; i < rawValues.size(); i++) {
            Metric clonePoint = rawValues.get(i).clone();
            clonePoint.setValue(0);
            clonePoint.setType(type);
            aggregatedValues.add(clonePoint);
        }
        switch (type) {
            case MTBF:
                int nullIndex = 0;
                while (true) {
                    Pair<Integer, Integer> nullInterval = findMTBFPeriod(rawValues, nullIndex, valueFunc);
                    if (nullInterval.getKey() != null && nullInterval.getValue() != null) {
                        long failureTime = rawValues.get(nullInterval.getValue()).getTimestamp().getTime() - rawValues.get(nullInterval.getKey()).getTimestamp().getTime();
                        nullIndex = nullInterval.getValue();
                        for (int j = nullInterval.getValue(); j < rawValues.size(); j++) {
                            aggregatedValues.get(j).setValue(failureTime);
                        }
                    } else {
                        break;
                    }
                }
                break;
            case MTTF:
                int startNullIndex = 0;
                while (true) {
                    Pair<Integer, Integer> notNullInterval = findMTTFPeriod(rawValues, startNullIndex, valueFunc);
                    if (notNullInterval.getKey() != null && notNullInterval.getValue() != null) {
                        long failureTime = rawValues.get(notNullInterval.getValue()).getTimestamp().getTime() - rawValues.get(notNullInterval.getKey()).getTimestamp().getTime();
                        startNullIndex = notNullInterval.getValue();
                        for (int j = notNullInterval.getValue(); j < rawValues.size(); j++) {
                            aggregatedValues.get(j).setValue(failureTime);
                        }
                    } else {
                        break;
                    }
                }
                break;
        }
        return aggregatedValues;
    }

    private static Pair<Integer, Integer> findMTBFPeriod(List<Metric> values, int startPoint, Function<Metric, Number> valueFunc) {
        boolean findNull = false;
        boolean findNotNull = false;
        Integer firsNullIndex = null;
        Integer secondNullIndex = null;
        for (int i = startPoint; i < values.size(); i++) {
            Number value = valueFunc.apply(values.get(i));
            if (!findNull && value == null) {
                findNull = true;
                firsNullIndex = i;
            } else {
                if (findNull && !findNotNull && value != null) {
                    findNotNull = true;
                }
                if (findNull && findNotNull && value == null) {
                    secondNullIndex = i;
                    return Pair.of(firsNullIndex, secondNullIndex);
                }
            }
        }
        return Pair.of(firsNullIndex, secondNullIndex);
    }

    private static Pair<Integer, Integer> findMTTFPeriod(List<Metric> values, int startPoint, Function<Metric, Number> valueFunc) {
        boolean findNotNull = false;
        Integer startNotNullIndex = null;
        Integer endNotNullIndex = null;
        for (int i = startPoint; i < values.size(); i++) {
            Number value = valueFunc.apply(values.get(i));
            if (!findNotNull && value != null) {
                findNotNull = true;
                startNotNullIndex = i;
            } else {
                if (findNotNull && value == null) {
                    endNotNullIndex = i;
                    return Pair.of(startNotNullIndex, endNotNullIndex);
                }
            }
        }
        return Pair.of(startNotNullIndex, endNotNullIndex);
    }

    private static Integer findLastValue(List<Metric> values, Function<Metric, Number> valueFunc, boolean beNull) {
        for (int i = 0; i < values.size(); i++) {
            if (beNull && valueFunc.apply(values.get(i)) != null) {
                return i == 0 ? null : i - 1;
            }
            if (!beNull && valueFunc.apply(values.get(i)) == null) {
                return i == 0 ? null : i - 1;
            }
        }
        return null;
    }

    public static void sort(List<Metric> values) {
        values.sort(Comparator.comparing(Metric::getTimestamp));
    }

    public static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
