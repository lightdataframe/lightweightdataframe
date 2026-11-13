package io.github.lightdataframe.lightweightdataframe;


import java.util.Arrays;
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
     * @param columns ordered list of columns
     * @return a row scorer that scores rows based on the column values in column order
     */
    public static RowScorer sortByColumns(String ...columns)
    {
        AtomicReference<Double> max = new AtomicReference<>(null);
        AtomicReference<Double> min = new AtomicReference<>(null);
        return (df, n) ->
        {
            if(!df.getColumns().containsAll(Arrays.asList(columns)))
                throw new IllegalArgumentException("One or more columns not found: [" + String.join(", ", Arrays.asList(columns)) + "] in [" + String.join(", ", df.getColumns()) + "]");

            if(max.get() == null)
            {
                double maxValue = Arrays.stream(columns)
                        .mapToDouble(col -> df.getColumn(col)
                                .map(Math::abs)
                                .withoutNan()
                                .max()
                        )
                        .filter(v -> !Double.isNaN(v))
                        .max()
                        .orElse(Double.NaN);
                max.set(maxValue + 1);
            }
            if(min.get() == null)
            {
                double minValue = Arrays.stream(columns)
                        .mapToDouble(col -> df.getColumn(col)
                                .map(Math::abs)
                                .withoutNan()
                                .min()
                        )
                        .filter(v -> !Double.isNaN(v))
                        .min()
                        .orElse(Double.NaN);
                min.set(minValue - 1);
            }


            return IntStream.range(0, columns.length)
                    .mapToDouble(i -> {
                        double multiplier = Math.pow(max.get(), columns.length - i);
                        Row row = df.getRow(n);
                        double value = row.get(columns[i]);
                        return Double.isNaN(value) ? min.get() * multiplier : value * multiplier;
                    })
                    .reduce(0, Double::sum);
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
     * Strategy to score rows by the values of a specific column
     *
     * @param column column name to score each row
     * @param scorer function to apply to each row value to score it
     * @return a row scorer that scores rows based on the values of the specified column
     */
    public static RowScorer sortByColumn(String column, ToDoubleFunction<Double> scorer)
    {
        if(column == null)
            throw new IllegalArgumentException("column is null");
        if(scorer == null)
            throw new IllegalArgumentException("scorer is null");

        return (df, n) ->
        {
            Row row = df.getRow(n);
            if (!row.containsKey(column))
                throw new IllegalArgumentException("column " + column + " not found");
            return scorer.applyAsDouble(row.get(column));
        };
    }

    /**
     * Strategy to score rows randomly, shuffling the dataframe
     * @return a row scorer randomly scoring rows
     */
    public static RowScorer shuffle()
    {
        return  (df, n) -> Math.random();
    }
}
