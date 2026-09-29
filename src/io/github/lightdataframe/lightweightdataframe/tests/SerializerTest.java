package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.JsonFormat;
import io.github.lightdataframe.lightweightdataframe.Serializer;
import io.github.lightdataframe.lightweightdataframe.Series;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SerializerTest
{
    Serializer serializer = new Serializer();

    private Dataframe createDataframe()
    {
        Dataframe df = new Dataframe();
        Map<String, Double> row0 = new LinkedHashMap<>();
        Map<String, Double> row1 = new LinkedHashMap<>();

        row0.put("age", 10d);
        row0.put("height", 1.75d);
        row0.put("weight", 60d);

        row1.put("age", 20d);
        row1.put("height", 1.65d);
        row1.put("weight", 50d);

        df.append(true, row0);
        df.append(true, row1);
        return df;
    }

    private Series createSeries()
    {
        return new Series(Arrays.asList(1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d));
    }

    @Test
    void dataframeToJson()
    {
        Dataframe df = createDataframe();

        String json0 = serializer.toJson(JsonFormat.ROWS, df);
        String json1 = serializer.toJson(JsonFormat.COLUMNS, df);
        String json2 = serializer.toJson(JsonFormat.COLUMNS_ROWS, df);
    }

    @Test
    void dataframeFromJson()
    {
        Dataframe df = createDataframe();
        Dataframe df0;
        String json;
        json = serializer.toJson(JsonFormat.ROWS, df);
        df0 = serializer.fromJson(JsonFormat.ROWS, json);

        for(int i = 0; i < df.size(); i++)
            assertEquals(df.getRow(i), df0.getRow(i));
        json = serializer.toJson(JsonFormat.COLUMNS, df);
        df0 = serializer.fromJson(JsonFormat.COLUMNS, json);
        for(int i = 0; i < df.size(); i++)
            assertEquals(df.getRow(i), df0.getRow(i));
        json = serializer.toJson(JsonFormat.COLUMNS_ROWS, df);
        df0 = serializer.fromJson(JsonFormat.COLUMNS_ROWS, json);
        for(int i = 0; i < df.size(); i++)
            assertEquals(df.getRow(i), df0.getRow(i));
    }

    @Test
    void seriesToJson()
    {
        Series series = createSeries();
        String json = serializer.toJson(series);
        assertTrue(json.length() > 0);
    }

    @Test
    void seriesFromJson()
    {
        Series series = createSeries();
        String json = serializer.toJson(series);
        Series series0 = serializer.fromJson(json);
        assertEquals(series, series0);
    }
}