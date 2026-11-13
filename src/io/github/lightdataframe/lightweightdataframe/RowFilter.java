package io.github.lightdataframe.lightweightdataframe;


/**
 * Strategy for filtering rows in a dataframe
 */
public interface RowFilter
{
    /**
     * Tests a row in the given dataframe to determine if it meets
     * some conditions.
     *
     * @param df the dataframe containing the data to be tested
     * @param n the index of the row to test
     * @return true if the row satisfies the conditions, false otherwise
     */
    boolean testRow(Dataframe df, int n);
}
