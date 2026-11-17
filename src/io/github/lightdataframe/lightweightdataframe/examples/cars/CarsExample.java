package io.github.lightdataframe.lightweightdataframe.examples.cars;

import io.github.lightdataframe.lightweightdataframe.*;

import java.util.*;


public class CarsExample
{
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

        df.append(row);
        df.remove(7);
        df.set("price", 7, Double.NaN);

        df.print();


        //Dataframe df0 = df.filter(Filter.between(0, 1));
        //df0 = df.filter(Filter.head(5));
        //df0 = df.filter(Filter.tail(5));
        //df0 = df.filter(Filter.filterByRow(r -> r.get("price") > 70000));
        //df0 = df.filter(Filter.valuesMatch(row));


        Map<Object, Dataframe> groups = new HashMap<>();
        groups = df.group(Grouper.random(4, 3));
        groups = df.group(Grouper.split(3));
        groups = df.group(Grouper.slidingWindow(2, 3));
        groups = df.group(Grouper.groupByColumnValues("manufacturer", "year"));

        Dataframe df0;
        df0 = df.sort(true, Sorter.shuffle());
        df0 = df.sort(true, Sorter.sortByColumns(true, "manufacturer", "year"));
        df0 = df.sort(true, Sorter.sortByRow(v -> Math.abs(v.get("year") - 2020)));

        Aggregator aggr = new Aggregator()
                .addAggregation("avg. price", d -> d.getColumn("price").average())
                .addAggregation("manufacturer", d -> d.getColumn("manufacturer").first())
                .addAggregation("year", d -> d.getColumn("year").first())
                .addAggregation("count", d -> d.size());

        Map<String, Double> ar = new HashMap<>();

        ar = df.aggregate(aggr);

        df0 = df.groupAggregate(
                Grouper.groupByColumnValues("manufacturer", "year"),
                aggr
        );

        Plotter.line(df0);


        String csv = Serializer.toCsv(true, df0);

        df0 = Serializer.fromCsv(true, csv);

    }
}
