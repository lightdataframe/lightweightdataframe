package io.github.lightdataframe.lightweightdataframe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;


/**
 * Class to define and perform aggregation operations on a {@link Dataframe}.
 */
public class Aggregator
{
    private final Map<String, ToDoubleFunction<Dataframe>> aggregations = new LinkedHashMap<>();

    /**
     * Adds a new aggregation function to the aggregator.
     *
     * @param column     column name for the aggregation
     * @param aggregator function to apply to the dataframe to compute the aggregation value
     * @return the aggregator instance for method chaining
     */
    public Aggregator addAggregation(String column, ToDoubleFunction<Dataframe> aggregator)
    {
        aggregations.put(column, aggregator);
        return this;
    }

    /**
     * Performs the aggregation operation on the given dataframe. Aggregations have to be first defined
     * through the {@code addAggregation()} method
     *
     * @param df dataframe to perform the aggregation on
     * @return a map containing the aggregated values for each column
     */
    public Map<String, Double> aggregate(Dataframe df)
    {
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
