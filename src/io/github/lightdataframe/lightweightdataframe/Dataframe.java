package io.github.lightdataframe.lightweightdataframe;

import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;




/**
 * Implementation of a dataframe, a tabular data structure for double values.
 */
public class Dataframe implements Serializable, Cloneable
{
    protected List<Map<String, Double>> data = new ArrayList<>();


    /**
     * Constructs an empty dataframe.
     */
    public Dataframe(){}


    /**
     * Retrieves an unmodifiable list of column names.
     * If the data is empty, an empty list is returned.
     *
     * @return a list of column names, or an empty list if the dataframe is empty
     */
    public List<String> getColumns()
    {
        if (data.isEmpty())
            return Collections.unmodifiableList(new ArrayList<>());
        return Collections.unmodifiableList(new ArrayList<>(data.get(0).keySet()));
    }


    /**
     * Retrieves a column from the dataframe as a {@link Series}. Modifications to the sSeries will not affect the dataframe.
     *
     * @param column The name of the column to retrieve. If the dataframe is not empty, it must exist in the dataframe.
     * @return A series containing the values of the specified column. If the dataframe is empty, an empty series is returned.
     * @throws IllegalArgumentException If the specified column name does not exist in the dataframe
     */
    public Series getColumn(String column)
    {
        if(column == null)
            throw new IllegalArgumentException("column is null");
        if (!data.isEmpty() && !data.get(0).containsKey(column))
            throw new IllegalArgumentException("column " + column + " not found");

        if (data.isEmpty())
            return new Series();

        return data.stream()
                .map(m -> m.get(column))
                .collect(Series.collector(v -> v));
    }


    /**
     * Sets a column in the dataframe with the specified name and data from the given series. If the column already
     * exists it is replaced, or a new column is added otherwise
     * If the dataframe already contains rows, the series must have the same size as the dataframe.
     * If the dataframe is empty, the new column will initialize the dataframe.
     *
     * @param column the name of the column to set
     * @param series the series containing the data for the column
     * @return a new dataframe instance with the specified column set
     * @throws IllegalArgumentException if the series size does not match the existing dataframe size
     */
    public Dataframe setColumn(String column, Series series)
    {
        if(column == null)
            throw new IllegalArgumentException("column is null");
        if(series == null)
            throw new IllegalArgumentException("series is null");
        if(!data.isEmpty() && data.size() != series.size())
            throw new IllegalArgumentException("series size does not match dataframe size");


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
            newData = IntStream.range(0, size())
                    .mapToObj(i ->
                    {
                        Map<String, Double> row = new LinkedHashMap<>(data.get(i));
                        row.put(column, series.get(i));
                        return row;
                    })
                    .collect(Collectors.toList());
        }
        Dataframe d = new Dataframe();
        d.data = newData;
        return d;
    }

    /**
     * Replaces the values of a column in the dataframe with the results of applying a function to the column's current values.
     *
     * @param column      name of the column to be replaced. Must exist in the dataframe
     * @param mapFunction function that takes a series and returns a series of the same size
     * @return a new dataframe instance with the specified column replaced
     */
    public Dataframe replaceColumn(String column, Function<Series, Series> mapFunction)
    {
        if(column == null)
            throw new IllegalArgumentException("column is null");
        if(mapFunction == null)
            throw new IllegalArgumentException("mapFunction is null");
        if (!getColumns().contains(column))
            throw new IllegalArgumentException("column " + column + " not found");

        Series colSeries = getColumn(column);
        Series newSeries = mapFunction.apply(colSeries);
        if (newSeries.size() != colSeries.size())
            throw new IllegalArgumentException("series size must be equal to column size");

        return setColumn(column, newSeries);
    }


    /**
     * Renames a specified column in the dataframe to a new name. The column's existing data
     * is preserved under the new name.
     *
     * @param column The current name of the column to be renamed. Must exist in the dataframe
     * @param newName The new name for the column. It must be a unique name not already present in the dataframe
     * @return A new dataframe instance with the specified column renamed
     * @throws IllegalArgumentException If the specified column does not exist
     * @throws IllegalArgumentException If the specified new column already exists
     */
    public Dataframe renameColumn(String column, String newName)
    {
        if(column == null)
            throw new IllegalArgumentException("column is null");
        if(newName == null)
            throw new IllegalArgumentException("newName is null");
        if(!getColumns().contains(column))
            throw new IllegalArgumentException("column " + column + " not found");
        if(getColumns().contains(newName) && !column.equals(newName))
            throw new IllegalArgumentException("column " + newName + " already exists");

        List<Map<String, Double>> newData = data.stream()
                .map(row -> {
                    Map<String, Double> map = new LinkedHashMap<>();

                    for(String col : row.keySet())
                        if(col.equals(column))
                            map.put(newName, row.get(col));
                        else
                            map.put(col, row.get(col));

                    return map;
                })
                .collect(Collectors.toList());
        Dataframe d = new Dataframe();
        d.data = newData;
        return d;
    }


    /**
     * Removes the specified column from the dataframe.
     *
     * @param column the name of the column to be removed
     * @return a new dataframe instance without the specified column
     * @throws IllegalArgumentException if the specified column does not exist in the Dataframe
     */
    public Dataframe dropColumn(String column)
    {
        if(column == null)
            throw new IllegalArgumentException("column is null");
        if(!getColumns().contains(column))
            throw new IllegalArgumentException("column " + column + " not found");

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
     * Reorders the columns of the dataframe based on the specified column order.
     * Columns not mentioned in the order will be appended at the end, in the order of the current dataframe's columns.
     *
     * @param columns The names of the columns in the desired order. Unspecified columns will be appended afterward
     * @return A new Dataframe with the columns rearranged in the specified order
     * @throws IllegalArgumentException if one or more specified columns do not exist in the Dataframe
     */
    public Dataframe reorderColumns(String... columns)
    {
        for(String column : columns)
            if(column == null)
                throw new IllegalArgumentException("column is null");
        if (columns.length > 0 && !getColumns().containsAll(Arrays.asList(columns)))
            throw new IllegalArgumentException("one or more columns not found: [" + String.join(", ", columns) + "] in [" + String.join(", ", getColumns()) + "]");

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
     * Returns an unmodifiable view of the dataframe as a list of rows. In each row keys are ordered by the dataframe's column names
     *
     * @return an unmodifiable view of the dataframe as a list of rows
     */
    public List<Row> getRows()
    {
        return Collections.unmodifiableList(
                IntStream.range(0, data.size())
                        .mapToObj(i -> new Row(i, data.get(i)))
                        .collect(Collectors.toList())
        );
    }


    /**
     * Retrieves an unmodifiable view of the row at the specified index.
     * Row keys are ordered by the dataframe's column names.
     *
     * @param n the index of the row to retrieve. Must be within valid bounds (0 to size() - 1)
     * @return an unmodifiable row for the specified index
     * @throws IllegalArgumentException if the index is out of bounds
     */
    public Row getRow(int n)
    {
        if(n < 0 || n >= data.size())
            throw new IllegalArgumentException("index out of bounds");

        return new Row(n, data.get(n));
    }


    /**
     * Counts the number of rows in the dataframe
     *
     * @return the size of the dataframe
     */
    public int size()
    {
        return data.size();
    }


    /**
     * Appends a new row to the dataframe.
     * If the dataframe already contains data, the row's keys must match the existing column names.
     *
     * @param inplace A boolean flag indicating whether the operation should be performed in-place
     *                If true, the current dataframe is modified. If false, a new dataframe instance is created and returned
     * @param row     A map representing the row to be appended. Keys represent column names, and values represent the associated data
     * @return a new dataframe instance or this instance depending on inplace parameter
     * @throws IllegalArgumentException If the supplied row doesn't contain all column names of the dataframe
     */
    public Dataframe append(boolean inplace, Map<String, Double> row)
    {
        if(row == null)
            throw new IllegalArgumentException("row is null");

        if(!data.isEmpty() && !row.keySet().containsAll(getColumns()))
            throw new IllegalArgumentException(
                    "attempting to add a row with invalid columns: [" + String.join(", ", row.keySet()) + "]. Should be [" + String.join(", ", getColumns()) + "]"
            );

        row = new LinkedHashMap<>(row);
        for(String col : getColumns())
            if(row.get(col) == null)
                row.put(col, Double.NaN);


        Dataframe df = inplace ? this : clone();

        LinkedHashMap<String, Double> newRow;

        if(df.data.isEmpty())
            newRow = new LinkedHashMap<>(row);
        else
        {
            newRow = df.getColumns().stream()
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
     * Removes a row from the dataframe.
     * @param inplace whether the operation is inplace
     * @param n index of the row to remove
     * @return a new dataframe instance or this instance depending on the inplace parameter
     * @throws IllegalArgumentException if n < 0 or > size()
     */
    public Dataframe remove(boolean inplace, int n)
    {
        if(n < 0 || n >= data.size())
            throw new IllegalArgumentException("index out of bounds");

        Dataframe df = inplace ? this : clone();
        df.data.remove(n);
        return df;
    }

    /**
     * Returns a cell value
     *
     * @param column cell column
     * @param n      cell index
     * @return the value of the cell
     * @throws IllegalArgumentException if n < 0 or n >= size()
     * @throws IllegalArgumentException if {@code column} is not in the dataframe columns
     */
    public double get(String column, int n)
    {
        if (column == null)
            throw new IllegalArgumentException("column is null");
        if (n < 0 || n >= data.size())
            throw new IllegalArgumentException("index out of bounds");
        if (!getColumns().contains(column))
            throw new IllegalArgumentException("column " + column + " not found");

        return data.get(n).get(column);
    }

    /**
     * Sets the value of every cell for a given column
     * @param inplace whether the operation is inplace
     * @param column column to set
     * @param value new value to set the column with
     * @return a new dataframe instance or this instance depending on the inplace parameter
     * @throws IllegalArgumentException if n < 0 or n >= size()
     * @throws IllegalArgumentException if {@code column} is not in the dataframe columns
     */
    public Dataframe set(boolean inplace, String column, double value)
    {
        if(column == null)
            throw new IllegalArgumentException("column is null");
        if(!getColumns().contains(column))
            throw new IllegalArgumentException("column " + column + " not found");

        Dataframe df = inplace ? this : clone();
        df.data.forEach(row -> row.put(column, value));
        return df;
    }

    /**
     * Sets a cell value.
     *
     * @param inplace whether the operation is inplace
     * @param column  column to set
     * @param n       row index
     * @param value   value to set. if null {@code Double.NaN} is used
     * @return a new dataframe instance or this instance depending on the inplace parameter
     * @throws IllegalArgumentException if n < 0 or n >= size()
     * @throws IllegalArgumentException if {@code column} is not in the dataframe columns
     */
    public Dataframe set(boolean inplace, String column, int n, double value)
    {
        if(column == null)
            throw new IllegalArgumentException("column is null");
        if(n < 0 || n >= data.size())
            throw new IllegalArgumentException("index out of bounds");
        if(!getColumns().contains(column))
            throw new IllegalArgumentException("column " + column + " not found");

        Dataframe df = inplace ? this : clone();

        df.data.get(n).put(column, value);
        return df;
    }

    /**
     * Replaces cells with the given value.
     * @param inplace whether the operation is inplace
     * @param newValue new cell value
     * @param valueTest cell test
     * @return a new dataframe instance or this instance depending on the inplace parameter
     */
    public Dataframe replace(boolean inplace, double newValue, Predicate<Double> valueTest)
    {
        if(valueTest == null)
            throw new IllegalArgumentException("test is null");

        Dataframe df = inplace ? this : clone();
        for(Map<String, Double> row : df.data)
        {
            for(String col : row.keySet())
                if(valueTest.test(row.get(col)))
                    row.put(col, newValue);
        }
        return df;
    }

    /**
     * Replaces cells of a column with the given value.
     * @param inplace whether the operation is inplace
     * @param column column to replace values of
     * @param newValue new cell value
     * @param valueTest cell test
     * @return a new dataframe instance or this instance depending on the inplace parameter
     */
    public Dataframe replace(boolean inplace, String column, double newValue, Predicate<Double> valueTest)
    {
        if(!getColumns().contains(column))
            throw new IllegalArgumentException("column " + column + " not found");
        if(valueTest == null)
            throw new IllegalArgumentException("test is null");

        Dataframe df = inplace ? this : clone();
        for(Map<String, Double> row : df.data)
        {
            if(valueTest.test(row.get(column)))
                row.put(column, newValue);
        }
        return df;
    }

    /**
     * Computes a statistical summary for each column in the dataframe and returns them as a new dataframe.
     * Each row of the returned dataframe corresponds to one column of the original dataframe, with the metrics as columns.
     * The computed metrics are <br>
     * - size (non-NaN elements) <br>
     * - average <br>
     * - standard deviation<br>
     * - variance <br>
     * - minimum value<br>
     * - maximum value<br>
     * - median <br>
     * - first element<br>
     * - middle element <br>
     * - last element<br>
     * - has nan <br>
     * If the dataframe is empty a single row is returned with all metrics are set to NaN. <br>
     * For each column NaN values are first removed. As such, size indicates the number of non-NaN values<br>
     *
     * @return A new dataframe containing the statistical metrics for each column
     */
    public Dataframe statistics()
    {
        List<Map<String, Double>> rows;
        if(!data.isEmpty())
        {
            rows = getColumns()
                    .stream()
                    .map(column -> getColumn(column).withoutNan().statistics())
                    .collect(Collectors.toList());
        }
        else
        {
            rows = new ArrayList<>();
            Series s = new Series();
            rows.add(s.statistics());
        }

        Dataframe df = new Dataframe();
        df.data = rows;
        return df;
    }


    private static int maxPrintedRows = 13;

    /**
     * Sets the maximum number of row printed when calling {@link #print()}
     * If the number of rows is greater than this value, middle values are skipped and only the head and tail
     * @param n
     */
    public static void setMaxPrintedRows(int n)
    {
        if(n < 0)
            throw new IllegalArgumentException("n must >= 0");
        maxPrintedRows = n;
    }

    /**
     * Prints to standard output a formatted representation of the dataframe.
     */
    public void print()
    {
        StringBuilder sb = new StringBuilder();
        List<String> columns = getColumns();
        buildHeader(sb, columns);
        if(size() <= maxPrintedRows)
        {
            for (Map<String, Double> row : data)
                buildLine(sb, "", row);
        }
        else
        {
            List<Map<String, Double>> head = data.stream()
                    .limit(maxPrintedRows / 2)
                    .collect(Collectors.toList());
            for (Map<String, Double> row : head)
                buildLine(sb, "", row);

            sb.append(String.format("%20s", ""));
            columns.forEach(col -> sb.append(String.format("%20s", "...")));
            sb.append("\n");

            List<Map<String, Double>> tail = data.stream()
                    .sorted(Comparator.comparingInt(row -> - data.indexOf(row)))
                    .limit(maxPrintedRows / 2)
                    .collect(Collectors.toList());
            tail.sort(Comparator.comparingInt(row -> - tail.indexOf(row)));
            for (Map<String, Double> row : tail)
                buildLine(sb, "", row);
        }
        System.out.print(sb);
    }


    /**
     * Prints to standard output the statistical summary of the dataframe in a tabular format.
     * See {@link #statistics()} for more details
     */
    public void printStatistics()
    {
        List<String> columns = getColumns();
        StringBuilder sb = new StringBuilder();

        Dataframe stats = statistics();

        buildHeader(sb, stats.getColumns());

        if (size() == 0)
            buildLine(sb, "", stats.getRow(0));

        for (int c = 0; c < columns.size(); c++)
            buildLine(sb, columns.get(c), stats.getRow(c));

        System.out.print(sb);
    }

    /*
    Builds the header of the dataframe for print methods.
     */
    private void buildHeader(StringBuilder sb, List<String> columns)
    {
        sb.append(String.format("%20s", ""));
        for (String col : columns)
            sb.append(String.format("%20s", col));
        sb.append("\n");
    }

    /*
    Builds a line of the dataframe for print methods.
     */
    private void buildLine(StringBuilder sb, String columnName, Map<String, Double> row)
    {
        sb.append(String.format("%20s", columnName));
        for (Double cval : row.values())
            sb.append(String.format("%20.3f", cval));
        sb.append("\n");
    }


    /**
     * Returns a deep copy of the dataframe.
     * @return a deep copy of the dataframe
     */
    @Override
    public Dataframe clone()
    {
        Dataframe df = new Dataframe();
        df.data = data.stream().map(LinkedHashMap::new).collect(Collectors.toList());
        return df;
    }

    /**
     * Returns a compact string representation of the dataframe
     * @return  a compact string representation of the dataframe
     */
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
     * Sorts the dataframe rows by the given {@link RowScorer}.<br>
     * See {@link Sorter} for common scoring strategies.
     * @param ascending true if the rows should be sorted in ascending order, false for descending order
     * @param scorer    the scorer to use for sorting the rows
     * @return a new dataframe containing the sorted rows
     */
    public Dataframe sort(boolean ascending, RowScorer scorer)
    {
        if(scorer == null)
            throw new IllegalArgumentException("scorer is null");

        double dir = ascending ? 1 : -1;
        List<Map<String, Double>> sortedData = IntStream.range(0, size())
                .boxed()
                .sorted(Comparator.comparingDouble(n -> dir * scorer.getRowScore(this, n)))
                .map(n -> new LinkedHashMap<>(data.get(n)))
                .collect(Collectors.toList());
        Dataframe df = new Dataframe();
        df.data = sortedData;
        return df;
    }

    /**
     * Groups the dataframe rows by the given {@link RowGrouper}.<br>
     * For example, grouping the given rows in the following way:<br>
     * {@code row0: [group0, group1]}<br>
     * {@code row1: [group1, group2]}<br>
     * {@code row2: [group2]}<br>
     * <p>
     * results into the groups:<br>
     * {@code group0: [row0]}<br>
     * {@code group1: [row0, row1]}<br>
     * {@code group2: [row1, row2]}<br>
     * <p>
     * See {@link Grouper} for common grouping strategies.
     *
     * @param grouper grouper to group the rows by
     * @return a map of groups, where keys are group identifier values are dataframes containing the rows in the group.
     */
    public Map<Object, Dataframe> group(RowGrouper grouper)
    {
        if(grouper == null)
            throw new IllegalArgumentException("grouper is null");

        Map<Object, Dataframe> groups = new LinkedHashMap<>();

        for (int i = 0; i < size(); i++)
        {
            Map<String, Double> row = getRow(i);
            List<Object> rowGroups = grouper.getRowGroups(this, i);
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
     * Finds indices of rows matching the given {@link RowFilter}.
     * See {@link Filter} for common filtering strategies.
     * @param filter filter to use to match rows
     * @return indices of the rows matching the filter
     */
    public List<Integer> find(RowFilter filter)
    {
        if(filter == null)
            throw new IllegalArgumentException("filter is null");

        return IntStream.range(0, size())
                .filter(idx -> filter.testRow(this, idx))
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * Filters the dataframe rows by the given {@link RowFilter}.<br>
     * See {@link Filter} for common filtering strategies.
     *
     * @param filter filter to use for filtering the rows
     * @return a new dataframe containing the filtered rows
     */
    public Dataframe filter(RowFilter filter)
    {
        if(filter == null)
            throw new IllegalArgumentException("filter is null");

        List<Map<String, Double>> filteredData = IntStream.range(0, size())
                .filter(i -> filter.testRow(this, i))
                .mapToObj(i -> new LinkedHashMap<>(data.get(i)))
                .collect(Collectors.toList());
        Dataframe df = new Dataframe();
        df.data = filteredData;
        return df;
    }

    /**
     * Drops rows matching the given {@link RowFilter}.<br>
     * See {@link Filter} for common filtering strategies.
     *
     * @param filter filter to match rows to drop
     * @return a new dataframe without the dropped rows
     */
    public Dataframe drop(RowFilter filter)
    {
        if (filter == null)
            throw new IllegalArgumentException("filter is null");

        List<Map<String, Double>> filteredData = IntStream.range(0, size())
                .filter(i -> !filter.testRow(this, i))
                .mapToObj(i -> new LinkedHashMap<>(data.get(i)))
                .collect(Collectors.toList());
        Dataframe df = new Dataframe();
        df.data = filteredData;
        return df;
    }

    /**
     * Aggregates the dataframe rows by the given {@link Aggregator}.<br>
     *
     * @param aggregator aggregator to use for aggregating the rows
     * @return a single map containing the aggregated values
     */
    public Map<String, Double> aggregate(Aggregator aggregator)
    {
        if(aggregator == null)
            throw new IllegalArgumentException("aggregator is null");

        return aggregator.aggregate(this);
    }

    /**
     * Performs a group-and-aggregate operation on the dataframe, using the given {@link RowGrouper} and {@link RowFilter}.
     *
     * @param grouper    grouper to group the rows by
     * @param aggregator aggregator to use for aggregating the rows in each group
     * @return a new dataframe containing the aggregated values for each group
     */
    public Dataframe groupAggregate(RowGrouper grouper, Aggregator aggregator)
    {
        if(grouper == null)
            throw new IllegalArgumentException("grouper is null");
        if(aggregator == null)
            throw new IllegalArgumentException("aggregator is null");

        Map<Object, Dataframe> groups = group(grouper);

        List<Map<String, Double>> aggregated = groups.values()
                .stream()
                .map(aggregator::aggregate)
                .collect(Collectors.toList());


        Dataframe df = new Dataframe();
        df.data = aggregated;
        return df;
    }


    /**
     * Creates a new collector for converting a stream of elements into a dataframe.
     * The collector uses user-defined mapping functions to derive numeric values for columns from
     * the elements in the input stream, enabling flexible configurations for constructing a dataframe.
     *
     * @param <T> the type of elements in the input stream
     * @return a {@link Dataframe.Collector} instance that can be used to collect stream elements
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
     *
     * {@code collector.addColumn("age", Person::getAge);} <br>
     * {@code collector.addColumn("weight", Person::getWeight);} <br>
     *
     *
     *
     * will result in a dataframe with two columns age and weight.
     * @param <T> the generic type of input elements to be processed by the collector
     *            for each column.
     */
    public static class Collector<T> implements java.util.stream.Collector<T, List<Map<String, Double>>, Dataframe>
    {
        private final Map<String, Function<T, Double>> functions = new HashMap<>();

        private Collector() {}

        /**
         * Configures a column for the result Dataframe
         *
         * @param columnName   name of the column
         * @param columnGetter function to extract the column value from the input element
         * @return this instance for method chaining
         */
        public Collector<T> addColumn(String columnName, Function<T, Number> columnGetter)
        {
            functions.put(columnName, x -> {
                Number v = columnGetter.apply(x);
                if(v == null)
                    v = Double.NaN;
                return v.doubleValue();
            });
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
