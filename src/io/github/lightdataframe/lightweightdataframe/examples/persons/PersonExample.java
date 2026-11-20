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
                                .addColumn(AGE, Person::getAge)
                                .addColumn(HEIGHT, Person::getHeight)
                                .addColumn(WEIGHT, Person::getWeight)
                );

        df.print();
        df.printStatistics();


        // ---- Sorting ----


        Dataframe sorted = df.sort(true, Sorter.sortByRow(age -> age.get(AGE)));

        Dataframe shuffled = sorted.sort(true, Sorter.shuffle());


        // ---- Grouping ----

        Map<Object, Dataframe> groupedByAge = df.group(Grouper.groupByColumnValues(AGE));

        Map<Object, Dataframe> splits = df.group(Grouper.split(3));


        // ---- Filtering ----

        Dataframe between = df.filter(Filter.between(3, 6));
        Dataframe head = df.filter(Filter.head(2));
        Dataframe tail = df.filter(Filter.tail(2));

        Dataframe filteredByAge = df.filter(Filter.rowTest(row -> row.get(AGE) >= 30));


        // ---- Aggregation ----

        // Aggregates columns "weight", "height" and "age" into their min and max values
        Aggregator aggregator = new Aggregator()
                .addAggregation("max weight", df0 -> df0.getColumn(WEIGHT).max())
                .addAggregation("min weight", df0 -> df0.getColumn(WEIGHT).min())
                .addAggregation("max height", df0 -> df0.getColumn(HEIGHT).max())
                .addAggregation("min height", df0 -> df0.getColumn(HEIGHT).min())
                .addAggregation("min age", df0 -> df0.getColumn(AGE).min())
                .addAggregation("max age", df0 -> df0.getColumn(AGE).max());

        // Groups by age and applies the aggregator to each group
        Dataframe groupByAgeAggregated = df.groupAggregate(
                Grouper.groupByColumnValues(AGE),
                aggregator
        );


        // ---- Plotting ----

        Plotter.line(groupByAgeAggregated);


        // ---- CSV Serialization ----

        String csv = Serializer.toCsv(true, df);

        Dataframe fromCsv = Serializer.fromCsv(true, csv);

        // ---- JSON Serialization ----

        String json = Serializer.toJson(JsonFormat.COLUMNS_ROWS, df);

        Dataframe reconstructed = Serializer.fromJson(JsonFormat.COLUMNS_ROWS, json);

    }
}