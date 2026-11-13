package io.github.lightdataframe.lightweightdataframe;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides factory methods for common {@link RowGrouper}
 */
public class Grouper
{
    /**
     * Strategy to group rows based on row values. Each row is assigned to a single group.
     * @param grouper grouping function to apply to each row
     * @return a row grouper to group rows based on their values
     */
    public static RowGrouper groupByRow(Function<Row, Object> grouper)
    {
        if(grouper == null)
            throw new IllegalArgumentException("grouper is null");

        return (df, i) -> {
            Row row = df.getRow(i);
            return Collections.singletonList(grouper.apply(row));
        };
    }

    /**
     * Strategy to group rows by the values of the specified columns.
     *
     * @param columns the columns to use for grouping
     * @return a row grouper that groups rows based on the values of the specified columns
     */
    public static RowGrouper groupByColumnValues(String... columns)
    {
        for(String column : columns)
            if(column == null)
                throw new IllegalArgumentException("columns is null");

        return (df, n) ->
        {
            if (!df.getColumns().containsAll(Arrays.asList(columns)))
                throw new IllegalArgumentException("one or more columns are not present in the dataframe");

            Map<String, Double> row = df.getRow(n);
            List<Object> values = Arrays.stream(columns).map(row::get).collect(Collectors.toList());

            return Collections.singletonList(values);
        };
    }

    /**
     * Strategy to group rows into a specified number of non-overlapping groups.
     *
     * @param splits the number of groups to split rows into
     * @return a row grouper that splits rows into the specified number of groups
     */
    public static RowGrouper split(int splits)
    {
        if (splits <= 0)
            throw new IllegalArgumentException("splits must be greater than 0");

        return (df, n) -> Collections.singletonList(splits * n / df.size());
    }

    /**
     * Strategy to group rows based on a sliding window strategy
     *
     * @param step       step size
     * @param windowSize sliding window size
     * @return a row grouper that groups rows based on a sliding window strategy
     */
    public static RowGrouper slidingWindow(int step, int windowSize)
    {
        if(step <= 0 || windowSize <= 0)
            throw new IllegalArgumentException("step and windowSize must be greater than 0");

        return (df, n) ->
        {
            List<Object> groups = new ArrayList<>();
            for (int i = n; i >= Math.max(n - windowSize + 1, 0); i--)
                if(i % step == 0)
                    groups.add(i);
            return groups;
        };
    }

    /*
     * Strategy to group rows based on sliding window strategy with step equal to 1
     *
     * @param windowSize sliding window size
     * @return a row grouper that groups rows based on a sliding window strategy

    public static RowGrouper slidingWindow(int windowSize)
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
     */
    /**
     * Strategy to assign random groups to rows.
     * @param numGroups number of groups to assign the rows to
     * @param numGroupsPerRow number of groups each row belongs to
     * @return a row grouper that randomly groups rows
     */
    public static RowGrouper random(int numGroups, int numGroupsPerRow)
    {
        if(numGroups < 0)
            throw new IllegalArgumentException("numGroups must be greater or equal to 0");
        if(numGroupsPerRow < 0)
            throw new IllegalArgumentException("numGroupsPerRow must be greater or equal to 0");

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
