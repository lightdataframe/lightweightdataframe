package io.github.lightdataframe.lightweightdataframe.tests;


import io.github.lightdataframe.lightweightdataframe.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Example2Test
{
    /*
    - **age**: Integer (years old, e.g., how old the car is).
    - **mileage**: Numeric (distance traveled in thousands of kilometers).
    - **engine_size**: Numeric (engine displacement in liters).
    - **horsepower**: Numeric (engine power in HP).
    - **is_electric**: Boolean (1 for electric cars, 0 for non-electric cars).
    - **car_class**: Integer (1 = Economy, 2 = Mid-range, 3 = Luxury).

     */
    static String csv = "age,mileage,engine_size,horsepower,is_electric,car_class\n" +
        "2,25.3,1.5,120,0,2\n"+
        "5,82.6,2.0,150,0,null\n"+
        "1,10.5,0.0,0,1,1\n"+
        "8,120.4,3.0,250,0,3\n"+
        "3,0,1.8,130,0,2\n"+
        "0,0.0,0.0,0,1,null\n"+
        "6,95.3,2.5,200,0,3\n"+
        "4,60.9,1.6,140,0,2\n"+
        "7,110.8,2.8,240,0,null\n"+
        "10,150.0,1.3,70,0,1\n";


    Dataframe df;

    @BeforeEach
    void initialize()
    {
        df = Serializer.fromCsv(true, csv);
    }

    @Test
    void testCsv()
    {
        df.print();
    }


    @Test
    void testColumns()
    {
        df.dropColumn("age").print();
    }

    /*
    Here is a list of tasks you can perform on the provided car dataset:

    ---

    ### **1. Basic Statistical Analysis**
    1. **Summarize dataset**:
       - Calculate the total count, mean, median, minimum, and maximum of all numerical columns (`age`, `mileage`, `engine_size`, `horsepower`).
    2. **Range of features**:
       - Find the range (max - min) for each column.

    ---


    */
    @Test
    void testSummarize()
    {
        List<String> columns = Arrays.asList("age", "mileage", "engine_size", "horsepower");
        for(String col : columns)
        {
            Map<String, Double> stats = df.getColumn(col).statistics();
            double max = df.getColumn(col).max();
            double min = df.getColumn(col).min();
        }

    }

/*

### **2. Data Cleaning**
1. **Check for missing data**:
   - Confirm there are no missing values in the dataset.
2. **Remove unrealistic values**:
   - Detect and remove inconsistencies, e.g.,:
     - Cars with `mileage = 0` but `age > 0`.
     - Non-electric cars (`is_electric = 0`) with `engine_size = 0`.
     - Electric cars (`is_electric = 1`) that have non-zero `engine_size` or `horsepower`.
*/

    @Test
    void testClean()
    {
        df.drop(Filter.hasNan()).print();

        df.drop(Filter.rowTest(row -> row.get("mileage") == 0 && row.get("age") > 0)).print();

        df.drop(Filter.rowTest(row -> row.get("is_electric") == 0 && row.get("engine_size") == 0)).print();
        df.drop(Filter.rowTest(row -> row.get("is_electric") == 1 && (row.get("engine_size") != 0 || row.get("horsepower") != 0))).print();
    }

/*

---

### **3. Transformations**
1. **Create derived features**:
   - Add new attributes, such as:
     - **"age_category"**: Bucket cars by age, e.g., (0-2 = New, 3-5 = Moderate, 6+ = Old).
     - **"mileage_per_year"**: Calculate mileage per year (`mileage / age`), taking into account edge cases like `age = 0`.
   - Add a **binary flag** for high-performance cars:
     - Set to `1` for `horsepower > 200`, otherwise `0`.
2. **Normalize numerical data**:
   - Perform min-max scaling or z-score normalization for columns like `mileage`, `horsepower`, and `engine_size`.
3. **Convert class column**:
   - Convert `car_class` integers into human-readable class names (e.g., 1 = "Economy", 2 = "Mid-range", 3 = "Luxury").


*/
    @Test
    void transformationsTest()
    {
        df.setColumn("age_category", df.getColumn("age").map(age -> age < 3 ? 0 : age < 6 ? 1 : 2));

        df.setColumn("mileage_per_year", df.getColumn("age").zip(df.getColumn("mileage"), (age, mileage) -> age == 0 ? 0 : mileage / age));
        df.setColumn("is_high_performance", df.getColumn("horsepower").map(hp -> hp > 200 ? 1 : 0));

        df.setColumn("mileage_norm", df.getColumn("mileage").normalized());
        df.setColumn("mileage_zscore", df.getColumn("mileage").zscore());
        df.setColumn("horsepower_norm", df.getColumn("horsepower").normalized());
        df.setColumn("horsepower_zscore", df.getColumn("horsepower").zscore());
        df.setColumn("engine_size_norm", df.getColumn("engine_size").normalized());
        df.setColumn("engine_size_zscore", df.getColumn("engine_size").zscore());

        df.print();
    }
/*
---

### **4. Data Grouping and Aggregation**
1. **Group by Car Class**:
   - Calculate the average and total mileage, engine size, and horsepower for each car class (`car_class`).
2. **Group by Age Buckets**:
   - Divide cars into age groups and calculate the average horsepower and mileage for each group.

*/
    @Test
    void groupAggregateTest()
    {
        df = df.drop(Filter.hasNan("car_class"));

        Dataframe aggr = df.groupAggregate(
                Grouper.groupByRow(row -> row.get("car_class")),
                new Aggregator()
                        .addAggregation("car_class", df -> df.getColumn("car_class").average())
                        .addAggregation("count", df -> df.size())
                        .addAggregation("avg_horsepower", df -> df.getColumn("horsepower").average())
                        .addAggregation("avg_mileage", df -> df.getColumn("mileage").average())
                        .addAggregation("avg_engine_size", df -> df.getColumn("engine_size").average())
                        .addAggregation("sum_horsepower", df -> df.getColumn("horsepower").sum())
                        .addAggregation("sum_mileage", df -> df.getColumn("mileage").sum())
                        .addAggregation("sum_engine_size", df -> df.getColumn("engine_size").sum())
        );
        aggr.print();
    }

    /*
---

### **5. Filtering**
1. **Find electric cars**:
   - Filter rows where `is_electric = 1`. Analyze the age and mileage distribution.
2. **Find high-performance cars**:
   - Select rows where `horsepower > 200` and analyze the engine size and car class.
3. **Filter by age**:
   - Extract cars older than 5 years to study how `mileage` and `horsepower` change over time.


*/

    @Test
    void filterTest()
    {
        df.filter(Filter.rowTest(row -> row.get("is_electric") == 1)).printStatistics();

        df.filter(Filter.rowTest(row -> row.get("horsepower") > 200)).printStatistics();

        df.groupAggregate(
                Grouper.groupByRow(row -> row.get("age") > 5),
                new Aggregator()
                        .addAggregation("average_age", df -> df.getColumn("age").average())
                        .addAggregation("min_age", df -> df.getColumn("age").min())
                        .addAggregation("max_age", df -> df.getColumn("age").max())
                        .addAggregation("count", df -> df.size())
                        .addAggregation("avg_horsepower", df -> df.getColumn("horsepower").average())
                        .addAggregation("avg_mileage", df -> df.getColumn("mileage").average())
                        .addAggregation("avg_engine_size", df -> df.getColumn("engine_size").average())
                        .addAggregation("sum_horsepower", df -> df.getColumn("horsepower").sum())
                        .addAggregation("sum_mileage", df -> df.getColumn("mileage").sum())
                        .addAggregation("sum_engine_size", df -> df.getColumn("engine_size").sum())
        ).print();
    }

    /*
    ---

    ### **6. Sorting**
    1. **Sort dataset**:
       - Sort cars by individual columns, such as `age`, `horsepower`, or `mileage` (ascending or descending).
    2. **Multi-column sorting**:
       - Sort cars by `car_class` and `mileage` (e.g., economy cars with the highest mileage).

    */
    @Test
    void sortTest()
    {
        df.sort(true, Sorter.sortByColumn(false, "age", v -> v)).print();

        df.drop(Filter.hasNan("car_class"))
                .sort(true, Sorter.sortByColumns(false, "car_class", "mileage"))
                .print();
    }


    /*

---

### **7. Outlier Detection**
1. **Identify mileage outliers**:
   - Find cars with extremely high mileage compared to their age.
2. **Detect horsepower extremes**:
   - Check if there are cars with very high or very low horsepower for their class range.
3. **Detect unusual data combinations**:
   - Look for cases where `engine_size` does not match `horsepower`.

*/

    @Test
    void outlierTest()
    {
        df.setColumn("mileage_age_ratio", df.getColumn("mileage").zip(df.getColumn("age"), (mileage, age) -> mileage / age));
        df.setColumn("horsepower_class_ratio", df.getColumn("horsepower").zip(df.getColumn("car_class"), (horsepower, car_class) -> horsepower / car_class));
        df.setColumn("engine_size_horsepower_ratio", df.getColumn("engine_size").zip(df.getColumn("horsepower"), (engine_size, horsepower) -> engine_size / horsepower));
        df.print();
    }

    /*


---

### **8. Visualization Tasks**
1. **Distribution plots**:
   - Create histograms or kernel density plots for features like `age`, `mileage`, or `engine_size`.
2. **Scatter plots**:
   - Plot relationships between key features, such as `horsepower vs. engine_size` or `mileage vs. age`.
3. **Grouped bar charts**:
   - Compare average `horsepower` or `mileage` across `car_class`.
4. **Box plots**:
   - Explore the range and outliers for `mileage` within each car class.

---

*/
    @Test
    void vizTest()
    {
        RowGrouper grouper = Grouper.groupByRow(row -> row.get("car_class"));
        df = df.drop(Filter.hasNan("car_class"));
        Dataframe df0 = df.groupAggregate(grouper, new Aggregator()
                        .addAggregation("car_class", df -> df.getColumn("car_class").average())
                        .addAggregation("count", df -> df.size())
                .addAggregation("avg_horsepower", df -> df.getColumn("horsepower").average())
                .addAggregation("avg_mileage", df -> df.getColumn("mileage").average())
                .addAggregation("sum_horsepower", df -> df.getColumn("horsepower").sum())
                .addAggregation("sum_mileage", df -> df.getColumn("mileage").sum())
        );
        df0 = df0.sort(true, Sorter.sortByColumn(false, "car_class", v -> v));
        df0.print();
        //Plotter.line(df0, "car_class", "avg_horsepower", "avg_mileage");
    }


    /*

### **9. Advanced Analysis**
1. **Correlation analysis**:
   - Compute the correlation matrix to identify:
     - Relationships between `horsepower`, `engine_size`, `mileage`, and `age`.
   - Analyze the correlation between `is_electric` and other features.
2. **Car segmentation**:
   - Use clustering techniques (e.g., k-means) to group cars based on similar features (e.g., `mileage`, `horsepower`, `age`).
3. **Feature importance**:
   - Identify which features (e.g., `age`, `engine_size`) best predict `car_class` using regression or classification models.

---
*/
}
