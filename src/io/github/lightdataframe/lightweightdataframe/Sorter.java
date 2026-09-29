package io.github.lightdataframe.lightweightdataframe;


import java.util.function.ToDoubleFunction;



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
