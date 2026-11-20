package io.github.lightdataframe.lightweightdataframe;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;




/**
 * Represents a list of numerical values designed to facilitate mathematical and statistical operations.
 * It extends the {@link  java.util.ArrayList} class, adding methods for data manipulation and analysis.
 * This class provides functionality for common statistical calculations, data transformations, and grouping.
 */
public class Series extends ArrayList<Double> implements Serializable, Cloneable
{
    /**
     * Constructs a new empty Series.
     */
    public Series() {}

    /**
     * Constructs a new series instance with the specified collection of numerical values.
     * @param collection A collection of Double values to initialize the series with. Cannot be null.
     *             Null values are replaced with {@code Double.NaN}
     */
    public Series(Collection<Double> collection)
    {
        if(collection == null)
            throw new IllegalArgumentException("collection is null");
        addAll(collection);
    }

    /**
     * Generates a sample series by randomly selecting elements.
     * The sampling can be done with or without replacement.
     *
     * @param n The number of elements to sample from this Series. Must be a non-negative integer
     * @param replacement A boolean indicating whether sampling should be with replacement
     *                    If true, elements can be selected multiple times; otherwise,
     *                    elements can be selected only one time
     *                    If false and n > size() then the sample will contain size() elements
     * @return A new series containing the sampled elements
     */
    public Series sample(int n, boolean replacement)
    {
        if(n < 0)
            throw new IllegalArgumentException("n must be positive");

        Series from = new Series(this);
        Series sample = new Series();

        while(n > 0 && from.size() > 0)
        {
            int idx = (int) Math.floor(Math.random() * from.size());
            sample.add(from.get(idx));
            if(!replacement)
                from.remove(idx);
            n--;
        }
        return sample;
    }

    /**
     * Groups the elements of the series based on the result of applying the specified grouping function.
     * The grouping operation partitions the series into subsets where all elements within a subset
     * produce the same result when the grouping function is applied.
     *
     * For example, for grouping negative and positive values:
     * {@code series.groupBy(v -> v > 0 ? 1d : 0d);}
     *
     * @param groupFunction a function that computes a grouping key (double value) for each element in the series
     * @return a map where the keys are the unique grouping values produced by the {@code groupFunction},
     * and the values are series containing the elements that correspond to each group
     */
    public Map<Double, Series> group(ToDoubleFunction<Double> groupFunction)
    {
        if(groupFunction == null)
            throw new IllegalArgumentException("groupFunction is null");

        List<Double> groups = stream()
                .mapToDouble(groupFunction)
                .boxed()
                .distinct()
                .collect(Collectors.toList());

        Map<Double, Series> result = new LinkedHashMap<>();
        for (Double group : groups)
        {
            Series series = new Series(
                    stream()
                    .filter(v -> groupFunction.applyAsDouble(v) == group)
                    .collect(Collectors.toList())
            );
            result.put(group, series);
        }

        return result;
    }

    /**
     * Transforms the elements of the current series by applying a specified mapping function
     * to each element and returns a new series containing the mapped values.
     *
     * @param valueMap A function that takes the series and index as input and returns the double to put at the index.
     *                 This function is applied to each element of the series.
     * @return A new series containing the values resulting from applying the mapping function to each element.
     */
    public Series map(ToDoubleBiFunction<Series, Integer> valueMap)
    {
        if(valueMap == null)
            throw new IllegalArgumentException("valueMap is null");

        Series series = new Series();
        IntStream.range(0, size())
                .mapToDouble(idx -> valueMap.applyAsDouble(this, idx))
                .forEach(series::add);
        return series;
    }

    /**
     * Transforms the elements of the current series by applying a specified mapping function
     * to each element and returns a new series containing the mapped values.
     *
     * @param valueMap A function that takes a Double as input and returns a double.
     *                 This function is applied to each element of the series.
     * @return A new series containing the values resulting from applying the mapping function to each element.
     */
    public Series map(ToDoubleFunction<Double> valueMap)
    {
        if(valueMap == null)
            throw new IllegalArgumentException("valueMap is null");

        Series series = new Series();
        stream()
                .map(valueMap::applyAsDouble)
                .forEach(series::add);
        return series;
    }

    /**
     * Tests each element in the series against the provided predicate and maps the results to either 1 or 0.
     *
     * @param test the predicate to evaluate each element in the series
     * @return a new series where each value is the result of applying the predicate to the corresponding element (1 or 0)
     */
    public Series test(Predicate<Double> test)
    {
        if(test == null)
            throw new IllegalArgumentException("test is null");

        return map(v -> test.test(v) ? 1d : 0d);
    }

    /**
     * Applies the given predicate to all elements in the series and checks if all elements satisfy the condition.
     *
     * @param test a predicate to apply to each element in the series
     * @return {@code true} if all elements in the stream satisfy the predicate, {@code false} otherwise
     */
    public boolean all(Predicate<Double> test)
    {
        if(test == null)
            throw new IllegalArgumentException("test is null");

        return stream().allMatch(test);
    }

    /**
     * Evaluates whether any element in the series satisfies the given predicate.
     *
     * @param test the predicate used to evaluate elements in the stream
     * @return {@code true} if any element in the stream matches the predicate, otherwise {@code false}
     */
    public boolean any(Predicate<Double> test)
    {
        if(test == null)
            throw new IllegalArgumentException("test is null");

        return stream().anyMatch(test);
    }

    /**
     * Returns the series without NaN value.
     * @return a new series without NaN value
     */
    public Series withoutNan()
    {
        Series series = new Series();
        for(Double v : this)
            if(!Double.isNaN(v))
                series.add(v);
        return series;
    }

    /**
     * Replaces values of the series matching the given predicate.
     * @param newValue value to replace the series values with
     * @param replacePredicate predicate to match values to be replaced
     * @return a new series with replaced values
     */
    public Series replace(double newValue, Predicate<Double> replacePredicate)
    {
        if(replacePredicate == null)
            throw new IllegalArgumentException("replacePredicate is null");

        Series series = new Series();
        for(Double v : this)
            if(replacePredicate.test(v))
                series.add(newValue);
            else
                series.add(v);
        return series;
    }

    /**
     * Inserts a value at the end of the series. If the value is null {@code Double.NaN} is inserted instead.
     * @param value value to be inserted
     * @return true (as specified by {@code Collection.add})
     */
    public boolean add(Double value)
    {
        if(value == null)
            value = Double.NaN;
        return super.add(value);
    }

    /**
     * Inserts a value at the specified index. If the value is null {@code Double.NaN} is inserted instead.
     * @param index index at which the specified element is to be inserted
     * @param value element to be inserted
     */
    public void add(int index, Double value)
    {
        if(value == null)
            value = Double.NaN;
        super.add(index, value);
    }

    /**
     * Inserts a collection of elements at the end of the series. {@code Double.NaN} is inserted in place of null values.
     * @param c collection containing elements to be added to this collection
     * @return true if the series changed as a result of the operation, false otherwise
     * @throws IllegalArgumentException if the given collection is null
     */
    public boolean addAll(Collection<? extends Double> c)
    {
        if(c == null)
            throw new IllegalArgumentException("c is null");

        c = c.stream()
                .map(v -> v == null ? Double.NaN : v)
                .collect(Collectors.toList());
        return super.addAll(c);
    }

    /**
     * Inserts a collection of elements at the specified index. {@code Double.NaN} is inserted in place of null values.
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c collection containing elements to be added to this list
     * @return true is the series changed as a result of the operation, false otherwise
     * @throws IllegalArgumentException if the given collection is null
     */
    public boolean addAll(int index, Collection<? extends Double> c)
    {
        if(c == null)
            throw new IllegalArgumentException("c is null");

        c = c.stream()
                .map(v -> v == null ? Double.NaN : v)
                .collect(Collectors.toList());
        return super.addAll(index, c);
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
        if(std == 0)
            return map(v -> 0);
        return map(v -> (v - mean) / std);
    }

    /**
     * Returns a new Series with its values normalized to a range between 0 and 1.
     * Normalization is achieved by scaling each value using the formula:
     * (value - min) / (max - min), where min and max are the minimum and maximum
     * values of the current series.
     *
     * @return a new series where all values are normalized to the range [0, 1].
     */
    public Series normalized()
    {
        double min = min();
        double max = max();
        if(min == max)
            return map(v -> 0);
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
        if(scorer == null)
            throw new IllegalArgumentException("scorer is null");

        Series series = new Series();
        series.addAll(this);
        series.sort(Comparator.comparing(scorer::applyAsDouble));
        return series;
    }

    /**
     * Computes the cumulative sum of the elements in the current series.
     * The method sequentially adds each element of the series to a running total
     * If the series has nan values the resulting series will be NaN starting from its first occurrence.
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
     * Returns a new series containing the first n elements of the current series.
     * If n is greater than the size of the series, the entire series is returned.
     *
     * @param n the maximum number of elements to include in the new series; must be non-negative
     * @return a new series containing up to the first n elements of the original series
     * @throws IllegalArgumentException if n is negative
     */
    public Series head(int n)
    {
        if(n < 0)
            throw new IllegalArgumentException("n must be positive");

        n = Math.min(n, size());
        List<Double> sublist = subList(0, n);

        return new Series(sublist);
    }

    /**
     * Returns the last n elements of the series.
     *
     * @param n the number of elements to return from the end of the series. Must be non-negative.
     * @return a new series containing the last n elements of this series. If n is greater than the size of the series, the entire series is returned.
     * @throws IllegalArgumentException if n is negative.
     */
    public Series tail(int n)
    {
        if(n < 0)
            throw new IllegalArgumentException("n must be positive");

        n = Math.min(n, size());

        List<Double> sublist = subList(size() - n, size());

        return new Series(sublist);
    }

    /**
     * Returns a new series containing elements within the specified range.
     *
     * @param from the starting index of the range (inclusive)
     * @param to   the ending index of the range (exclusive)
     * @return a new series containing elements from the specified range
     * @throws IllegalArgumentException if the indices are invalid
     */
    public Series between(int from, int to)
    {
        if(from > to || from < 0 || to > size())
            throw new IllegalArgumentException("invalid indices");

        return new Series(subList(from, to));
    }

    /**
     * Combines the elements of this series and another series using a specified binary function.
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
        if(other == null)
            throw new IllegalArgumentException("other is null");
        if(zipFunction == null)
            throw new IllegalArgumentException("zipFunction is null");
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
     * @param step the number of elements to move the window forward in each iteration. Must be positive
     * @param window the number of elements included in each window. Must be positive
     * @return a list of Series objects representing each window as defined by step and window size
     * @throws IllegalArgumentException if step or window is not positive
     */
    public List<Series> slidingWindow(int step, int window)
    {
        if (step <= 0 || window <= 0)
            throw new IllegalArgumentException("step and window must be positive");

        List<Series> result = new ArrayList<>();
        for (int i = 0; i < size(); i += step)
            result.add(new Series(subList(i, Math.min(size(), i + window))));
        return result;
    }

    /**
     * Calculates the dot product of the current series with another series.
     *
     * @param other the series to compute the dot product with
     * @return the resulting dot product as a double value
     */
    public double dot(Series other)
    {
        if (other == null)
            throw new IllegalArgumentException("other is null");
        if (size() != other.size())
            throw new IllegalArgumentException("series must have the same size");

        return zip(other, (a, b) -> a * b).sum();
    }


    /**
     * Computes and returns the sum of all values contained in the series. If the series is empty the method returns 0.
     * If the series has nan values the method returns {@code Double.NaN}.
     *
     * @return the sum of all values in the Series as a double, or {@code Double.NaN} if the series has nan values
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
     * Calculates and returns the average of the values in the series.
     * The average is computed by summing all elements and dividing by the number of elements.
     * If the series is empty or has nan values, the method returns {@code Double.NaN}.
     *
     * @return the average or {@code Double.NaN} if the series is empty or has nan values
     */
    public double average()
    {
        return stream().mapToDouble(d -> d).average().orElse(Double.NaN);
    }

    /**
     * Computes and returns the variance of the numerical values in the series.
     * Variance is calculated as the average of the squared differences from the mean.
     * If the series is empty or has nan values, the method returns {@code Double.NaN}.
     * @return the variance of the series as a double. If the series is empty or has nan values,
     *         the method returns {@code Double.NaN}.
     */
    public double var()
    {
        double mean = average();
        double n = size();

        if(n == 0)
            return Double.NaN;

        double sum = stream().mapToDouble(x -> Math.pow(x - mean, 2)).sum();
        return sum / n;
    }

    /**
     * Computes and returns the sample variance of the numerical values in the series.
     * The sample variance is calculated as the sum of the squared differences from the mean divided by n - 1.
     * If the series size is < 2 or has nan values, the method returns {@code Double.NaN}.
     * @return the sample variance of the series as a double. If the series size is < 2 or has nan values,
     *         the method returns {@code Double.NaN}.
     */
    public double sampleVar()
    {
        double mean = average();
        double n = size();

        if(n < 2)
            return Double.NaN;

        double sum = stream().mapToDouble(x -> Math.pow(x - mean, 2)).sum();
        return sum / (n - 1);
    }

    /**
     * Computes and returns the sample standard deviation of the numerical values in the series.
     * It is the square root of the sample variance.
     *
     * @return the sample standard deviation. If the series size is < 2 or has nan values {@code Double.NaN} is returned.
     */
    public double sampleStd()
    {
        return Math.sqrt(sampleVar());
    }

    /**
     * Computes and returns the standard deviation of the numerical values in the series.
     * The standard deviation is a measure of the dispersion or spread of the values relative to their mean.
     * It is the square root of the variance.
     *
     * @return the standard deviation. If the series is empty or has nan values {@code Double.NaN} is returned.
     */
    public double std()
    {
        return Math.sqrt(var());
    }

    /**
     * Computes and returns the middle value of the series.
     * The middle value is determined by skipping half of the series' elements.
     * If the series is empty {@code Double.NaN} is returned.
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
        return stream().skip(Math.max(0, size() - 1)).findFirst().orElse(Double.NaN);
    }

    /**
     * Calculates the median of the elements in the series.
     * The median is the middle value in a sorted list of odd size.
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
     * Returns the percentile of a given value, calculated as the percentage of elements that are less or equal to it.
     * If the series is empty {@code Double.NaN} is returned.
     * @param value value to find the percentile of
     * @return the percentile of a given value
     */
    public double percentile(double value)
    {
        if(isEmpty())
            return Double.NaN;

        double lesser = stream()
                .mapToDouble(aDouble -> aDouble <= value ? 1 : 0)
                .sum();

        return lesser / size();
    }

    /**
     * Calculates the value at which a given percentile lies. The percentile of a value is the percentage of elements
     * that are less or equal to it.
     * For example, {@code percentileValue(0.95)} returns the value of the series having 0.95 elements lesser than it.
     *
     * @param percentile percentile to calculate the value for
     * @return the value for the given percentile. If the series is empty {@code Double.NaN} is returned.
     */
    public double percentileValue(double percentile)
    {
        if (percentile < 0 || percentile > 1)
            throw new IllegalArgumentException("percentile must be between 0 and 1");

        if (isEmpty())
            return Double.NaN;

        Series sorted = sort(v -> v);

        BigDecimal rank = new BigDecimal(percentile)  // percentile * size()
                .multiply(new BigDecimal(size()))
                .setScale(7, RoundingMode.HALF_DOWN);

        double x = rank.doubleValue();
        if (x == 0)
            return sorted.get(0);
        if (x % 1 == 0)
            return sorted.get((int) (x - 1));
        return sorted.get((int) (Math.ceil(x) - 1));
    }

    /**
     * Checks if the Series contains any {@code Double.NaN} values.
     *
     * @return true if the Series contains at least one {@code Double.NaN} value, false otherwise
     */
    public boolean hasNan()
    {
        return stream().anyMatch(v -> Double.isNaN(v));
    }

    /**
     * Computes and returns a map of statistical metrics for the current series.
     * These metrics include size, average, standard deviation, variance, minimum,
     * maximum, median, first element, median, last element, and a flag indicating
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
        stats.put("sum", sum());
        stats.put("avg", average());
        stats.put("std", std());
        stats.put("var", var());
        stats.put("min", min());
        stats.put("max", max());
        stats.put("median", median());
        stats.put("first", first());
        stats.put("middle", middle());
        stats.put("last", last());
        stats.put("hasNan", hasNan() ? 1.0 : 0.0);
        return stats;
    }

    /**
     * Compares the specified object with this list for equality. See {@link List#equals(Object)}.
     *
     * @param o the object to be compared for equality with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        return super.equals(o);
    }

    /**
     * Returns a copy of the series
     * @return a copy of the series
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

    private static int maxPrintedValues = 30;
    private static int printedValuesLength = 12;
    private static int printedValuesPrecision = 3;

    /**
     * Sets the maximum number of printed values when calling {@link #print()}.
     * If the size of the series is greater than this value, middle values are skipped and only the head and tail are printed.
     * Default is 30.
     * @param n number of values to print
     */
    public static void setMaxPrintedValues(int n)
    {
        if(n < 0)
            throw new IllegalArgumentException("n must be >= 0");
        maxPrintedValues = n;
    }

    /**
     * Sets the length (number of characters) of the printed values when calling {@link #print()}.
     * Default is 12.
     *
     * @param n length of printed values
     */
    public static void setPrintedValuesLength(int n)
    {
        if (n <= 0)
            throw new IllegalArgumentException("n must be > 0");
        printedValuesLength = n;
    }

    /**
     * Sets the precision (number of decimal places) of the printed values when calling {@link #print()}.
     *
     * @param n precision of printed values. Must be >= 0
     */
    public static void setPrintedValuesPrecision(int n)
    {
        if (n < 0)
            throw new IllegalArgumentException("n must be >= 0");
        printedValuesPrecision = n;
    }

    /**
     * Prints the contents of the series to the standard output.
     */
    public void print()
    {
        String formatString = "%" + printedValuesLength + "." + printedValuesPrecision + "f";
        if(size() <= maxPrintedValues)
        {
            String printString = stream().map(v -> String.format(Locale.US, formatString, v)).collect(Collectors.joining(",", "[", "]"));
            System.out.println(printString);
        }
        else
        {
            Series head = head(maxPrintedValues / 2);
            Series tail = tail(maxPrintedValues / 2);
            String printString = String.format(
                    "[%s, %s, %s]",
                    head.stream().map(v -> String.format(Locale.US, formatString, v)).collect(Collectors.joining(",")),
                    String.format("%" + printedValuesLength + "s", "..."),
                    tail.stream().map(v -> String.format(Locale.US, formatString, v)).collect(Collectors.joining(","))
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
        if(valueConvert == null)
            throw new IllegalArgumentException("valueConvert cannot be null");

        return new Collector<>(valueConvert);
    }

    /**
     * Generates a series of numbers starting from the specified lower bound (inclusive)
     * to the specified upper bound (exclusive).
     *
     * @param from the starting value (inclusive) of the range
     * @param to   the ending value (exclusive) of the range
     * @param step the step size between consecutive values in the range
     * @return a Series object containing the numbers within the specified range
     * @throws IllegalArgumentException if the starting value is greater than the ending value or if step is
     * lesser or equal to 0
     */
    public static Series range(double from, double to, double step)
    {
        if(from > to)
            throw new IllegalArgumentException("from must be less than to");
        if(step <= 0)
            throw new IllegalArgumentException("step must be greater than 0");
        if(Double.isNaN(from) || Double.isNaN(to) || Double.isNaN(step))
            throw new IllegalArgumentException("from, to, and step must be finite");

        Series series = new Series();


        BigDecimal d = BigDecimal.valueOf(from);

        do
        {
            series.add(d.doubleValue());
            d = d.add(BigDecimal.valueOf(step));
        }
        while(d.doubleValue() < to);

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
     * Returns a series of size {@code size} filled with the specified value.
     *
     * @param size  series size
     * @param value series value
     * @return a new series filled with the specified value
     * @throws IllegalArgumentException if {@code size} is negative
     */
    public static Series fill(int size, double value)
    {
        if (size < 0)
            throw new IllegalArgumentException("size must be >= 0");
        Series series = new Series();
        for (int i = 0; i < size; i++)
            series.add(value);
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
            if(convertFunction == null)
                throw new IllegalArgumentException("convertFunction cannot be null");

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
