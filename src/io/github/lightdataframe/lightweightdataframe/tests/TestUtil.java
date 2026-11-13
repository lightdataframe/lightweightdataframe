package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Series;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class TestUtil
{
    public static Series randomSeries(int n)
    {
        Series series = new Series();
        for(int i = 0; i < n; i++)
        {
            Double v = Math.random();
            if(Math.random() < 0.05)
                v = null;
            series.add(v);
        }
        return series;
    }

    public static Dataframe randomDataframe(int n)
    {
        Dataframe df = new Dataframe();
        for(int i = 0; i < n; i ++)
            df.append(true, randomRow());
        return df;
    }

    public static Map<String, Double> randomRow()
    {
        Map<String, Double> row = new LinkedHashMap<>();
        row.put("col0", randomValue(-10, 10, 0.05));
        row.put("col1", randomValue(-100, 0, 0.05));
        row.put("col2", randomValue(0, 50, 0.1));
        row.put("col3", randomValue(0, 50, 1));
        row.put("col4", randomValue(-2, 1, 0.95));
        row.put("col5", randomInt(0, 4, 0.1));
        return row;
    }

    private static Random random = new Random();
    private static Double randomInt(int min, int max, double nanProb)
    {
        Double v = (double) random.nextInt(max - min + 1) + min;
        if(Math.random() < nanProb)
            v = null;
        return v;
    }

    private static Double randomValue(double min, double max, double nanProb)
    {
        Double v =  Math.random() * (max - min) + min;
        if(Math.random() < nanProb)
            v = null;
        return v;
    }
}
