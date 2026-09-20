package io.github.lightdataframe.lightweightdataframe;

import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;




/**
 * Implementation of a dataframe, a tabular data structure for double values.
 */
public class Dataframe implements Serializable, Cloneable
{
    protected List<Map<String, Double>> data = new ArrayList<>();


    /**
     * Constructs an empty dataframe
     */
    public Dataframe(){}


    /**
     * Retrieves the list of column names from the data structure.
     * If the data is empty, an empty list is returned. Otherwise,
     * it provides an unmodifiable list of column names.
     *
     * @return a list of column names, or an empty list if the data is empty
     */
    public List<String> getColumns()
    {
        if (data.isEmpty())
            return new ArrayList<>();
        return Collections.unmodifiableList(new ArrayList<>(data.get(0).keySet()));
    }


    /**
     * Retrieves a column from the Dataframe specified by its name.
     * The column data is represented as a Series of Double values. Modifications to the Series will not affect the Dataframe.
     *
     * @param column The name of the column to retrieve. If the dataframe is not empty it must exist in the Dataframe.
     * @return A Series containing the values of the specified column. If the Dataframe is empty, an empty Series is returned.
     * @throws IllegalArgumentException If the specified column name does not exist in the Dataframe.
     */
    public Series getColumn(String column)
    {
        if (!data.isEmpty() && !data.get(0).containsKey(column))
            throw new IllegalArgumentException("Column " + column + " not found");


        if (data.isEmpty())
            return new Series();

        return data.stream()
                .map(m -> m.get(column))
                .collect(Series.collector(v -> v));
    }


    /**
     * Sets a column in the dataframe with the specified name and data from the given series.
     * If the dataframe already contains rows, the series must have the same size as the dataframe.
     * If the dataframe is empty, the new column will initialize the dataframe with rows corresponding
     * to the size of the series.
     *
     * @param column the name of the column to set or add
     * @param series the series containing the data for the column
     * @return a new Dataframe instance with the specified column set
     * @throws IllegalArgumentException if the series size does not match the existing dataframe size
     */
    public Dataframe setColumn(String column, Series series)
    {
        if(!data.isEmpty() && data.size() != series.size())
            throw new IllegalArgumentException("Series size does not match dataframe size");


        List<Map<String, Double>> newData;

        if(data.isEmpty())
        {
            newData = series.stream()
                    .map(v -> {
                        Map<String, Double> map = new LinkedHashMap<>();
                        map.put(column, v);
                        return map;
                    })
                    .collect(Collectors.toList());
        }
        else
        {
            newData = Stream.iterate(0, i -> i + 1)
                    .limit(size())
                    .map(i ->
                    {
                        Map<String, Double> row = new LinkedHashMap<>(getRow(i));
                        row.put(column, series.get(i));
                        return row;
                    })
                    .collect(Collectors.toList());
        }
        Dataframe d = new Dataframe();
        d.data = newData;
        return d;
    }

    public Dataframe replaceColumn(String column, Function<Series, Series> mapFunction)
    {
        if(!getColumns().contains(column))
            throw new IllegalArgumentException("Column " + column + " not found");

        Series colSeries = getColumn(column);
        Series newSeries = mapFunction.apply(colSeries);
        if(newSeries.size() != colSeries.size())
            throw new IllegalArgumentException("Series size must be equal to column size");

        return setColumn(column, newSeries);
    }


    /**
     * Renames a specified column in the Dataframe to a new name. The column's existing data
     * is preserved under the new name. A new Dataframe instance is returned with the renamed column.
     *
     * @param column The current name of the column to be renamed. Must exist in the Dataframe.
     * @param newName The new name for the column. It must be a unique name not already present in the Dataframe.
     * @return A new Dataframe instance with the specified column renamed.
     * @throws IllegalArgumentException If the specified column does not exist in the Dataframe.
     */
    public Dataframe renameColumn(String column, String newName)
    {
        if(!getColumns().contains(column))
            throw new IllegalArgumentException("Column " + column + " not found");


        List<Map<String, Double>> newData = data.stream()
                .map(row -> {
                    Map<String, Double> map = new LinkedHashMap<>(row);
                    map.put(newName, map.remove(column));
                    return map;
                })
                .collect(Collectors.toList());
        Dataframe d = new Dataframe();
        d.data = newData;
        return d;
    }


    /**
     * Removes the specified column from the Dataframe and returns a new Dataframe
     * instance without the removed column.
     *
     * @param column the name of the column to be removed
     * @return a new Dataframe instance without the specified column
     * @throws IllegalArgumentException if the specified column does not exist in the Dataframe
     */
    public Dataframe dropColumn(String column)
    {
        if(!getColumns().contains(column))
            throw new IllegalArgumentException("Column " + column + " not found");


        List<Map<String, Double>> newData = data.stream()
                .map(row -> {
                    Map<String, Double> newRow = new LinkedHashMap<>(row);
                    newRow.remove(column);
                    return newRow;
                })
                .collect(Collectors.toList());
        Dataframe d = new Dataframe();
        d.data = newData;
        return d;
    }


    /**
     * Retrieves the rows of the dataframe as a list of maps, where each map represents a row
     * with column names as keys and their corresponding values as doubles.
     * Rows keys are ordered by the column names in the dataframe.
     * Modifications to the rows are not propagated to the Dataframe.
     *
     * @return a list of maps, where each map contains the column-value pairs for a row.
     */
    public List<Map<String, Double>> getRows()
    {
        return Collections.unmodifiableList(data.stream().map(LinkedHashMap::new).collect(Collectors.toList()));
    }


    /**
     * Retrieves a row from the data structure at the specified index.
     * Row keys are ordered by the column names in the dataframe.
     * Modifications to the rows are not propagated to the Dataframe.
     *
     * @param n the index of the row to retrieve; must be within valid bounds (0 to data.size() - 1)
     * @return a map representing the row at the specified index, where the key is a String and the value is a Double
     * @throws IllegalArgumentException if the index is out of bounds
     */
    public Map<String, Double> getRow(int n)
    {
        if(n < 0 || n >= data.size())
            throw new IllegalArgumentException("Index out of bounds");

        return new LinkedHashMap<>(data.get(n));
    }


    /**
     * Calculates the total number of rows in the dataframe.
     *
     * @return the size of the dataframe
     */
    public int size()
    {
        return data.size();
    }


    /**
     * Appends a new row to the Dataframe.
     * If the `inplace` parameter is true, the operation modifies the current Dataframe, otherwise, a new Dataframe is returned.
     * If the Dataframe already contains data, the row's keys must match the existing column names.
     *
     * @param inplace A boolean flag indicating whether the operation should be performed in-place.
     *                If true, the current Dataframe is modified. If false, a new Dataframe instance is created and returned.
     * @param row     A map representing the row to be appended. Keys represent column names, and values represent the associated data.
     * @return The modified Dataframe (current or new) with the appended row.
     * @throws IllegalArgumentException If the supplied row contains keys (column names) that do not match the existing columns of the Dataframe.
     */
    public Dataframe append(boolean inplace, Map<String, Double> row)
    {
        if(!data.isEmpty() && !row.keySet().containsAll(getColumns()))
            throw new IllegalArgumentException("Attempting to add a row with invalid columns");


        Dataframe df = inplace ? this : new Dataframe();
        LinkedHashMap<String, Double> newRow;

        if(data.isEmpty())
        {
            newRow = new LinkedHashMap<>(row);
        }
        else
        {
            newRow = getColumns().stream()
                    .collect(Collectors.toMap(
                            column -> column,
                            row::get,
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));
        }

        df.data.add(newRow);
        return df;
    }


    /**
     * Removes rows from the Dataframe where a value in the specified column satisfies the given predicate.
     *
     * @param column The name of the column to evaluate for filtering rows. Must exist in the Dataframe.
     * @param predicate A predicate function that returns true for rows to be removed based on the value in the specified column.
     * @return A new Dataframe containing rows that do not satisfy the given predicate for the specified column.
     * @throws IllegalArgumentException If the specified column does not exist in the Dataframe.
     */
    public Dataframe dropRows(String column, Predicate<Double> predicate)
    {
        if(!getColumns().contains(column))
            throw new IllegalArgumentException("Column " + column + " not found");


        List<Map<String, Double>> newData = data.stream()
                .filter(row -> predicate.test(row.get(column)))
                .collect(Collectors.toList());
        Dataframe d = new Dataframe();
        d.data = newData;
        return d;
    }


    /**
     * Reorders the columns of the Dataframe based on the specified column order.
     * Columns not mentioned in the order will be appended at the end, in the order of the dataframe's columns.
     *
     * @param columns The names of the columns in the desired order. Unspecified columns will be appended afterward.
     * @return A new Dataframe with the columns rearranged in the specified order.
     * @throws IllegalArgumentException if one or more specified columns do not exist in the Dataframe.
     */
    public Dataframe reorderColumns(String... columns)
    {
        if (columns.length > 0 && !getColumns().containsAll(Arrays.asList(columns)))
            throw new IllegalArgumentException("One or more columns not found: [" + Arrays.toString(columns) + "] in [" + getColumns() + "]");


        List<String> currentColumns = getColumns();
        currentColumns.sort(Comparator.comparing(column -> Arrays.binarySearch(columns, column)));
        List<Map<String, Double>> newData = data.stream()
                .map(row ->
                {
                    Map<String, Double> prevRow = new LinkedHashMap<>(row);
                    Map<String, Double> newRow = new LinkedHashMap<>();
                    Arrays.stream(columns).forEach(column -> newRow.put(column, prevRow.remove(column)));
                    newRow.putAll(prevRow);
                    return newRow;
                })
                .collect(Collectors.toList());
        Dataframe d = new Dataframe();
        d.data = newData;
        return d;
    }


    /**
     * Computes a statistical summary for each column in the dataframe and returns them as a new dataframe.
     * Each row of the returned dataframe corresponds to one column of the original dataframe, with the metrics as columns.
     * The computed metrics are <br>
     * - size <br>
     * - mean <br>
     * - standard deviation<br>
     * - variance <br>
     * - minimum value<br>
     * - maximum value<br>
     * - median <br>
     * - first element<br>
     * - middle element <br>
     * - last element<br>
     * If the dataframe is empty all metrics are set to NaN.
     *
     * @return A new {@code Dataframe} containing the statistical metrics for each column. Each row of the new dataframe
     * represents one column of the original dataframe, with the metrics as columns. Rows order correspond to the
     * column order of the original dataframe.
     */
    public Dataframe statistics()
    {
        List<Map<String, Double>> rows = getColumns()
                .stream()
                .map(column ->
                {
                    Series columnSeries = getColumn(column);
                    Map<String, Double> stats = new LinkedHashMap<>();
                    stats.put("mean", columnSeries.mean());
                    stats.put("std", columnSeries.std());
                    stats.put("var", columnSeries.var());
                    stats.put("min", columnSeries.min());
                    stats.put("max", columnSeries.max());
                    stats.put("median", columnSeries.median());
                    stats.put("first", columnSeries.first());
                    stats.put("middle", columnSeries.middle());
                    stats.put("last", columnSeries.last());
                    return stats;
                })
                .collect(Collectors.toList());

        Dataframe df = new Dataframe();
        df.data = rows;
        return df;
    }


    private static final int maxPrintedRows = 13;


    /**
     * Prints to standard output a formatted representation of the data contained in the dataframe.
     */
    public void print()
    {
        StringBuilder sb = new StringBuilder();
        List<String> columns = getColumns();
        buildHeader(sb, columns);
        if(size() < maxPrintedRows)
        {
            for (Map<String, Double> row : data)
                buildStatsLine(sb, "", row);
        }
        else
        {
            List<Map<String, Double>> head = data.stream()
                    .limit(maxPrintedRows / 2)
                    .collect(Collectors.toList());
            for (Map<String, Double> row : head)
                buildStatsLine(sb, "", row);

            sb.append(String.format("%20s", ""));
            columns.forEach(col -> sb.append(String.format("%20s", "...")));
            sb.append("\n");

            List<Map<String, Double>> tail = data.stream()
                    .sorted(Comparator.comparingInt(row -> - data.indexOf(row)))
                    .limit(maxPrintedRows / 2)
                    .collect(Collectors.toList());
            for (Map<String, Double> row : tail)
                buildStatsLine(sb, "", row);
        }
        System.out.print(sb);
    }


    /**
     * Prints to standard output the statistical summary of data in a tabular format.
     * See {@link #statistics()} for more details.}
     */
    public void printStatistics()
    {
        List<String> columns = getColumns();
        StringBuilder sb = new StringBuilder();

        Dataframe stats = statistics();

        buildHeader(sb, stats.getColumns());

        if (size() == 0)
            buildStatsLine(sb, "", stats.getRow(0));

        for (int c = 0; c < columns.size(); c++)
            buildStatsLine(sb, columns.get(c), stats.getRow(c));

        System.out.print(sb);
    }

    private void buildHeader(StringBuilder sb, List<String> columns)
    {
        sb.append(String.format("%20s", ""));
        for(String col : columns)
            sb.append(String.format("%20s", col));
        sb.append("\n");
    }


    private void buildStatsLine(StringBuilder sb, String columnName, Map<String, Double> row)
    {
        sb.append(String.format("%20s", columnName));
        for(Double cval : row.values())
            sb.append(String.format("%20.3f", cval));
        sb.append("\n");
    }

    /**
     * Returns a deep copy of the dataframe
     * @return a deep copy of the dataframe
     */
    public Dataframe clone()
    {
        Dataframe df = new Dataframe();
        df.data = data.stream().map(LinkedHashMap::new).collect(Collectors.toList());
        return df;
    }


    @Override
    public String toString()
    {
        return String.format(
                "Dataframe(size=%d, columns=[%s])",
                data.size(),
                String.join(", ", getColumns())
                );
    }

    /**
     * Compares the dataframe to the specified object for equality.
     * Returns true if the specified object is a dataframe and the contained data is exactly the same.
     *
     * @param o the object to be compared for equality with this instance
     * @return true if the specified object is equal to this instance; false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Dataframe dataframe = (Dataframe) o;
        return Objects.equals(data, dataframe.data);
    }


    /**
     * Filters the current dataframe using the specified {@link Filter}.
     *
     * @param filter the filter to apply to the dataframe
     * @return a new dataframe containing only the rows that meet the filter criteria
     */
    public Dataframe filter(Filter filter)
    {
        return filter.filter(this);
    }


    /**
     * Groups the current dataframe based on the specified {@link Grouper} logic.
     *
     * @param grouper the {@link Grouper} instance defining the grouping logic
     * @return a map where the keys represent group identifiers and the values are dataframes for each group
     */
    public Map<Object, Dataframe> group(Grouper grouper)
    {
        return grouper.group(this);
    }


    /**
     * Groups and aggregates the data in the current dataframe using the specified aggregator
     * and grouper. The grouper defines how the data is grouped, while the aggregator computes
     * aggregated values for the grouped data.
     *
     * @param grouper    a {@link Grouper} instance that defines the grouping logic for the data.
     * @param aggregator an {@link Aggregator} instance responsible for computing aggregated values
     *                   for the grouped data.
     * @return a new dataframe containing the grouped and aggregated data.
     */
    public Dataframe groupAggregate(Grouper grouper, Aggregator aggregator)
    {
        return aggregator.groupAggregate(this, grouper);
    }


    /**
     * Sorts the dataframe using the provided Sorter and returns a new sorted Dataframe.
     *
     * @param sorter a Sorter instance that defines the sorting criteria to be applied to the Dataframe
     * @return a new Dataframe instance sorted according to the specified criteria
     */
    public Dataframe sort(Sorter sorter)
    {
        return sorter.sort(this);
    }


    /**
     * Generates and displays a line plot for every column.
     */
    public void plot()
    {
        new Plotter().line(this);
    }



    /**
     * Creates a new collector for converting a stream of elements into a Dataframe.
     * The collector uses user-defined mapping functions to derive numeric values for columns from
     * the elements in the input stream, enabling flexible configurations for constructing a Dataframe.
     *
     * @param <T> the type of elements in the input stream
     * @return a {@code Dataframe.Collector} instance that can be used to collect stream elements
     *         into a Dataframe structure
     */
    public static <T> Dataframe.Collector<T> collector()
    {
        return new Dataframe.Collector<>();
    }


    /**
     * Collector to transform a stream of objects into a {@link Dataframe}.
     * The collector uses user-defined mapping functions to derive numeric values for columns from
     * the elements in the stream.<br>
     * For example,<br>
     * {@code collector.addColumn("age", Person::getAge);} <br>
     * {@code collector.addColumn("weight", Person::getWeight);} <br>
     * will result in a dataframe with two columns age and weight.
     * @param <T> the generic type of input elements to be processed by the collector
     *            for each column.
     */
    public static class Collector<T> implements java.util.stream.Collector<T, List<Map<String, Double>>, Dataframe>
    {
        private final Map<String, Function<T, Double>> functions = new HashMap<>();

        private Collector() {}

        public Collector<T> addColumn(String columnName, Function<T, Number> columnGetter)
        {
            functions.put(columnName, x -> columnGetter.apply(x).doubleValue());
            return this;
        }

        @Override
        public Supplier<List<Map<String, Double>>> supplier()
        {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<Map<String, Double>>, T> accumulator()
        {
            return (acc, data) ->
            {
                Map<String, Double> row = functions.keySet()
                        .stream()
                        .collect(Collectors.toMap(k -> k, k -> functions.get(k).apply(data)));
                acc.add(row);
            };
        }

        @Override
        public BinaryOperator<List<Map<String, Double>>> combiner()
        {
            return (acc0, acc1) ->
            {
                acc0.addAll(acc1);
                return acc0;
            };
        }

        @Override
        public Function<List<Map<String, Double>>, Dataframe> finisher()
        {
            return data ->
            {
                Dataframe dataframe = new Dataframe();
                dataframe.data = data;
                return dataframe;
            };
        }

        @Override
        public Set<Characteristics> characteristics()
        {
            return new HashSet<>();
        }
    }
}
