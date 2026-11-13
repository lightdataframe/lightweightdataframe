package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Sorter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;



public class SorterTest
{
    @Test
    public void shuffleTest()
    {
        Dataframe df = TestUtil.randomDataframe(1000);

        Dataframe sorted = df.sort(true, Sorter.shuffle());

        assertNotEquals(df, sorted);
    }

    @Test
    public void sortByNanColumn()
    {
        Dataframe df0 = TestUtil.randomDataframe(1000);
        Dataframe df1 = df0.sort(true, Sorter.sortByColumns("col3")); // Column of all NaN values
        assertEquals(df0, df1); // The dataframe is not sorted
        df1.print();
    }

    @Test
    public void sortByColumnsTest()
    {
        Dataframe df0 = TestUtil.randomDataframe(1000);
        Dataframe df1 = df0.sort(true, Sorter.sortByColumns());
        assertEquals(df0, df1); // The dataframe is not sorted

        Dataframe df2 = df0.sort(false, Sorter.sortByColumns("col5", "col2"));
        df2.print();
    }

    @Test
    public void sortByRowsTest()
    {
        Dataframe df0 = TestUtil.randomDataframe(1000);
        Dataframe df1 = df0.sort(true, Sorter.sortByRow(row -> row.get("col0")));
        df0.print();
        df1.print();
    }
}
