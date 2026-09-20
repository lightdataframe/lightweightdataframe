package io.github.lightdataframe.lightweightdataframe;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.IntStream;


/**
 * Provides basic plotting methods for {@link Dataframe} and {@link Series}.
 */
public class Plotter implements Serializable, Cloneable
{

    /**
     * Plots a chart of the given dataframe using the specified column as the x-axis and one or more columns as the y-axis.
     * By default, if no y-axis columns are specified, all columns except the x-axis column will be plotted.
     *
     * @param df       dataframe containing the data to plot.
     * @param xColumn  The name of the column to use as the x-axis. Must exist in the Dataframe.
     * @param yColumns The names of the columns to use as the y-axis. If no columns are specified, all columns except the x-axis column are used.
     *                 Each column specified must exist in the Dataframe.
     * @throws IllegalArgumentException If the x-axis column does not exist in the Dataframe or if one or more of
     *                                  the specified y-axis columns do not exist in the Dataframe.
     */
    public void line(Dataframe df, String xColumn, String... yColumns)
    {
        if (!df.getColumns().contains(xColumn))
            throw new IllegalArgumentException("Column " + xColumn + " not found");
        if (yColumns.length > 0 && !df.getColumns().containsAll(Arrays.asList(yColumns)))
            throw new IllegalArgumentException("One or more columns not found: [" + Arrays.toString(yColumns) + "] in [" + df.getColumns() + "]");


        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .xAxisTitle(xColumn)
                .build();


        // Customize Chart
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setChartTitleVisible(false);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setMarkerSize(8);

        double[] x = df.getColumn(xColumn).stream().mapToDouble(Double::doubleValue).toArray();

        if (yColumns.length == 0)
            yColumns = df.getColumns().toArray(new String[0]);

        for (String column : yColumns)
        {
            if (column.equals(xColumn))
                continue;
            double[] y = df.getColumn(column).stream().mapToDouble(Double::doubleValue).toArray();
            XYSeries series = chart.addSeries(column, x, y);
            series.setMarker(SeriesMarkers.NONE);
        }

        new SwingWrapper(chart).displayChart();
    }

    /**
     * Creates a line chart from the specified dataframe. Each column in the dataframe is plotted as a separate series
     * with the row indices used as the x-axis values.
     *
     * @param df The dataframe containing the data to plot. Each column is treated as a series, and the index values are
     *           automatically used as the x-axis.
     */
    public void line(Dataframe df)
    {
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .build();

        // Customize Chart
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setChartTitleVisible(false);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setMarkerSize(8);

        double[] x = IntStream.range(0, df.size()).mapToDouble(v -> v).toArray();

        for (String column : df.getColumns())
        {
            double[] y = df.getColumn(column).stream().mapToDouble(Double::doubleValue).toArray();
            XYSeries series = chart.addSeries(column, x, y);
            series.setMarker(SeriesMarkers.NONE);
        }

        new SwingWrapper(chart).displayChart();
    }

    /**
     * Creates a line chart using the provided list of series.
     *
     * @param series A variable-length array of series data to plot. Each series represents y-values.
     *               At least one series must be provided.
     * @throws IllegalArgumentException If no series are provided.
     */
    public void line(Series ... series)
    {
        if(series.length == 0)
            throw new IllegalArgumentException("series must have at least one element");

        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .xAxisTitle("X")
                .yAxisTitle("Y")
                .build();


        // Customize Chart
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setChartTitleVisible(false);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setMarkerSize(8);

        for (int i = 0; i < series.length; i++)
        {
            double[] x = IntStream.range(0, series[i].size()).mapToDouble(v -> v).toArray();
            double[] y = series[i].stream().mapToDouble(v -> v).toArray();
            XYSeries s = chart.addSeries("Series " + i, x, y);
            s.setMarker(SeriesMarkers.NONE);
        }


        new SwingWrapper(chart).displayChart();
    }

    /**
     * Creates a scatter plot chart using the provided series data.
     *
     * @param xySeries A variable-length array of series data, where each pair of series represents
     *                 the x-axis and y-axis data for the scatter plot. Must be provided in pairs.
     *                 All series must have the same size.
     * @throws IllegalArgumentException If no series are provided, if the number of series is not even,
     *                                  or if the sizes of the series are not identical.
     */
    public void scatter(Series ... xySeries)
    {
        if(xySeries.length == 0)
            throw new IllegalArgumentException("xySeries must have at least two elements");
        if(xySeries.length % 2 != 0)
            throw new IllegalArgumentException("xySeries must have an even number of elements");
        if (Arrays.stream(xySeries).map(Series::size).distinct().count() != 1)
            throw new IllegalArgumentException("xySeries must have the same size");



        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(500)
                .xAxisTitle("X")
                .yAxisTitle("Y")
                .build();


        // Customize Chart
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
        chart.getStyler().setChartTitleVisible(false);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setMarkerSize(8);
        chart.getStyler().setLegendVisible(true);

        for(int i = 0; i < xySeries.length; i+=2)
        {
            XYSeries s = chart.addSeries("Series " + i / 2, xySeries[i], xySeries[i + 1]);
            s.setMarker(SeriesMarkers.CIRCLE);
        }

        new SwingWrapper(chart).displayChart();
    }

    /**
     * Returns a new instance of the plotter
     *
     * @return a new instance of the plotter
     */
    @Override
    protected Plotter clone() throws CloneNotSupportedException
    {
        return new Plotter();
    }
}
