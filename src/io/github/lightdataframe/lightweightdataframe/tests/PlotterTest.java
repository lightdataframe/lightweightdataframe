package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;

import io.github.lightdataframe.lightweightdataframe.Plotter;
import io.github.lightdataframe.lightweightdataframe.Sorter;
import org.junit.jupiter.api.Test;



public class PlotterTest
{
    @Test
    public void plotTest() throws InterruptedException
    {
        Dataframe df0 = TestUtil.randomDataframe(100).sort(true, Sorter.sortByColumns(true, "col0"));
        Plotter.line(df0, "col0");
        Thread.currentThread().join();
    }
}
