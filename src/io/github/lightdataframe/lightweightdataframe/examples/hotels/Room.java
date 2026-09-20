package io.github.lightdataframe.lightweightdataframe.examples.hotels;

public class Room
{
    public final int capacity;
    public final double pricePerNight;
    public final int type;

    public Room(int capacity, int type, double pricePerNight)
    {
        this.capacity = capacity;
        this.type = type;
        this.pricePerNight = pricePerNight;
    }

    public static Room randomRoom(int baseCapacity, double basePricePerNight)
    {
        return new Room(
                (int) (Math.random() * 3 + baseCapacity),
                (int) Math.round(Math.random() * 5),
                (Math.random() * 1000 + basePricePerNight)
        );
    }
}
