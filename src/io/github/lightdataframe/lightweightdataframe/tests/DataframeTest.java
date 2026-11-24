package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class DataframeTest
{
    Dataframe df;

    @BeforeEach
    void initialize()
    {
        df = TestUtil.randomDataframe(1000); // 6 columns
    }

    @Test
    void getColumns()
    {
        Dataframe df0 = new Dataframe();
        assertEquals(Collections.emptyList(), df0.getColumns());
        assertEquals(0, df0.getColumns().size());

        assertEquals(6, df.getColumns().size());
        assertEquals(df.getColumns(), Arrays.asList("col0", "col1", "col2", "col3", "col4", "col5"));
    }

    @Test
    void getColumn()
    {
        assertThrows(IllegalArgumentException.class, () ->  new Dataframe().getColumn("invalid_col"));
        assertThrows(IllegalArgumentException.class, () -> df.getColumn(null));
        assertThrows(IllegalArgumentException.class, () -> df.getColumn("invalid_col"));
        for(String col : Arrays.asList("col0", "col1", "col2", "col3", "col4", "col5"))
            assertEquals(1000, df.getColumn(col).size());
    }

    @Test
    void setColumn()
    {
        //Invalid
        Dataframe finalDf = df;
        assertThrows(IllegalArgumentException.class, () -> finalDf.setColumn(null, finalDf.getColumn("col0")));
        assertThrows(IllegalArgumentException.class, () -> finalDf.setColumn("col0", null));
        //Valid
        df = df.setColumn("col0", df.getColumn("col0").map(v -> v + 1));
        df = df.setColumn("col6", df.getColumn("col0").map(v -> v + 1));
        assertEquals(7, df.getColumns().size());
        for(Row r : df.getRows())
            assertTrue(r.containsKey("col6"));

        Dataframe df0 = new Dataframe();
        df0.setColumn("col0", Series.range(0, 200, 1));
        assertEquals(200, df0.size());
        assertEquals(Collections.singletonList("col0"), df0.getColumns());
    }

    @Test
    void renameColumn()
    {
        df.renameColumn("col0", "col0_new");
        assertEquals(Arrays.asList("col0_new", "col1", "col2", "col3", "col4", "col5"), df.getColumns());
        df.renameColumn("col2", "col2_new");
        assertEquals(Arrays.asList("col0_new", "col1", "col2_new", "col3", "col4", "col5"), df.getColumns());
        assertThrows(IllegalArgumentException.class, () -> df.renameColumn("invalid_col", "col_new"));
        assertThrows(IllegalArgumentException.class, () -> df.renameColumn("col3", "col4"));
    }

    @Test
    void replaceColumn()
    {
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.replaceColumn("col0", null));
        assertThrows(IllegalArgumentException.class, () -> df.replaceColumn(null, col -> col));
        assertThrows(IllegalArgumentException.class, () -> df.replaceColumn("invalid_col", col -> col));
        assertThrows(IllegalArgumentException.class, () -> df.replaceColumn("col0", col0 -> new Series()));
        //Valid
        df.replaceColumn("col0", col0 -> col0.map(v -> v + 1));
        df.replaceColumn("col5", col5 -> col5.map(v -> v + 1));
        assertEquals(6, df.getColumns().size());
    }

    @Test
    void dropColumn()
    {
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.dropColumn(null));
        assertThrows(IllegalArgumentException.class, () -> df.dropColumn("invalid_col"));
        assertThrows(IllegalArgumentException.class, () -> new Dataframe().dropColumn("col0"));
        //Valid
        df.dropColumn("col0");
        df.dropColumn("col5");
        assertEquals(4, df.getColumns().size());
        assertEquals(Arrays.asList("col1", "col2", "col3", "col4"), df.getColumns());

        for(Row r : df.getRows())
            assertFalse(r.containsKey("col0") || r.containsKey("col5"));
    }

    @Test
    void reorderColumns()
    {
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.reorderColumns("col1", null));
        assertThrows(IllegalArgumentException.class, () -> df.reorderColumns("col6"));
        assertThrows(IllegalArgumentException.class, () -> new Dataframe().reorderColumns("col0"));
        assertThrows(IllegalArgumentException.class, () -> new Dataframe().reorderColumns(null, null));
        //Valid
        df.reorderColumns("col2", "col1", "col0");
        assertEquals(Arrays.asList("col2", "col1", "col0", "col3", "col4", "col5"), df.getColumns());

        for(Row row : df.getRows())
            assertEquals(Arrays.asList("col2", "col1", "col0", "col3", "col4", "col5"), new ArrayList<>(row.keySet()));
    }

    @Test
    void getRows()
    {
        assertEquals(0, new Dataframe().getRows().size());
        List<Row> rows = df.getRows();
        assertEquals(df.size(), rows.size());
        for(Row r : rows)
            assertEquals(df.getColumns(), new ArrayList<>(r.keySet()));
    }

    @Test
    void getRow()
    {
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.getRow(-1));
        assertThrows(IllegalArgumentException.class, () -> df.getRow(df.size()));

        //Valid
        for(int i = 0; i < df.size(); i++)
            assertEquals(df.getRow(i), df.getRows().get(i));

        for(int i = 0; i < df.size(); i++)
            assertEquals(i, df.getRow(i).getIndex());

        assertThrows(UnsupportedOperationException.class, () -> df.getRow(0).put("col0", 1.0));
        assertThrows(UnsupportedOperationException.class, () -> df.getRow(0).remove("col0"));
        assertThrows(UnsupportedOperationException.class, () -> df.getRow(0).clear());

    }

    @Test
    void size()
    {
        Dataframe df0 = new Dataframe();
        assertEquals(0, df0.size());
        assertEquals(1000, df.size());
    }

    @Test
    void append()
    {
        Dataframe df0 = new Dataframe();

        Map<String, Double> testRow = new LinkedHashMap<>();
        testRow.put("col0", 0d);
        testRow.put("col1", 1d);
        df0.append(testRow);
        assertEquals(1, df0.size());
        assertEquals(Arrays.asList("col0", "col1"), df0.getColumns());

        testRow.put("col2", 2d);
        df0.append(testRow);
        assertEquals(2, df0.size());
        assertEquals(Arrays.asList("col0", "col1"), df0.getColumns());


        testRow.remove("col1");
        Map<String, Double> finalTestRow = testRow;
        assertThrows(IllegalArgumentException.class, () -> df0.append(finalTestRow));

        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.append(null));
        assertThrows(IllegalArgumentException.class, () -> df.append(Collections.singletonMap("col0", 0d)));

        //Valid
        testRow = new HashMap<>(df.getRow(0));
        testRow.put("col6", 1d);
        df.append(testRow);
        assertEquals(1001, df.size());
        df.setColumn("col0", df.getColumn("col0").map(v -> v + 1));
        df.append(testRow);
        assertEquals(1002, df.size());
    }

    @Test
    void append2()
    {
        Row r0 = df.getRow(0);
        df = df.sort(true, Sorter.shuffle());

        df.append(r0);
        assertEquals(1001, df.size());
        assertEquals(r0, df.getRow(1000));

        df.print();
    }


    @Test
    void insert()
    {
        Dataframe df0 = new Dataframe();

        //invalid
        assertThrows(IllegalArgumentException.class, () -> df0.insert(0, null));
        assertThrows(IllegalArgumentException.class, () -> df0.insert(1, TestUtil.randomRow()));

        df0.insert(0, TestUtil.randomRow());
        assertEquals(1, df0.size());

        Map<String, Double> testRow = TestUtil.randomRow();
        df0.insert(0, testRow);
        assertEquals(2, df0.size());

        testRow = TestUtil.randomRow();
        df0.insert(2, testRow);

        df0.insert(df0.size(), TestUtil.randomRow());
        assertEquals(4, df0.size());
    }

    @Test
    void remove()
    {
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.remove(-1));
        assertThrows(IllegalArgumentException.class, () -> df.remove(df.size()));
        //Valid
        df.remove(0);
        assertEquals(999, df.size());
        df.remove(0);
        assertEquals(998, df.size());

        Row row = df.getRow(0);
        df.remove(0);
        assertEquals(997, df.size());
        assertEquals(0, df.find(Filter.rowTest(r -> r.equals(row))).size());
    }

    @Test
    void get()
    {
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.get(null, 0));
        assertThrows(IllegalArgumentException.class, () -> df.get("col0", -1));
        assertThrows(IllegalArgumentException.class, () -> df.get("col0", df.size()));
        assertThrows(IllegalArgumentException.class, () -> df.get("invalid_col", df.size() - 1));
        //Valid
        for(int i = 0; i < df.size(); i++)
            assertEquals(df.getRow(i).get("col0"), df.get("col0", i));

    }

    @Test
    void set()
    {
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.set(null, 0, 0d));
        assertThrows(IllegalArgumentException.class, () -> df.set("invalid_col", 0, 0d));
        assertThrows(IllegalArgumentException.class, () -> df.set("col0", df.size(), 0d));
        assertThrows(IllegalArgumentException.class, () -> df.set(-1, 0d));
        assertThrows(IllegalArgumentException.class, () -> df.set(df.size(), 0d));

        Map<String, Double> testRow = new LinkedHashMap<>();
        testRow.put("col0", null);
        testRow.put("col1", 1d);
        df.set(1, testRow);
        assertEquals(Double.NaN, df.get("col0", 1));

        Map<String, Double> testRow1 = new LinkedHashMap<>();
        testRow1.put("invalid_column", 0d);
        Row before = df.getRow(1);
        df.set(1, testRow1);
        assertEquals(before, df.getRow(1));

        assertThrows(IllegalArgumentException.class, () -> new Dataframe().set(0, testRow1).size());
    }


    @Test
    void setWithColumnN()
    {
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.set(null, 0, 0d));
        assertThrows(IllegalArgumentException.class, () -> df.set("invalid_col", 0, 0d));

        assertThrows(IllegalArgumentException.class, () -> df.set("col0", -1, 0d));
        assertThrows(IllegalArgumentException.class, () -> df.set("col0", df.size(), 0d));
        //Valid
        df.set("col2", 0, 0d);
        assertEquals(0d, df.get("col2", 0));

        df.set("col3", 10, Double.NaN);
        assertEquals(Double.NaN, df.get("col3", 10));
    }

    @Test
    void setWithColumn()
    {
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.set(null, 0d));
        assertThrows(IllegalArgumentException.class, () -> df.set("invalid_col", 0d));
        //Valid
        df.set("col3", 0d);
        for(Row r : df.getRows())
            assertEquals(0d, r.get("col3"));

        df.set("col2", 1000d);
        assertEquals(1000, df.get("col2", 10));
        df.print();
    }


    @Test
    void print()
    {
        Dataframe df = TestUtil.randomDataframe(20);
        df.print();
        Dataframe.setMaxPrintedRows(0);
        df.print();
        Dataframe.setMaxPrintedRows(20);
        df.print();
        Dataframe.setPrintedValuesLength(10);
        Dataframe.setPrintedValuesPrecision(0);
        df.print();
        Dataframe.setPrintedValuesPrecision(5);
        df.print();
        assertThrows(IllegalArgumentException.class, () -> Dataframe.setMaxPrintedRows(-1));
        assertThrows(IllegalArgumentException.class, () -> Dataframe.setPrintedValuesLength(-1));
        assertThrows(IllegalArgumentException.class, () -> Dataframe.setPrintedValuesPrecision(-1));
    }

    @Test
    void replace()
    {
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.replace(null, 0d, v -> true));
        assertThrows(IllegalArgumentException.class, () -> df.replace("col0", 0d, null));

        //Valid
        df.replace(Double.NaN, v -> v < 0);
        for(Row r : df.getRows())
            for(String col : df.getColumns())
                assertTrue(Double.isNaN(r.get(col)) || r.get(col) >= 0);

        df.replace(Double.NaN, v -> v >= 0);
        for(Row r : df.getRows())
            for(String col : df.getColumns())
                assertTrue(Double.isNaN(r.get(col)));
    }

    @Test
    void statistics()
    {

        assertEquals(6, df.statistics().size());
        df.printStatistics();

        Dataframe df0 = new Dataframe();
        assertEquals(1, df0.statistics().size());
        df0.printStatistics();
    }

    @Test
    void cloneTest()
    {
        Dataframe df0 = df.clone();
        assertNotEquals(df, df0);
        assertEquals(df.getRows(), df0.getRows());
    }

    @Test
    void apply()
    {
        assertThrows(IllegalArgumentException.class, () -> df.apply(null));

        Series result = df.apply(Mapper.mapByRow(row -> row.get("col0") + row.get("col1")));
        assertEquals(df.size(), result.size());


        for(int i = 0; i < df.size(); i++)
            assertEquals(df.getRow(i).get("col0") + df.getRow(i).get("col1"), result.get(i));

        assertThrows(IllegalArgumentException.class, () -> df.apply(null));

        df.setColumn("index", df.apply(Mapper.mapByIndex(i -> i)));
        df.reorderColumns("index").print();

        result = df.apply(Mapper.mapByIndex(i -> i));
        assertEquals(Series.range(0, df.size(), 1), result);

        Series col0 = df.getColumn("col0");
        result = df.apply(Mapper.mapByColumn("col0", v -> v * 2));

        assertEquals(result, col0.map(v -> v * 2));

    }

    @Test
    void sort()
    {
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.sort(true, null));
        RowScorer s0 = Sorter.sortByColumns(true, "col1");
        RowScorer s1 = Sorter.sortByColumns(false, "col1");
        RowScorer s2 = Sorter.sortByColumns(false, "col3");

        assertEquals(df.getRows(), df.sort(true, s2).getRows());

        df.sort(true, s0).print();

        df.sort(true, s1).print();
    }


    @Test
    void find()
    {
        List<Integer> ids = df.find(Filter.hasNan("col0"));
        for(Integer id : ids)
            assertTrue(Double.isNaN(df.getRow(id).get("col0")));

        int n = 10;
        ids = df.find(Filter.rowTest(row -> row.equals(df.getRow(n))));
        assertEquals(Collections.singletonList(n), ids);


        df.set(10, 1000);
        df.set(50, 1000);
        df.set(55, 1000);
        ids = df.find(Filter.rowTest(row -> row.equals(df.getRow(10))));
        assertEquals(Arrays.asList(10, 50, 55), ids);
    }

    @Test
    void group()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        Map<Object, Dataframe> groups = df.group(Grouper.groupByColumnValues("col0"));
        assertTrue(groups.size() <= 1000);

        for(Object key : groups.keySet())
        {
            Dataframe df1 = groups.get(key);
            assertTrue(df1.getColumn("col0").all(v -> v.equals(df1.get("col0", 0))));
            assertEquals(1, df1.getColumn("col0").unique().size());
        }
    }

    @Test
    void group2()
    {
        RowGrouper g = Grouper.groupByRow(row -> row.get("col0") > 0);

        Map<Object, Dataframe> groups = df.group(g);

        assertTrue(groups.get(true).getColumn("col0").withoutNan().all(v -> v > 0));
        assertTrue(groups.get(false).getColumn("col0").withoutNan().all(v -> v <= 0));

        groups = df.group(Grouper.groupByRow(row -> Double.isNaN(row.get("col4"))));

        groups = df.group(Grouper.groupByRow(row -> Double.isNaN(row.get("col0")) ? Double.NaN : row.get("col0").intValue()));
    }


    @Test
    void aggregate()
    {
        Dataframe agg = df.aggregate(
                new Aggregator()
                        .addAggregation("sum4", df0 -> df0.getColumn("col4").withoutNan().sum())
                        .addAggregation("sum5", df0 -> df0.getColumn("col5").withoutNan().sum())
        );
        assertEquals(1, agg.size());
        assertEquals(2, agg.getColumns().size());
        agg.print();
    }

    @Test
    void groupAggregate()
    {
        RowGrouper grouper = Grouper.groupByRow(row -> Double.isNaN(row.get("col0")) ? Double.NaN : Math.floor(row.get("col0")));

        Map<Object, Dataframe> groups = df.group(grouper);

        List<Object> grps = groups.keySet().stream().sorted().collect(Collectors.toList());

        assertEquals(21, grps.size());

        Aggregator aggr = new Aggregator()
                .addAggregation("sum4", df0 -> df0.getColumn("col4").withoutNan().sum())
                .addAggregation("sum5", df0 -> df0.getColumn("col5").withoutNan().sum());

        Dataframe agg = df.groupAggregate(grouper, aggr);

        assertEquals(groups.size(), agg.size());
        assertEquals(Arrays.asList("sum4", "sum5"), agg.getColumns());
    }
}
