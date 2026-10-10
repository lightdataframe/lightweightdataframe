package io.github.lightdataframe.lightweightdataframe.examples.cars;

import io.github.lightdataframe.lightweightdataframe.*;

import java.util.*;


public class CarsExample
{
    static Sorter sorter = new Sorter();
    static Grouper grouper = new Grouper();
    static Filter filter = new Filter();


    public static void main(String[] args)
    {

        List<Car> cars = Arrays.asList(
                new Car(1, Car.Manufacturer.fiat, 2015, 12000, false, 100),
                new Car(2, Car.Manufacturer.fiat, 2017, 15000, false, 110),
                new Car(3, Car.Manufacturer.fiat, 2017, 15000, false, 110),
                new Car(4, Car.Manufacturer.fiat, 2020, 18000, false, 150),
                new Car(5, Car.Manufacturer.ferrari, 2000, 80000, true, 500),
                new Car(6, Car.Manufacturer.ferrari, 1995, 100000, true, 600),
                new Car(7, Car.Manufacturer.ferrari, 2017, 100000, false, 800),
                new Car(8, Car.Manufacturer.toyota, 2013, 20000, false, 200),
                new Car(9, Car.Manufacturer.toyota, 2016, 15000, false, 160),
                new Car(10, Car.Manufacturer.toyota, 2016, 17000, false, 110),
                new Car(11, Car.Manufacturer.toyota, 2018, 20000, false, 150),
                new Car(12, Car.Manufacturer.toyota, 2010, 15000, true, 150)
        );

        Dataframe df = cars.stream()
                .sorted(Comparator.comparing(c -> c.id))
                .collect(Dataframe.<Car>collector()
                        .addColumn("id", car -> car.id)
                        .addColumn("manufacturer", car -> car.manufacturer.ordinal())
                        .addColumn("year", car -> car.year)
                        .addColumn("price", car -> car.price)
                        .addColumn("cabrio", car -> car.cabrio ? 1 : 0)
                        .addColumn("horses", car -> car.horses)
                );

        HashMap<String, Double> row = new HashMap<>();
        row.put("id", 13d);
        row.put("manufacturer", 1d);
        row.put("year", 2015d);
        row.put("price", 12000d);
        row.put("cabrio", 0d);
        row.put("horses", 100d);

        df.append(true, row);
        df.remove(false, 7);
        df.set(true, 7, "price", null);

        df.print();

        Filter filter = new Filter();

        Dataframe df0 = df.filter(filter.between(0, 1));
        df0 = df.filter(filter.head(5));
        df0 = df.filter(filter.tail(5));
        df0 = df.filter(filter.valueTest("price", v -> v > 70000));
        df0 = df.filter(filter.valuesMatch(row));


        Grouper grouper = new Grouper();
        Map<Object, Dataframe> groups = new HashMap<>();
        groups = df.group(grouper.random(4, 3));
        groups = df.group(grouper.split(3));
        groups = df.group(grouper.slidingWindow(2, 3));
        groups = df.group(grouper.groupByColumnValues("manufacturer", "year"));

        Sorter sorter = new Sorter();

        df0 = df.sort(true, sorter.shuffle());
        df0 = df.sort(true, sorter.sortByColumns("manufacturer", "year"));
        df0 = df.sort(true, sorter.sortByColumn("year", v -> Math.abs(v - 2020)));


        Aggregator aggr = new Aggregator()
                .addAggregation("avg. price", d -> d.getColumn("price").average())
                .addAggregation("manufacturer", d -> d.getColumn("manufacturer").first())
                .addAggregation("year", d -> d.getColumn("year").first())
                .addAggregation("count", d -> d.size())
                ;
        Map<String, Double> ar = new HashMap<>();

        ar = df.aggregate(aggr);

        df0 = df.groupAggregate(
                grouper.groupByColumnValues("manufacturer", "year"),
                aggr
        );
        new Plotter().line(df0);

        Serializer serializer = new Serializer();

        String csv = serializer.toCsv(true, df0);

        df0 = serializer.fromCsv(true, csv);

    }
}
