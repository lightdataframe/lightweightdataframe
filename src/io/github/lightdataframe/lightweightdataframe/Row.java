package io.github.lightdataframe.lightweightdataframe;

import java.util.*;


/**
 * An unmodifiable view of a dataframe row
 */
public class Row implements Map<String, Double>
{
    private final Map<String, Double> row;
    private final int index;

    protected Row(int index, Map<String, Double> row)
    {
        this.index = index;
        this.row = Collections.unmodifiableMap(row);
    }

    /**
     * Returns the index of the row in the dataframe
     * @return the row index
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * Converts the row values into a {@link Series}. Series values are ordered in the dataframe column order.
     * Modifications to the series are not reflected in the dataframe
     * @return a series containing the row values ordered in column order
     */
    public Series toSeries()
    {
        return new Series(values());
    }

    /**
     * Returns the row size. Equal to the number of columns.
     * @return the row size
     */
    @Override
    public int size()
    {
        return row.size();
    }

    /**
     * Returns whether the row has no columns defined.
     *
     * @return true if the row is empty, false otherwise
     */
    @Override
    public boolean isEmpty()
    {
        return row.isEmpty();
    }

    /**
     * Returns whether the given column is defined in the row.
     *
     * @param column key whose presence in the row is to be tested
     * @return true if the row contains the column, false otherwise
     */
    @Override
    public boolean containsKey(Object column)
    {
        return row.containsKey(column);
    }

    /**
     * Returns whether the given value is present in the row in any column.
     *
     * @param value value whose presence in this map is to be tested
     * @return true if the row contains the value, false otherwise
     */
    @Override
    public boolean containsValue(Object value)
    {
        return row.containsValue(value);
    }

    /**
     * Returns the value associated to the given column.
     *
     * @param column the column whose associated value is to be returned
     * @return the value associated with the column, or null if the column is not present in the row
     */
    @Override
    public Double get(Object column)
    {
        if(column == null)
            throw new IllegalArgumentException("column is null");
        if(!containsKey(column))
            return null;
        return row.get(column);
    }

    /**
     * Operation not supported by the row, use methods in {@link Dataframe} to modify the row data.
     *
     * @param column
     * @param value
     * @return
     * @throws UnsupportedOperationException
     */
    @Override
    public Double put(String column, Double value)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Operation not supported by the row, use methods in {@link Dataframe} to modify the row data.
     *
     * @param column
     * @return
     * @throws UnsupportedOperationException
     */
    @Override
    public Double remove(Object column)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Operation not supported by the row, use methods in {@link Dataframe} to modify the row data.
     *
     * @param m
     * @return
     * @throws UnsupportedOperationException
     */
    @Override
    public void putAll(Map<? extends String, ? extends Double> m)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Operation not supported by the row, use methods in {@link Dataframe} to modify the row data.
     *
     * @return
     * @throws UnsupportedOperationException
     */
    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the list of columns ordered in column order.
     * @return the list of columns
     */
    @Override
    public Set<String> keySet()
    {
        return row.keySet();
    }

    /**
     * Returns the list of values ordered in column order.
     *
     * @return the list of values
     */
    @Override
    public Collection<Double> values()
    {
        return row.values();
    }

    /**
     * Returns the set of column-value pairs ordered in column order.
     *
     * @return the set of column-value pairs
     */
    @Override
    public Set<Entry<String, Double>> entrySet()
    {
        return row.entrySet();
    }

    /**
     * Two rows are equals if they have the same columns and values (indices can differ)
     *
     * @param o object to be compared for equality
     * @return true if the objects (rows) are equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Row row1 = (Row) o;
        return Objects.equals(row, row1.row);
    }
}
