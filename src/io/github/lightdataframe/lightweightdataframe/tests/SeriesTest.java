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
        assertEquals(2.5, series.average());

        assertEquals(Double.NaN, new Series().average());
    }

    @Test
    void var()
    {
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d));
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
        Series series = Series.from(3d, 2d, 1d, 0d,4, 5, 6, 7, 8, 9);
        double percentile = series.percentile(4);
        assertEquals(0.4, percentile);
    }

    @Test
    void slidingWindow()
    {
        Series s = Series.from(1,2,3,4,5,6,7,8,9,10);
        s.slidingWindow(6, 5);
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
    }

    @Test
    public void from()
    {
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