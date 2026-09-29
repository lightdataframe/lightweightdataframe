package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Filter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class FilterTest
{
    Filter filter = new Filter();

    Dataframe dummyDataframe()
    {
        Dataframe df = new Dataframe();
        Map<String, Double> row1 = new HashMap<>();
        row1.put("col1", 10.0);
        row1.put("col2", 20.0);
        Map<String, Double> row2 = new HashMap<>();
        row2.put("col1", 15.0);
        row2.put("col2", 25.0);
        Map<String, Double> row3 = new HashMap<>();
        row3.put("col1", 20.0);
        row3.put("col2", 30.0);
        df.append(true, row1);
        df.append(true, row2);
        df.append(true, row3);
        return df;
    }

    @Test
    public void testHead()
    {
        Dataframe df = dummyDataframe();
        Dataframe df0 = df.filter(filter.head(2));
    }

    @Test
    public void testTail()
    {
        Dataframe df = dummyDataframe();

        Dataframe df0 = df.filter(filter.tail(2));
        assertEquals(2, df0.size());
    }

    @Test
    public void testBetween()
    {
        Dataframe df = dummyDataframe();

        Dataframe df0 = df.filter(filter.between(1, 2));
        assertEquals(1, df0.size());
    }

    @Test
    public void testRowTest()
    {
        Dataframe df = dummyDataframe();

        Dataframe df0 = df.filter(filter.rowTest(row -> row.get("col1") == 10.0));
        assertEquals(1, df0.size());
    }
}
