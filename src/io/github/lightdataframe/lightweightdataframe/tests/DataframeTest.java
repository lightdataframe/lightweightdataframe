package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Sorter;
import org.junit.jupiter.api.Test;


import java.util.LinkedHashMap;
import java.util.Map;

public class DataframeTest
{
    private Dataframe createDataframe()
    {

        Map<String, Double> row1 = new LinkedHashMap<>();
        row1.put("col1", 1d);
        row1.put("col2", null);
        Map<String, Double> row2 = new LinkedHashMap<>();
        row2.put("col1", 2d);
        row2.put("col2", 25.0);
        Map<String, Double> row3 = new LinkedHashMap<>();
        row3.put("col1", 3d);
        row3.put("col2", 30.0);
        Map<String, Double> row4 = new LinkedHashMap<>();
        row4.put("col1", 3d);
        row4.put("col2", 10.0);
        Map<String, Double> row5 = new LinkedHashMap<>();
        row5.put("col1", 5d);
        row5.put("col2", 30.0);
        Map<String, Double> row6 = new LinkedHashMap<>();
        row6.put("col1", 5d);
        row6.put("col2", 20.0);
        Map<String, Double> row7 = new LinkedHashMap<>();
        row7.put("col1", 7d);
        row7.put("col2", 20.0);
        Map<String, Double> row8 = new LinkedHashMap<>();
        row8.put("col1", 7d);
        row8.put("col2", null);


        Dataframe df = new Dataframe();
        df.append(true, row1);
        df.append(true, row2);
        df.append(true, row3);
        df.append(true, row4);
        df.append(true, row5);
        df.append(true, row6);
        df.append(true, row7);
        df.append(true, row8);
        return df;
    }

    @Test
    void nullValuesTest()
    {
        Dataframe df = createDataframe();

        df.set(true, 0, "col1", null);
        df.print();
    }

    @Test
    void testReorderColumns()
    {
        Dataframe df = createDataframe();
        Dataframe reordered = df.reorderColumns("col2", "col1");
        reordered.printStatistics();

        df.sort(true, new Sorter().sortByColumns("col1", "col2")).print();

    }

    @Test
    void statisticsTest()
    {
        Dataframe df = createDataframe();

        df.printStatistics();

        df.set(true, 0, "col1", null);
        df.printStatistics();
    }

    @Test
    void testAddColumn()
    {
        Dataframe df = createDataframe();

        df = df.setColumn("newcol", df.getColumn("col1").zip(df.getColumn("col2"), Double::compareTo));
    }
}
