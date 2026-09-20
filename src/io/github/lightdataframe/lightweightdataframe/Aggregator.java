package io.github.lightdataframe.lightweightdataframe;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;


/**
 * Defines aggregation operations on a {@link Dataframe}.
 */
public class Aggregator implements Serializable, Cloneable
{
    /**
     * A mapping of output column names to input column names.
     * Used to define how aggregation operations map input data columns
     * to their corresponding resulting columns.
     */
    private final Map<String, String> columnsMapping = new LinkedHashMap<>();
    /**
     * A map that holds aggregation functions associated with column names.
     * The keys represent the names of the target columns for the results of
     * the aggregation, and the values are functions that define the aggregation
     * logic. Each function takes a {@code Series} as input and produces a
     * {@code Double} as the result of the aggregation.
     *
     * This map is used to configure and store multiple aggregation operations
     * to be applied on data, where each operation corresponds to a specific
     * target column.
     */
    private final Map<String, ToDoubleFunction<Series>> aggregations = new LinkedHashMap<>();


    /**
     * Defines an aggregation operation for a specified column in the dataframe.<br>
     * Examples:<br>
     * {@code addAggregation("age", "age_mean", Series::mean);}<br>
     * {@code addAggregation("height", "height_median", Series::median);}<br>
     *
     * @param column                the name of the dataframe column to aggregate values from
     * @param aggregationColumnName the name of the resulting column
     * @param aggregation           aggregation logic to be applied on the column
     * @return the current Aggregator instance to allow method chaining
     */
    public Aggregator addAggregation(String column, String aggregationColumnName, ToDoubleFunction<Series> aggregation)
    {
        columnsMapping.put(aggregationColumnName, column);
        aggregations.put(aggregationColumnName, aggregation);
        return this;
    }


    /**
     * Groups the rows of the Dataframe by unique values in the specified column.
     * Each unique value of the column becomes a key in the returned map,
     * and the corresponding value is a new Dataframe containing all rows
     * from the original Dataframe matching that key value.
     *
     * @param column The name of the column to group by. Must exist in the Dataframe.
     * @return A map where keys are unique values from the specified column and values are Dataframes
     *         containing the rows associated with each key.
     * @throws IllegalArgumentException If the specified column does not exist in the Dataframe.
     */
    public Map<Double, Dataframe> groupBy(Dataframe df, String column)
    {
        if(!df.getColumns().contains(column))
            throw new IllegalArgumentException("Column " + column + " not found");


        List<Double> uniqueValues = df.getColumn(column).stream().distinct().collect(Collectors.toList());
        Map<Double, Dataframe> groups = new LinkedHashMap<>();
        for(Double d : uniqueValues)
        {
            Dataframe group = new Dataframe();
            for(Map<String, Double> m : df.data)
            {
                if(Objects.equals(m.get(column), d))
                    group.data.add(m);
            }
            groups.put(d, group);
        }
        return groups;
    }



    /**
     * Performs aggregations specified through {@link #addAggregation(String, String, ToDoubleFunction)} on a dataframe.<br>.
     *
     * @param df the dataframe to aggregate
     * @return a map where the keys are the column names defined in the mapping and the values are the aggregated results
     * @throws IllegalArgumentException if the dataframe is null or if required columns for aggregation are not present in the dataframe
     */
    public Map<String, Double> aggregate(Dataframe df)
    {
        if (df == null)
            throw new IllegalArgumentException("Dataframe cannot be null");
        if (!df.getColumns().containsAll(columnsMapping.values()))
            throw new IllegalArgumentException("Aggregator contains columns not in dataframe");


        return columnsMapping.keySet()
                .stream()
                .collect(Collectors.toMap(
                        col -> col,
                        col -> aggregations.get(col).applyAsDouble(df.getColumn(columnsMapping.get(col))),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }


    /**
     * Performs a group-based aggregation on the given dataframe, by first grouping the dataframe by {@code groupByColumn}
     * and then applying the aggregate operation on each group. Group operations are merged in a resulting dataframe
     * where each row represents an aggregated result for a specific group.
     *
     * @param df      the dataframe to operate on
     * @param grouper the grouper to use for grouping the dataframe
     * @return a new dataframe resulting from the group-based aggregation
     * @throws IllegalArgumentException if the {@code groupByColumn} does not exist in the dataframe or if required columns
     *                                  for aggregation are not present in the dataframe
     */
    public Dataframe groupAggregate(Dataframe df, Grouper grouper)
    {
        if (!df.getColumns().containsAll(columnsMapping.values()))
            throw new IllegalArgumentException("base.Aggregator contains columns not in dataframe: " + aggregations.keySet());

        Map<Object, Dataframe> groups = grouper.group(df);
        List<Map<String, Double>> rows = groups.keySet()
                .stream()
                .map(group -> aggregate(groups.get(group)))
                .collect(Collectors.toList());
        df = new Dataframe();
        df.data = rows;
        return df;
    }

    /**
     * Returns a copy of the Aggregator instance.
     *
     * @return  a copy of the Aggregator instance.
     */
    @Override
    public Aggregator clone()
    {
        Aggregator a = new Aggregator();
        a.columnsMapping.putAll(columnsMapping);
        a.aggregations.putAll(aggregations);
        return a;
    }
}
