package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Series;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SeriesTest
{


    @Test
    void sample()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d));

        Series sample = series.sample(5, false);
        assertEquals(5, sample.size());

        sample = series.sample(20, true);
        assertEquals(20, sample.size());

        sample = series.sample(20, false);
        assertEquals(10, sample.size());
    }
    @Test
    void unique()
    {
        Series series = new Series(Arrays.asList(1d, Double.NaN, 3d, 4d, 4d, 7d, 7d, 8d, Double.NaN, 10d));
        series.unique().print();
    }

    @Test
    void argmax()
    {
        int amin = new Series().argmax();
        assertEquals(-1, amin);

        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d, 5d, 0d, 7d, 8d, Double.NaN, Double.NaN));
        assertEquals(7, series.argmax());

        // All nan values
        series = Series.fill(10, Double.NaN);
        assertEquals(-1, series.argmax());
        for(int i = 0; i < 20; i++)
        {
            Series s = TestUtil.randomSeries(10000);
            assertEquals(s.withoutNan().max(), s.get(s.argmax()));
        }

        series = Series.range(0, 1, 0.05);
        assertEquals(19, series.argmax());
    }

    @Test
    void argmin()
    {
        int amin = new Series().argmin();
        assertEquals(-1, amin);


        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d, 5d, 0d, 7d, 8d, Double.NaN, Double.NaN));
        assertEquals(5, series.argmin());

        // All nan values
        series = Series.fill(10, Double.NaN);
        assertEquals(-1, series.argmin());

        for(int i = 0; i < 20; i++)
        {
            Series s = TestUtil.randomSeries(10000);
            assertEquals(s.withoutNan().min(), s.get(s.argmin()));
        }

        series = Series.range(0, 1, 0.05);
        assertEquals(0, series.argmin());
    }

    @Test
    void groupBy()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d));
        Map<Double, Series> groups = series.group(v -> v % 2 == 0 ? 1d : 0d);
        assertEquals(2, groups.size());
        assertEquals(5, groups.get(1d).size());
        assertEquals(5, groups.get(0d).size());


        groups = series.group(v -> v > 0 ? 1d : 0d);
        assertEquals(1, groups.size());
        assertEquals(10, groups.get(1d).size());

    }

    @Test
    void map()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d));
        Series mapped = series.map(v -> v * 2);
        assertEquals(series.size(), mapped.size());
        assertEquals(2, mapped.min());
        assertEquals(20, mapped.max());
    }

    @Test
    void map2()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d));
        Series mapped = series.map((s, i) -> Math.pow(s.get(i), 2));
        mapped.print();
    }

    @Test
    void count()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, null, 5d, 6d, 7d, 8d, null, 10d));

        assertEquals(1, series.count(1d));
        assertEquals(1, series.count(8d));
        assertEquals(2, series.count(Double.NaN));
    }


    @Test
    void print()
    {
        Series series = Series.range(0, 10, 0.1);
        series.print();

        Series.setMaxPrintedValues(0);
        series.print();
        Series.setPrintedValuesLength(7);
        Series.setMaxPrintedValues(10);
        Series.setPrintedValuesPrecision(0);
        series.print();
        Series.setMaxPrintedValues(10);
        Series.setPrintedValuesPrecision(5);
        series.print();
        assertThrows(IllegalArgumentException.class, () -> Series.setMaxPrintedValues(-1));
        assertThrows(IllegalArgumentException.class, () -> Series.setPrintedValuesPrecision(-1));
    }

    @Test
    void zscore()
    {

        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d));

        double mean = series.average();
        double std = series.std();
        Series zscore = series.zscore();
        assertEquals(series, zscore.map(v -> new BigDecimal(v * std + mean).setScale(8, RoundingMode.DOWN).doubleValue()));

    }

    @Test
    void normalized()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d));

        Series normalized = series.normalized();
        assertEquals(0, normalized.min());
        assertEquals(1, normalized.max());

        normalized = new Series().cumsum();
        assertEquals(0, normalized.size());
    }

    @Test
    void cumsum()
    {
        Series series = new Series(Arrays.asList(1d, 2d, Double.NaN, 4d));
        Series cumsum = series.cumsum();
        assertEquals(1, cumsum.get(0));
        assertEquals(3, cumsum.get(1));
        assertEquals(Double.NaN, cumsum.get(2));
        assertEquals(Double.NaN, cumsum.get(3));

        cumsum = new Series().cumsum();
        assertEquals(0, cumsum.size());

        series = Series.from(0, 1, 2, 4);
        assertTrue(Arrays.asList(0d, 1d, 3d, 7d).equals(series.cumsum()));
    }

    @Test
    void sum()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d));
        double sum = series.sum();
        assertEquals(10, sum);
        sum = new Series().sum();
        assertEquals(0, sum);

        Series series0 = new Series(Arrays.asList(1d, 2d, 3d, Double.NaN));
        assertEquals(Double.NaN, series0.sum());
        assertEquals(Double.NaN, series0.var());
        assertEquals(Double.NaN, series0.std());
    }

    @Test
    void min()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d));
        assertEquals(1, series.min());

        assertEquals(Double.NaN, new Series().min());
    }

    @Test
    void max()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d));
        assertEquals(4, series.max());

        assertEquals(Double.NaN, new Series().max());
    }

    @Test
    void mean()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d));
        double mean = (1 + 2 + 3 + 4) / 4d;
        assertEquals(mean, series.average());

        assertEquals(Double.NaN, new Series().average());
    }

    @Test
    void var()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d));
        double mean = (1 + 2 + 3 + 4) / 4d;
        double var = ((4 - mean) * (4 - mean) + (3 - mean) * (3 - mean) + (2 - mean) * (2 - mean) + (1 - mean) * (1 - mean)) / 4;
        double std = Math.sqrt(var);
        assertEquals(var, series.var());
        assertEquals(std, series.std());


        assertTrue(series.var() > 0);
        assertEquals(BigDecimal.valueOf(series.var()).setScale(8, RoundingMode.DOWN), BigDecimal.valueOf(Math.pow(series.std(), 2)).setScale(8, RoundingMode.DOWN));

        assertEquals(Double.NaN, new Series().var());
    }

    @Test
    void headAndTail()
    {
        Series series = Series.from(0, 1, 2, 4);
        Series head = series.head(2);
        assertEquals(2, head.size());
        assertEquals(0, head.get(0));
        assertEquals(1, head.get(1));

        head = series.head(5);
        assertEquals(4, head.size());

        head = series.head(0);
        assertEquals(0, head.size());


        Series tail = series.tail(2);
        assertEquals(2, tail.size());
        assertEquals(2, tail.get(0));
        assertEquals(4, tail.get(1));

        tail = series.tail(5);
        assertEquals(4, tail.size());
        tail = series.tail(0);
        assertEquals(0, tail.size());
    }

    @Test
    void headAndTail2()
    {
        Series series = Series.from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        assertEquals(Arrays.asList(0d, 1d), series.head(2));
        assertEquals(Arrays.asList(8d, 9d, 10d), series.tail(3));

        assertEquals(Arrays.asList(0d, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d), series.head(20));
        assertEquals(Arrays.asList(0d, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d), series.tail(20));
    }

    @Test
    void std()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d));
        assertTrue(series.std() > 0);
        assertEquals(Double.NaN, new Series().std());
    }

    @Test
    void middle()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d));
        assertEquals(3, series.middle());
        series = new Series(Arrays.asList(1d, 2d, 3d, 4d, 5d, 6d, 7d));
        assertEquals(4, series.middle());

        assertEquals(Double.NaN, new Series().middle());
    }

    @Test
    void first()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d));
        assertEquals(1, series.first());
    }

    @Test
    void last()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d));
        assertEquals(4, series.last());

        assertEquals(Double.NaN, new Series().last());
    }

    @Test
    void between()
    {
        Series series = new Series(Arrays.asList(0d, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d));
        assertEquals(Arrays.asList(1d, 2d), series.between(1, 3));
        assertEquals(Arrays.asList(0d, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d), series.between(0, 10));
    }

    @Test
    void median()
    {
        Series series = new Series(Arrays.asList(3d, 2d, 1d, 4d));
        assertEquals(2.5, series.median());
        series = new Series(Arrays.asList(1d, 7d, 3d, 4d, 5d, 6d, 2d));
        assertEquals(4, series.median());
    }

    @Test
    void any()
    {
        Series series = new Series(Arrays.asList(3d, 2d, 1d, 4d));
        assertTrue(series.any(v -> v > 3));
        assertFalse(series.any(v -> v < 0));
    }

    @Test
    void all()
    {
        Series series = new Series(Arrays.asList(3d, 2d, 1d, 4d));
        assertTrue(series.all(v -> v > 0));
        assertFalse(series.all(v -> v < 0));
    }

    @Test
    void sorted()
    {
        Series series0 = new Series(Arrays.asList(3d, 2d, 1d, 4d));
        Series series1 = new Series(Arrays.asList(1d, 2d, 3d, 4d));
        assertEquals(series1, series0.sort(v -> v));
        assertEquals(series1, series0.sort(v -> v * 2));
    }

    @Test
    void test()
    {

        Series series = new Series(Arrays.asList(3d, 2d, 1d, 4d));
        Series test = series.test(v -> v > 2);
        assertEquals(1, test.get(0));
        assertEquals(0, test.get(1));
        assertEquals(0, test.get(2));
        assertEquals(1, test.get(3));
    }

    @Test
    void hasNan()
    {
        Series series = new Series(Arrays.asList(3d, 2d, 1d, 4d));
        assertFalse(series.hasNan());
        series = new Series(Arrays.asList(3d, 2d, 1d, 4d, Double.NaN));
        assertTrue(series.hasNan());
    }

    @Test
    void statistics()
    {
        Series series = new Series(Arrays.asList(3d, 2d, 1d, Double.NaN));

        series.statistics();

        Series series2 = new Series();
        series2.statistics();
    }

    @Test
    void percentile()
    {
        Series series = Series.fill(1303, 1).map(v -> Math.random() * 100);

        assertEquals(series.max(), series.percentileValue(1));
        assertEquals(series.min(), series.percentileValue(0));
        assertEquals(1d / series.size(), series.percentile(series.min()));
        assertEquals(1, series.percentile(series.max()));

        for(double v : series)
        {
            double percentile = series.percentile(v);
            double valueBack = series.percentileValue(percentile);
            assertEquals(v, valueBack);
        }
    }


    @Test
    void percentile2()
    {
        Series series = Series.range(-10, 10, 1);
        double percentile = series.percentile(2);
        double value = series.percentileValue(percentile);
        assertEquals(percentile, series.percentile(value));

        percentile = series.percentile(-10);
        value = series.percentileValue(percentile);
        assertEquals(percentile, series.percentile(value));
        assertThrows(IllegalArgumentException.class, () -> series.percentileValue(-1));
        assertThrows(IllegalArgumentException.class, () -> series.percentileValue(2));

    }

    @Test
    void slidingWindow()
    {
        Series s = Series.from(1,2,3,4,5,6,7,8,9,10);
        List<Series> windows = s.slidingWindow(6, 5);

        assertEquals(2, windows.size());
        assertEquals(Arrays.asList(1d,2d,3d,4d,5d), windows.get(0));
        assertEquals(Arrays.asList(7d,8d,9d,10d), windows.get(1));

    }

    @Test
    void testEquals()
    {
        Series series0 = new Series(Arrays.asList(3d, 2d, 1d, 4d));
        Series series1 = new Series(Arrays.asList(3d, 2d, 1d, 4d));
        assertEquals(series0, series1);
        assertNotSame(series0, series1.sort(v -> v));
    }

    @Test
    void collector()
    {
        List<Object> objs = Arrays.asList(1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d);

        Series series = objs.stream().collect(Series.collector(o -> 1d));
        assertEquals(objs.size(), series.size());
    }

    @Test
    public void range()
    {
        Series range = Series.range(0, 0.1, 0.001);
        assertEquals(100, range.size());
        assertEquals(5, Series.range(0, 5, 1).size());

        range = Series.range(0, 100, 1);
        assertEquals(100, range.size());
    }

    @Test
    public void from()
    {
        Series s = Series.range(0, 1, 0.1);
        s = Series.range(0, 1, 0.033333333333333333333333);
        s.print();
        assertEquals(5, Series.from(0, 1, 2, 3, 4).size());
    }

    @Test
    public void replace()
    {
        Series series = new Series(Arrays.asList(3d, 2d, 1d, Double.NaN, Double.NaN));
        series = series.replace(0.0, v -> Double.isNaN(v));
        assertFalse(series.hasNan());
        series = series.replace(5d, v -> v == 2d);
        assertEquals(5, series.get(1));
    }
}