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
        Car c1 = new Car(1, Car.Manufacturer.fiat, 2015, 12000, false, 100);
        Car c8 = new Car(2, Car.Manufacturer.fiat, 2017, 15000, false, 110);
        Car c9 = new Car(3, Car.Manufacturer.fiat, 2017, 15000, false, 110);
        Car c10 = new Car(4, Car.Manufacturer.fiat, 2020, 18000, false, 150);
        Car c2 = new Car(5, Car.Manufacturer.ferrari, 2000, 80000, true, 500);
        Car c4 = new Car(6, Car.Manufacturer.ferrari, 1995, 100000, true, 600);
        Car c6 = new Car(7, Car.Manufacturer.ferrari, 2017, 100000, false, 800);
        Car c3 = new Car(8, Car.Manufacturer.toyota, 2013, 20000, false, 200);
        Car c5 = new Car(9, Car.Manufacturer.toyota, 2016, 15000, false, 160);
        Car c11 = new Car(10, Car.Manufacturer.toyota, 2016, 17000, false, 110);
        Car c7 = new Car(11, Car.Manufacturer.toyota, 2018, 20000, false, 150);
        Car c0 = new Car(12, Car.Manufacturer.toyota, 2010, 15000, true, 150);

        List<Car> cars = Arrays.asList(c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11);
        Collections.shuffle(cars);

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

        //df.sort(true, sorter.sortByColumn("year")).print();
        //df.filter(filter.rowTest(row -> row.get("year") >= 2015)).print();
        //df.filter(filter.between(5, 7)).print();
        //df.group(grouper.groupByColumnValues("manufacturer", "price")).values().forEach(d -> d.print());
        //df.group(grouper.split(7)).values().forEach(d -> d.print());
        //df.group(grouper.groupByColumnValues("year")).values().forEach(d -> d.print());
        /*
        df.groupAggregate(
                grouper.groupByColumnValues("year"),
                new Aggregator()
                        .addAggregation("cabrios", df0 -> df0.getColumn("cabrio").sum())
                        .addAggregation("average price", df0 -> df0.getColumn("price").mean())
                        .addAggregation("year", df0 -> df0.getColumn("year").first())
        )
                .sort(true, sorter.sortByColumn("year"))
                .print();

        */
    }
}
