package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Sorter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;




public class SorterTest
{
    private Dataframe df;

    @BeforeEach
    void initialize()
    {
        df = TestUtil.randomDataframe(200);
    }


    @Test
    public void sortByColumns()
    {
        Dataframe df0 = df.clone();
        df0.sort(false, Sorter.sortByColumns(true, "col5", "col2"));
        df0.getColumn("col5").print();
        df0.getColumn("col2").print();
    }

    @Test
    void sortByColumn()
    {
        Dataframe df0 = df.clone();
        df0.sort(true, Sorter.sortByColumn(true, "col2", v -> v));
        df0.getColumn("col2").print();
    }


    @Test
    public void sortByRow()
    {
        Dataframe df0 = df.clone();
        df0.sort(true, Sorter.sortByRow(row -> row.get("col0")));
        df0.getColumn("col0").print();
    }


    @Test
    public void shuffleTest()
    {
        Dataframe sorted = df.sort(true, Sorter.shuffle());

        assertNotEquals(df.getRows(), sorted.getRows());
    }

    @Test
    public void sortByNanColumn()
    {
        Dataframe df0 = df.clone();
        df0.sort(true, Sorter.sortByColumns(true, "col3")); // All nans
        assertEquals(df.getRows(), df0.getRows());
    }

    @Test
    void invalidInput()
    {
        assertThrows(IllegalArgumentException.class, () -> Sorter.sortByColumns(true, "col3", null));
        assertThrows(IllegalArgumentException.class, () -> Sorter.sortByColumn(true, "col3", null));
        assertThrows(IllegalArgumentException.class, () -> Sorter.sortByRow(null));
        assertThrows(IllegalArgumentException.class, () -> df.sort(true, Sorter.sortByColumn(true, "invalid_col", null)));
        assertThrows(IllegalArgumentException.class, () -> df.sort(true, Sorter.sortByColumns(true, "col3", "invalid_col")));
    }
}
