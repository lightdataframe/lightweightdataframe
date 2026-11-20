package io.github.lightdataframe.lightweightdataframe.tests;


import io.github.lightdataframe.lightweightdataframe.Series;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SeriesBisTest {
    private Series series;

    @BeforeEach
    public void setUp() {
        series = new Series();
    }

    @Test
    public void testConstructor_emptySeries() {
        assertTrue(series.isEmpty(), "Series should be empty");
    }

    @Test
    public void testConstructor_withCollection() {
        series = new Series(Arrays.asList(1.0, null, 2.0));
        assertEquals(3, series.size());
        assertEquals(1.0, series.get(0));
        assertTrue(Double.isNaN(series.get(1))); // Null becomes NaN
        assertEquals(2.0, series.get(2));
    }

    @Test
    public void testSample_withReplacement() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0));
        Series sample = series.sample(5, true);
        assertEquals(5, sample.size(), "Sample size should match");
        assertTrue(series.containsAll(sample), "Sample elements must belong to the original series");
    }

    @Test
    public void testSample_withoutReplacement() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0));
        Series sample = series.sample(2, false);
        assertEquals(2, sample.size());
        assertTrue(series.containsAll(sample));
    }

    @Test
    public void testGroupBy() {
        series.addAll(Arrays.asList(-1.0, 0.0, 1.0, 2.0, 3.0));
        Map<Double, Series> grouped = series.group(v -> v > 0 ? 1.0 : 0.0);
        assertEquals(2, grouped.size());
        assertTrue(grouped.containsKey(0.0));
        assertTrue(grouped.containsKey(1.0));
    }

    @Test
    public void testMap_singleInputFunction() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0));
        Series result = series.map(v -> v * 2);
        assertEquals(Arrays.asList(2.0, 4.0, 6.0), result);
    }

    @Test
    public void testMap_withIndexFunction() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0));
        Series result = series.map((s, i) -> s.get(i) + i);
        assertEquals(Arrays.asList(1.0, 3.0, 5.0), result);
    }

    @Test
    public void testTestPredicate() {
        series.addAll(Arrays.asList(1.0, 2.0, -3.0, 0.0));
        Series result = series.test(v -> v > 0);
        assertEquals(Arrays.asList(1.0, 1.0, 0.0, 0.0), result);
    }

    @Test
    public void testAll() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0));
        assertTrue(series.all(v -> v > 0), "All elements should be greater than 0");
        assertFalse(series.all(v -> v > 2), "Not all elements are greater than 2");
    }

    @Test
    public void testAny() {
        series.addAll(Arrays.asList(1.0, -1.0, 3.0));
        assertTrue(series.any(v -> v < 0), "At least one element should be less than 0");
        assertFalse(series.any(v -> v > 4), "No elements are greater than 4");
    }

    @Test
    public void testWithoutNan() {
        series.addAll(Arrays.asList(1.0, Double.NaN, 2.0, Double.NaN));
        Series result = series.withoutNan();
        assertEquals(Arrays.asList(1.0, 2.0), result);
    }

    @Test
    public void testReplaceValues() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0));
        Series result = series.replace(0.0, v -> v > 2);
        assertEquals(Arrays.asList(1.0, 2.0, 0.0), result);
    }

    @Test
    public void testAddWithNullValues() {
        series.add(null);
        assertTrue(Double.isNaN(series.get(0)), "Null value should be converted to NaN");
        series.add(5.0);
        assertEquals(5.0, series.get(1));
    }

    @Test
    public void testAddAll_nullValuesHandled() {
        series.addAll(Arrays.asList(1.0, null, 2.0));
        assertEquals(3, series.size());
        assertTrue(Double.isNaN(series.get(1)));
    }

    @Test
    public void testMinEmptySeries() {
        assertTrue(Double.isNaN(series.min()), "Empty series should return NaN for min");
    }

    @Test
    public void testMaxEmptySeries() {
        assertTrue(Double.isNaN(series.max()), "Empty series should return NaN for max");
    }

    @Test
    public void testMedian() {
        series.addAll(Arrays.asList(1.0, 3.0, 2.0));
        assertEquals(2.0, series.median());
        series.add(4.0);
        assertEquals(2.5, series.median());
    }

    @Test
    public void testZScore() {
        series.addAll(Arrays.asList(2.0, 4.0, 6.0));
        Series zscores = series.zscore();
        assertEquals(0.0, zscores.average(), 1e-6); // Z-score mean is zero
    }

    @Test
    public void testNormalized() {
        series.addAll(Arrays.asList(2.0, 4.0, 6.0));
        Series normalized = series.normalized();
        assertEquals(0.0, normalized.get(0), 1e-6);
        assertEquals(1.0, normalized.get(2), 1e-6);
    }

    @Test
    public void testSum() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0));
        assertEquals(6.0, series.sum(), 1e-6);
    }

    @Test
    public void testCumulativeSum() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0));
        Series cumsum = series.cumsum();
        assertEquals(Arrays.asList(1.0, 3.0, 6.0), cumsum);
    }

    @Test
    public void testZip() {
        Series other = new Series(Arrays.asList(3.0, 2.0, 1.0));
        series.addAll(Arrays.asList(1.0, 2.0, 3.0));
        Series result = series.zip(other, (a, b) -> a + b);
        assertEquals(Arrays.asList(4.0, 4.0, 4.0), result);
    }

    @Test
    public void testHeadTail() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));
        assertEquals(Arrays.asList(1.0, 2.0), series.head(2));
        assertEquals(Arrays.asList(4.0, 5.0), series.tail(2));
    }

    @Test
    public void testBetween() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));
        Series result = series.between(1, 4);
        assertEquals(Arrays.asList(2.0, 3.0, 4.0), result);
    }

    @Test
    public void testSlidingWindow() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));
        List<Series> windows = series.slidingWindow(2, 3);
        assertEquals(3, windows.size());
        assertEquals(Arrays.asList(1.0, 2.0, 3.0), windows.get(0));
        assertEquals(Arrays.asList(3.0, 4.0, 5.0), windows.get(1));
    }

    @Test
    public void testRange() {
        Series range = Series.range(1.0, 5.0, 1.0);
        assertEquals(Arrays.asList(1.0, 2.0, 3.0, 4.0), range);
    }

    @Test
    public void testStatistics() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0));
        Map<String, Double> stats = series.statistics();
        assertEquals(3.0, stats.get("size"));
        assertEquals(2.0, stats.get("avg"));
    }

    @Test
    public void testClone() {
        series.addAll(Arrays.asList(1.0, 2.0, 3.0));
        Series clone = series.clone();
        assertNotSame(series, clone);
        assertEquals(series, clone);
    }
}
