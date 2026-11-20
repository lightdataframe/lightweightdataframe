package io.github.lightdataframe.lightweightdataframe.tests;


import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.JsonFormat;
import io.github.lightdataframe.lightweightdataframe.Serializer;
import io.github.lightdataframe.lightweightdataframe.Series;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SerializerBisTest {

    @Test
    public void testToCsv_withHeader() {
        Dataframe df = new Dataframe();
        df.setColumn("A", new Series(Arrays.asList(new Double[]{1.0, 2.0})));
        df.setColumn("B", new Series(Arrays.asList(new Double[]{3.0, 4.0})));

        String csv = Serializer.toCsv(true, df);
        String expectedCsv = "A,B\n1.0,3.0\n2.0,4.0\n";
        assertEquals(expectedCsv, csv, "CSV with header should match expected format");
    }

    @Test
    public void testToCsv_withoutHeader() {
        Dataframe df = new Dataframe();
        df.setColumn("A", new Series(Arrays.asList(new Double[]{1.0, 2.0})));
        df.setColumn("B", new Series(Arrays.asList(new Double[]{3.0, 4.0})));

        String csv = Serializer.toCsv(false, df);
        String expectedCsv = "1.0,3.0\n2.0,4.0\n";
        assertEquals(expectedCsv, csv, "CSV without header should match expected format");
    }

    @Test
    public void testToCsv_emptyDataframe() {
        Dataframe df = new Dataframe();
        String csv = Serializer.toCsv(true, df);
        assertEquals("", csv, "CSV of an empty Dataframe should be empty");
    }

    @Test
    public void testFromCsv_withHeader() {
        String csv = "A,B\n1.0,3.0\n2.0,4.0";
        Dataframe df = Serializer.fromCsv(true, csv);

        assertEquals(2, df.size());
        assertEquals(2, df.getColumns().size());
        assertEquals(1.0, df.get("A", 0));
        assertEquals(3.0, df.get("B", 0));
    }

    @Test
    public void testFromCsv_withoutHeader() {
        String csv = "1.0,3.0\n2.0,4.0";
        Dataframe df = Serializer.fromCsv(false, csv);

        assertEquals(2, df.size());
        assertEquals(2, df.getColumns().size());
        assertEquals(1.0, df.get("0", 0));
        assertEquals(4.0, df.get("1", 1));
    }

    @Test
    public void testFromCsv_incorrectRowLength() {
        String csv = "A,B\n1.0\n";
        assertThrows(RuntimeException.class, () -> Serializer.fromCsv(true, csv), "Mismatched row length should throw exception");
    }

    @Test
    public void testToJson_rowsFormat() {
        Dataframe df = new Dataframe();
        df.setColumn("A", new Series(Arrays.asList(new Double[]{1.0, 2.0})));
        df.setColumn("B", new Series(Arrays.asList(new Double[]{3.0, 4.0})));

        String json = Serializer.toJson(JsonFormat.ROWS, df);
        String expectedJson = "[{\"A\": 1.0,\"B\": 3.0},{\"A\": 2.0,\"B\": 4.0}]";
        assertEquals(expectedJson, json, "JSON in rows format should match expected output");
    }

    @Test
    public void testToJson_columnsFormat() {
        Dataframe df = new Dataframe();
        df.setColumn("A", new Series(Arrays.asList(new Double[]{1.0, 2.0})));
        df.setColumn("B", new Series(Arrays.asList(new Double[]{3.0, 4.0})));

        String json = Serializer.toJson(JsonFormat.COLUMNS, df);
        String expectedJson = "{\"A\": [1.0,2.0], \"B\": [3.0,4.0]}";
        assertEquals(expectedJson, json, "JSON in columns format should match expected output");
    }

    @Test
    public void testToJson_columnsRowsFormat() {
        Dataframe df = new Dataframe();
        df.setColumn("A", new Series(Arrays.asList(new Double[]{1.0, 2.0})));
        df.setColumn("B", new Series(Arrays.asList(new Double[]{3.0, 4.0})));

        String json = Serializer.toJson(JsonFormat.COLUMNS_ROWS, df);
        String expectedJson = "{\"columns\": [\"A\", \"B\"],\"rows\": [{\"A\": 1.0,\"B\": 3.0},{\"A\": 2.0,\"B\": 4.0}]}";
        assertEquals(expectedJson, json, "JSON in columns-rows format should match expected output");
    }

    @Test
    public void testFromJson_rowsFormat() {
        String json = "[{\"A\":1.0,\"B\":3.0},{\"A\":2.0,\"B\":4.0}]";
        Dataframe df = Serializer.fromJson(JsonFormat.ROWS, json);

        assertEquals(2, df.size());
        assertEquals(2, df.getColumns().size());
        assertEquals(1.0, df.get("A", 0));
        assertEquals(4.0, df.get("B", 1));
    }

    @Test
    public void testFromJson_columnsFormat() {
        String json = "{\"A\":[1.0,2.0],\"B\":[3.0,4.0]}";
        Dataframe df = Serializer.fromJson(JsonFormat.COLUMNS, json);

        assertEquals(2, df.size());
        assertEquals(2, df.getColumns().size());
        assertEquals(1.0, df.get("A", 0));
        assertEquals(4.0, df.get("B", 1));
    }

    @Test
    public void testFromJson_columnsRowsFormat() {
        String json = "{\"columns\":[\"A\",\"B\"],\"rows\":[{\"A\":1.0,\"B\":3.0},{\"A\":2.0,\"B\":4.0}]}";
        Dataframe df = Serializer.fromJson(JsonFormat.COLUMNS_ROWS, json);

        assertEquals(2, df.size());
        assertEquals(2, df.getColumns().size());
        assertEquals(1.0, df.get("A", 0));
        assertEquals(4.0, df.get("B", 1));
    }

    @Test
    public void testFromJson_invalidJsonFormat() {
        String json = "[{\"A\":1.0,\"B\":3.0},{\"A\":2.0}]";
        assertThrows(IllegalArgumentException.class, () -> Serializer.fromJson(JsonFormat.ROWS, json), "Invalid JSON structure should throw exception");
    }

    @Test
    public void testToJson_series() {
        Series series = new Series(Arrays.asList(new Double[]{1.0, 2.0, 3.0}));
        String json = Serializer.toJson(series);
        assertEquals("[1.0,2.0,3.0]", json, "JSON for series should match expected output");
    }

    @Test
    public void testFromJson_series() {
        String json = "[1.0,2.0,3.0]";
        Series series = Serializer.fromJson(json);
        assertEquals(3, series.size());
        assertEquals(1.0, series.get(0));
        assertEquals(3.0, series.get(2));
    }

    @Test
    public void testFromJson_invalidSeriesJson() {
        String json = "{\"A\":1.0}";
        assertThrows(IllegalArgumentException.class, () -> Serializer.fromJson(json), "Invalid series JSON should throw exception");
    }

    @Test
    public void testNullInputs() {
        assertThrows(IllegalArgumentException.class, () -> Serializer.toCsv(true, null));
        assertThrows(IllegalArgumentException.class, () -> Serializer.fromCsv(true, null));
        assertThrows(IllegalArgumentException.class, () -> Serializer.toJson(null, new Dataframe()));
        assertThrows(IllegalArgumentException.class, () -> Serializer.fromJson(null, ""));
        assertThrows(IllegalArgumentException.class, () -> Serializer.toJson((Series) null));
        assertThrows(IllegalArgumentException.class, () -> Serializer.fromJson(null));
    }
}