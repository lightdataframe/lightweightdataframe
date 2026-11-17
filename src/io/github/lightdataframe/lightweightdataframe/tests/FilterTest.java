package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FilterTest
{
    Dataframe df;

    @BeforeEach
    void initialize()
    {
        df = TestUtil.randomDataframe(200);
    }

    @Test
    public void testHead()
    {
        Dataframe df0 = df.filter(Filter.head(2));
        assertEquals(2, df0.size());
    }

    @Test
    public void testTail()
    {
        Dataframe df0 = df.filter(Filter.tail(2));
        assertEquals(2, df0.size());
    }

    @Test
    public void testBetween()
    {
        Dataframe df0 = df.filter(Filter.between(1, 2));
        assertEquals(1, df0.size());
    }

    @Test
    public void testRowTest()
    {
        df.filter(Filter.rowTest(row -> Double.isNaN(row.get("col1"))));
        assertTrue(df.getColumn("col1").all(v -> Double.isNaN(v)));
    }

    @Test
    void valueTest()
    {
        df.filter(Filter.valueTest("col1", v -> v > 0));
        assertTrue(df.getRows().stream().allMatch(r -> r.get("col1") > 0));
    }

    @Test
    void valuesMatch()
    {
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("col0", null);

        df.filter(Filter.valuesMatch(values));
        assertTrue(df.getRows().stream().allMatch(r -> Double.isNaN(r.get("col0"))));
    }
}
