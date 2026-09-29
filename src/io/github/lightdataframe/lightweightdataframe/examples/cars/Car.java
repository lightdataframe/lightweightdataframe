package io.github.lightdataframe.lightweightdataframe.examples.cars;

public class Car
{

    public enum Manufacturer
    {
        toyota,
        fiat,
        ferrari
    }
    public final int id;

    public final Manufacturer manufacturer;
    public final int year;
    public final double price;
    public final boolean cabrio;
    public final int horses;


    public Car(int id, Manufacturer manufacturer, int year, double price, boolean cabrio, int horses)
    {
        this.id = id;
        this.manufacturer = manufacturer;
        this.year = year;
        this.price = price;
        this.cabrio = cabrio;
        this.horses = horses;
    }
}
