package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Grouper;
import io.github.lightdataframe.lightweightdataframe.RowGrouper;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GrouperTest
{

    @Test
    void testSplitValid()
    {
        RowGrouper rowGrouper = Grouper.split(4);

        Dataframe df = TestUtil.randomDataframe(8);


        // Test splitting into groups
        List<Object> result = rowGrouper.getRowGroups(df, 4);
        assertEquals(Collections.singletonList(2), result);

        Map<Object, Dataframe> groups = df.group(Grouper.split(1));

        assertEquals(1, groups.size());


    }

    @Test
    void testSplitInvalid()
    {
        // Test invalid splits
        assertThrows(IllegalArgumentException.class, () -> Grouper.split(0));
        assertThrows(IllegalArgumentException.class, () -> Grouper.split(-1));
    }

    @Test
    void testGroupByColumnValuesValid()
    {
        Dataframe df = TestUtil.randomDataframe(8);

        RowGrouper rowGrouper = Grouper.groupByColumnValues("col1", "col2");

        for(int n = 0; n < df.size(); n++)
        {
            // Group rows by col1 and col2 values
            List<Object> expected = Arrays.asList(df.getRow(n).get("col1"), df.getRow(n).get("col2"));
            List<Object> result = rowGrouper.getRowGroups(df, n);
            assertEquals(Collections.singletonList(expected), result);
        }
    }

    @Test
    void testGroupByColumnValuesInvalidColumns()
    {
        Dataframe df = TestUtil.randomDataframe(8);

        // Test invalid column names
        RowGrouper rowGrouper = Grouper.groupByColumnValues("col1", "invalid_col");
        assertThrows(IllegalArgumentException.class, () -> rowGrouper.getRowGroups(df, 0));
    }

    @Test
    void testSlidingWindowValid()
    {
        RowGrouper rowGrouper = Grouper.slidingWindow(6, 5);
        Dataframe df = TestUtil.randomDataframe(8);

        Map<Object, Dataframe> groups = df.group(rowGrouper);

        // Test a valid sliding window
        List<Object> result = rowGrouper.getRowGroups(df, 4);
        assertEquals(Collections.singletonList(0), result);
    }

    @Test
    void testSlidingWindowEmptyGroup()
    {
        RowGrouper rowGrouper = Grouper.slidingWindow(1, 3);
        Dataframe df = TestUtil.randomDataframe(8);

        // Test with an index not a multiple of step
        List<Object> result = rowGrouper.getRowGroups(df, 3);
        assertTrue(!result.isEmpty());
    }

    @Test
    void testSlidingWindowInvalidParams()
    {
        // Test slidingWindow with invalid parameters
        assertThrows(IllegalArgumentException.class, () -> Grouper.slidingWindow(1, -1));
    }

    @Test
    void testRandomGroups()
    {
        Dataframe df = TestUtil.randomDataframe(8);

        Map<Object, Dataframe> groups = df.group(Grouper.random(4,2));
    }
}
