package io.github.lightdataframe.lightweightdataframe;


/**
 * Strategy to map rows to associated double values
 */
public interface RowMapper
{
    /**
     * Maps a row in the given dataframe to a double value
     *
     * @param df the dataframe to map the rows from
     * @param n  the index of the row to map
     * @return a double representing the mapped value
     */
    double getRowValue(Dataframe df, int n);
}
