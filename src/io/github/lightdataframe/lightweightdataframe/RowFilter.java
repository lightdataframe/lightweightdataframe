package io.github.lightdataframe.lightweightdataframe;


import java.io.Serializable;


/**
 * Strategy interface for filtering rows in a dataframe.
 */
public interface RowFilter extends Serializable
{
    /**
     * Tests a specific row in the given dataframe to determine if it meets
     * certain conditions.
     *
     * @param df the dataframe containing the data to be tested
     * @param n the index of the row to test
     * @return true if the row satisfies the conditions, false otherwise
     */
    boolean testRow(Dataframe df, int n);
}
