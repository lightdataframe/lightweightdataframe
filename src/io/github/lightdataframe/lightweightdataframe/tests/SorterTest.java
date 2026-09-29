package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Sorter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;



public class SorterTest
{
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

    Sorter sorter = new Sorter();

    @Test
    public void shuffleTest()
    {
        Dataframe df = dummyDataframe();

        df = df.sort(true, sorter.shuffle());

        assertEquals(3, df.size());

    }

    @Test
    public void sortByColumnTest()
    {
        Dataframe df0 = dummyDataframe();
        Dataframe df1 = df0.sort(false, sorter.sortByColumn("col1"));
    }


}
