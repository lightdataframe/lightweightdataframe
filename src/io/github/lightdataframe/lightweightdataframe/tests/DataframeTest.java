package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.*;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class DataframeTest
{
    @Test
    public void testClone()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        assertEquals(df.getRows(), df.clone().getRows());
    }


    @Test
    public void testAggregation()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        df = df.sort(true, Sorter.sortByColumns("col0"));
        df = df.groupAggregate(Grouper.slidingWindow(1, 3), new Aggregator()
                .addAggregation("col0_avg", df0 -> df0.getColumn("col0").average())
                .addAggregation("col1_avg", df1 -> df1.getColumn("col1").average())
        );
        assertEquals(1000, df.size());
        df.print();
    }


    @Test
    public void replaceTest()
    {
        Dataframe df = TestUtil.randomDataframe(100);

        df.replace(true, 0d, v -> Double.isNaN(v));

        assertEquals(0, df.find(Filter.filterByRow(row -> row.toSeries().hasNan())).size());

        df.replace(true, 0d, v -> Math.abs(v) > 40);

        df.sort(true, Sorter.sortByColumns("col1")).print();
    }

    @Test
    public void replaceColumnTest()
    {
        Dataframe df = TestUtil.randomDataframe(100);

        df.replace(true, "col2", 0d, v -> Double.isNaN(v));

        assertEquals(0, df.find(Filter.filterByRow(row -> Double.isNaN(row.get("col2")))).size());

        df.replace(true, "col2", 0d, v -> Math.abs(v) > 40);
        df.sort(true, Sorter.sortByColumns("col2")).print();
    }

    @Test
    public void nullValuesTest()
    {
        Dataframe df = TestUtil.randomDataframe(1000);

        df.set(true, "col1", 0, Double.NaN);
        df.print();
    }

    @Test
    public void testReorderColumns()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        Dataframe reordered = df.reorderColumns("col2", "col1");
        reordered.printStatistics();
    }


    @Test
    public void testRenameColumns()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        Dataframe reordered = df.renameColumn("col0", "renamed");
        reordered.print();
        assertEquals("renamed", reordered.getColumns().get(0));
    }

    @Test
    public void statisticsTest()
    {
        Dataframe df = TestUtil.randomDataframe(1000);

        Dataframe stats = df.statistics();
        assertEquals(stats.size(), df.getColumns().size());

        df.printStatistics();

        df.set(true, "col1", 0, Double.NaN);
        df.printStatistics();
    }

    @Test
    public void testAddColumn()
    {
        Dataframe df = TestUtil.randomDataframe(1000);

        df = df.setColumn("newcol", df.getColumn("col1").zip(df.getColumn("col2"), Double::compareTo));
    }
}
