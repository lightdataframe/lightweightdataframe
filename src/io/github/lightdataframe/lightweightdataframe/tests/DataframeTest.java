package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataframeTest
{
    @Test
    void testReorderColumns()
    {
        Map<String, Double> row1 = new LinkedHashMap<>();
        row1.put("col1", 1d);
        row1.put("col2", 20.0);
        Map<String, Double> row2 = new LinkedHashMap<>();
        row2.put("col1", 2d);
        row2.put("col2", 25.0);
        Map<String, Double> row3 = new LinkedHashMap<>();
        row3.put("col1", 3d);
        row3.put("col2", 30.0);
        Map<String, Double> row4 = new LinkedHashMap<>();
        row4.put("col1", 4d);
        row4.put("col2", 30.0);
        Map<String, Double> row5 = new LinkedHashMap<>();
        row5.put("col1", 5d);
        row5.put("col2", 30.0);
        Map<String, Double> row6 = new LinkedHashMap<>();
        row6.put("col1", 6d);
        row6.put("col2", 30.0);
        Map<String, Double> row7 = new LinkedHashMap<>();
        row7.put("col1", 7d);
        row7.put("col2", 30.0);
        Map<String, Double> row8 = new LinkedHashMap<>();
        row8.put("col1", 8d);
        row8.put("col2", 30.0);


        Dataframe df = new Dataframe();
        df.append(true, row1);
        df.append(true, row2);
        df.append(true, row3);
        df.append(true, row4);
        df.append(true, row5);
        df.append(true, row6);
        df.append(true, row7);
        df.append(true, row8);
        Dataframe reordered = df.reorderColumns("col2", "col1");
        reordered.print();
    }
}
