package io.github.lightdataframe.lightweightdataframe.tests;


import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Series;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class DataframeBisTest
{

    private Dataframe dataframe;

    @BeforeEach
    public void setUp() {
        dataframe = new Dataframe();
    }

    @Test
    public void testGetColumns_emptyDataframe() {
        assertTrue(dataframe.getColumns().isEmpty(), "Expected an empty list of columns");
    }

    @Test
    public void testGetColumns_nonEmptyDataframe() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0, 3.0)));
        dataframe.setColumn("B", new Series(Arrays.asList(4.0, 5.0, 6.0)));
        List<String> columns = dataframe.getColumns();
        assertEquals(2, columns.size());
        assertTrue(columns.containsAll(Arrays.asList("A", "B")));
    }

    @Test
    public void testGetColumn_existingColumn() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0, 3.0)));
        Series column = dataframe.getColumn("A");
        assertEquals(3, column.size());
        assertEquals(1.0, column.get(0));
        assertEquals(2.0, column.get(1));
        assertEquals(3.0, column.get(2));
    }

    @Test
    public void testGetColumn_nonExistingColumn() {
        assertThrows(IllegalArgumentException.class, () -> dataframe.getColumn("NonExisting"));
    }

    @Test
    public void testSetColumn_newColumn() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0, 3.0)));
        assertEquals(Arrays.asList("A"), dataframe.getColumns());
        Series column = dataframe.getColumn("A");
        assertEquals(3, column.size());
    }

    @Test
    public void testSetColumn_replaceExistingColumn() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0, 3.0)));
        dataframe.setColumn("A", new Series(Arrays.asList(4.0, 5.0, 6.0)));
        Series column = dataframe.getColumn("A");
        assertEquals(Arrays.asList(4.0, 5.0, 6.0), column.stream().collect(Collectors.toList()));
    }


    @Test
    public void testReplaceColumn_nonExistingColumn() {
        assertThrows(IllegalArgumentException.class, () -> dataframe.replaceColumn("NonExisting", series -> series.map(value -> value * 2)));
    }

    @Test
    public void testRenameColumn_validRename() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0, 3.0)));
        dataframe.renameColumn("A", "B");
        assertEquals(Arrays.asList("B"), dataframe.getColumns());
        Series column = dataframe.getColumn("B");
        assertEquals(Arrays.asList(1.0, 2.0, 3.0), column.stream().collect(Collectors.toList()));
    }

    @Test
    public void testRenameColumn_existingNewName() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0, 3.0)));
        dataframe.setColumn("B", new Series(Arrays.asList(4.0, 5.0, 6.0)));
        assertThrows(IllegalArgumentException.class, () -> dataframe.renameColumn("A", "B"));
    }

    @Test
    public void testDropColumn_existingColumn() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0, 3.0)));
        dataframe.setColumn("B", new Series(Arrays.asList(4.0, 5.0, 6.0)));
        dataframe.dropColumn("A");
        assertEquals(Collections.singletonList("B"), dataframe.getColumns());
    }

    @Test
    public void testDropColumn_nonExistingColumn() {
        assertThrows(IllegalArgumentException.class, () -> dataframe.dropColumn("NonExisting"));
    }

    @Test
    public void testReorderColumns_validOrder() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0)));
        dataframe.setColumn("B", new Series(Arrays.asList(3.0, 4.0)));
        dataframe.reorderColumns("B", "A");
        assertEquals(Arrays.asList("B", "A"), dataframe.getColumns());
    }

    @Test
    public void testReorderColumns_invalidOrder() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0)));
        dataframe.setColumn("B", new Series(Arrays.asList(3.0, 4.0)));
        assertThrows(IllegalArgumentException.class, () -> dataframe.reorderColumns("C"));
    }

    @Test
    public void testAppend_validRow() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0)));
        dataframe.setColumn("B", new Series(Arrays.asList(3.0, 4.0)));
        Map<String, Double> row = new HashMap<>();
        row.put("A", 5.0);
        row.put("B", 6.0);
        dataframe.append(row);
        assertEquals(3, dataframe.size());
        assertEquals(5.0, dataframe.get("A", 2));
        assertEquals(6.0, dataframe.get("B", 2));
    }

    @Test
    public void testAppend_invalidRow() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0)));
        dataframe.setColumn("B", new Series(Arrays.asList(3.0, 4.0)));

        Map<String, Double> row = new HashMap<>();
        row.put("C", 5.0);
        assertThrows(IllegalArgumentException.class, () -> dataframe.append(row));
    }

    @Test
    public void testClone_createsDeepCopy() {
        dataframe.setColumn("A", new Series(Arrays.asList(1.0, 2.0)));
        Dataframe cloned = dataframe.clone();
        assertNotSame(dataframe, cloned);
        assertEquals(dataframe.toString(), cloned.toString());
    }
}
