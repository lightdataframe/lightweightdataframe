package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExampleTest
{
    private List<Person> people;
    private Dataframe df;

    @BeforeEach
    void initialize()
    {
        people = Arrays.asList(
                new Person(20, 180, 60, 10000.0, 1),
                new Person(60, 156, 76, null, -2),
                new Person(30, 160, 60, 11000.0, null),
                new Person(40, null, 75, 13000.0, 4),
                new Person(50, 153, 86, 14000.0, -5),
                new Person(20, 180, null, null, 6),
                new Person(40, 165, 65, 16000.0, -7),
                new Person(50, 160, 45, 17000.0, 8),
                new Person(45, null, 70, 18000.0, -9),
                new Person(60, 176, 83, 14000, 5),
                new Person(45, 186, 70, 20000, 13),
                new Person(35, 190, 85, null, 4),
                new Person(20, 168, 90, 8000, -3),
                new Person(30, 157, 110, 12000, -10)
                //
        );
        df = people.stream().collect(Dataframe.<Person>collector()
                        .addColumn("age", Person::getAge)
                        .addColumn("height", Person::getHeight)
                        .addColumn("weight", Person::getWeight)
                        .addColumn("salary", Person::getSalary)
                        .addColumn("favoriteNumber", Person::getFavoriteNumber));
    }

    @Test
    void print()
    {
        Dataframe.setMaxPrintedRows(8);
        df.print();
    }

    /*
    This dataset contains a list of people, where each person has attributes such as age, height, weight, salary, and favoriteNumber. Here's a list of meaningful analyses and operations you could perform on this data:

    ---

    ### **Individual Aggregations**
    1. **Compute averages**:
       - Calculate the average age, height, weight, salary, and favorite numbers for the dataset.

2. **Get total sums**:
   - Sum up attributes such as total age, height, or weight across all people.

3. **Minimum/Maximum values**:
   - Identify the minimum and maximum values for numerical columns like age, height, weight, salary, and favoriteNumber.
*/

    @Test
    void test1()
    {
        df.printStatistics();
    }

    /*
    ---

    ### **Filtering Operations**
    1. **Filter non-null rows**:
       - Exclude rows with missing (null) values in any column.

    2. **Based on a range of values**:
       - Filter rows based on specific numerical ranges, e.g.:
         - People whose age is > 30.
         - People with a salary between 10,000 and 20,000.

    3. **Condition-based filtering**:
       - Filter rows where:
         - Favorite number is negative.
         - Weight is greater than 80.
         - Salary is below a specific threshold.

    ---
    */
    @Test
    void filterTest()
    {
        Dataframe filtered = df.drop(Filter.hasNan());
        filtered = df.filter(Filter.rowTest(row -> row.get("age") > 30));
        filtered = df.filter(Filter.rowTest(row -> row.get("salary") > 10000 && row.get("salary") < 15000));
        filtered = df.filter(Filter.valueTest("favoriteNumber", v -> v < 0));
        filtered = df.filter(Filter.valueTest("weight", v -> v > 80));
        filtered = df.filter(Filter.valueTest("salary", v -> v < 10000));
        filtered.print();
    }
/*

### **Grouping and Aggregation**
1. **Group by specific attributes**:
   - Group data by "age", "height range", or "favoriteNumber". For example:
     - Group people by age into buckets (e.g., 20-30, 30-40, etc.).
     - Group people by "favoriteNumber being positive or negative".

2. **Aggregate within groups**:
   - Calculate statistics (mean, sum, min, max):
     - Average weight for each age group.
     - Minimum and maximum salary in groups of favoriteNumber ranges.
     - Distribution of heights in each age group.

3. **Sliding window operations**:
   - Use a sliding window grouping mechanism to aggregate consecutive blocks of rows.

---
*/
    @Test
    void groupAggregateTest()
    {
        for(String col : df.getColumns())
            df.group(Grouper.groupByColumnValues(col));
        RowGrouper grouper = Grouper.groupByRow(row -> Math.floor(row.get("age") / 10));

        Map<Object, Dataframe> groups = df.group(grouper);
        groups.values().forEach(Dataframe::print);

        Aggregator aggr0 = new Aggregator()
                .addAggregation("size", df -> df.size())
                .addAggregation("minSalary", df -> df.getColumn("salary").min())
                .addAggregation("maxSalary", df -> df.getColumn("salary").max())
                .addAggregation("avgWeight", df -> df.getColumn("weight").average());

        Aggregator aggr1 = new Aggregator()
                .addAggregation("size", df -> df.getColumn("height").withoutNan().size())
                .addAggregation("heightAvg", df -> df.getColumn("height").withoutNan().average())
                .addAggregation("heightStd", df -> df.getColumn("height").withoutNan().std());

        Dataframe aggr = df.drop(Filter.hasNan("height")).groupAggregate(grouper, aggr1);

        aggr = df.groupAggregate(Grouper.slidingWindow(1, 3), aggr1);

        aggr.print();
    }

/*

### **Sorting**
1. **Sort data by a single column**:
   - Sort people by their age, favoriteNumber, or salary in ascending or descending order.

2. **Sort by multiple columns**:
   - Sort people by age and then by height for entries with the same age.

3. **Custom sorting**:
   - Apply transformations like absolute value or squaring (`Math.pow(v, 2)`) on numeric columns for refined sorting.
*/

    @Test
    void sortTest()
    {
        Dataframe sorted = df.sort(true, Sorter.sortByColumn(true, "age", v -> v));
        sorted.print();
        sorted = df.sort(true, Sorter.sortByColumn(false, "favoriteNumber", v -> v));
        sorted.print();
        sorted = df.sort(true, Sorter.sortByColumn(true, "salary", v -> v));
        sorted.print();
        sorted = df.sort(true, Sorter.sortByColumns(false, "age", "height"));
        sorted.print();
        sorted = df.sort(false, Sorter.sortByColumns(false, "weight", "age", "height"));
        sorted.print();
        sorted = df.sort(true, Sorter.sortByColumns(true));
        sorted.print();
        sorted = df.sort(true, Sorter.sortByColumn(false, "favoriteNumber", v -> Math.abs(v)));
        sorted.print();
    }

/*
---

### **Handling Missing Data**
1. **Identify rows with missing values**:
   - Find rows where certain columns (e.g., "salary" or "height") are null.

2. **Impute missing values**:
   - Replace missing numeric values with default values such as:
     - The mean or median of their respective columns.
     - A predetermined constant, e.g., 0 or -1.

3. **Drop rows with missing data**:
   - Create a new dataset with all rows containing missing data removed.
*/
    @Test
    void missingDataTest()
    {
        Dataframe filtered = df.filter(Filter.hasNan("salary", "height"));

        for(String col : df.getColumns())
            df.replace(col, df.getColumn(col).withoutNan().average(), v -> Double.isNaN(v));
        df.print();
        filtered = df.drop(Filter.hasNan());

    }
/*

---

### **Derived Metrics and Calculations**
1. **Add derived columns**:
   - Compute new columns based on existing ones, e.g.:
     - Body-Mass Index (BMI) using `weight / (height * height)`.
     - Income-to-age ratio using `salary / age`.

2. **Normalize data**:
   - Scale attributes like age, salary, and height to a [0, 1] range.

3. **Calculate statistical distributions**:
   - Find variance, standard deviation, and percentiles for numeric columns.
*/

    @Test
    void derivedTest()
    {
        df.setColumn("height", df.getColumn("height").map(v -> v / 100));
        Series height = df.getColumn("height");
        Series weight = df.getColumn("weight");
        Series salary = df.getColumn("salary");
        Series age = df.getColumn("age");

        Series bmi = height.zip(weight, (h, w) -> w / (h * h));
        Series salaryToAge = salary.zip(age, (s, a) -> s / a);
        df.setColumn("bmi", bmi);
        df.setColumn("salaryToAge", salaryToAge);
        df.setColumn("heightNormalized", height.map(v -> (v - height.withoutNan().min()) / (height.withoutNan().max() - height.withoutNan().min())));
        df.setColumn("weightNormalized", weight.map(v -> (v - weight.withoutNan().min()) / (weight.withoutNan().max() - weight.withoutNan().min())));

        df.setColumn("agePercentile", age.map(v -> age.withoutNan().percentile(v)));
        df.print();
    }
/*
---

### **Visual Groupings**
1. **Frequency counts**:
   - Count the number of people in each age group, weight bucket, or salary bracket.

2. **Identify outliers**:
   - Spot anomalies in data, such as:
     - Extremely high or low weights compared to others.
     - People with negative favorite numbers.

---

### **Transformations**
1. **Update values conditionally**:
   - For example:
     - Set salary to `0` for rows where it is null.
     - Modify favorite numbers to their absolute values.

2. **Clone and Modify Data**:
   - Duplicate rows or datasets, and apply custom transformations for analysis.

---

### **Complex Example Scenarios**
1. **Who is underpaid for their age?**
   - Compare salary-to-age ratios and find people whose salary seems unusually low for their age group.

2. **Health assessment with missing data:**
   - Calculate an adjusted BMI for rows where weight or height is missing by applying sensible defaults.

3. **Custom group performance:**
   - Compare average salary across any user-defined group (e.g., people with positive favorite numbers vs those with negatives).

Let me know if you'd like to work on any specific operation or need implementation details!
*/



    @Test
    void averages()
    {
        double v = (20 + 60 + 30 + 40 + 50 + 20 + 40 + 50 + 45 + 60 + 45 + 35 + 20 + 30) / 14d;
        assertEquals(v, df.getColumn("age").withoutNan().average());
        v = (180 + 156 + 160 + 153 + 180 + 165 + 160 + 176 + 186 + 190 + 168 + 157) / 12d;
        assertEquals(v, df.getColumn("height").withoutNan().average());
        v = (1 - 2 + 4 - 5 + 6 - 7 + 8 - 9 + 5 + 13 + 4 - 3 - 10) / 13d;
        assertEquals(v, df.getColumn("favoriteNumber").withoutNan().average());
    }

    @Test
    void find()
    {
        List<Integer> ids = df.find(Filter.rowTest(row -> row.get("favoriteNumber") < 0));
        assertEquals(Arrays.asList(1,4,6,8,12,13), ids);
        ids = df.find(Filter.rowTest(row -> row.toSeries().hasNan()));
        assertEquals(Arrays.asList( 1, 2, 3, 5, 8, 11), ids);
        ids = df.find(Filter.valueTest("height", h -> h < 0));
        assertEquals(Collections.emptyList(), ids);

        ids = df.find(Filter.valueTest("weight", w -> w > 80));
        assertEquals(Arrays.asList(4, 9, 11, 12, 13), ids);
        ids = df.find(Filter.rowTest(row -> row.get("salary") <= 12000 && row.get("favoriteNumber") > 0));
        assertEquals(Collections.singletonList(0), ids);


        double minSalary = df.getColumn("salary").withoutNan().min();

        ids = df.find(Filter.valueTest("salary", s -> s == minSalary));


    }

    @Test
    void group()
    {
        Map<Object, Dataframe> groups = df.group(Grouper.groupByColumnValues("age"));
        groups = df.group(Grouper.groupByRow(row -> row.get("favoriteNumber") > 0));

        groups = df.group(Grouper.slidingWindow(3, 2));
        groups = df.group(Grouper.groupByRow(row -> row.toSeries().hasNan()));
        groups = df.group(Grouper.groupByRow(row -> row.getIndex() > 3));

        List<Object> g = new ArrayList<>(groups.keySet());
        for(int i = 0; i < groups.size(); i++)
        {
            Dataframe df = groups.get(g.get(i));
            df.setColumn("group", Series.fill(df.size(), i).map((s, j) -> Math.pow(j, 2)));
            df.print();
        }
    }

    @Test
    void sort()
    {
        Dataframe sorted = df.sort(true, Sorter.sortByColumn(true, "age", v -> v));
        sorted = df.sort(true, Sorter.sortByColumns(false, "age", "height"));
        sorted = df.sort(true, Sorter.sortByColumn(false, "favoriteNumber", v -> v));
        sorted = df.sort(false, Sorter.sortByColumns(false, "weight", "age", "height"));
        sorted = df.sort(true, Sorter.sortByColumn(true, "favoriteNumber", v -> Math.pow(v, 2)));
        sorted = df.sort(true, Sorter.sortByColumns(true));
        assertEquals(df.getRows(), sorted.getRows());
        sorted.print();

        sorted.append(sorted.getRow(0));
        sorted.insert(1, sorted.getRow(0));

        Dataframe finalSorted = sorted;
        List<Integer> ids = sorted.find(Filter.rowTest(row -> row.equals(finalSorted.getRow(0))));
        assertEquals(Arrays.asList(0, 1, 15), ids);
    }

    @Test
    void filter()
    {
        Dataframe filtered = df.filter(Filter.rowTest(row -> !row.toSeries().hasNan()));
        assertEquals(8, filtered.size());
        filtered = df.filter(Filter.rowTest(row -> row.equals(df.getRow(0))));
        assertEquals(1, filtered.size());
        filtered = df.filter(Filter.valueTest("height", h -> h < 0));
        assertEquals(0, filtered.size());
        Dataframe head = df.filter(Filter.head(2));
        filtered = df.filter(Filter.rowTest(row -> head.getRows().contains(row)));
        filtered = df.filter(Filter.valuesMatch(df.getRow(df.size() - 1)));
        filtered = df.filter(Filter.hasNan());
        assertThrows(IllegalArgumentException.class, () -> df.filter(Filter.hasNan("invalid_column")));
        filtered = df.filter(Filter.hasNan("height"));
        filtered.append(filtered.getRow(0));
        filtered.print();
    }

    @Test
    void aggregate()
    {
        Aggregator aggregator = new Aggregator()
                .addAggregation("size", df -> df.size())
                .addAggregation("ageSum", df -> df.getColumn("age").sum())
                .addAggregation("heightSum", df -> df.getColumn("height").sum())
                .addAggregation("weightSum", df -> df.getColumn("weight").sum())
                .addAggregation("salaryMin", df -> df.getColumn("salary").min())
                .addAggregation("salaryMax", df -> df.getColumn("salary").max())
                .addAggregation("favNumberMin", df -> df.getColumn("favoriteNumber").min())
                .addAggregation("favNumberMax", df -> df.getColumn("favoriteNumber").max())
                ;

        Dataframe aggr = df.drop(Filter.hasNan()).aggregate(aggregator);
        aggr.print();
        double ageSum = 20 + 50 + 40 + 50 + 60 + 45 + 20 + 30;
        assertEquals(ageSum, aggr.get("ageSum", 0));

        df.aggregate(new Aggregator()).print();
    }

    @Test
    void groupAggregate()
    {
        RowGrouper grouper = Grouper.groupByRow(row -> row.get("favoriteNumber") > 0);
        Aggregator aggregator = new Aggregator()
                .addAggregation("size", df -> df.size())
                .addAggregation("ageSum", df -> df.getColumn("age").sum())
                .addAggregation("heightSum", df -> df.getColumn("height").sum())
                .addAggregation("weightSum", df -> df.getColumn("weight").sum())
                .addAggregation("salaryMin", df -> df.getColumn("salary").min())
                .addAggregation("salaryMax", df -> df.getColumn("salary").max())
                .addAggregation("favNumberMin", df -> df.getColumn("favoriteNumber").min())
                .addAggregation("favNumberMax", df -> df.getColumn("favoriteNumber").max());

        Dataframe aggr = df.drop(Filter.hasNan()).groupAggregate(grouper, aggregator);
        grouper = Grouper.groupByRow(row -> row.toSeries().hasNan());
        aggr = df.groupAggregate(grouper, aggregator);
        aggr.print();
    }

    @Test
    void set()
    {
        List<Integer> ids = df.find(Filter.rowTest(row -> row.get("favoriteNumber") < 0));
        Dataframe modified = df.clone();
        for(int id : ids)
            modified.set(id, Double.NaN);

        modified = df.clone();
        modified.set("age", Double.NaN);
        modified.set(0, df.getRow(13));

        assertEquals(30, modified.get("age", 0));
        assertEquals(-10, modified.get("favoriteNumber", 13));

        modified = df.clone();

        modified.set("favoriteNumber", 13, Double.NaN);

        assertEquals(Double.NaN, modified.get("favoriteNumber", 13));
        modified.print();
    }

    @Test
    void appendInsert()
    {
        Dataframe df0 = df.clone();
        df0.append(df.getRow(0));
        df0.insert(0, df.getRow(0));
        assertThrows(IllegalArgumentException.class, () -> df0.insert(-1, df.getRow(1)));
        assertThrows(IllegalArgumentException.class, () -> df0.insert(df0.size() + 1, df.getRow(1)));
        df0.print();
    }


    public static class Person
    {
        private Integer age;
        private Double height;
        private Double weight;
        private Double salary;
        private final Integer favoriteNumber;

        public Person(Integer age, Number height, Number weight, Number salary, Integer favoriteNumber)
        {
            this.age = age;
            this.height = height != null ? height.doubleValue() : null;
            this.weight = weight != null ? weight.doubleValue() : null;
            this.salary = salary != null ? salary.doubleValue() : null;
            this.favoriteNumber = favoriteNumber;
        }

        public Integer getFavoriteNumber()
        {
            return favoriteNumber;
        }

        public Double getSalary()
        {
            return salary;
        }

        public Double getWeight()
        {
            return weight;
        }

        public Double getHeight()
        {
            return height;
        }

        public Integer getAge()
        {
            return age;
        }
    }
}
