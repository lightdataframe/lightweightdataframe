package io.github.lightdataframe.lightweightdataframe;

import java.io.Serializable;

/**
 * Strategy interface to score rows in a dataframe.
 */
public interface RowScorer extends Serializable
{
    /**
     * Computes and returns the score for a specific row in the given dataframe.
     *
     * @param df the dataframe containing to score rows from
     * @param n the index of the row to be scored
     * @return the computed score
     */
    double getRowScore(Dataframe df, int n);
}
