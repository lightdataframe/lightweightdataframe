package io.github.lightdataframe.lightweightdataframe.examples.persons;

import io.github.lightdataframe.lightweightdataframe.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class PersonExample
{
    public static final String HEIGHT = "height";
    public static final String WEIGHT = "weight";
    public static final String AGE = "age";


    public static void main(String[] args)
    {
        List<Person> people = Arrays.asList(
                new Person(20, 180, 60),
                new Person(60, 156, 76),
                new Person(30, 160, 60),
                new Person(40, 155, 75),
                new Person(50, 153, 86),
                new Person(20, 180, 80),
                new Person(40, 165, 65),
                new Person(50, 160, 45),
                new Person(30, 170, 70),
                new Person(60, 176, 83)
        );


        // ---- Series ----

        Series peopleAge = people.stream().collect(Series.collector(Person::getAge));

        // ---- Statistics ----

        double averageAge = peopleAge.average();
        double minAge = peopleAge.min();
        double maxAge = peopleAge.max();


        // ---- Dataframe ----

        Dataframe df = people.stream()
                .collect(
                        Dataframe.<Person>collector()
                                .addColumn(HEIGHT, Person::getHeight)
                                .addColumn(WEIGHT, Person::getWeight)
                                .addColumn(AGE, Person::getAge)
                );

        df.printStatistics();


        // ---- Sorting ----

        Sorter sorter = new Sorter();

        Dataframe sortedDataframe = df.sort( true, sorter.sortByColumn(AGE, age -> age));

        Dataframe shuffledDataframe = df.sort( true, sorter.shuffle());


        // ---- Grouping ----

        Grouper grouper = new Grouper();

        Map<Object, Dataframe> groupedByAge = df.group(grouper.groupByColumnValues(AGE));

        Map<Object, Dataframe> splits = df.group(grouper.split(3));


        // ---- Filtering ----

        Filter filter = new Filter();

        Dataframe head = df.filter(filter.head(2));
        Dataframe between = df.filter(filter.between(3, 4));
        Dataframe tail = df.filter(filter.tail(2));

        Dataframe filteredByAge = df.filter(filter.rowTest(row -> row.get(AGE) >= 30));


        // ---- Aggregation ----

        // Aggregates columns "weight", "height" and "age" into their min and max values
        Aggregator aggregator = new Aggregator()
                .addAggregation("max weight", df0 -> df0.getColumn(WEIGHT).max())
                .addAggregation("min weight", df0 -> df0.getColumn(WEIGHT).min())
                .addAggregation("max height", df0 -> df0.getColumn(HEIGHT).max())
                .addAggregation("min height", df0 -> df0.getColumn(HEIGHT).min())
                .addAggregation("min age", df0 -> df0.getColumn(AGE).min())
                .addAggregation("max age", df0 -> df0.getColumn(AGE).max());


        Dataframe groupByAgeAggregated = df.groupAggregate(
                grouper.groupByColumnValues(AGE),
                aggregator
        );


        // ---- Plotting ----

        Plotter plotter = new Plotter();

        plotter.line(groupByAgeAggregated);



        // ---- JSON Serialization ----

        Serializer serializer = new Serializer();

        String json = serializer.toJson(JsonFormat.COLUMNS_ROWS, df);

        Dataframe reconstructed = serializer.fromJson(JsonFormat.COLUMNS_ROWS, json);

    }
}