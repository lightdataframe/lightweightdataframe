package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.*;
import org.junit.jupiter.api.Test;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DataframeTest
{
    @Test
    void getColumns()
    {
        Dataframe df = new Dataframe();
        assertEquals(Arrays.asList(), df.getColumns());
        assertEquals(0, df.getColumns().size());

        df = TestUtil.randomDataframe(1000);
        assertEquals(6, df.getColumns().size());
        assertEquals(df.getColumns(), Arrays.asList("col0", "col1", "col2", "col3", "col4", "col5"));
    }

    @Test
    void getColumn()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        assertThrows(IllegalArgumentException.class, () -> df.getColumn(null));
        assertThrows(IllegalArgumentException.class, () -> df.getColumn("invalid_col"));
        assertEquals(1000, df.getColumn("col0").size());
    }

    @Test
    void setColumn()
    {
        Dataframe df0 = new Dataframe();
        Dataframe df = TestUtil.randomDataframe(1000);
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

        df0 = df0.setColumn("col0", Series.range(0, 1000, 1));
        assertEquals(1000, df0.size());
        assertEquals(Collections.singletonList("col0"), df0.getColumns());
    }

    @Test
    void renameColumn()
    {
        Dataframe df = TestUtil.randomDataframe(1000);

        df.renameColumn("col0", "col0_new");
        assertEquals(Arrays.asList("col0_new", "col1", "col2", "col3", "col4", "col5"), df.getColumns());
    }

    @Test
    void replaceColumn()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        //Invalid
        Dataframe finalDf = df;
        assertThrows(IllegalArgumentException.class, () -> finalDf.replaceColumn("col0", null));
        assertThrows(IllegalArgumentException.class, () -> finalDf.replaceColumn(null, col -> col));
        assertThrows(IllegalArgumentException.class, () -> finalDf.replaceColumn("invalid_col", col -> col));
        assertThrows(IllegalArgumentException.class, () -> finalDf.replaceColumn("col0", col0 -> new Series()));
        //Valid
        df = df.replaceColumn("col0", col0 -> col0.map(v -> v + 1));
        df = df.replaceColumn("col5", col5 -> col5.map(v -> v + 1));
        assertEquals(6, df.getColumns().size());
    }

    @Test
    void dropColumn()
    {
        Dataframe df0 = new Dataframe();
        Dataframe df = TestUtil.randomDataframe(1000);
        //Invalid
        Dataframe finalDf = df;
        assertThrows(IllegalArgumentException.class, () -> finalDf.dropColumn(null));
        assertThrows(IllegalArgumentException.class, () -> finalDf.dropColumn("invalid_col"));
        assertThrows(IllegalArgumentException.class, () -> df0.dropColumn("col0"));
        //Valid
        df = df.dropColumn("col0");
        df = df.dropColumn("col5");
        assertEquals(4, df.getColumns().size());
        for(Row r : df.getRows())
            assertFalse(r.containsKey("col0") || r.containsKey("col5"));
    }

    @Test
    void reorderColumns()
    {
        Dataframe df0 = new Dataframe();
        Dataframe df = TestUtil.randomDataframe(1000);
        //Invalid
        Dataframe finalDf = df;
        assertThrows(IllegalArgumentException.class, () -> finalDf.reorderColumns("col1", null));
        assertThrows(IllegalArgumentException.class, () -> finalDf.reorderColumns("col6"));
        assertThrows(IllegalArgumentException.class, () -> df0.reorderColumns("col0"));
        //Valid
        df = df.reorderColumns("col2", "col1", "col0");
        assertEquals(Arrays.asList("col2", "col1", "col0", "col3", "col4", "col5"), df.getColumns());

        for(Row row : df.getRows())
            assertEquals(Arrays.asList("col2", "col1", "col0", "col3", "col4", "col5"), new ArrayList<>(row.keySet()));
    }

    @Test
    void getRows()
    {
        Dataframe df = TestUtil.randomDataframe(1000);

        List<Row> rows = df.getRows();
        assertEquals(df.size(), rows.size());
        for(Row r : rows)
            assertEquals(df.getColumns(), new ArrayList<>(r.keySet()));
    }

    @Test
    void getRow()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.getRow(-1));
        assertThrows(IllegalArgumentException.class, () -> df.getRow(df.size()));
        //Valid
        for(int i = 0; i < df.size(); i++)
            assertEquals(df.getRow(i), df.getRows().get(i));
    }

    @Test
    void size()
    {
        Dataframe df = new Dataframe();
        assertEquals(0, df.size());
        df = TestUtil.randomDataframe(1000);
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
        testRow.put("col2", 2d);
        df0.append(testRow);
        assertEquals(2, df0.size());
        testRow.remove("col1");
        Map<String, Double> finalTestRow = testRow;
        assertThrows(IllegalArgumentException.class, () -> df0.append(finalTestRow));

        Dataframe df = TestUtil.randomDataframe(1000);
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
        Dataframe df = TestUtil.randomDataframe(200);
        Row r0 = df.getRow(0);
        df = df.sort(true, Sorter.shuffle());

        df.append(r0);
        assertEquals(201, df.size());
        assertEquals(r0, df.getRow(200));

        df.print();
    }


    @Test
    void insert()
    {
        Dataframe df = new Dataframe();

        //invalid
        assertThrows(IllegalArgumentException.class, () -> df.insert(0, null));
        assertThrows(IllegalArgumentException.class, () -> df.insert(1, TestUtil.randomRow()));

        df.insert(0, TestUtil.randomRow());
        assertEquals(1, df.size());

        df.insert(0, TestUtil.randomRow());
        assertEquals(2, df.size());
        df.insert(2, TestUtil.randomRow());
        assertEquals(3, df.size());

    }

    @Test
    void remove()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        //Invalid
        Dataframe finalDf = df;
        assertThrows(IllegalArgumentException.class, () -> finalDf.remove(-1));
        assertThrows(IllegalArgumentException.class, () -> finalDf.remove(finalDf.size()));
        //Valid
        df = df.remove(0);
        assertEquals(999, df.size());
        df.remove(0);
        assertEquals(998, df.size());
    }

    @Test
    void get()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.get(null, 0));
        assertThrows(IllegalArgumentException.class, () -> df.get("col0", -1));
        assertThrows(IllegalArgumentException.class, () -> df.get("col0", df.size()));
        //Valid
        for(int i = 0; i < df.size(); i++)
            assertEquals(df.getRow(i).get("col0"), df.get("col0", i));
    }

    @Test
    void set()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        //Invalid
        Dataframe finalDf = df;
        assertThrows(IllegalArgumentException.class, () -> finalDf.set(null, 0, 0d));
        assertThrows(IllegalArgumentException.class, () -> finalDf.set("invalid_col", 0, 0d));

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
    }

    @Test
    void print()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        df.print();
        Dataframe.setMaxPrintedRows(0);
        df.print();
        Dataframe.setMaxPrintedRows(10);
        df.print();
    }


    @Test
    void setWithColumnN()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        //Invalid
        Dataframe finalDf = df;
        assertThrows(IllegalArgumentException.class, () -> finalDf.set(null, 0, 0d));
        assertThrows(IllegalArgumentException.class, () -> finalDf.set("invalid_col", 0, 0d));

        assertThrows(IllegalArgumentException.class, () -> finalDf.set("col0", -1, 0d));
        assertThrows(IllegalArgumentException.class, () -> finalDf.set("col0", finalDf.size(), 0d));
        //Valid
        df = df.set("col0", 0, 0d);
        assertEquals(0d, df.get("col0", 0));

        df.set("col0", 10, 1000d);
        assertEquals(1000, df.get("col0", 10));
    }

    @Test
    void setWithColumn()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        //Invalid
        Dataframe finalDf = df;
        assertThrows(IllegalArgumentException.class, () -> finalDf.set(null, 0d));
        assertThrows(IllegalArgumentException.class, () -> finalDf.set("invalid_col", 0d));
        //Valid
        df = df.set("col3", 0d);
        for(Row r : df.getRows())
            assertEquals(0d, r.get("col3"));

        df.set("col3", 1000d);
        assertEquals(1000, df.get("col3", 10));
    }

    @Test
    void replace()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
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
        Dataframe df = TestUtil.randomDataframe(1000);

        assertEquals(6, df.statistics().size());
        df.printStatistics();

        Dataframe df0 = new Dataframe();
        assertEquals(1, df0.statistics().size());
        df0.printStatistics();
    }

    @Test
    void cloneTest()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        Dataframe df0 = df.clone();
        assertNotEquals(df, df0);
        assertEquals(df.getRows(), df0.getRows());
    }

    @Test
    void sort()
    {
        RowScorer s0 = Sorter.sortByColumns(true, "col0");
        RowScorer s1 = Sorter.sortByColumns(false, "col0");
        Dataframe df = TestUtil.randomDataframe(1000);
        //Invalid
        assertThrows(IllegalArgumentException.class, () -> df.sort(true, null));


        df.sort(true, s0).print();

        df.append(df.getRow(0));
        df.sort(true, s1).print();

    }


    @Test
    void find()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        List<Integer> ids = df.find(Filter.rowTest(row -> Double.isNaN(row.get("col0"))));
        for(Integer id : ids)
            assertTrue(Double.isNaN(df.getRow(id).get("col0")));
        int n = 10;
        ids = df.find(Filter.rowTest(row -> row.equals(df.getRow(n))));
        assertEquals(Collections.singletonList(n), ids);


        df.set(10, 1000);
        df.set(50, 1000);
        ids = df.find(Filter.rowTest(row -> row.equals(df.getRow(10))));
        assertEquals(Arrays.asList(10, 50), ids);
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
        }

    }

    @Test
    void group2()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        RowGrouper g = Grouper.groupByRow(row -> row.get("col0") > 0);

        Map<Object, Dataframe> groups = df.group(g);

        assertTrue(groups.get(true).getColumn("col0").withoutNan().all(v -> v > 0));
        assertTrue(groups.get(false).getColumn("col0").withoutNan().all(v -> v <= 0));
    }
    @Test
    void aggregate()
    {
        Dataframe df = TestUtil.randomDataframe(1000);
        Dataframe agg = df.aggregate(
                new Aggregator()
                .addAggregation("sum4", df0 -> df0.getColumn("col4").withoutNan().sum())
                        .addAggregation("sum5", df0 -> df0.getColumn("col5").withoutNan().sum())
        );
        assertEquals(1, agg.size());
        assertEquals(2, agg.getColumns().size());
        agg.print();
    }
}
