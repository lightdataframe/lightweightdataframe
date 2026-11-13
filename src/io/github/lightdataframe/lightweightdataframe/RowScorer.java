package io.github.lightdataframe.lightweightdataframe;


/**
 * Strategy to score rows in a dataframe
 */
public interface RowScorer
{
    /**
     * Computes and returns the score for a specific row in the given dataframe
     *
     * @param df the dataframe to score rows from
     * @param n the index of the row to be scored
     * @return the computed score
     */
    double getRowScore(Dataframe df, int n);
}
