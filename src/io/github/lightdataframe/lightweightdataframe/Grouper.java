package io.github.lightdataframe.lightweightdataframe;

import java.io.Serializable;
import java.util.*;

/**
 * Provides functionality to group rows of a {@link Dataframe} based on custom grouping strategies.
 * Rows are mapped to groups using a {@link RowGrouper} strategy and can belong to multiple groups.
 * <p>
 * For example, grouping the given rows:<br>
 * row0: [group0, group1]<br>
 * row1: [group1, group2]<br>
 * row2: [group2]<br>
 * <p>
 * results into the following groups:<br>
 * group0: [row0]<br>
 * group1: [row0, row1]<br>
 * group2: [row1, row2]<br>
 */
public class Grouper implements Serializable, Cloneable
{
    private RowGrouper groupFunction = (df, n) -> Collections.singletonList(1L);

    /**
     * Sets the {@link RowGrouper} strategy for this Grouper.
     *
     * @param groupFunction the strategy that specifies how rows in a dataframe
     *                      should be grouped.
     * @return the current Grouper instance with the updated row grouping strategy.
     */
    public Grouper setRowGroup(RowGrouper groupFunction)
    {
        this.groupFunction = groupFunction;
        return this;
    }

    /**
     * Splits the dataframe into the specified number of groups. Rows are split in dataframe order.
     *
     * @param splits the number of groups to split the dataframe rows into. Must be greater than 0.
     * @return the current Grouper instance with the updated row grouping strategy.
     * @throws IllegalArgumentException if the number of splits is less than or equal to 0.
     */
    public Grouper split(int splits)
    {
        if(splits <= 0)
            throw new IllegalArgumentException("splits must be greater than 0");
        return setRowGroup((df, n) -> Collections.singletonList(n / splits));
    }

    /**
     * Groups the rows of a dataframe based on the specified columns. Each row will be grouped
     * according to its values in the specified columns.
     *
     * @param columns the column names to group the dataframe rows by
     * @return the current Grouper instance with the updated row grouping strategy
     * @throws IllegalArgumentException if one or more specified column names are not present in the dataframe
     */
    public Grouper groupByColumnValues(String... columns)
    {
        return setRowGroup((df, n) ->
        {
            if (!df.getColumns().containsAll(Arrays.asList(columns)))
                throw new IllegalArgumentException("One or more columns are not present in the dataframe");

            Map<String, Double> row = df.getRow(n);
            List<Object> groups = new ArrayList<>();

            for (String column : columns)
                groups.add(row.get(column));

            return Arrays.asList(groups);
        });
    }


    /**
     * Groups the rows based on a sliding window strategy.
     * The rows are grouped into windows of the specified size, with each window starting at intervals defined by the step size.
     *
     * @param step the interval at which each sliding window starts.
     * @param window the size of the sliding window. Must be a positive integer.
     * @return the current Grouper instance with the updated row grouping strategy
     * @throws IllegalArgumentException if either the step or window is less than or equal to 0.
     */
    public Grouper slidingWindow(int step, int window)
    {
        if (step <= 0 || window <= 0)
            throw new IllegalArgumentException("step and window must be positive");
        return setRowGroup((df, n) ->
        {
            if (n % step != 0)
                return Collections.emptyList();
            List<Object> groups = new ArrayList<>();
            for (int i = n; i >= Math.max(n - window, 0); i--)
                groups.add(i);
            return groups;
        });
    }

    /**
     * Groups the rows of the given dataframe into subsets based on a defined grouping strategy.
     * The rows are processed according to the specified {@link RowGrouper} strategy and grouped
     * into a map, where each key represents a group and the corresponding value is a
     * new dataframe containing rows belonging to that group.
     *
     * @param df the dataframe to be grouped
     * @return a map where each key is a group identifier and the corresponding value
     * is a dataframe containing the rows belonging to that group
     */
    public Map<Object, Dataframe> group(Dataframe df)
    {
        Map<Object, Dataframe> groups = new LinkedHashMap<>();

        for (int i = 0; i < df.size(); i++)
        {
            Map<String, Double> row = df.getRow(i);
            List<Object> rowGroups = groupFunction.getRowGroups(df, i);
            for (Object grp : rowGroups)
            {
                if (!groups.containsKey(grp))
                    groups.put(grp, new Dataframe());
                groups.get(grp).append(true, row);
            }

        }

        return groups;
    }

    /**
     * Returns a shallow copy of the grouper
     *
     * @return a shallow copy of the grouper
     */
    @Override
    protected Grouper clone() throws CloneNotSupportedException
    {
        Grouper copy = new Grouper();
        copy.groupFunction = groupFunction;
        return copy;
    }
}
