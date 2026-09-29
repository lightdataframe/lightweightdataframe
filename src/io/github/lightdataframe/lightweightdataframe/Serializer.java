package io.github.lightdataframe.lightweightdataframe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Defines serialization methods for {@link Dataframe} and {@link Series} objects.
 */
public class Serializer
{


    /**
     * Converts the given {@link Dataframe} into a JSON string representation based on the specified format.
     *
     * @param format the {@link JsonFormat} that defines how the JSON string should be structured.
     *               Supported formats include ROWS, COLUMNS, and COLUMNS_ROWS.
     * @param df the {@link Dataframe} to be converted into a JSON string.
     * @return the JSON string representation of the given dataframe in the specified format.
     * @throws IllegalArgumentException if the provided format is not valid.
     */
    public String toJson(JsonFormat format, Dataframe df)
    {
        switch (format)
        {
            case ROWS:
                return onlyRowsJson(df);
            case COLUMNS_ROWS:
                return columnsRowsJson(df);
            case COLUMNS:
                return columnsJson(df);
            default:
                throw new IllegalArgumentException("Invalid JsonFormat");
        }
    }

    private String columnsJson(Dataframe df)
    {
        return df.getColumns().stream()
                .map(col -> String.format(Locale.US, "\"%s\": %s", col, toJson(df.getColumn(col))))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private String columnsRowsJson(Dataframe df)
    {
        return "{" +
                "\"columns\": " + df.getColumns().stream().map(col -> "\"" + col + "\"").collect(Collectors.joining(", ", "[", "],")) +
                "\"rows\": " + onlyRowsJson(df) +
                "}";
    }

    private String onlyRowsJson(Dataframe df)
    {
        return df.data.stream().map(Serializer::rowToJson).collect(Collectors.joining(",", "[", "]"));
    }

    private static String rowToJson(Map<String, Double> row)
    {
        return row.entrySet().stream()
                .map(e -> String.format(Locale.US, "\"%s\": %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
    }


    /**
     * Parses a JSON string representation into a {@link Dataframe} based on the specified format.
     *
     * @param format the {@link JsonFormat} that determines how the JSON string should be interpreted.
     *               Supported formats include ROWS, COLUMNS, and COLUMNS_ROWS.
     * @param json the JSON string input that represents a dataframe structure in the specified format.
     * @return a {@link Dataframe} created from the provided JSON string.
     * @throws IllegalArgumentException if the provided format is not valid.
     */
    public Dataframe fromJson(JsonFormat format, String json)
    {
        switch (format)
        {
            case ROWS:
                return fromJsonRows(json);
            case COLUMNS:
                return fromJsonColumns(json);
            case COLUMNS_ROWS:
                return fromJsonColumnsRows(json);
            default:
                throw new IllegalArgumentException("Invalid JsonFormat");
        }
    }

    private Dataframe fromJsonRows(String json)
    {
        List<Map<String, Double>> rows = new ArrayList<>();
        try
        {
            JSONArray jarr = new JSONArray(json);
            for(int i = 0; i < jarr.length(); i++)
            {
                Map<String, Double> row = new LinkedHashMap<>();
                JSONObject jobj = jarr.getJSONObject(i);
                List<String> sortedKeys = jobj.keySet().stream().sorted().collect(Collectors.toList());
                for(String key : sortedKeys)
                    row.put(key, jobj.getDouble(key));
                rows.add(row);
            }
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Invalid Json");
        }
        Dataframe df = new Dataframe();
        df.data = rows;
        return df;
    }

    private Dataframe fromJsonColumns(String json)
    {
        Map<String, List<Double>> parsed = new LinkedHashMap<>();
        try
        {
            JSONObject jobj = new JSONObject(json);
            List<String> sortedKeys = jobj.keySet().stream().sorted().collect(Collectors.toList());

            for(String key : sortedKeys)
            {
                JSONArray jarr = jobj.getJSONArray(key);
                List<Double> columnValues = new ArrayList<>();
                for(int i = 0; i < jarr.length(); i++)
                    columnValues.add(jarr.getDouble(i));
                parsed.put(key, columnValues);
            }
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Invalid Json");
        }

        boolean sizes = parsed.values().stream().mapToInt(List::size).distinct().count() > 1;

        if(sizes)
            throw new IllegalArgumentException("Columns have different sizes");

        List<Map<String, Double>> rows = new ArrayList<>();
        int length = parsed.values().stream().findFirst().map(List::size).orElse(0);

        for(int i = 0; i < length; i++)
        {
            Map<String, Double> row = new LinkedHashMap<>();
            for(String column : parsed.keySet())
                row.put(column, parsed.get(column).get(i));
            rows.add(row);
        }
        Dataframe df = new Dataframe();
        df.data = rows;
        return df;
    }

    private Dataframe fromJsonColumnsRows(String json)
    {
        JSONObject jobj = new JSONObject(json);
        List<String> columns = new ArrayList<>();
        for(Object col : jobj.getJSONArray("columns"))
            columns.add(col.toString());

        Dataframe df = fromJsonRows(jobj.getJSONArray("rows").toString());

        if(!df.getColumns().containsAll(columns) && !columns.containsAll(df.getColumns()))
            System.err.println("Mismatch between columns and rows");

        return df;
    }




    // Series serialization


    /**
     * Converts the given {@link Series} into a JSON string representation.
     *
     * @param series the {@link Series} to be converted into a JSON string
     * @return a JSON string representation of the given series
     */
    public String toJson(Series series)
    {
        return "[" +
                series. stream().map(v -> String.format(Locale.US, "%s", v)).collect(Collectors.joining(",")) +
                "]";
    }


    /**
     * Parses a JSON array string representation into a {@link Series} object.
     *
     * @param json the JSON string input that contains an array of numbers. It should
     *             be formatted as a JSON array, such as "[1.0, 2.0, 3.0]".
     * @return a {@link Series} object created from the numeric values in the provided JSON string.
     * @throws IllegalArgumentException if the input JSON string is invalid or cannot be parsed.
     */
    public Series fromJson(String json)
    {
        try
        {
            Series series = new Series();
            JSONArray jarr = new JSONArray(json);

            for(int i = 0; i < jarr.length(); i++)
                series.add(jarr.getDouble(i));
            return series;
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Invalid Json");
        }
    }


}
