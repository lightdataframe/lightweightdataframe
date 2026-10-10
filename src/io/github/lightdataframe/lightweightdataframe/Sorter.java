package io.github.lightdataframe.lightweightdataframe;


import java.util.Arrays;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;


/**
 * Provides factory methods for sorting {@link Dataframe} objects.
 */
public class Sorter
{
    /**
     * Score rows of a dataframe by the values of a specific column.
     *
     * @param column column name to score each row
     * @return a row scorer that scores rows based on the values of the specified column
     */
    public RowScorer sortByColumn(String column)
    {
        return (df, n) ->
        {
            if (!df.getColumns().contains(column))
                throw new IllegalArgumentException("Column " + column + " not found");
            return df.getRow(n).get(column);
        };
    }

    /**
     * Score rows of a dataframe based on the values of the given columns
     * @param columns ordered list of columns
     * @return a row scorer that scores rows based on the column values in column order
     */
    public RowScorer sortByColumns(String ...columns)
    {
        return (df, n) ->
        {
            if(!df.getColumns().containsAll(Arrays.asList(columns)))
                throw new IllegalArgumentException("One or more columns not found: [" + String.join(", ", Arrays.asList(columns)) + "] in [" + String.join(", ", df.getColumns()) + "]");
            return IntStream.range(0, columns.length)
                    .mapToDouble(i -> {
                        double multiplier = Math.pow(1e4, columns.length - i);
                        return df.getRow(n).get(columns[i]) * multiplier;
                    })
                    .reduce(0, Double::sum);
        };
    }

    /**
     * Score rows of a dataframe by the values of a specific column.
     *
     * @param column column name to score each row
     * @param scorer function to apply to each row value to score it
     * @return a row scorer that scores rows based on the values of the specified column
     */
    public RowScorer sortByColumn(String column, ToDoubleFunction<Double> scorer)
    {
        return (df, n) ->
        {
            if (!df.getColumns().contains(column))
                throw new IllegalArgumentException("Column " + column + " not found");
            return scorer.applyAsDouble(df.getRow(n).get(column));
        };
    }

    /**
     * Score rows randomly
     * @return a random row scorer
     */
    public RowScorer shuffle()
    {
        return  (df, n) -> Math.random();
    }

}
