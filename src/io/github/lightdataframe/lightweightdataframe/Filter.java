package io.github.lightdataframe.lightweightdataframe;

import java.util.Map;
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
     * Strategy to yield only the rows with specific values
     *
     * @param rowTest predicate to test each row
     * @return a row filter that yields only the rows that satisfy the predicate
     */
    public RowFilter rowTest(Predicate<Map<String, Double>> rowTest)
    {
        return (df, i) -> rowTest.test(df.getRow(i));
    }
}
