package io.github.lightdataframe.lightweightdataframe;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;


/**
 * Provides factory methods for common {@link RowScorer}
 */
public class Sorter
{

    /**
     * Strategy to score rows based on column values, first by the first column, then second, third, etc.
     *
     * @param nansLast whether nan values should be scored with minimum (nans last) or maximum (nans first) score
     * @param columns  ordered list of columns
     * @return a row scorer that scores rows based on the column values in column order
     */
    public static RowScorer sortByColumns(boolean nansLast, String... columns)
    {
        if(Arrays.stream(columns).anyMatch(Objects::isNull))
            throw new IllegalArgumentException("One or more columns are null");

        AtomicReference<Double> multiplierAbs = new AtomicReference<>(null);
        AtomicReference<Double> min = new AtomicReference<>(null);
        AtomicReference<Double> max = new AtomicReference<>(null);
        return (df, n) ->
        {
            if (!df.getColumns().containsAll(Arrays.asList(columns)))
                throw new IllegalArgumentException("One or more columns not found: [" + String.join(", ", Arrays.asList(columns)) + "] in [" + String.join(", ", df.getColumns()) + "]");

            if (multiplierAbs.get() == null)
            {
                double maxAbsValue = Arrays.stream(columns)
                        .mapToDouble(col -> df.getColumn(col)
                                .map(Math::abs)
                                .withoutNan()
                                .max()
                        )
                        .filter(v -> !Double.isNaN(v))
                        .max()
                        .orElse(Double.NaN);
                multiplierAbs.set(maxAbsValue + 2);
            }
            if (min.get() == null)
            {
                double minValue = Arrays.stream(columns)
                        .mapToDouble(col -> df.getColumn(col)
                                .withoutNan()
                                .min()
                        )
                        .filter(v -> !Double.isNaN(v))
                        .min()
                        .orElse(Double.NaN);
                min.set(minValue - 1);
            }
            if (max.get() == null)
            {
                double maxValue = Arrays.stream(columns)
                        .mapToDouble(col -> df.getColumn(col)
                                .withoutNan()
                                .max()
                        )
                        .filter(v -> !Double.isNaN(v))
                        .max()
                        .orElse(Double.NaN);
                max.set(maxValue + 1);
            }

            return IntStream.range(0, columns.length)
                    .mapToDouble(i ->
                    {
                        Row row = df.getRow(n);
                        double multiplier = Math.pow(multiplierAbs.get(), columns.length - i);
                        double value = row.get(columns[i]);
                        if (Double.isNaN(value) && nansLast)
                            value = min.get();
                        if (Double.isNaN(value) && !nansLast)
                            value = max.get();
                        return value * multiplier;
                    })
                    .reduce(0, Double::sum);
        };
    }


    /**
     * Strategy to score rows by the values of a specific column.
     *
     * @param nansLast whether nan values should be scored with minimum (nans last) or maximum (nans first) score
     * @param column   column name to score each row
     * @param scorer   function to apply to each row value to score it
     * @return a row scorer that scores rows based on the values of the specified column
     */
    public static RowScorer sortByColumn(boolean nansLast, String column, ToDoubleFunction<Double> scorer)
    {
        if (column == null)
            throw new IllegalArgumentException("column is null");
        if (scorer == null)
            throw new IllegalArgumentException("scorer is null");

        return (df, n) ->
        {
            Row row = df.getRow(n);
            if (!row.containsKey(column))
                throw new IllegalArgumentException("column " + column + " not found");

            double value = row.get(column);
            if (Double.isNaN(value) && nansLast)
                return - Double.MAX_VALUE;
            if(Double.isNaN(value) && !nansLast)
                return Double.MAX_VALUE;
            return scorer.applyAsDouble(row.get(column));
        };
    }

    /**
     * Strategy to score rows based on the provided function
     * @param scorer function to score rows with
     * @return a row scorer to score rows based on the provided function
     */
    public static RowScorer sortByRow(ToDoubleFunction<Row> scorer)
    {
        if(scorer == null)
            throw new IllegalArgumentException("Scorer is null");
        return (df, n) -> scorer.applyAsDouble(df.getRow(n));
    }

    /**
     * Strategy to score rows randomly, shuffling the dataframe
     * @return a row scorer randomly scoring rows
     */
    public static RowScorer shuffle()
    {
        Map<Integer, Double> scores = new HashMap<>();
        return  (df, n) -> {
            scores.putIfAbsent(n, Math.random());
            return scores.get(n);
        };
    }

    /**
     * Strategy to map the score of a row scorer to a new score using the provided function
     *
     * @param scorer scorer to map the score of
     * @param mapper function to map the score
     * @return a row scorer that maps the score of the provided scorer to a new score using the provided function
     */
    public static RowScorer map(RowScorer scorer, ToDoubleFunction<Double> mapper)
    {
        if (scorer == null)
            throw new IllegalArgumentException("scorer is null");
        if (mapper == null)
            throw new IllegalArgumentException("mapper is null");
        return (df, n) -> mapper.applyAsDouble(scorer.getRowScore(df, n));
    }
}
