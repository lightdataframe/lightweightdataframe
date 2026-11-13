package io.github.lightdataframe.lightweightdataframe;

import java.util.*;


/**
 * An unmodifiable view of a dataframe row
 */
public class Row implements Map<String, Double>, Cloneable
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

    @Override
    public int size()
    {
        return row.size();
    }

    @Override
    public boolean isEmpty()
    {
        return row.isEmpty();
    }

    @Override
    public boolean containsKey(Object column)
    {
        return row.containsKey(column);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return row.containsValue(value);
    }

    @Override
    public Double get(Object column)
    {
        return row.get(column);
    }

    @Override
    public Double put(String column, Double value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Double remove(Object column)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Double> m)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet()
    {
        return row.keySet();
    }

    @Override
    public Collection<Double> values()
    {
        return row.values();
    }

    @Override
    public Set<Entry<String, Double>> entrySet()
    {
        return row.entrySet();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Row row1 = (Row) o;
        return index == row1.index && Objects.equals(row, row1.row);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(index, row);
    }

    @Override
    public Row clone()
    {
        return new Row(index, row);
    }
}
