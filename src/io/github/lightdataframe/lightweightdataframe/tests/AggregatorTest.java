package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.Aggregator;
import io.github.lightdataframe.lightweightdataframe.Dataframe;
import io.github.lightdataframe.lightweightdataframe.Grouper;
import io.github.lightdataframe.lightweightdataframe.RowGrouper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AggregatorTest
{
    Dataframe df;

    @BeforeEach
    void initialize()
    {
        df = TestUtil.randomDataframe(200);
    }

    @Test
    void testAggregate()
    {
        Aggregator aggregator = new Aggregator()
                .addAggregation("sum0", df -> df.getColumn("col0").withoutNan().sum())
                .addAggregation("sum1", df -> df.getColumn("col1").withoutNan().sum())
                .addAggregation("sum3", df -> df.getColumn("col3").withoutNan().sum());
        Map<String, Double> aggr = aggregator.aggregate(df);

        Dataframe df0 = df.aggregate(aggregator);

        assertEquals(3, aggr.size());
        assertEquals(1, df0.size());
        assertEquals(Arrays.asList("sum0", "sum1", "sum3"), df0.getColumns());
        df0.print();
    }

    @Test
    void testGroupAggregate()
    {
        RowGrouper rowGrouper = Grouper.slidingWindow(1, 3);
        df.set(1, Double.NaN);
        Map<Object, Dataframe> groups = df.group(rowGrouper);

        Dataframe df0 = df.groupAggregate(rowGrouper,
                new Aggregator()
                        .addAggregation("sum0", df -> df.getColumn("col0").sum())
                        .addAggregation("sum3", df -> df.getColumn("col3").sum())
        );
        //Dataframe.setMaxPrintedRows(df0.size());
        df0.print();
        assertEquals(groups.size(), df0.size());
    }

    @Test
    void testInvalid()
    {
        assertThrows(IllegalArgumentException.class, () -> new Aggregator().addAggregation("sum0", null));
        assertThrows(IllegalArgumentException.class, () -> new Aggregator()
                .addAggregation("sum0", df -> df.getColumn("invalid_col").sum())
                .aggregate(df));
    }
}
