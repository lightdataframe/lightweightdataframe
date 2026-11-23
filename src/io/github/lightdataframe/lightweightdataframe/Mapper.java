package io.github.lightdataframe.lightweightdataframe;

import java.util.function.ToDoubleFunction;



/**
 * Provides factory methods for common {@link RowMapper}.
 */
public class Mapper
{
    /**
     * Strategy to map rows to double values using a provided function.
     *
     * @param mapper function to apply to each row
     * @return a row mapper that maps each row to a double value using the provided function
     */
    public static RowMapper mapByRow(ToDoubleFunction<Row> mapper)
    {
        if (mapper == null)
            throw new IllegalArgumentException("mapper is null");

        return (df, n) -> mapper.applyAsDouble(df.getRow(n));
    }

    /**
     * Strategy to map indices to double values using a provided function.
     *
     * @param mapper function to apply to each index
     * @return a row mapper that maps indices to double values using the provided function
     */
    public static RowMapper mapByIndex(ToDoubleFunction<Integer> mapper)
    {
        if (mapper == null)
            throw new IllegalArgumentException("mapper is null");

        return (df, n) -> mapper.applyAsDouble(n);
    }

    /**
     * Strategy to map a column to double values using a provided function.
     *
     * @param column      name of the column to map
     * @param valueMapper function to apply to each column value
     * @return a row mapper that maps a column to double values using the provided function
     * @throws IllegalArgumentException if the column is not found in the dataframe
     */
    public static RowMapper mapByColumn(String column, ToDoubleFunction<Double> valueMapper)
    {
        if (column == null)
            throw new IllegalArgumentException("column is null");
        if (valueMapper == null)
            throw new IllegalArgumentException("valueMapper is null");

        return (df, n) ->
        {
            if (!df.getColumns().contains(column))
                throw new IllegalArgumentException("column " + column + " not in the dataframe");
            return valueMapper.applyAsDouble(df.getRow(n).get(column));
        };
    }
}
