package io.github.lightdataframe.lightweightdataframe;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides functionality for sorting {@link Dataframe} objects.
 */
public class Sorter implements Serializable, Cloneable
{
    private RowScorer scorer = (df, n) -> df.data.indexOf(df.getRow(n));

    /**
     * Sets the {@link RowScorer} strategy for scoring rows during sorting operations.
     *
     * @param scorer the {@link RowScorer} implementation to be used for scoring rows
     * @return the current {@link Sorter} instance, allowing for method chaining
     */
    public Sorter setRowScorer(RowScorer scorer)
    {
        this.scorer = scorer;
        return this;
    }


    /**
     * Sorts rows in the dataframe based on the values in a specified column.
     *
     * @param column the name of the column to sort by
     * @param ascending a boolean indicating whether sorting should be in ascending order
     *                  (true for ascending, false for descending)
     * @return the current Sorter instance with the configured sorting criteria
     */
    public Sorter sortByColumn(String column, boolean ascending)
    {
        return setRowScorer((df, n) -> (ascending ? 1 : -1) * df.getRow(n).get(column));
    }

    /**
     * Configures the {@link Sorter} instance to shuffle rows randomly when sorting.
     *
     * @return the current {@link Sorter} instance with the shuffle strategy applied,
     *         allowing for method chaining
     */
    public Sorter shuffle()
    {
        return setRowScorer((df, n) -> Math.random());
    }


    /**
     * Sorts the rows of the given {@link Dataframe} based on the current scoring strategy.
     *
     * @param df the Dataframe to be sorted
     * @return a new Dataframe with rows sorted according to the specified scoring strategy
     */
    public Dataframe sort(Dataframe df)
    {
        List<Map<String, Double>> sortedData = IntStream.range(0, df.size())
                .boxed()
                .sorted(Comparator.comparingDouble(n -> scorer.getRowScore(df, n)))
                .map(df::getRow)
                .collect(Collectors.toList());
        Dataframe result = new Dataframe();
        result.data = sortedData;
        return result;
    }


    /**
     * Returns a shallow copy of the sorter
     * @return  a shallow copy of the sorter
     */
    @Override
    protected Sorter clone() throws CloneNotSupportedException
    {
        Sorter copy = new Sorter();
        copy.scorer = scorer;
        return copy;
    }
}
