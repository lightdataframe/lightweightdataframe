package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class Example4Test
{
    String csv = "ProductID,Price,Discount,Stock,Target,Rating\n" +
            "1,150.25,10,50,1,NaN\n" +
            "2,250.00,20,30,0,nan\n" +
            "3,120.75,5,100,,4.7\n" +
            "4,300.10,15,20,0,3.2\n" +
            "5,90.00,0,0,1,4.9\n" +
            "6,180.85,25,60,1,4.2\n" +
            "7,220.40,10,40,0,3.5\n" +
            "8,75.50,30,10,1,4.8\n" +
            "9,400.30,5,0,0,2.1\n" +
            "10,50.00,0,null,1,5.0\n";

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

    /*
    Here is a list of tasks you can perform on the **products dataset**:

---

### **1. Data Inspection and Cleaning**
1. **Inspect the dataset**:
   - View the first and last few rows (`head` and `tail`) to understand the structure and data.
   - Display the total number of rows and columns.
2. **Check for missing values**:
   - Identify if any row contains `null` values or inconsistencies (e.g., negative values for `Price` or `Stock`).
3. **Remove duplicates**:
   - Check if there are duplicate rows (e.g., products with the same `ProductID` or identical attributes) and remove them.

---
*/

    @Test
    void testClean()
    {
        df.filter(Filter.head(2)).print();
        df.filter(Filter.tail(2)).print();
        df.printStatistics();


        df.filter(Filter.hasNan()).print();

        boolean duplicated = df.getColumn("ProductID").unique().size() == df.getColumn("ProductID").size();

        List<Integer> duplicates = df.find(Filter.rowTest(row -> df.getColumn("ProductID").count(row.get("ProductID")) > 1));

        List<Double> scanned = new ArrayList<>();

        List<Integer> toRemove = new ArrayList<>();


        for(Integer idx : duplicates)
        {
            double id = df.getRow(idx).get("ProductID");
            if(!scanned.contains(id))
                scanned.add(id);
            else
                toRemove.add(idx);
        }
        df.drop(Filter.rowTest(row -> toRemove.contains(row.getIndex()))).print();
    }

    /*

### **2. Statistical Analysis**
1. **Summary statistics**:
   - Calculate basic statistics for each column (`Price`, `Discount`, `Stock`, `Rating`), such as:
     - Mean, median, standard deviation, min, max.
2. **Correlation analysis**:
   - Compute the correlation between features, e.g.:
     - Is there a relationship between `Price` and `Rating`?
     - Does a high `Discount` correlate with low `Stock`?

---
*/


    @Test
    void testSummary()
    {
        df.printStatistics();
    }


    /*

### **4. Data Transformation**
1. **Create derived features**:
   - Calculate `DiscountedPrice` = `Price` - (`Price` × `Discount` / 100).
   - Create a binary feature `OutOfStock` where:
     - 1 = Stock is 0.
     - 0 = Stock is greater than 0.
   - Normalize `Price` and `Rating` to scale them between 0 and 1.
2. **Bin and categorize**:
   - Create price brackets such as "Low", "Medium", "High" based on the range of `Price`.
   - Bin products into rating categories:
     - 0-2 = "Poor", 2-4 = "Average", 4-5 = "Excellent".

*/

    double priceBracket(double price)
    {
        if(price < 100)
            return 0;
        else if(price < 200)
            return 1;
        else
            return 2;
    }

    @Test
    void testTransform()
    {
        df.setColumn("DiscountedPrice", df.getColumn("Price").zip(df.getColumn("Discount"), (price, discount) -> price - (price * discount / 100.0)));
        df.setColumn("OutOfStock", df.getColumn("Stock").map(stock -> stock == 0 ? 1 : 0));
        df.setColumn("PriceNorm", df.getColumn("Price").normalized());
        df.setColumn("RatingNorm", df.getColumn("Rating").normalized());
        df.setColumn("PriceBracket", df.getColumn("Price").map(this::priceBracket));
        df.print();
    }

    /*

---

### **5. Filtering and Subsetting**
1. **Filter by criteria**:
   - Products with high discounts (`Discount >= 20`).
   - Products with a high rating (`Rating >= 4`).
   - Products currently out of stock (`Stock = 0`).
   - Products priced above a specific value (e.g., `Price > 200`).
2. **Sort the products**:
   - Sort by `Price`, `Discount`, or `Rating` (ascending or descending).
3. **Find specific cases**:
   - Products with highest and lowest prices or highest ratings.

*/

    @Test
    void testFilter()
    {
        df.filter(Filter.rowTest(row -> row.get("Discount") >= 20)).print();
        df.filter(Filter.rowTest(row -> row.get("Rating") >= 4)).print();
        df.filter(Filter.rowTest(row -> row.get("Stock") == 0)).print();
        df.filter(Filter.rowTest(row -> row.get("Price") > 200)).print();
        df.sort(true, Sorter.sortByColumns(true, "Price")).print();
        df.sort(true, Sorter.sortByColumns(true, "Discount")).print();
        df.sort(true, Sorter.sortByColumns(true, "Rating")).print();
        df.filter(Filter.rowTest(row -> row.get("Price") == df.getColumn("Price").max())).print();
        df.filter(Filter.rowTest(row -> row.get("Price") == df.getColumn("Price").min())).print();

    }

    /*

---

### **6. Grouping and Aggregation**
1. **Group by target**:
   - Find the average `Price`, `Discount`, and `Rating` for `Target = 1` vs. `Target = 0`.
2. **Analyze stock levels**:
   - Group by `OutOfStock` and calculate the average `Rating` or total `Price`.
3. **Explore by rating categories**:
   - Aggregate statistics like mean `Price` or sum of `Stock` by custom rating bins (e.g., "Poor", "Average", "Excellent").
*/

    @Test
    void testGroup()
    {
        df.drop(Filter.hasNan())
                .groupAggregate(
                        Grouper.groupByRow(row -> row.get("Target")),
                        new Aggregator()
                                .addAggregation("Target", df -> df.getColumn("Target").average())
                                .addAggregation("Count", df -> df.size())
                                .addAggregation("Avg. Price", df -> df.getColumn("Price").average())
                                .addAggregation("Sum. Price", df -> df.getColumn("Price").sum())
                                .addAggregation("Avg. Discount", df -> df.getColumn("Discount").average())
                                .addAggregation("Sum. Discount", df -> df.getColumn("Discount").sum())
                                .addAggregation("Avg. Rating", df -> df.getColumn("Rating").average())
                                .addAggregation("Sum. Rating", df -> df.getColumn("Rating").sum())
                ).print();
    }

    /*
---

### **7. Outlier Detection**
1. **Identify outliers**:
   - Use box plots or z-scores to detect outliers in `Price`, `Stock`, or `Rating`.
   - Identify extreme discounts (e.g., `Discount > 50%`).
2. **Handle outliers**:
   - Decide whether to remove or cap unusually high/low values (e.g., absurdly high prices, negative discounts).

---

### **8. Machine Learning Tasks**
1. **Regression tasks**:
   - Predict `Rating` based on features like `Price`, `Discount`, and `Stock`.
2. **Classification tasks**:
   - Predict `Target` based on numerical features such as `Price`, `Stock`, and `Discount`.

---

### **9. Filtering Business Insights**
1. **Top-performing products**:
   - Find the top-N products based on `Rating` and `Price`.
2. **Low-performing products**:
   - Identify poorly rated products with high stock or low discounts.
3. **Inventory recommendations**:
   - Identify products with low stock and high demand.

---

### **10. Comparison Tasks**
1. **Compare products by `Target` value**:
   - Are the prices of `Target = 1` products significantly different from `Target = 0` products?
2. **Analyze discounted vs. non-discounted products**:
   - What is the average rating for products with discounts vs. those without?

---

Let me know if you want to focus on any particular task and I can help elaborate or implement it!
     */
}
