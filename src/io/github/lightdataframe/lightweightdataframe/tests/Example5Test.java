package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

public class Example5Test
{
    String csv = "PlantID,Height,Width,Age,Sunlight,WaterRequirement,GrowthRate\n" +
            "1,120.5,50.2,5,-2,30,1.5\n" +
            "2,80.0,45.3,3,8,40,1.2\n" +
            "3,135.7,40.5,2,10,25,2.1\n" +
            "4,150.0,55.0,10,15,50,0.8\n" +
            "5,95.3,45.1,7,5,35,1.0\n" +
            "6,140.0,60.0,20,20,45,1.3\n" +
            "7,110.2,50.0,8,-5,40,1.7\n" +
            "8,123.5,50.0,12,12,15,2.4\n" +
            "9,98.0,42.2,6,6,20,1.8\n" +
            "10,115.0,48.5,3,9,38,NaN\n" +
            "11,130.3,58.4,11,14,55,0.9\n" +
            "12,87.6,46.8,4,7,33,1.6\n" +
            "13,NaN,53.0,9,11,37,2.0\n" +
            "14,102.4,44.1,2,-1,22,1.1\n" +
            "15,117.6,49.5,5,10,40,1.4\n" +
            "16,125.0,56.0,15,13,48,1.0\n" +
            "17,70.5,38.2,1,-3,25,2.3\n" +
            "18,NaN,41.0,3,10,20,1.9\n" +
            "19,120.0,54.2,4,8,35,1.2\n" +
            "20,110.0,47.0,6,12,40,NaN\n";

    Dataframe df;

    @BeforeEach
    void initialize()
    {
        df = Serializer.fromCsv(true, csv);
    }

    @Test
    void print()
    {
        df.print();
    }

    /*
    Here is a **list of tasks** you can perform on the plant dataset for analysis and processing. These tasks range from basic data validation to advanced analysis:

---

### **1. Data Inspection and Cleaning**
1. **Inspect the dataset**:
   - View the first and last few rows (`head` and `tail`) to understand the structure and data.
   - Display basic information, such as the number of rows, columns, and data types of each column.
2. **Check for missing values**:
   - Identify columns with `NaN` values and calculate the percentage of missing data in each column.
   - Visualize missing data patterns using a heatmap or bar chart.
3. **Handle missing data**:
   - Impute missing values with appropriate techniques:
     - Mean or median for `Height`, `Width`, or `GrowthRate`.
     - Forward-fill or backward-fill for `WaterRequirement`.
   - Drop rows or columns with excessive missing values if necessary.
4. **Detect and handle invalid data**:
   - Verify there are no invalid values (e.g., negative values for `Height` or `Width`).
   - Correct any inconsistencies if found.

*/
    @Test
    void testClean()
    {
        df.filter(Filter.head(2)).print();
        df.filter(Filter.tail(2)).print();
        df.printStatistics();

        Aggregator aggr = new Aggregator();
        for(String col : df.getColumns())
            aggr.addAggregation(col + " nans", df -> df.getColumn(col).count(Double.NaN) / df.getColumn(col).size());

        df.aggregate(aggr).print();

        for(String col : df.getColumns())
            df.replace(col, df.getColumn(col).withoutNan().average(), v -> Double.isNaN(v));
        df.print();

        df.drop(Filter.rowTest(row -> row.toSeries().count(Double.NaN) >= 2)).print();

        df.filter(Filter.rowTest(row -> row.get("Height") < 0 || row.get("Width") < 0 || row.get("GrowthRate") < 0)).print();
    }

    /*

---

### **2. Statistical Analysis**
1. **Generate summary statistics**:
   - Compute the mean, median, standard deviation, min, and max for each numeric feature.
   - Focus on understanding ranges of features like `Height`, `Width`, `Sunlight`, or `GrowthRate`.
2. **Analyze outliers**:
   - Use box plots or z-score calculations to detect outliers in `Height`, `Width`, or `GrowthRate`.
   - Decide whether to remove or adjust these outliers.
3. **Correlation analysis**:
   - Compute the correlation matrix to determine relationships between numeric features:
     - Does `Sunlight` have a direct impact on `GrowthRate`?
     - Is there a relationship between `Age` and `Height`?


     */

    @Test
    void testSummary()
    {
        df.printStatistics();
        Aggregator aggr = new Aggregator()
                .addAggregation("min_height", df -> df.getColumn("Height").min())
                .addAggregation("max_height", df -> df.getColumn("Height").max())
                .addAggregation("min_width", df -> df.getColumn("Width").min())
                .addAggregation("max_width", df -> df.getColumn("Width").max())
                .addAggregation("min_sunlight", df -> df.getColumn("Sunlight").min())
                .addAggregation("max_sunlight", df -> df.getColumn("Sunlight").max())
                .addAggregation("min_growth_rate", df -> df.getColumn("GrowthRate").min())
                .addAggregation("max_growth_rate", df -> df.getColumn("GrowthRate").max());

        df.aggregate(aggr).print();

        df = df.drop(Filter.hasNan());

        for(String col : Arrays.asList("Height", "Width", "GrowthRate"))
        {
            df.setColumn(col+"_ZScore", df.getColumn(col).zscore());
            df.setColumn(col+"_Outlier", df.getColumn(col+"_ZScore").zscore().test(z -> Math.abs(z) > 1.5));
        }
        df.print();
    }

    private double correlation(Series sx, Series sy)
    {
        double meanX = sx.average();
        double meanY = sy.average();

        double num = sx.zip(sy, (x, y) -> (x - meanX) * (y - meanY)).sum();
        double denom = Math.sqrt(sx.map(x -> Math.pow(x - meanX, 2)).sum() * sy.map(x -> Math.pow(x - meanY, 2)).sum());

        return num / denom;
    }

    @Test
    void correlationTest()
    {
        df = df.drop(Filter.hasNan());
        List<String> columns = df.getColumns();
        Dataframe corrDf = new Dataframe();
        for(String col0 : columns)
        {
            Map<String, Double> correlation = new LinkedHashMap<>();
            for (String col1 : columns)
            {
                double corr = correlation(df.getColumn(col0), df.getColumn(col1));
                correlation.put(col1, corr);
            }
            corrDf.append(correlation);
        }
        corrDf.print();

        Dataframe strongCorr = new Dataframe();
        for(String col : corrDf.getColumns())
            strongCorr.setColumn(col, corrDf.getColumn(col).test(v -> Math.abs(v) > 0.7));
        strongCorr.print();

    }

    /*
---

### **3. Data Visualization**
1. **Histogram analysis**:
   - Plot histograms for `Height`, `Width`, `Sunlight`, and `GrowthRate` to analyze their distributions.
2. **Scatter plots for relationships**:
   - Plot scatter plots for:
     - `Height` vs. `Width` (to check plant proportions).
     - `Sunlight` vs. `GrowthRate` (to understand the effect of sunlight).
     - `Age` vs. `WaterRequirement`.
3. **Box plots**:
   - Visualize the spread of data and outliers for `Height`, `Width`, and `GrowthRate`.
4. **Heatmap for correlation**:
   - Create a heatmap for the feature correlation matrix to visualize any strong relationships or dependencies.

*/

    @Test
    void testPlot() throws InterruptedException
    {
        Plotter.scatter(df.getColumn("Height"), df.getColumn("Width"));
    }

    /*

---

### **4. Data Transformation**
1. **Handle negative values**:
   - Address negative values in `Sunlight`:
     - Keep them if they represent valid categories (e.g., plants that need shade or nighttime growth).
     - Replace with a new category for plants requiring "low light."
2. **Create derived features**:
   - Calculate `Volume` of the plant as an approximation:
     - `Volume = Height × Width`.
   - Normalize `Sunlight` and `WaterRequirement` to scale them between 0 and 1 for machine learning tasks.
   - Categorize plants into age groups:
     - Young (0-5 years), Middle-aged (6-10 years), Mature (11+ years).
3. **Binning**:
   - Create bins for numeric features such as `WaterRequirement` (Low, Medium, High) or `GrowthRate` (Slow, Moderate, Fast).

*/
    @Test
    void testTransform()
    {
        df.setColumn("Low Light", df.getColumn("Sunlight").test(s -> s <= 0));
        df.replace("Sunlight", 0, s -> s < 0);

        df.setColumn("Volume", df.getColumn("Height").zip(df.getColumn("Width"), (h, w) -> h * w));
        df.replaceColumn("Sunlight", s -> s.normalized());
        df.replaceColumn("WaterRequirement", s -> s.normalized());

        df.setColumn("Age Group", df.getColumn("Age").map(a -> a < 6 ? 0 : a < 11 ? 1 : 2));

        df.getColumn("WaterRequirement")
                .normalized()
                .map(v -> Math.floor(v * 100) )
                .map(v -> v - v % 10)
                .map(v -> v / 10)
                .print();
    }


    /*

---

### **5. Filtering and Subsetting**
1. **Filter by criteria**:
   - Select plants with `Height > 120 cm` and `GrowthRate > 1.5 cm/week`.
   - Identify plants with `Sunlight < 0` (shade-loving plants).
   - Locate young plants (`Age <= 5`) with the highest `GrowthRate`.
2. **Subset the dataset**:
   - Extract plants with missing `Height` or `GrowthRate` for further inspection.
   - Create sub-datasets based on specific conditions (e.g., Mature plants, Fast-growing plants).
3. **Out-of-range detection**:
   - Find plants that require excessive `WaterRequirement` (e.g., >50 liters) or have very low `GrowthRate`.
*/

    @Test
    void testFilter()
    {
        df.filter(Filter.rowTest(row -> row.get("Height") > 120 && row.get("GrowthRate") > 1.5)).print();
        df.filter(Filter.rowTest(row -> row.get("Sunlight") < 0)).print();
        Dataframe df0 = df.filter(Filter.rowTest(row -> row.get("Age") <= 5));
        df0.filter(Filter.rowTest(row -> row.get("GrowthRate") == df0.getColumn("GrowthRate").withoutNan().max()))
                .print();

        df.filter(Filter.rowTest(row -> Double.isNaN(row.get("Height")) || Double.isNaN(row.get("GrowthRate")))).print();
        df.filter(Filter.rowTest(row -> row.get("GrowthRate") >= 2)).print();

        df.filter(Filter.valueTest("WaterRequirement", v -> v > 50)).print();
    }

    /*

---

### **6. Grouping and Aggregation**
1. **Group by derived categories**:
   - Group plants by `Age` category (Young, Middle-aged, Mature) and calculate:
     - Average `Height`, `Width`, and `GrowthRate`.
     - Total `WaterRequirement` for each group.
2. **Sunlight needs grouping**:
   - Aggregate statistics like mean `Height`, `GrowthRate`, or `WaterRequirement` for plants grouped by `Sunlight` needs (low, moderate, high).
3. **Analyze by water requirements**:
   - Determine the total number of plants in each `WaterRequirement` bin (Low, Medium, High).

*/
    @Test
    void testGroup()
    {
        df.print();
        df = df.drop(Filter.hasNan());
        Dataframe ga = df.groupAggregate(
                Grouper.groupByRow(row -> row.get("Age") <= 3 ? 0 : row.get("Age") <= 8 ? 1 : 2),
                new Aggregator()
                        .addAggregation("age_group", df -> {
                            double a = df.getColumn("Age").first();
                            return a <= 3 ? 0 : a <= 8 ? 1 : 2;
                        })
                        .addAggregation("count", df -> df.size())
                        .addAggregation("sum_height", df -> df.getColumn("Height").sum())
                        .addAggregation("sum_width", df -> df.getColumn("Width").sum())
                        .addAggregation("sum_growth_rate", df -> df.getColumn("GrowthRate").sum())
                        .addAggregation("avg_height", df -> df.getColumn("Height").average())
                        .addAggregation("avg_width", df -> df.getColumn("Width").average())
                        .addAggregation("avg_growth_rate", df -> df.getColumn("GrowthRate").average())
        );
        ga.sort(true, Sorter.sortByColumn(true, "age_group", v -> v)).print();
    }
    /*
---

### **7. Outlier Detection**
1. **Identify unusual data patterns**:
   - Plants with extremely high/low `Height` or `GrowthRate` compared to their `Age`.
   - Outliers in `WaterRequirement` that don’t match the expected range.
2. **Handle outliers**:
   - Decide whether to remove, cap, or replace these values (based on domain knowledge).

*/
    @Test
    void testOutlier()
    {
        df = df.drop(Filter.hasNan());
        df.setColumn("Height_Age_Ratio", df.getColumn("Height").zip(df.getColumn("Age"), (h, a) -> h / a));

        df.setColumn("Ratio_ZScore", df.getColumn("Height_Age_Ratio").zscore());
        df.setColumn("Ratio_Outlier", df.getColumn("Ratio_ZScore").zscore().test(z -> Math.abs(z) > 2));

        df = df.drop(Filter.valueTest("Ratio_Outlier", v -> v == 1));

        df.print();
    }
    /*

---

### **8. Advanced Insights**
1. **Find high-performing plants**:
   - Identify plants with maximum `GrowthRate` in each `Age` category.
   - Select plants with high `Height` and `Width` but low `WaterRequirement`.
2. **Explore plant survival conditions**:
   - Analyze the relationship between `Sunlight`, `WaterRequirement`, and `GrowthRate` to identify optimal survival conditions.
3. **Analyze missing data trends**:
   - Find patterns related to missing values (e.g., do plants of a certain `Age` have more missing data?).
   - Impute or make decisions based on observed trends.


*/

    private double ageCategory(double age)
    {
        if(age <= 5)
            return 0;
        else if(age <= 10)
            return 1;
        else
            return 2;
    }

    private double survival(Row row)
    {
        double sunlight = row.get("Sunlight");
        double waterReq = row.get("WaterRequirement");
        double growthRate = row.get("GrowthRate");
        return 0;
    }

    @Test
    void testAdvanced()
    {
        df.setColumn("Age_Category", df.getColumn("Age").map(this::ageCategory));
        RowGrouper grouper = Grouper.groupByRow(row -> row.get("Age_Category"));


        Dataframe maxGR = new Dataframe();
        df.group(grouper)
                .values()
                .stream()
                .map(df0 -> df0.filter(Filter.rowTest(row -> row.get("GrowthRate") == df0.getColumn("GrowthRate").withoutNan().max())))
                .map(Dataframe::getRows)
                .flatMap(Collection::stream)
                .forEach(maxGR::append);
        maxGR.print();

        df.setColumn("survival", df.apply(Mapper.mapByRow(this::survival)));



        Dataframe.setMaxPrintedRows(20);
        df.print();
        Dataframe df0 = df.groupAggregate(grouper,
                new Aggregator()
                        .addAggregation("category", df -> df.getColumn("Age_Category").first())
                        .addAggregation("missing", df -> df.find(Filter.hasNan()).size())
        );
        df0.print();

        for(String col : df.getColumns())
            df.replace(col, df.getColumn(col).withoutNan().average(), v -> Double.isNaN(v));

    }

    /*

---

### **9. Predictive Modeling**
1. **Regression tasks**:
   - Predict `Height` based on `Age`, `Sunlight`, and `WaterRequirement`.
   - Predict `GrowthRate` based on the other features.
2. **Clustering**:
   - Perform clustering on the dataset to group similar plants based on `Height`, `WaterRequirement`, `GrowthRate`, etc.
3. **Classification tasks**:
   - Categorize plants into "Fast-growing" or "Slow-growing" using the `GrowthRate`.

---

### **10. Documentation and Reporting**
1. Summarize findings:
   - Write a concise summary or create a dashboard showing trends, distributions, and key takeaways from the dataset.
2. Create actionable insights:
   - Provide recommendations for optimal water and sunlight needs based on the analysis.

---

Let me know if you want to focus on any specific category or set up an example!
     */
}
