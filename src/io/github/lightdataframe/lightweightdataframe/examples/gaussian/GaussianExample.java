package io.github.lightdataframe.lightweightdataframe.examples.gaussian;

import io.github.lightdataframe.lightweightdataframe.Plotter;
import io.github.lightdataframe.lightweightdataframe.Series;




public class GaussianExample
{
    public static void main(String[] args)
    {
        double mean = 0.5;
        double std = 0.05;

        Series x = Series.range(0, 1, 0.01);

        Series gaussian = x.map(v -> 1 / (std * Math.sqrt(2 * Math.PI)) * Math.exp(-Math.pow(v - mean, 2) / (2 * Math.pow(std, 2))));
        double area = gaussian.sum();
        gaussian = gaussian.map(v -> v / area);

        Series cumfun = gaussian.cumsum();

        Plotter plotter = new Plotter();
        plotter.line(x.normalized(), gaussian.normalized(), cumfun);

    }
}
