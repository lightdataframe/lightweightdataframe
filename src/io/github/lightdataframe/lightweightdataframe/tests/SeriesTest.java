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
        Map<Double, Series> groups = series.groupBy(v -> v % 2 == 0 ? 1d : 0d);
        assertEquals(2, groups.size());
        assertEquals(5, groups.get(1d).size());
        assertEquals(5, groups.get(0d).size());


        groups = series.groupBy(v -> v > 0 ? 1d : 0d);
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

        zscore = new Series().cumsum();
        assertEquals(0, zscore.size());
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
        Series series = new Series(Arrays.asList(1d, 2d, 3d, 4d));
        Series cumsum = series.cumsum();
        assertEquals(1, cumsum.get(0));
        assertEquals(3, cumsum.get(1));
        assertEquals(6, cumsum.get(2));
        assertEquals(10, cumsum.get(3));

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
        assertEquals(Double.NaN, sum);
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
        assertEquals(new BigDecimal(series.var()).setScale(8, RoundingMode.DOWN), new BigDecimal(Math.pow(series.std(), 2)).setScale(8, RoundingMode.DOWN));

        assertEquals(Double.NaN, new Series().var());
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
        series = new Series(Arrays.asList(1d, 2d, 3d, 4d, 5d, 6d, 7d));
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
        Series series = new Series(Arrays.asList(3d, 2d, 1d, 4d));
    }

    @Test
    void plot()
    {
    }

    @Test
    void slidingWindow()
    {
        Series s = Series.from();

        double median = s.median();

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
        assertEquals(5, Series.range(0, 5, 1).size());
    }

    @Test
    public void from()
    {

        assertEquals(5, Series.from(0, 1, 2, 3, 4).size());
    }
}