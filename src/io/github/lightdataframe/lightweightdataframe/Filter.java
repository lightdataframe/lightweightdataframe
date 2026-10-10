package io.github.lightdataframe.lightweightdataframe;


import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;


/**
 * Provides factory methods for {@link RowFilter} instances
 */
public class Filter
{
    /**
     * Strategy to yield only the first rows (head) of the dataframe
     *
     * @param n number of rows to yield
     * @return a row filter that yields only the first n rows
     */
    public RowFilter head(int n)
    {
        return (df, i) -> i < n;
    }

    /**
     * Strategy to yield only the last rows (tail) of the dataframe
     *
     * @param n number of rows to yield
     * @return a row filter that yields only the last n rows
     */
    public RowFilter tail(int n)
    {
        return (df, i) -> i >= df.size() - n;
    }

    /**
     * Strategy to yield only the rows withing a specified range
     *
     * @param start start index of the range inclusive
     * @param end   end index of the range exclusive
     * @return a row filter that yields only the rows with values in the specified range
     */
    public RowFilter between(int start, int end)
    {
        return (df, i) -> i >= start && i < end;
    }

    /**
     * Strategy to filter rows with a given probability
     * @param retainProbability probability of retaining a row
     * @return a row filter that yields rows with a given probability
     */
    public RowFilter random(double retainProbability)
    {
        return (df, i) -> {
            if(retainProbability < 0 || retainProbability > 1)
                throw new IllegalArgumentException("retainProbability must be between 0 and 1");
            return Math.random() < retainProbability;
        };
    }

    /**
     * Strategy to yield only the rows with specific values
     *
     * @param rowTest predicate to test each row
     * @return a row filter that yields only the rows that satisfy the predicate
     */
    public RowFilter rowTest(Predicate<Map<String, Double>> rowTest)
    {
        return (df, i) -> rowTest.test(df.getRow(i));
    }

    /**
     * Strategy that yields only rows with column value matching the predicate
     * @param column column to test
     * @param valueTest predicate to test the column value with
     * @return a row filter that yields only the rows passing the test
     */
    public RowFilter valueTest(String column, Predicate<Double> valueTest)
    {
        return (df, i) -> {
            if(!df.getColumns().contains(column))
                throw new IllegalArgumentException("Column " + column + " not found");
            Map<String, Double> row = df.getRow(i);
            return valueTest.test(row.get(column));
        };
    }

    /**
     * Strategy to yield only rows matching the given values
     * @param values values to test each row with
     * @return a row filter that yields only the rows with the given values
     */
    public RowFilter valuesMatch(Map<String, Double> values)
    {
        return (df, i) -> {
            if(!df.getColumns().containsAll(values.keySet()))
                throw new IllegalArgumentException("One or more columns not found: [" + String.join(", ", values.keySet()) + "] in [" + String.join(", ", df.getColumns()) + "]");

            Map<String, Double> row = df.getRow(i);
            return values.keySet().stream().allMatch(col -> Objects.equals(row.get(col), values.get(col)));
        };
    }
}
