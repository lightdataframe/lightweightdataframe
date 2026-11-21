package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.JsonFormat;
import io.github.lightdataframe.lightweightdataframe.Serializer;
import io.github.lightdataframe.lightweightdataframe.Series;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerializerTest
{
    Dataframe df;

    @BeforeEach
    void initialize()
    {
        df = TestUtil.randomDataframe(200);
    }

    @Test
    void dataframeToCsv()
    {
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
        JSONArray jarr;
        JSONObject jobj;
        String json0 = Serializer.toJson(JsonFormat.ROWS, df);
        jarr = new JSONArray(json0);
        assertEquals(200, jarr.length());
        String json1 = Serializer.toJson(JsonFormat.COLUMNS, df);
        jobj = new JSONObject(json1);
        assertEquals(6, jobj.length());
        assertEquals(df.size(), jobj.getJSONArray("col1").length());
        String json2 = Serializer.toJson(JsonFormat.COLUMNS_ROWS, df);
        jobj = new JSONObject(json2);
        assertEquals(df.size(), jobj.getJSONArray("rows").length());
    }

    @Test
    void dataframeFromJson()
    {
        String json = "[{\"col1\" : 0.1, \"col2\" : 0.6}, {\"col1\" : NaN, \"col2\" : null}]";
        Dataframe df = Serializer.fromJson(JsonFormat.ROWS, json);
        df.print();
        json = "{columns: [\"col1\", \"col2\"], rows: [{\"col2\" : 0.6, \"col1\" : 0.1}, {\"col1\" : \"NaN\", \"col2\" : null}]}";
        df = Serializer.fromJson(JsonFormat.COLUMNS_ROWS, json);
        df.print();
        json = "{\"col1\" : [0.1, null, -4], \"col2\" : [0.2, -0.4, NaN]}";
        df = Serializer.fromJson(JsonFormat.COLUMNS, json);
        df.print();
    }

    @Test
    void dataframeToFromJson()
    {
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

        JSONArray jarr = new JSONArray(json);
        assertEquals(100, jarr.length());
        assertFalse(json.isEmpty());
    }

    @Test
    void seriesToFromJson()
    {
        Series series = TestUtil.randomSeries(100);
        String json = Serializer.toJson(series);
        Series series0 = Serializer.fromJson(json);
        assertEquals(series, series0);
    }

    @Test
    void invalidCsv()
    {
        String csv = "a,b,c";
        String finalCsv = csv;
        assertThrows(RuntimeException.class, () -> Serializer.fromCsv(false, finalCsv));
        csv = "12.1\t12.3";
        String finalCsv1 = csv;
        assertThrows(RuntimeException.class, () -> Serializer.fromCsv(false, finalCsv1));
    }

    @Test
    void invalidJson()
    {
        String json = "[{\"col1\" : 0.1, \"col2\" : 0.6}, {\"col1\" : NaN, \"col2\" :}]"; //Wrong json
        String finalJson = json;
        assertThrows(RuntimeException.class, () -> Serializer.fromJson(JsonFormat.ROWS, finalJson));

        json = "{columns: [\"col1\", \"], rows: [{\"col2\" : 0.6, \"col1\" : 0.1}, {\"col1\" : NaN, \"col2\" : null}]}"; // Wrong json
        String finalJson1 = json;
        assertThrows(RuntimeException.class, () -> Serializer.fromJson(JsonFormat.COLUMNS_ROWS, finalJson1));
        json = "{columns: [\"col1\", null], rows: [{\"col2\" : 0.6, \"col1\" : 0.1}, {\"col1\" : NaN, \"col2\" : null}]}"; // Null column name
        String finalJson2 = json;
        assertThrows(RuntimeException.class, () -> Serializer.fromJson(JsonFormat.COLUMNS_ROWS, finalJson2));

        json = "{\"col1\" : [0.1, , -4], \"col2\" : [0.2, -0.4]}"; // Column length mismatch
        String finalJson3 = json;
        assertThrows(RuntimeException.class, () -> Serializer.fromJson(JsonFormat.COLUMNS, finalJson3));
    }
}