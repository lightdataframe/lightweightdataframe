package io.github.lightdataframe.lightweightdataframe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;


/**
 * Defines and perform aggregation operations on a {@link Dataframe}.
 */
public class Aggregator
{
    private final Map<String, ToDoubleFunction<Dataframe>> aggregations = new LinkedHashMap<>();


    /**
     * Adds a new aggregation function to the aggregator.
     *
     * @param column     column name for the aggregation
     * @param aggregation function to apply to the dataframe to compute the aggregation value
     * @return the aggregator instance for method chaining
     */
    public Aggregator addAggregation(String column, ToDoubleFunction<Dataframe> aggregation)
    {
        if(column == null)
            throw new IllegalArgumentException("column is null");
        if(aggregation == null)
            throw new IllegalArgumentException("aggregation is null");

        aggregations.put(column, aggregation);
        return this;
    }

    /**
     * Performs the aggregation operation on the given dataframe. Aggregations have to be first defined
     * through the {@link #addAggregation(String, ToDoubleFunction)} method.
     *
     * @param df    dataframe to perform the aggregation on
     * @return a map containing the aggregated values for each column
     */
    public Map<String, Double> aggregate(Dataframe df)
    {
        if (df == null)
            throw new IllegalArgumentException("df is null");

        return aggregations.keySet()
                .stream()
                .collect(Collectors.toMap(
                        k -> k,
                        k -> aggregations.get(k).applyAsDouble(df),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}
