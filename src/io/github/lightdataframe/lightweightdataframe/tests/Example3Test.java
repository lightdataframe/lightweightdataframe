package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Example3Test
{
    String json =
            "[\n" +
                    "  {\n" +
                    "    \"price\": 150,\n" +
                    "    \"sqm\": null,\n" +
                    "    \"type\": 1,\n" +
                    "    \"is_rented\": 1,\n" +
                    "    \"num_bedrooms\": 2,\n" +
                    "    \"num_bathrooms\": 1,\n" +
                    "    \"floor\": 3\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"price\": 250,\n" +
                    "    \"sqm\": 80,\n" +
                    "    \"type\": 2,\n" +
                    "    \"is_rented\": 0,\n" +
                    "    \"num_bedrooms\": NaN,\n" +
                    "    \"num_bathrooms\": 2,\n" +
                    "    \"floor\": 1\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"price\": 100,\n" +
                    "    \"sqm\": 30,\n" +
                    "    \"type\": 1,\n" +
                    "    \"is_rented\": 1,\n" +
                    "    \"num_bedrooms\": 1,\n" +
                    "    \"num_bathrooms\": 1,\n" +
                    "    \"floor\": 2\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"price\": 350,\n" +
                    "    \"sqm\": 120,\n" +
                    "    \"type\": 3,\n" +
                    "    \"is_rented\": 0,\n" +
                    "    \"num_bedrooms\": 4,\n" +
                    "    \"num_bathrooms\": 3,\n" +
                    "    \"floor\": 0\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"price\": 90,\n" +
                    "    \"sqm\": 45,\n" +
                    "    \"type\": null,\n" +
                    "    \"is_rented\": 1,\n" +
                    "    \"num_bedrooms\": 1,\n" +
                    "    \"num_bathrooms\": 1,\n" +
                    "    \"floor\": 0\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"price\": 200,\n" +
                    "    \"sqm\": 100,\n" +
                    "    \"type\": null,\n" +
                    "    \"is_rented\": 0,\n" +
                    "    \"num_bedrooms\": 2,\n" +
                    "    \"num_bathrooms\": 2,\n" +
                    "    \"floor\": -1\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"price\": 300,\n" +
                    "    \"sqm\": 150,\n" +
                    "    \"type\": 3,\n" +
                    "    \"is_rented\": 0,\n" +
                    "    \"num_bedrooms\": 5,\n" +
                    "    \"num_bathrooms\": 4,\n" +
                    "    \"floor\": 0\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"price\": 180,\n" +
                    "    \"sqm\": 70,\n" +
                    "    \"type\": 2,\n" +
                    "    \"is_rented\": 1,\n" +
                    "    \"num_bedrooms\": 2,\n" +
                    "    \"num_bathrooms\": 2,\n" +
                    "    \"floor\": 1\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"price\": 120,\n" +
                    "    \"sqm\": 50,\n" +
                    "    \"type\": 1,\n" +
                    "    \"is_rented\": null,\n" +
                    "    \"num_bedrooms\": 2,\n" +
                    "    \"num_bathrooms\": 1,\n" +
                    "    \"floor\": 4\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"price\": 400,\n" +
                    "    \"sqm\": 200,\n" +
                    "    \"type\": 3,\n" +
                    "    \"is_rented\": 0,\n" +
                    "    \"num_bedrooms\": 6,\n" +
                    "    \"num_bathrooms\": 5,\n" +
                    "    \"floor\": -1\n" +
                    "  }\n" +
                    "]";

    Dataframe df;

    @BeforeEach
    void initialize()
    {
        df = Serializer.fromJson(JsonFormat.ROWS, json);
    }

    @Test
    void jsonTest()
    {
        df.print();
    }

/*
Here’s a list of tasks you can perform on the **house dataset** in JSON format:

---

### **1. Data Inspection and Summary**
1. **Dataset overview**:
   - Calculate the total count of houses in the dataset.
   - Display the first and last few entries to understand its structure and values.
2. **Summary statistics**:
   - Compute the mean, median, minimum, and maximum values for the `price`, `sqm`, `num_bedrooms`, and `num_bathrooms`.
3. **Dataset validation**:
   - Check if any fields are missing or incorrectly formatted (e.g., negative values for `price` or `sqm`).
*/

    @Test
    void testSummary()
    {
        df.size();

        df.filter(Filter.head(3)).print();
        df.filter(Filter.tail(3)).print();

        df.print();

        df.printStatistics();

        boolean test = df.getColumn("price").any(v -> v < 0);
        assertFalse(test);
        test = df.getColumn("sqm").any(v -> v < 0);
        assertFalse(test);
        test = df.getColumn("num_bedrooms").any(v -> v < 0);
        assertFalse(test);
        test = df.getColumn("num_bathrooms").any(v -> v < 0);
        assertFalse(test);

        df.filter(Filter.hasNan()).print();
    }

    @Test
    void testMedian()
    {
        double median = df.getColumn("price").median();
        assertEquals(190, median);
    }

    /*
    ---

    ### **2. Data Cleaning**
    0. **Fill Nan values**:
    1. **Correct outliers**:
       - Identify and handle possible outliers, such as:
         - Houses with `sqm <= 0` or `price <= 0`.
         - Unrealistic combinations, like `num_bedrooms = 0` but `sqm > 50`.
    2. **Standardize `type` field**:
       - Check for consistent use of house types (`type`): ensure all values are 1 (Apartment), 2 (Loft), or 3 (Villa).

    ---
    */
    @Test
    void testClean()
    {

        //for(String col : df.getColumns())
        //    df.replace(col, df.getColumn(col).withoutNan().average(), v -> Double.isNaN(v));
        //df.print();

        df.filter(Filter.rowTest(row -> row.get("sqm") <= 0 || row.get("price") <= 0)).print();
        df.filter(Filter.rowTest(row -> row.get("num_bedrooms") == 0 && row.get("sqm") > 50)).print();
        df.filter(Filter.rowTest(row -> row.get("type") != 1 && row.get("type") != 2 && row.get("type") != 3)).print();
    }
    /*

### **3. Data Transformation**
1. **Add derived fields**:
   - Create a new field for `price_per_sqm` (calculated as `price / sqm`).
   - Categorize houses into price ranges:
     - Low: `price < 150`
     - Medium: `150 <= price <= 300`
     - High: `price > 300`
   - Create a `size_category` based on `sqm`:
     - Small: `sqm <= 50`
     - Medium: `50 < sqm <= 100`
     - Large: `sqm > 100`
2. **Convert type to text values**:
   - Replace numerical `type` values:
     - 1 → "Apartment", 2 → "Loft", 3 → "Villa".

*/

    @Test
    void testTransform()
    {
        df.setColumn("price_per_sqm", df.getColumn("price").zip(df.getColumn("sqm"), (p, s) -> p / s));

        ToDoubleFunction<Double> priceCategory = price -> {
            if(price < 150)
                return 0;
            else if(price < 300)
                return 1;
            else
                return 2;
        };
        df.setColumn("price_range", df.getColumn("price").map(priceCategory));

        ToDoubleFunction<Double> sizeCategory = sqm -> {
            if(sqm <= 50)
                return 0;
            else if(sqm <= 100)
                return 1;
            else
                return 2;
        };

        df.setColumn("size_category", df.getColumn("sqm").map(sizeCategory));

        df.print();
    }

    /*
---

### **4. Data Filtering**
1. **Filter by criteria**:
   - Extract all rented houses (`is_rented = 1`).
   - Find all houses with more than 3 bedrooms.
   - List all houses with `price > 300` (luxury houses).
2. **Search for specific cases**:
   - Houses with `floor = 0` (ground-level homes).
   - Basement houses (`floor < 0`) and analyze their prices and sizes.


*/
    @Test
    void testFilter()
    {
        df.filter(Filter.valueTest("is_rented", v -> v == 1d)).print();
        df.filter(Filter.valueTest("num_bedrooms", v -> v >= 3)).print();
        df.filter(Filter.valueTest("price", v -> v >= 300)).print();

        df.filter(Filter.valueTest("floor", v -> v == 0d)).print();
        df.filter(Filter.valueTest("floor", v -> v < 0d)).printStatistics();
    }
    /*
---

### **5. Grouping and Aggregation**
1. **Group by type (`type`)**:
   - Calculate the average `price`, `sqm`, and `num_bedrooms` for each house type (Apartment, Loft, Villa).
2. **Group by `is_rented`**:
   - Analyze the difference in average `price` and `sqm` between rented (1) and not-rented (0) houses.

*/

    @Test
    void testGroupAggregate()
    {
        df.groupAggregate(
                Grouper.groupByRow(row -> row.get("type")),
                new Aggregator()
                        .addAggregation("type", df -> df.getColumn("type").withoutNan().average())
                        .addAggregation("avg_price", df -> df.getColumn("price").withoutNan().average())
                .addAggregation("avg_sqm", df -> df.getColumn("sqm").withoutNan().average())
                .addAggregation("avg_num_bedrooms", df -> df.getColumn("num_bedrooms").withoutNan().average())
        ).print();

        Dataframe df0 = df.drop(Filter.hasNan("is_rented")).groupAggregate(
                Grouper.groupByRow(row -> row.get("is_rented")),
                new Aggregator()
                        .addAggregation("is_rented", df -> df.getColumn("is_rented").withoutNan().average())
                        .addAggregation("avg_price", df -> df.getColumn("price").withoutNan().average())
                        .addAggregation("avg_sqm", df -> df.getColumn("sqm").withoutNan().average())
                        .addAggregation("avg_num_bedrooms", df -> df.getColumn("num_bedrooms").withoutNan().average())
        );

        df0.setColumn("diff_avg_price", df0.getColumn("avg_price").map(p -> p - df0.getColumn("avg_price").average())).print();

    }

    /*
---

### **6. Sorting**
1. **Sort dataset**:
   - Sort houses by `price` (ascending or descending).
   - Sort houses by `sqm` within each `type`.
2. **Multi-column sorting**:
   - Sort houses first by `type`, then by `num_bedrooms`, and finally by `price`.


*/

    @Test
    void testSort()
    {
        df.sort(true, Sorter.sortByColumn(true, "price", v -> v)).print();

        df.group(Grouper.groupByRow(row -> row.get("type")))
                .values()
                .forEach(df0 -> df0.sort(true, Sorter.sortByColumn(true, "sqm", v -> v)).print());

        df.sort(true, Sorter.sortByColumns(false, "type", "num_bedrooms", "price")).print();
    }

    /*
---

### **7. Outlier Detection**
1. **Detect outliers**:
   - Identify houses where `price_per_sqm` is unusually low or high.
   - Find potential inconsistencies:
     - Large `sqm` with few bedrooms (e.g., `num_bedrooms < 2` for `sqm > 150`).
     - High `price` but very small `sqm`.

*/
    @Test
    void testOutlier()
    {
        df.setColumn("price_per_sqm", df.getColumn("price").zip(df.getColumn("sqm"), (p, s) -> p / s));


        df.filter(Filter.rowTest(row -> row.get("price_per_sqm") < 100 || row.get("price_per_sqm") > 1000)).print();

        df.filter(Filter.rowTest(row -> row.get("sqm") > 150 && row.get("num_bedrooms") < 2)).print();

        df.filter(Filter.rowTest(row -> row.get("price") > 150 && row.get("sqm") < 100)).print();

        df.sort(false, Sorter.sortByColumn(true, "price_per_sqm", v -> v)).print();
    }
    /*
---

### **8. Visualization Tasks**
1. **Distribution plots**:
   - Visualize the distribution of `price`, `sqm`, and `num_bedrooms` as histograms or box plots.
2. **Scatter plots**:
   - Create scatter plots to explore relationships:
     - `price` vs. `sqm`
     - `price_per_sqm` vs. `type`
   - Highlight differences between rented and non-rented houses using colors or markers.
3. **Bar charts**:
   - Compare average `price` and `sqm` across house types or floor levels.
*/

    @Test
    void testVisualize() throws InterruptedException
    {
        //Plotter.line(df.sort(true, Sorter.sortByRow(row -> row.get("price"))));

        df = df.setColumn("price_per_sqm", df.getColumn("price").zip(df.getColumn("sqm"), (p, s) -> p / s));

        //Plotter.scatter(df.getColumn("price"), df.getColumn("sqm"));
        //Plotter.scatter(df.getColumn("price_per_sqm"), df.getColumn("type"));

        //Thread.currentThread().join();
    }

    /*
---

### **9. Advanced Analysis**
1. **Correlation analysis**:
   - Analyze the correlation between numerical features:
     - `price` vs. `sqm`
     - `num_bedrooms` vs. `num_bathrooms`
   - Explore how `is_rented` correlates with other features.
2. **Feature extraction**:
   - Identify key features that drive house prices (e.g., `sqm`, `num_bedrooms`, `type`).

---

### **10. Prediction and Machine Learning**
1. **Regression tasks**:
   - Predict `price` based on `sqm`, `num_bedrooms`, `type`, and other features.
2. **Classification tasks**:
   - Predict house type (`type`) based on `sqm`, `num_bedrooms`, and `num_bathrooms`.
   - Predict whether a house is rented (`is_rented`) using other features.

---

Let me know if you want to explore or implement one of these tasks!
     */
}
