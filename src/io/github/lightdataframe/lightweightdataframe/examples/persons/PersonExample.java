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

        double averageAge = peopleAge.mean();
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

        Dataframe sortedDataframe = sorter.sortByColumn(AGE, true).sort(df);

        Dataframe shuffledDataframe = sorter.shuffle().sort(df);


        // ---- Grouping ----

        Grouper grouper = new Grouper();

        Map<Object, Dataframe> groupedByAge = grouper.groupByColumnValues(AGE).group(df);

        Map<Object, Dataframe> splits = grouper.split(3).group(df);


        // ---- Aggregation ----

        // Aggregates columns "weight", "height" and "age" into their min and max values
        Aggregator aggregator = new Aggregator()
                .addAggregation(WEIGHT, "max weight", Series::max)
                .addAggregation(WEIGHT, "min weight", Series::min)
                .addAggregation(HEIGHT, "max height", Series::max)
                .addAggregation(HEIGHT, "min height", Series::min)
                .addAggregation(AGE, "min age", Series::min)
                .addAggregation(AGE, "max age", Series::max);

        Map<String, Double> aggregation = aggregator.aggregate(df);


        Dataframe groupByAgeAggregated = aggregator.groupAggregate(
                df,
                grouper.groupByColumnValues(AGE)
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