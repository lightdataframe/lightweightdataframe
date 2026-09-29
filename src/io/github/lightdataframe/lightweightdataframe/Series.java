package io.github.lightdataframe.lightweightdataframe;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a specialized list of numerical values designed
 * to facilitate mathematical and statistical operations. It extends the
 * {@link  java.util.ArrayList} class, adding methods for data manipulation, analysis,
 * and visualization.
 * This class provides functionality for common statistical calculations, data
 * transformations, grouping, and plotting.
 */
public class Series extends ArrayList<Double> implements Serializable, Cloneable
{
    /**
     * Constructs a new, empty Series.
     * This constructor initializes an instance of the Series class, which is a specialized list of numerical values
     * designed to support advanced mathematical and statistical operations.
     */
    public Series() {}

    /**
     * Constructs a new Series instance with the specified list of numerical values.
     * This constructor initializes the Series with an existing list, allowing for further
     * mathematical and statistical operations on the provided data.
     *
     * @param list A list of Double values to initialize the Series. Cannot be null.
     */
    public Series(List<Double> list){super(list);}

    /**
     * Generates a sample Series by randomly selecting elements from the current Series.
     * The sampling can be done with or without replacement based on the given parameter.
     *
     * @param n The number of elements to sample from the Series. Must be a non-negative integer.
     * @param replacement A boolean indicating whether sampling should be with replacement.
     *                    If true, elements can be selected multiple times; otherwise,
     *                    selected elements are removed from the source before the next draw.
     *                    If false and n > size() then the sample will contain size() elements.
     * @return A new Series containing the sampled elements.
     */
    public Series sample(int n, boolean replacement)
    {
        Series from = new Series(this);
        Series sample = new Series();

        while(n > 0 && from.size() > 0)
        {
            int idx = (int)Math.floor(Math.random() * from.size());
            sample.add(from.get(idx));
            if(!replacement)
                from.remove(idx);
            n--;
        }
        return sample;
    }

    /**
     * Groups the elements of the Series based on the result of applying the specified grouping function.
     * The grouping operation partitions the Series into subsets where all elements within a subset
     * produce the same result when the grouping function is applied.
     *
     * For example, for grouping negative and positive values:
     * {@code series.groupBy(v -> v > 0 ? 1d : 0d);}
     *
     * @param groupFunction a stateless function that computes a grouping key (double value) for each element in the Series
     * @return a map where the keys are the unique grouping values produced by the {@code groupFunction},
     * and the values are Series containing the elements that correspond to each group
     */
    public Map<Double, Series> groupBy(ToDoubleFunction<Double> groupFunction)
    {
        List<Double> groups = stream().mapToDouble(groupFunction).boxed().distinct().collect(Collectors.toList());

        Map<Double, Series> result = new HashMap<>();
        for (Double group : groups)
        {
            Series series = stream().filter(v -> groupFunction.applyAsDouble(v) == group).collect(collector(v -> v));
            result.put(group, series);
        }

        return result;
    }

    /**
     * Transforms the elements of the current Series by applying a specified mapping function
     * to each element and returns a new Series containing the mapped values.
     *
     * @param valueMap A function that takes the series and index as input and returns the double to put at the index.
     *                 This function is applied to each element of the series.
     * @return A new Series containing the values resulting from applying the mapping function to each element.
     */
    public Series map(ToDoubleBiFunction<Series, Integer> valueMap)
    {
        Series series = new Series();
        IntStream.range(0, series.size())
                .mapToDouble(idx -> valueMap.applyAsDouble(series, idx))
                .forEach(series::add);
        return series;
    }

    /**
     * Transforms the elements of the current Series by applying a specified mapping function
     * to each element and returns a new Series containing the mapped values.
     *
     * @param valueMap A function that takes a Double as input and returns a double.
     *                 This function is applied to each element of the current Series.
     * @return A new Series containing the values resulting from applying the mapping function to each element.
     */
    public Series map(ToDoubleFunction<Double> valueMap)
    {
        Series series = new Series();
        stream()
                .map(valueMap::applyAsDouble)
                .forEach(series::add);
        return series;
    }

    /**
     * Tests each element in the series against the provided predicate and maps the results.
     * Converts elements that satisfy the predicate to 1.0 and those that do not to 0.0.
     *
     * @param test the predicate to evaluate each element in the series
     * @return a new series where each value is the result of applying the predicate to the corresponding element
     */
    public Series test(Predicate<Double> test)
    {
        return map(v -> test.test(v) ? 1d : 0d);
    }

    /**
     * Applies the given predicate to all elements in the stream and checks if all elements satisfy the condition.
     *
     * @param test a Predicate to apply to each element in the stream
     * @return true if all elements in the stream satisfy the predicate, false otherwise
     */
    public boolean all(Predicate<Double> test)
    {
        return stream().allMatch(test);
    }

    /**
     * Evaluates whether any element in the stream satisfies the given predicate.
     *
     * @param test the predicate used to evaluate elements in the stream
     * @return {@code true} if any element in the stream matches the predicate, otherwise {@code false}
     */
    public boolean any(Predicate<Double> test)
    {
        return stream().anyMatch(test);
    }

    /**
     * Computes the z-score for all elements of the series.
     * The z-score is calculated as (value - mean) / standard_deviation.
     *
     * @return A new Series where each value is the z-score of the corresponding value in the original series.
     */
    public Series zscore()
    {
        double mean = average();
        double std = std();
        return map(v -> (v - mean) / std);
    }

    /**
     * Returns a new Series with its values normalized to a range between 0 and 1.
     * Normalization is achieved by scaling each value using the formula:
     * (value - min) / (max - min), where min and max are the minimum and maximum
     * values of the current Series, respectively.
     *
     * @return a new Series where all values are normalized to the range [0, 1].
     */
    public Series normalized()
    {
        double min = min();
        double max = max();
        return map(v -> (v - min) / (max - min));
    }

    /**
     * Returns a new series sorted based on the specified score function.
     *
     * @param scorer a function to score the series elements
     * @return a new series with sorted elements
     */
    public Series sort(ToDoubleFunction<Double> scorer)
    {
        Series series = new Series();
        series.addAll(this);
        series.sort(Comparator.comparing(scorer::applyAsDouble));
        return series;
    }

    /**
     * Computes the cumulative sum of the elements in the current Series.
     * The method sequentially adds each element of the Series to a running total
     * and stores the result in a new Series, such that each value in the
     * resulting Series represents the sum of all preceding elements (inclusive).
     * If the series has nan values {@code Double.NaN} is returned.
     *
     * @return a new Series containing the cumulative sum of the elements in the original Series.
     */
    public Series cumsum()
    {
        Series series = new Series();
        double sum = 0;
        for (Double v : this)
        {
            sum += v;
            series.add(sum);
        }
        return series;
    }

    /**
     * Returns a new Series containing the first n elements of the current Series.
     * If n is greater than the size of the Series, the entire Series is returned.
     *
     * @param n the maximum number of elements to include in the new Series; must be non-negative
     * @return a new Series containing up to the first n elements of the original Series
     * @throws IllegalArgumentException if n is negative
     */
    public Series head(int n)
    {
        if(n < 0)
            throw new IllegalArgumentException("n must be positive");
        n = Math.min(n, size());
        return new Series(stream().limit(n).collect(Collectors.toList()));
    }

    /**
     * Returns the last n elements of the series.
     *
     * @param n the number of elements to return from the end of the series. Must be non-negative.
     * @return a new Series containing the last n elements of this series. If n is greater than the size of the series, the entire series is returned.
     * @throws IllegalArgumentException if n is negative.
     */
    public Series tail(int n)
    {
        if(n < 0)
            throw new IllegalArgumentException("n must be positive");
        n = Math.min(n, size());
        return new Series(stream().skip(size() - n).collect(Collectors.toList()));
    }

    /**
     * Returns a new Series containing elements within the specified range.
     *
     * @param from the starting index of the range (inclusive)
     * @param to   the ending index of the range (exclusive)
     * @return a new Series containing elements from the specified range
     * @throws IllegalArgumentException if the indices are invalid
     */
    public Series between(int from, int to)
    {
        if(from > to || from < 0 || to > size())
            throw new IllegalArgumentException("Invalid indexes");
        return new Series(subList(from, to));
    }

    /**
     * Combines the elements of this series and another series using a specified
     * binary function.
     *
     * @param other the other series to combine with this series
     * @param zipFunction a function that takes two double values (one from this
     * series and one from the other series) and produces a double result
     * @return a new series containing the results of applying the zipFunction
     * to each corresponding pair of elements
     * @throws IllegalArgumentException if the sizes of the two series are not equal
     */
    public Series zip(Series other, ToDoubleBiFunction<Double, Double> zipFunction)
    {
        if (size() != other.size())
            throw new IllegalArgumentException("series must have the same size");
        Series series = new Series();
        for(int i = 0; i < size(); i ++)
            series.add(zipFunction.applyAsDouble(get(i), other.get(i)));
        return series;
    }

    /**
     * Generates a list of sub-series by applying a sliding window operation.
     * The window slides over the elements with a specified step size.
     *
     * @param step the number of elements to move the window forward in each iteratio must be positive
     * @param window the number of elements included in each window
     * @return a list of Series objects representing each window as defined by step and window size
     * @throws IllegalArgumentException if step or window is not positive
     */
    public List<Series> slidingWindow(int step, int window)
    {
        if (step <= 0 || window <= 0)
            throw new IllegalArgumentException("step and window must be positive");
        List<Series> result = new ArrayList<>();
        for (int i = 0; i < size() - step + 1; i += step)
            result.add(between(i, Math.min(size(), i + window)));
        return result;
    }

    /**
     * Calculates the dot product of the current series with the given series.
     *
     * @param other the series to compute the dot product with
     * @return the resulting dot product as a double value
     */
    public double dot(Series other)
    {
        return zip(other, (a, b) -> a * b).sum();
    }


    /**
     * Computes and returns the sum of all numerical values contained in the Series.
     * If the Series has nan values the method returns {@code Double.NaN}.
     *
     * @return the sum of all values in the Series as a double, or {@code Double.NaN} if the Series has nan values
     */
    public double sum()
    {
        return stream().mapToDouble(d -> d).sum();
    }

    /**
     * Returns the minimum value in the series.
     * If the series is empty or has nan values, {@code Double.NaN} is returned.
     *
     * @return the minimum value as a double, or {@code Double.NaN} if the series is empty or has nan values
     */
    public double min()
    {
        return stream().mapToDouble(d -> d).min().orElse(Double.NaN);
    }

    /**
     * Returns the maximum value in the series.
     * If the series is empty or has nan values, {@code Double.NaN} is returned.
     *
     * @return the maximum value as a double, or {@code Double.NaN} if the series is empty or has nan values
     */
    public double max()
    {
        return stream().mapToDouble(d -> d).max().orElse(Double.NaN);
    }

    /**
     * Calculates and returns the average of the numerical values in the Series.
     * The mean is the arithmetic average, computed by summing all elements
     * and dividing by the number of elements. If the Series is empty or has nan values,
     * the method returns {@code Double.NaN}.
     *
     * @return the mean value of the Series as a double, or {@code Double.NaN} if the Series is empty or has nan values
     */
    public double average()
    {
        return stream().mapToDouble(d -> d).average().orElse(Double.NaN);
    }

    /**
     * Computes and returns the variance of the numerical values in the Series.
     * Variance is a measure of the dispersion of the data, calculated as the
     * average of the squared differences from the mean.
     * If the Series is empty or has nan values, the method returns {@code Double.NaN}.
     * @return the variance of the Series as a double. If the Series is empty or has nan values,
     *         the method returns {@code Double.NaN}.
     */
    public double var()
    {
        double mean = average();
        double n = size();
        return stream().mapToDouble(x -> Math.pow(x - mean, 2) / n).sum();
    }

    /**
     * Computes and returns the standard deviation of the numerical values
     * in the Series. The standard deviation is a measure of the dispersion
     * or spread of the values relative to their mean.
     *
     *
     * @return the standard deviation as a double, calculated as the square root of the variance. If the series is
     * empty or has nan values {@code Double.NaN} is returned.
     */
    public double std()
    {
        return Math.sqrt(var());
    }

    /**
     * Computes and returns the middle value of the series.
     * The middle value is determined by skipping half of the series' elements.
     * If the series is empty or has nan values, {@code Double.NaN} is returned.
     *
     * @return the middle value of the series as a double, or {@code Double.NaN} if the series is empty or has nan values
     */
    public double middle()
    {
        return stream().skip(size() / 2).findFirst().orElse(Double.NaN);
    }

    /**
     * Retrieves the first element of the series. If the series is empty, it returns {@code Double.NaN}.
     *
     * @return the first element of the series, or {@code Double.NaN} if the series is empty
     */
    public double first()
    {
        return stream().findFirst().orElse(Double.NaN);
    }

    /**
     * Returns the last element of the series. If the series is empty, it returns {@code Double.NaN}.
     *
     * @return the last element of the series, or {@code Double.NaN} if the series is empty
     */
    public double last()
    {
        return stream().skip(size() - 1).findFirst().orElse(Double.NaN);
    }

    /**
     * Calculates the median of the elements in the series.
     * The median is the middle value in a sorted list of numbers.
     * If the series has an even number of elements, the median is calculated as the average of the two middle numbers.
     *
     * @return the median value of the series as a double. If the series is empty {@code Double.NaN} is returned.
     */
    public double median()
    {
        if (isEmpty())
            return Double.NaN;
        List<Double> sorted = stream().sorted().collect(Collectors.toList());
        if (sorted.size() % 2 == 0)
            return (sorted.get(sorted.size() / 2 - 1) + sorted.get(sorted.size() / 2)) / 2;
        else
            return sorted.get(sorted.size() / 2);

    }

    /**
     * Checks if the Series contains any {@code Double.NaN} values.
     *
     * @return {@code true} if the Series contains at least one {@code Double.NaN} value, {@code false} otherwise
     */
    public boolean hasNan()
    {
        return stream().anyMatch(v -> Double.isNaN(v));
    }

    /**
     * Computes and returns a map of statistical metrics for the current Series.
     * These metrics include size, mean, standard deviation, variance, minimum,
     * maximum, first element, median, last element, and a flag indicating
     * the presence of any NaN values.
     *
     * @return a map where keys are the names of statistical metrics (e.g., "size",
     *         "mean", "std", "var", etc.) and values are the corresponding
     *         computed values as Doubles
     */
    public Map<String, Double> statistics()
    {
        Map<String, Double> stats = new LinkedHashMap<>();

        stats.put("size", (double) size());
        stats.put("average", average());
        stats.put("std", std());
        stats.put("var", var());
        stats.put("min", min());
        stats.put("max", max());
        stats.put("first", first());
        stats.put("median", middle());
        stats.put("last", last());
        stats.put("hasNan", hasNan() ? 1.0 : 0.0);
        return stats;

    }

    /**
     * Checks equality with another object. Returns true if the other object is a Series and the elements are the same
     *
     * @param o the object to be compared for equality with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        return getClass().equals(o.getClass()) && super.equals(o);
    }

    /**
     * Returns a deep copy of the series
     * @return a deep copy of the series
     */
    @Override
    public Series clone()
    {
        Series s = new Series();
        s.addAll(this);
        return s;
    }

    /**
     * Returns a compact string representation of the series.
     *
     * @return a string containing the elements of the series, separated by commas
     */
    @Override
    public String toString()
    {
        return String.format("Series[size=%d]", size());
    }

    public static final int maxPrintedValues = 30;

    /**
     * Prints the contents of the series to the standard output.
     */
    public void print()
    {
        if(size() <= maxPrintedValues)
        {
            String printString = stream().map(v -> String.format(Locale.US, "%.2f", v)).collect(Collectors.joining(", ", "[", "]"));
            System.out.println(printString);
        }
        else
        {
            Series head = head(maxPrintedValues / 2);
            Series tail = tail(maxPrintedValues / 2);
            String printString = String.format(
                    "[%s, ..., %s]",
                    head.stream().map(v -> String.format(Locale.US, "%.2f", v)).collect(Collectors.joining(", ")),
                    tail.stream().map(v -> String.format(Locale.US, "%.2f", v)).collect(Collectors.joining(", "))
            );
            System.out.println(printString);
        }
    }

    /**
     * Creates a collector that converts elements of a stream into a {@link Series}
     * using the provided function to map the elements to double values.
     *
     * @param <T> the type of elements in the stream
     * @param valueConvert a function that maps an input element of type {@code T} to a double value
     * @return a {@code Series.Collector<T>} that collects elements into a {@code Series}
     */
    public static <T> Series.Collector<T> collector(ToDoubleFunction<T> valueConvert)
    {
        return new Collector<>(valueConvert);
    }

    /**
     * Generates a series of numbers starting from the specified lower bound (inclusive)
     * to the specified upper bound (exclusive).
     * The precision of generated values is of 5 decimals
     *
     * @param from the starting value (inclusive) of the range
     * @param to   the ending value (exclusive) of the range
     * @param step the step size between consecutive values in the range
     * @return a Series object containing the numbers within the specified range
     * @throws IllegalArgumentException if the starting value is greater than or equal to the ending value or if step is
     * lesser or equal to 0
     */
    public static Series range(double from, double to, double step)
    {
        if (from > to)
            throw new IllegalArgumentException("from must be less than to");
        if(step <= 0)
            throw new IllegalArgumentException("step must be greater than 0");

        Series series = new Series();


        int n = (int) ((to - from) / step);

        IntStream.range(0, n)
                .mapToObj(i -> new BigDecimal(i * step))
                .map(d -> d.setScale(5, RoundingMode.DOWN))
                .forEach(d -> series.add(d.doubleValue()));

        return series;
    }

    /**
     * Creates a new Series instance and populates it with the given numbers.
     *
     * @param numbers an array of Number objects to be added to the Series
     * @return a new Series instance containing the provided numbers
     */
    public static Series from(Number... numbers)
    {
        Series series = new Series();
        for(Number n : numbers)
            series.add(n.doubleValue());
        return series;
    }

    /**
     * Collector that accumulates elements of type T into a Series object.
     *
     * @param <T> The type of input elements to be collected.
     */
    public static class Collector<T> implements java.util.stream.Collector<T, Series, Series>
    {
        private final ToDoubleFunction<T> convertFunction;

        private Collector(ToDoubleFunction<T> convertFunction)
        {
            this.convertFunction = convertFunction;
        }

        @Override
        public Supplier<Series> supplier()
        {
            return Series::new;
        }

        @Override
        public BiConsumer<Series, T> accumulator()
        {
            return (s, v) -> s.add(convertFunction.applyAsDouble(v));
        }

        @Override
        public BinaryOperator<Series> combiner()
        {
            return (l0, l1) -> {
                l0.addAll(l1);
                return l0;
            };
        }

        @Override
        public Function<Series, Series> finisher()
        {
            return s -> s;
        }

        @Override
        public Set<Characteristics> characteristics()
        {
            return new HashSet<>(Collections.singletonList(Characteristics.IDENTITY_FINISH));
        }
    }
}
