package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.JsonFormat;
import io.github.lightdataframe.lightweightdataframe.Serializer;
import io.github.lightdataframe.lightweightdataframe.Series;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerializerTest
{
    @Test
    void dataframeToCsv()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        String csv = Serializer.toCsv(true, df);
        Dataframe df0 = Serializer.fromCsv(true, csv);
        assertEquals(df.getRows(), df0.getRows());
    }

    @Test
    void singleRowToDataframe()
    {
        String csv = "1.2, 3.6,4, NaN, null\n1.2, 3.6,4, NaN, null";
        Dataframe df0 = Serializer.fromCsv(false, csv);
        assertEquals(2, df0.size());
    }

    @Test
    void dataframeToJson()
    {
        Dataframe df = TestUtil.randomDataframe(1000);

        String json0 = Serializer.toJson(JsonFormat.ROWS, df);
        String json1 = Serializer.toJson(JsonFormat.COLUMNS, df);
        String json2 = Serializer.toJson(JsonFormat.COLUMNS_ROWS, df);
    }

    @Test
    void dataframeFromJson()
    {
        String json = "[{\"col1\" : 0.1, \"col2\" : 0.6}, {\"col1\" : NaN, \"col2\" : null}]";
        Dataframe df = Serializer.fromJson(JsonFormat.ROWS, json);
        df.print();
        json = "{columns: [\"col1\", \"col2\"], rows: [{\"col2\" : 0.6, \"col1\" : 0.1}, {\"col1\" : NaN, \"col2\" : null}]}";
        df = Serializer.fromJson(JsonFormat.COLUMNS_ROWS, json);
        df.print();
        json = "{\"col1\" : [0.1, null, -4], \"col2\" : [0.2, -0.4, NaN]}";
        df = Serializer.fromJson(JsonFormat.COLUMNS, json);
        df.print();
    }

    @Test
    void dataframeToFromJson()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        Dataframe df0;
        String json;
        json = Serializer.toJson(JsonFormat.ROWS, df);
        df0 = Serializer.fromJson(JsonFormat.ROWS, json);

        assertEquals(df.getRows(), df0.getRows());
        for(int i = 0; i < df.size(); i++)
            assertEquals(df.getRow(i), df0.getRow(i));


        json = Serializer.toJson(JsonFormat.COLUMNS, df);
        df0 = Serializer.fromJson(JsonFormat.COLUMNS, json);

        assertEquals(df.getRows(), df0.getRows());
        for(int i = 0; i < df.size(); i++)
            assertEquals(df.getRow(i), df0.getRow(i));


        json = Serializer.toJson(JsonFormat.COLUMNS_ROWS, df);
        df0 = Serializer.fromJson(JsonFormat.COLUMNS_ROWS, json);

        assertEquals(df.getRows(), df0.getRows());
        for(int i = 0; i < df.size(); i++)
            assertEquals(df.getRow(i), df0.getRow(i));
    }

    @Test
    void seriesToJson()
    {
        Series series = TestUtil.randomSeries(100);
        String json = Serializer.toJson(series);
        assertFalse(json.isEmpty());
    }

    @Test
    void seriesFromJson()
    {
        Series series = TestUtil.randomSeries(100);
        String json = Serializer.toJson(series);
        Series series0 = Serializer.fromJson(json);
        assertEquals(series, series0);
    }
}