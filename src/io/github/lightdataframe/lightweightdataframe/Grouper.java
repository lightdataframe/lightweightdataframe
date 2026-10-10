package io.github.lightdataframe.lightweightdataframe;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
     * @param step       step size
     * @param windowSize sliding window size
     * @return a row grouper that groups rows based on a sliding window strategy
     */
    public RowGrouper slidingWindow(int step, int windowSize)
    {
        if(windowSize <= 0)
            throw new IllegalArgumentException("windowSize must be greater than 0");
        if(step <= 0)
            throw new IllegalArgumentException("step must be greater than 0");
        return (df, n) ->
        {
            List<Object> groups = new ArrayList<>();
            for (int i = n; i >= Math.max(n - windowSize + 1, 0); i--)
                if(i % step == 0)
                    groups.add(i);
            return groups;
        };
    }

    /**
     * Strategy to group rows based on sliding window strategy
     *
     * @param windowSize sliding window size
     * @return a row grouper that groups rows based on a sliding window strategy
     */
    public RowGrouper slidingWindow(int windowSize)
    {
        if(windowSize <= 0)
            throw new IllegalArgumentException("windowSize must be greater than 0");
        return (df, n) ->
        {
            List<Object> groups = new ArrayList<>();
            for (int i = n; i >= Math.max(n - windowSize + 1, 0); i--)
                    groups.add(i);
            return groups;
        };
    }

    /**
     * Strategy to assign random groups to rows
     * @param numGroups number of groups to assign the rows to
     * @param numGroupsPerRow number of groups each row belongs to
     * @return a row grouper that randomly groups rows
     */
    public RowGrouper random(int numGroups, int numGroupsPerRow)
    {
        return  (df, n) ->
        {
            int m = numGroupsPerRow;
            List<Integer> groups = IntStream.range(0, numGroups).boxed().collect(Collectors.toList());
            List<Object> sample = new ArrayList<>();
            while(!groups.isEmpty() && m > 0)
            {
                int idx = (int) (groups.size() * Math.random());
                sample.add(groups.remove(idx));
                m--;
            }
            return sample;
        };
    }

}
