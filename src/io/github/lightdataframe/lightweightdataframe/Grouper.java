package io.github.lightdataframe.lightweightdataframe;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides factory methods for {@link RowGrouper} instances
 * <p>
 */
public class Grouper
{
    /**
     * Strategy to split the dataframe into a specified number of groups.
     *
     * @param splits the number of groups to split the dataframe into
     * @return a row grouper that splits the dataframe into the specified number of groups
     */
    public RowGrouper split(int splits)
    {
        if (splits <= 0)
            throw new IllegalArgumentException("splits must be greater than 0");

        return (df, n) -> Collections.singletonList(splits * n / df.size());
    }

    /**
     * Strategy to group the dataframe by the values of specific columns.
     *
     * @param columns the columns to use for grouping
     * @return a row grouper that groups rows based on the values of the specified columns
     */
    public RowGrouper groupByColumnValues(String... columns)
    {
        return (df, n) ->
        {
            if (!df.getColumns().containsAll(Arrays.asList(columns)))
                throw new IllegalArgumentException("One or more columns are not present in the dataframe");

            Map<String, Double> row = df.getRow(n);
            List<Object> groups = Arrays.stream(columns).map(row::get).collect(Collectors.toList());

            return Collections.singletonList(groups);
        };
    }

    /**
     * Strategy to group rows based on sliding window strategy
     *
     * @param window sliding window size
     * @return a row grouper that groups rows based on a sliding window strategy
     */
    public RowGrouper slidingWindow(int window)
    {
        return (df, n) ->
        {
            List<Object> groups = new ArrayList<>();
            for (int i = n; i >= Math.max(n - window + 1, 0); i--)
                groups.add(i);
            return groups;
        };
    }
}
