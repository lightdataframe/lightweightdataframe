package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Filter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class FilterTest
{

    @Test
    public void testHead()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        Dataframe df0 = df.filter(Filter.head(2));
        assertEquals(2, df0.size());
    }

    @Test
    public void testTail()
    {
        Dataframe df = TestUtil.randomDataframe(1000);

        Dataframe df0 = df.filter(Filter.tail(2));
        assertEquals(2, df0.size());
    }

    @Test
    public void testBetween()
    {
        Dataframe df = TestUtil.randomDataframe(1000);

        Dataframe df0 = df.filter(Filter.between(1, 2));
        assertEquals(1, df0.size());
    }

    @Test
    public void testRowTest()
    {
        Dataframe df = TestUtil.randomDataframe(1000);

        Dataframe df0 = df.filter(Filter.filterByRow(row -> Double.isNaN(row.get("col1"))));
        assertTrue(df0.getColumn("col1").all(v -> Double.isNaN(v)));
    }
}
