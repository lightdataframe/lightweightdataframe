package io.github.lightdataframe.lightweightdataframe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Defines serialization methods for {@link Dataframe} and {@link Series} from/to JSON and CSV formats.
 */
public class Serializer
{
    /**
     * Convert a dataframe into a comma-separated values (csv) string.
     *
     * @param header whether the csv should include the header
     * @param df the dataframe to convert
     * @return the csv string
     */
    public static String toCsv(boolean header, Dataframe df)
    {
        if(df == null)
            throw new IllegalArgumentException("df is null");

        StringBuilder sb = new StringBuilder();

        if(header)
        {
            df.getColumns().forEach(col -> sb.append(col).append(","));
            if(!df.getColumns().isEmpty())
                sb.replace(sb.length() - 1, sb.length(), "");
            sb.append("\n");
        }
        df.getRows().forEach(row -> {
            for(String col : row.keySet())
                sb.append(row.get(col)).append(",");
            if(!df.getColumns().isEmpty())
                sb.replace(sb.length() - 1, sb.length(), "");
            sb.append("\n");
        });
        return sb.toString();
    }

    /**
     * Parses a comma-separated values (csv) string into a dataframe.
     *
     * @param header whether the first line of the string is the csv header with column names
     * @param csv the csv string to parse
     * @return a new dataframe instance with the parsed values
     */
    public static Dataframe fromCsv(boolean header, String csv)
    {
        if(csv == null)
            throw new IllegalArgumentException("csv is null");

        Dataframe df = new Dataframe();
        List<String> lines = Arrays.stream(csv.split("\n"))
                .map(String::trim)
                .map(l -> {
                    if(l.endsWith(","))
                        l = l.substring(0, l.length() - 1);
                    return l;
                })
                .collect(Collectors.toList());

        if(lines.isEmpty())
            return df;

        List<String> columns;
        if(header)
        {
            columns = Arrays.stream(lines.get(0).split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            lines.remove(0);
        }
        else
        {
            String[] row = lines.get(0).split(",");
            columns = IntStream.range(0, row.length)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.toList());
        }
        for(String line : lines)
        {
            List<Double> row = Arrays.stream(line.split(","))
                    .map(v -> {
                        if(v.trim().equals("null"))
                            return Double.NaN;
                        return Double.parseDouble(v.trim());
                    })
                    .collect(Collectors.toList());

            if(row.size() != columns.size())
                throw new RuntimeException("Wrong number of columns for row: " + line);

            Map<String, Double> rowMap = IntStream.range(0, columns.size())
                    .boxed()
                    .collect(Collectors.toMap(
                            columns::get,
                            row::get,
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));
            df.append(true, rowMap);
        }
        return df;
    }


    /**
     * Converts the given dataframe into a JSON string representation based on the specified format.
     *
     * @param format the {@link JsonFormat} that defines how the JSON string should be structured.
     *               Supported formats include ROWS, COLUMNS, and COLUMNS_ROWS. See {@link JsonFormat} for details
     * @param df the {@link Dataframe} to be converted into a JSON string.
     * @return the JSON string representation of the given dataframe in the specified format.
     */
    public static String toJson(JsonFormat format, Dataframe df)
    {
        if(format == null)
            throw new IllegalArgumentException("format is null");
        if(df == null)
            throw new IllegalArgumentException("df is null");

        switch (format)
        {
            case ROWS:
                return rowsJson(df);
            case COLUMNS_ROWS:
                return columnsRowsJson(df);
            case COLUMNS:
                return columnsJson(df);
            default:
                throw new IllegalArgumentException("invalid format");
        }
    }

    private static String columnsJson(Dataframe df)
    {
        return df.getColumns().stream()
                .map(col -> String.format(Locale.US, "\"%s\": %s", col, toJson(df.getColumn(col))))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String columnsRowsJson(Dataframe df)
    {
        return "{" +
                "\"columns\": " + df.getColumns().stream().map(col -> "\"" + col + "\"").collect(Collectors.joining(", ", "[", "],")) +
                "\"rows\": " + rowsJson(df) +
                "}";
    }

    private static String rowsJson(Dataframe df)
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
     * Parses a JSON string representation into a dataframe based on the specified format.
     *
     * @param format the {@link JsonFormat} that determines how the JSON string should be interpreted.
     *               Supported formats include ROWS, COLUMNS, and COLUMNS_ROWS.
     * @param json the JSON string input that represents a dataframe structure in the specified format.
     * @return a {@link Dataframe} created from the provided JSON string.
     * @throws IllegalArgumentException if the provided format is not valid.
     */
    public static Dataframe fromJson(JsonFormat format, String json)
    {
        if(format == null)
            throw new IllegalArgumentException("format is null");
        if(json == null)
            throw new IllegalArgumentException("json is null");

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

    private static Dataframe fromJsonRows(String json)
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
                {
                    double v = jobj.isNull(key) ? Double.NaN : jobj.getDouble(key);
                    row.put(key, v);
                }
                rows.add(row);
            }
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Invalid Json");
        }

        Dataframe df = new Dataframe();
        for(Map<String, Double> row : rows)
            df.append(true, row);
        return df;
    }

    private static Dataframe fromJsonColumns(String json)
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
                {
                    double v = jarr.isNull(i) ? Double.NaN : jarr.getDouble(i);
                    columnValues.add(v);
                }
                parsed.put(key, columnValues);
            }
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("invalid json");
        }

        boolean sizes = parsed.values().stream().mapToInt(List::size).distinct().count() > 1;

        if(sizes)
            throw new IllegalArgumentException("columns have different sizes");

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
        for(Map<String, Double> row : rows)
            df.append(true, row);
        return df;
    }

    private static Dataframe fromJsonColumnsRows(String json)
    {
        JSONObject jobj = new JSONObject(json);
        List<String> columns = new ArrayList<>();
        for(Object col : jobj.getJSONArray("columns"))
            columns.add(col.toString());

        Dataframe df = fromJsonRows(jobj.getJSONArray("rows").toString());

        if(!df.getColumns().equals(columns))
            throw new IllegalStateException("mismatch between columns and rows. Columns: [" + String.join(", ", columns)+"]. Rows: [" + String.join(", ", df.getColumns()) +"]");

        return df;
    }




    // Series serialization


    /**
     * Converts the given series into a JSON array string representation.
     *
     * @param series the {@link Series} to be converted into a JSON string
     * @return a JSON string representation of the given series
     */
    public static String toJson(Series series)
    {
        if(series == null)
            throw new IllegalArgumentException("series is null");

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
    public static Series fromJson(String json)
    {
        if(json == null)
            throw new IllegalArgumentException("json is null");

        try
        {
            Series series = new Series();
            JSONArray jarr = new JSONArray(json);

            for(int i = 0; i < jarr.length(); i++)
            {
                double v = jarr.isNull(i) ? Double.NaN : jarr.getDouble(i);
                series.add(v);
            }
            return series;
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Invalid Json");
        }
    }
}
