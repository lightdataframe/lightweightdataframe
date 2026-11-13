package io.github.lightdataframe.lightweightdataframe;

import java.util.List;

/**
 * Strategy for grouping rows in a dataframe
 */
public interface RowGrouper
{
    /**
     * Groups a row of the given dataframe into a specified list of groups
     *
     * @param df the dataframe to group the rows from
     * @param n  the index of the row to group
     * @return a list of objects representing the groups of rows. The method {@code equal()} determines
     * groups equality
     */
    List<Object> getRowGroups(Dataframe df, int n);
}
