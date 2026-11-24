package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Series;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;

class SeriesTestBis {

    @Test
    void testConstructor() {
        Series emptySeries = new Series();
        assertTrue(emptySeries.isEmpty());

        Series seriesWithValues = new Series(Arrays.asList(1.0, 2.0, 3.0));
        assertEquals(3, seriesWithValues.size());
        assertEquals(1.0, seriesWithValues.get(0));
        assertEquals(2.0, seriesWithValues.get(1));
        assertEquals(3.0, seriesWithValues.get(2));

        assertThrows(IllegalArgumentException.class, () -> new Series(null));
    }

    @Test
    void testSample() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));

        Series sampleWithReplacement = series.sample(3, true);
        assertEquals(3, sampleWithReplacement.size());

        Series sampleWithoutReplacement = series.sample(3, false);
        assertEquals(3, sampleWithoutReplacement.size());
        assertTrue(series.containsAll(sampleWithoutReplacement));

        assertThrows(IllegalArgumentException.class, () -> series.sample(-1, false));
    }


    @Test
    void testNormalized() {
        Series series = new Series(Arrays.asList(10.0, 20.0, 30.0, 40.0));

        Series normalized = series.normalized();
        assertEquals(4, normalized.size());
        assertEquals(0.0, normalized.get(0)); // Min should be normalized to 0
        assertEquals(1.0, normalized.get(3)); // Max should be normalized to 1

        Series identicalValues = new Series(Arrays.asList(5.0, 5.0, 5.0));
        Series normalizedIdentical = identicalValues.normalized();
        assertEquals(Arrays.asList(0.0, 0.0, 0.0), normalizedIdentical); // All equal values normalize to 0
    }

    @Test
    void testCumulativeSum() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0, 4.0));
        Series cumsum = series.cumsum();

        assertEquals(4, cumsum.size());
        assertEquals(1.0, cumsum.get(0));
        assertEquals(3.0, cumsum.get(1));
        assertEquals(6.0, cumsum.get(2));
        assertEquals(10.0, cumsum.get(3));

        Series emptySeries = new Series();
        Series emptyCumSum = emptySeries.cumsum();
        assertTrue(emptyCumSum.isEmpty()); // Cumsum of an empty series should be empty
    }

    @Test
    void testZip() {
        Series series1 = new Series(Arrays.asList(1.0, 2.0, 3.0));
        Series series2 = new Series(Arrays.asList(4.0, 5.0, 6.0));

        Series zippedSum = series1.zip(series2, Double::sum);
        assertEquals(Arrays.asList(5.0, 7.0, 9.0), zippedSum);

        Series zippedProd = series1.zip(series2, (a, b) -> a * b);
        assertEquals(Arrays.asList(4.0, 10.0, 18.0), zippedProd);

        Series empty = new Series();
        assertThrows(IllegalArgumentException.class, () -> series1.zip(empty, Double::sum)); // Invalid size
    }

    @Test
    void testSlidingWindow2() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));

        List<Series> windows = series.slidingWindow(1, 3);
        assertEquals(5, windows.size());
        assertEquals(Arrays.asList(1.0, 2.0, 3.0), windows.get(0));
        assertEquals(Arrays.asList(2.0, 3.0, 4.0), windows.get(1));
        assertEquals(Arrays.asList(3.0, 4.0, 5.0), windows.get(2));

        assertThrows(IllegalArgumentException.class, () -> series.slidingWindow(-1, 2)); // Invalid step
    }

    @Test
    void testDotProduct() {
        Series series1 = new Series(Arrays.asList(1.0, 2.0, 3.0));
        Series series2 = new Series(Arrays.asList(4.0, 5.0, 6.0));

        double dotProduct = series1.dot(series2);
        assertEquals(32.0, dotProduct, 1e-8);

        Series empty = new Series();
        assertThrows(IllegalArgumentException.class, () -> series1.dot(empty)); // Invalid size
    }

    @Test
    void testPercentileAndPercentileValue() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));

        assertEquals(0.6, series.percentile(3.0), 1e-8); // 60% of the elements are <= 3.0
        assertEquals(3.0, series.percentileValue(0.6), 1e-8); // 60th percentile value is 3

        assertThrows(IllegalArgumentException.class, () -> series.percentileValue(-0.1)); // Invalid percentile
        assertThrows(IllegalArgumentException.class, () -> series.percentileValue(1.5)); // Invalid percentile
    }

    @Test
    void testStatisticalMethods() {
        Series series = new Series(Arrays.asList(10.0, 20.0, 30.0, 40.0));


        assertEquals(25.0, series.average(), 1e-8); // Mean
        assertEquals(11.180339887498949, series.std(), 1e-8); // Standard Deviation
        assertEquals(125.0, series.var(), 1e-8); // Variance
        assertEquals(10.0, series.min(), 1e-8); // Min Value
        assertEquals(40.0, series.max(), 1e-8); // Max Value
        assertEquals(25.0, series.median(), 1e-8); // Median
    }

    @Test
    void testArgMaxAndArgMin() {
        Series series = new Series(Arrays.asList(1.0, 3.0, 5.0, 2.0, 4.0));

        assertEquals(2, series.argmax()); // Index of maximum value (5.0)
        assertEquals(0, series.argmin()); // Index of minimum value (1.0)

        Series empty = new Series();
        assertEquals(-1, empty.argmax()); // Edge case: empty series
        assertEquals(-1, empty.argmin()); // Edge case: empty series
    }


    @Test
    void testGroup() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));

        Map<Double, Series> grouped = series.group(v -> v % 2 == 0 ? 1.0 : 0.0);
        assertEquals(2, grouped.size());

        assertEquals(Arrays.asList(2.0, 4.0), grouped.get(1.0)); // Even values
        assertEquals(Arrays.asList(1.0, 3.0, 5.0), grouped.get(0.0)); // Odd values

        assertThrows(IllegalArgumentException.class, () -> series.group(null));
    }

    @Test
    void testMap() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0));

        Series mapped = series.map(v -> v * 2);
        assertEquals(Arrays.asList(2.0, 4.0, 6.0), mapped);

        Series indexedMapped = series.map((s, i) -> s.get(i) + i);
        assertEquals(Arrays.asList(1.0, 3.0, 5.0), indexedMapped);

        assertThrows(IllegalArgumentException.class, () -> series.map((ToDoubleFunction<Double>) null));
        assertThrows(IllegalArgumentException.class, () -> series.map((ToDoubleBiFunction<Series, Integer>) null));
    }

    @Test
    void testTestMethod() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0, 4.0));

        Series tested = series.test(v -> v > 2.0);
        assertEquals(Arrays.asList(0.0, 0.0, 1.0, 1.0), tested);

        assertThrows(IllegalArgumentException.class, () -> series.test(null));
    }

    @Test
    void testAllAny() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0));

        assertTrue(series.all(v -> v > 0));
        assertFalse(series.all(v -> v > 2));

        assertTrue(series.any(v -> v > 2));
        assertFalse(series.any(v -> v > 5));

        assertThrows(IllegalArgumentException.class, () -> series.all(null));
        assertThrows(IllegalArgumentException.class, () -> series.any(null));
    }

    @Test
    void testWithoutNan() {
        Series series = new Series(Arrays.asList(1.0, Double.NaN, 3.0));
        Series withoutNan = series.withoutNan();

        assertEquals(2, withoutNan.size());
        assertEquals(Arrays.asList(1.0, 3.0), withoutNan);
    }

    @Test
    void testUnique() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 2.0, 3.0));
        Series unique = series.unique();

        assertEquals(3, unique.size());
        assertEquals(Arrays.asList(1.0, 2.0, 3.0), unique);
    }

    @Test
    void testReplace() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0));
        Series replaced = series.replace(0.0, v -> v > 2.0);

        assertEquals(Arrays.asList(1.0, 2.0, 0.0), replaced);

        assertThrows(IllegalArgumentException.class, () -> series.replace(0.0, null));
    }

    @Test
    void testZScore() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0));
        Series zscore = series.zscore();

        assertEquals(3, zscore.size());
        assertNotNull(zscore); // Each value is normalized
    }

    @Test
    void testStatistics() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0));
        Map<String, Double> stats = series.statistics();

        assertEquals(3, stats.get("size"));
        assertEquals(2.0, stats.get("avg"));
    }

    @Test
    void testSlidingWindow() {
        Series series = new Series(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));

        List<Series> windows = series.slidingWindow(2, 3);
        assertEquals(3, windows.size());
        assertEquals(Arrays.asList(1.0, 2.0, 3.0), windows.get(0));
        assertEquals(Arrays.asList(3.0, 4.0, 5.0), windows.get(1));

        assertThrows(IllegalArgumentException.class, () -> series.slidingWindow(-1, 3));
    }

    @Test
    void testRange() {
        Series range = Series.range(1, 5, 1);
        assertEquals(Arrays.asList(1.0, 2.0, 3.0, 4.0), range);

        assertThrows(IllegalArgumentException.class, () -> Series.range(5, 1, 1)); // From > To
        assertThrows(IllegalArgumentException.class, () -> Series.range(1, 5, 0)); // Step <= 0
    }

    @Test
    void testClone() {
        Series original = new Series(Arrays.asList(1.0, 2.0, 3.0));
        Series cloned = original.clone();

        assertEquals(original, cloned);
        assertNotSame(original, cloned); // Ensure it's a deep copy
    }

    @Test
    void testFill() {
        Series filled = Series.fill(3, 5.0);

        assertEquals(3, filled.size());
        assertEquals(Arrays.asList(5.0, 5.0, 5.0), filled);
    }

        @Test
        void testNormalized2() {
            Series series = new Series(Arrays.asList(10.0, 20.0, 30.0, 40.0));
            Series normalized = series.normalized();

            assertEquals(4, normalized.size());
            assertEquals(0.0, normalized.get(0));
            assertEquals(1.0, normalized.get(normalized.size() - 1)); // Max should normalize to 1
            assertTrue(normalized.stream().allMatch(v -> v >= 0.0 && v <= 1.0)); // All values should be within [0,1]

            Series singleValueSeries = new Series(Collections.singletonList(5.0));
            Series normalizedSingleValue = singleValueSeries.normalized();
            assertEquals(0.0, normalizedSingleValue.get(0)); // Single value doesn't scale
        }

        @Test
        void testPercentileAndPercentileValue2() {
            Series series = new Series(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));

            // Testing percentile
            double percentile50 = series.percentile(3.0);
            assertEquals(0.6, percentile50); // 3.0 should be around the 60th percentile

            double percentileValue50 = series.percentileValue(0.5);
            assertEquals(3.0, percentileValue50); // 50th percentile should be the middle value

            assertThrows(IllegalArgumentException.class, () -> series.percentileValue(-0.1));
            assertThrows(IllegalArgumentException.class, () -> series.percentileValue(1.1));
        }

        @Test
        void testMinMaxArgMethods() {
            Series series = new Series(Arrays.asList(4.0, 1.0, 3.0, 2.0, 6.0));

            assertEquals(1.0, series.min());
            assertEquals(6.0, series.max());

            assertEquals(1, series.argmin()); // Min value at index 1
            assertEquals(4, series.argmax()); // Max value at index 4

            Series emptySeries = new Series();
            assertEquals(Double.NaN, emptySeries.min());
            assertEquals(Double.NaN, emptySeries.max());
            assertEquals(-1, emptySeries.argmin());
            assertEquals(-1, emptySeries.argmax());
        }

        @Test
        void testCumsum() {
            Series series = new Series(Arrays.asList(1.0, 2.0, 3.0));
            Series cumsum = series.cumsum();

            assertEquals(Arrays.asList(1.0, 3.0, 6.0), cumsum); // Running total
        }

        @Test
        void testStatisticsMethod() {
            Series series = new Series(Arrays.asList(10.0, 20.0, 30.0, 40.0, 50.0));
            Map<String, Double> statistics = series.statistics();

            assertEquals(5.0, statistics.get("size"));
            assertEquals(30.0, statistics.get("avg"));
            assertEquals(200, statistics.get("var"));
            assertEquals(14.142135623730951, statistics.get("std")); // Variance is 100; std is sqrt(100) = 10
            assertEquals(10.0, statistics.get("min"));
            assertEquals(50.0, statistics.get("max"));
            assertEquals(30.0, statistics.get("middle"));
            assertEquals(0.0, statistics.get("hasNan"));
        }

        @Test
        void testDot() {
            Series series1 = new Series(Arrays.asList(1.0, 2.0, 3.0));
            Series series2 = new Series(Arrays.asList(4.0, 5.0, 6.0));

            double dotResult = series1.dot(series2);
            assertEquals(32.0, dotResult); // (1*4) + (2*5) + (3*6) = 4 + 10 + 18 = 32

            assertThrows(IllegalArgumentException.class, () -> series1.dot(new Series(Arrays.asList(1.0, 2.0))));
        }

        @Test
        void testSlidingWindow3() {
            Series series = new Series(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));

            List<Series> windows = series.slidingWindow(2, 3);
            assertEquals(3, windows.size());
            assertEquals(Arrays.asList(1.0, 2.0, 3.0), windows.get(0));
            assertEquals(Arrays.asList(3.0, 4.0, 5.0), windows.get(1));

            assertThrows(IllegalArgumentException.class, () -> series.slidingWindow(-1, 3));
            assertThrows(IllegalArgumentException.class, () -> series.slidingWindow(2, 0));
        }

        @Test
        void testBetween() {
            Series series = new Series(Arrays.asList(10.0, 20.0, 30.0, 40.0, 50.0));

            Series subSeries = series.between(1, 4); // Get elements 20.0, 30.0, 40.0
            assertEquals(Arrays.asList(20.0, 30.0, 40.0), subSeries);

            assertThrows(IllegalArgumentException.class, () -> series.between(4, 1)); // invalid range
            assertThrows(IllegalArgumentException.class, () -> series.between(0, 10)); // invalid upper index
        }

        @Test
        void testHasNan() {
            Series seriesWithNan = new Series(Arrays.asList(1.0, 2.0, Double.NaN, 4.0));
            Series seriesWithoutNan = new Series(Arrays.asList(1.0, 2.0, 3.0, 4.0));

            assertTrue(seriesWithNan.hasNan());
            assertFalse(seriesWithoutNan.hasNan());
        }

        @Test
        void testReplace2() {
            Series series = new Series(Arrays.asList(1.0, 2.0, 3.0, 2.0, 5.0));
            Series replaced = series.replace(0.0, v -> v.equals(2.0));

            assertEquals(Arrays.asList(1.0, 0.0, 3.0, 0.0, 5.0), replaced);
        }

        @Test
        void testFrom() {
            Series result = Series.from(1, 2.5, 3, 4.8);

            assertEquals(Arrays.asList(1.0, 2.5, 3.0, 4.8), result);
        }

        @Test
        void testFill2() {
            Series filled = Series.fill(5, 99.0);

            assertEquals(Arrays.asList(99.0, 99.0, 99.0, 99.0, 99.0), filled);
        }

}
