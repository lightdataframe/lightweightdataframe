package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Grouper;
import io.github.lightdataframe.lightweightdataframe.RowGrouper;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GrouperTest
{

    /**
     * Helper method to create dummy Dataframe objects for testing.
     *
     * @return A sample Dataframe with predefined rows and columns.
     */
    private Dataframe createDummyDataframe()
    {
        Dataframe df = new Dataframe();
        Map<String, Double> row1 = new HashMap<>();
        row1.put("col1", 10.0);
        row1.put("col2", 20.0);
        row1.put("col3", 30.0);
        Map<String, Double> row2 = new HashMap<>();
        row2.put("col1", 15.0);
        row2.put("col2", 25.0);
        row2.put("col3", 35.0);
        Map<String, Double> row3 = new HashMap<>();
        row3.put("col1", 20.0);
        row3.put("col2", 30.0);
        row3.put("col3", 40.0);
        Map<String, Double> row4 = new HashMap<>();
        row4.put("col1", 50.0);
        row4.put("col2", 20.0);
        row4.put("col3", 40.0);
        Map<String, Double> row5 = new HashMap<>();
        row5.put("col1", 50.0);
        row5.put("col2", 20.0);
        row5.put("col3", 30.0);

        df = df.append(false, row1);
        df = df.append(false, row2);
        df = df.append(false, row3);
        df = df.append(false, row4);
        df = df.append(false, row5);

        return df;
    }

    @Test
    void testSplitValid()
    {
        Grouper grouper = new Grouper();
        RowGrouper rowGrouper = grouper.split(3);

        Dataframe df = createDummyDataframe();


        // Test splitting into groups
        List<Object> result = rowGrouper.getRowGroups(df, 4);
        assertEquals(Collections.singletonList(2), result);

        Map<Object, Dataframe> groups = df.group(grouper.split(1));

        assertEquals(1, groups.size());


    }

    @Test
    void testSplitInvalid()
    {
        Grouper grouper = new Grouper();

        // Test invalid splits
        assertThrows(IllegalArgumentException.class, () -> grouper.split(0));
        assertThrows(IllegalArgumentException.class, () -> grouper.split(-1));
    }

    @Test
    void testGroupByColumnValuesValid()
    {
        Grouper grouper = new Grouper();
        Dataframe df = createDummyDataframe();

        RowGrouper rowGrouper = grouper.groupByColumnValues("col1", "col2");

        // Group rows by col1 and col2 values
        List<Object> expected = Arrays.asList(10.0, 20.0);
        List<Object> result = rowGrouper.getRowGroups(df, 0);
        assertEquals(Collections.singletonList(expected), result);

        Map<Object, Dataframe> groups = df.group(rowGrouper);

    }

    @Test
    void testGroupByColumnValuesInvalidColumns()
    {
        Grouper grouper = new Grouper();
        Dataframe df = createDummyDataframe();

        // Test invalid column names
        RowGrouper rowGrouper = grouper.groupByColumnValues("col1", "invalid_col");
        assertThrows(IllegalArgumentException.class, () -> rowGrouper.getRowGroups(df, 0));
    }

    @Test
    void testSlidingWindowValid()
    {
        Grouper grouper = new Grouper();
        RowGrouper rowGrouper = grouper.slidingWindow(3);
        Dataframe df = createDummyDataframe();

        // Test a valid sliding window
        List<Object> result = rowGrouper.getRowGroups(df, 4);
        assertEquals(Arrays.asList(4, 3, 2), result);
    }

    @Test
    void testSlidingWindowEmptyGroup()
    {
        Grouper grouper = new Grouper();
        RowGrouper rowGrouper = grouper.slidingWindow(3);
        Dataframe df = createDummyDataframe();

        // Test with an index not a multiple of step
        List<Object> result = rowGrouper.getRowGroups(df, 3);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSlidingWindowInvalidParams()
    {
        Grouper grouper = new Grouper();

        // Test slidingWindow with invalid parameters
        assertThrows(IllegalArgumentException.class, () -> grouper.slidingWindow(3));
        assertThrows(IllegalArgumentException.class, () -> grouper.slidingWindow(-1));
    }
}
