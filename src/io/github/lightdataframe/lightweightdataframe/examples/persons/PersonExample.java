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
                new Person(20, 180, 80),
                new Person(30, 160, 60),
                new Person(30, 170, 70),
                new Person(40, 155, 75),
                new Person(40, 165, 65),
                new Person(50, 153, 86),
                new Person(50, 160, 45),
                new Person(60, 156, 76),
                new Person(60, 176, 83)
        );


        Dataframe df = people.stream()
                .collect(
                        Dataframe.<Person>collector()
                                .addColumn(HEIGHT, Person::getHeight)
                                .addColumn(WEIGHT, Person::getWeight)
                                .addColumn(AGE, Person::getAge)
                );

        df.printStatistics();
        Map<Object, Dataframe> sw = df.group(new Grouper().slidingWindow(1, 30));

        df = df.groupAggregate(
                new Grouper().groupByColumnValues(AGE),
                new Aggregator()
                        .addAggregation(WEIGHT, "max weight", Series::max)
                        .addAggregation(WEIGHT, "min weight", Series::min)
                        .addAggregation(HEIGHT, "max height", Series::max)
                        .addAggregation(HEIGHT, "min height", Series::min)
                        .addAggregation(AGE, "age", Series::min)
        )
                .sort(new Sorter().sortByColumn(AGE, true));



    }
}