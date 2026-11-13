package io.github.lightdataframe.lightweightdataframe;


import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;


/**
 * Provides factory methods for common {@link RowFilter}
 */
public class Filter
{

    /**
     * Strategy to retain only the rows with specific values
     *
     * @param rowTest predicate to test each row
     * @return a row filter that retains only the rows that satisfy the predicate
     */
    public static RowFilter rowTest(Predicate<Map<String, Double>> rowTest)
    {
        if(rowTest == null)
            throw new IllegalArgumentException("rowTest is null");

        return (df, i) -> rowTest.test(df.getRow(i));
    }

    /**
     * Strategy that retains only rows with column value matching the predicate
     * @param column column to test
     * @param valueTest predicate to test the column value with
     * @return a row filter that retains only the rows passing the test
     */
    public static RowFilter valueTest(String column, Predicate<Double> valueTest)
    {
        if(column == null)
            throw new IllegalArgumentException("column is null");
        if(valueTest == null)
            throw new IllegalArgumentException("valueTest is null");

        return (df, i) -> {
            if(!df.getColumns().contains(column))
                throw new IllegalArgumentException("column " + column + " not in the dataframe");
            Map<String, Double> row = df.getRow(i);
            return valueTest.test(row.get(column));
        };
    }

    /**
     * Strategy to retain only rows matching all the given values
     * @param values values to test each row with
     * @return a row filter that retains only the rows with the given values
     */
    public static RowFilter valuesMatch(Map<String, Double> values)
    {
        if(values == null)
            throw new IllegalArgumentException("values is null");

        return (df, i) -> {
            if(!df.getColumns().containsAll(values.keySet()))
                throw new IllegalArgumentException("one or more columns not found: [" + String.join(", ", values.keySet()) + "] in [" + String.join(", ", df.getColumns()) + "]");

            Map<String, Double> row = df.getRow(i);
            return values.keySet().stream().allMatch(col -> Objects.equals(row.get(col), values.get(col)));
        };
    }

    /**
     * Strategy to retain rows based on a given predicate
     * @param rowFilter predicate to test rows with
     * @return a row filter that retains rows based on the given predicate
     */
    public static RowFilter filterByRow(Predicate<Row> rowFilter)
    {
        if(rowFilter == null)
            throw new IllegalArgumentException("rowFilter is null");

        return (df, i) -> {
            Row row = df.getRow(i);
            return rowFilter.test(row);
        };
    }



    /**
     * Strategy to retain only the first {@code n} rows (head) of the dataframe
     *
     * @param n number of rows to retain
     * @return a row filter that retains only the first n rows
     */
    public static RowFilter head(int n)
    {
        return (df, i) -> i < n;
    }

    /**
     * Strategy to retain only the last {@code n} rows (tail) of the dataframe
     *
     * @param n number of rows to retain
     * @return a row filter that retains only the last n rows
     */
    public static RowFilter tail(int n)
    {
        return (df, i) -> i >= df.size() - n;
    }

    /**
     * Strategy to retain only the rows withing a specified range
     *
     * @param start start index of the range (inclusive)
     * @param end   end index of the range (exclusive)
     * @return a row filter that retains only the rows in the specified range
     */
    public static RowFilter between(int start, int end)
    {
        return (df, i) -> i >= start && i < end;
    }

    /**
     * Strategy to retain rows randomly by a given probability
     * @param retainProbability probability of retaining a row
     * @return a row filter that retains rows with a given probability
     */
    public static RowFilter random(double retainProbability)
    {
        if(retainProbability < 0 || retainProbability > 1)
            throw new IllegalArgumentException("retainProbability must be between 0 and 1");

        return (df, i) -> Math.random() < retainProbability;
    }
}
