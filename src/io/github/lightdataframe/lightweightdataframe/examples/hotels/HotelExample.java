package io.github.lightdataframe.lightweightdataframe.examples.hotels;

import io.github.lightdataframe.lightweightdataframe.*;


import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class HotelExample
{
    public static final String HOTEL = "hotel";
    public static final String CAPACITY = "capacity";
    public static final String PRICE_PER_NIGHT = "price_per_night";
    public static final String ROOM_TYPE = "room_type";
    public static final String AVERAGE_PRICE = "average_price";
    public static final String AVERAGE_CAPACITY = "average_capacity";
    private static final String AVERAGE_PRICE_PER_NIGHT = "average_price_per_night";

    public static void main(String[] args)
    {
        Hotel hotel1 = new Hotel();
        IntStream.range(0, 50)
                .boxed()
                .map(i -> Room.randomRoom(1, 1000))
                .forEach(hotel1.rooms::add);
        Hotel hotel2 = new Hotel();
        IntStream.range(0, 50)
                .boxed()
                .map(i -> Room.randomRoom(2, 500))
                .forEach(hotel2.rooms::add);
        Hotel hotel3 = new Hotel();
        IntStream.range(0, 50)
                .boxed()
                .map(i -> Room.randomRoom(5, 100))
                .forEach(hotel3.rooms::add);


        List<Hotel> hotels = Arrays.asList(hotel1, hotel2, hotel3);

        Dataframe df = Stream.of(hotel1, hotel2, hotel3)
                .flatMap(hotel -> hotel.rooms.stream())
                .collect(Dataframe.<Room>collector()
                        .addColumn(HOTEL,
                                room -> hotels.stream()
                                        .filter(h -> h.rooms.contains(room))
                                        .findFirst()
                                        .map(hotels::indexOf)
                                        .orElse(-1)
                        )
                        .addColumn(CAPACITY, room -> room.capacity)
                        .addColumn(PRICE_PER_NIGHT, room -> room.pricePerNight)
                        .addColumn(ROOM_TYPE, room -> room.type)
                );

        df.print();

        df = df.groupAggregate(
                new Grouper().groupByColumnValues(ROOM_TYPE),
                new Aggregator()
                .addAggregation(PRICE_PER_NIGHT, AVERAGE_PRICE, Series::mean)
                .addAggregation(CAPACITY, AVERAGE_CAPACITY, Series::mean)
                .addAggregation(PRICE_PER_NIGHT, AVERAGE_PRICE_PER_NIGHT, Series::mean))
                .replaceColumn(AVERAGE_PRICE, Series::normalized)
                .replaceColumn(AVERAGE_CAPACITY, Series::normalized)
                .sort(new Sorter().sortByColumn(ROOM_TYPE, true));

        Plotter plotter = new Plotter();

        plotter.line(
                df.getColumn(AVERAGE_PRICE),
                df.getColumn(AVERAGE_CAPACITY)
        );

        plotter.scatter(
                df.getColumn(AVERAGE_PRICE), df.getColumn(AVERAGE_CAPACITY),
                df.getColumn(AVERAGE_PRICE), df.getColumn(AVERAGE_PRICE_PER_NIGHT)
        );

    }
}
