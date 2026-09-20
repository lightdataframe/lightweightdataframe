package io.github.lightdataframe.lightweightdataframe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



/**
 * Provides mechanisms to filter rows from a {@link Dataframe} based on specified conditions.
 * It allows combining multiple {@link RowFilter} instances for flexible and complex row filtering operations.
 * Supports predefined filter operations such as head, tail,selecting rows between indices, or testing row values against certain conditions.
 */
public class Filter implements Serializable, Cloneable
{
    private final List<RowFilter> filters = new ArrayList<>();

    /**
     * Adds a new {@link RowFilter} to the list of filters applied by this Filter instance.
     * The added filter contributes to the logic for filtering rows in a dataframe.
     * For a row to be retained, all filters in the list must return true for it.
     *
     * @param filter the RowFilter condition to be added
     * @return the current Filter instance, allowing for method chaining
     */
    public Filter addRowFilter(RowFilter filter)
    {
        filters.add(filter);
        return this;
    }


    /**
     * Limits the number of rows to be processed by retaining only the first {@code n} rows.
     *
     * @param n the number of rows to retain from the beginning of the dataframe
     * @return the current Filter instance with the added filter condition
     */
    public Filter head(int n)
    {
        return addRowFilter((df, i) -> i < n);
    }

    /**
     * Retains the last {@code n} rows from the dataframe when applying the filter.
     *
     * @param n the number of rows to retain from the end of the dataframe
     * @return the current Filter instance with the added filter condition
     */
    public Filter tail(int n)
    {
        return addRowFilter((df, i) -> i >= df.size() - n);
    }

    /**
     * Filters rows based on their indices, retaining rows with indices within the specified range.
     * The range is inclusive of the start index and exclusive of the end index.
     *
     * @param start the starting index (inclusive) of the range
     * @param end the ending index (exclusive) of the range
     * @return the current Filter instance with the applied range filter
     */
    public Filter between(int start, int end)
    {
        return addRowFilter((df, i) -> i >= start && i < end);
    }

    /**
     * Filters rows in a dataframe based on the specified condition.
     *
     * @param rowTest a predicate that tests each row of the dataframe.
     * @return the current Filter instance with the applied row filter.
     */
    public Filter valuesTest(Predicate<Map<String, Double>> rowTest)
    {
        return addRowFilter((df, i) -> rowTest.test(df.getRow(i)));
    }

    /**
     * Filters rows in the given dataframe based on applied filter conditions.
     * Each row in the dataframe is evaluated, and only rows that satisfy
     * all conditions are included in the result.
     *
     * @param df the dataframe to be filtered
     * @return a new dataframe containing only the rows that satisfy all filter conditions
     */
    public Dataframe filter(Dataframe df)
    {
        List<Map<String, Double>> filteredData = IntStream.range(0, df.size())
                .filter(i -> filters.stream().allMatch(f -> f.testRow(df, i)))
                .mapToObj(df::getRow)
                .collect(Collectors.toList());
        Dataframe result = new Dataframe();
        result.data = filteredData;
        return result;
    }

    /**
     * Returns a shallow copy of the filter
     * @return  a shallow copy of the filter
     */
    @Override
    protected Filter clone() throws CloneNotSupportedException
    {
        Filter copy = new Filter();
        copy.filters.addAll(filters);
        return copy;
    }
}
