package io.github.lightdataframe.lightweightdataframe.tests;

import io.github.lightdataframe.lightweightdataframe.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Example6Test
{
    String csv = "ReceiptID,RoomTypeCode,NumGuests,Nights,TotalAmount,DiscountApplied,PaymentMethodCode\n" +
            "1,101,2,3,450.00,-50.00,1\n" +
            "2,102,3,5,750.00,0.00,2\n" +
            "3,103,1,1,120.00,-10.00,0\n" +
            "4,104,4,2,1200.00,-200.00,3\n" +
            "5,101,2,4,600.00,0.00,1\n" +
            "6,103,1,7,850.00,-150.00,2\n" +
            "7,104,5,3,2300.00,-500.00,3\n" +
            "8,102,3,1,300.00,-20.00,1\n" +
            "9,103,2,2,240.00,0.00,0\n" +
            "10,104,4,4,1800.00,-350.00,3\n" +
            "11,102,3,6,1700.00,-100.00,1\n" +
            "12,101,1,5,750.00,-50.00,2\n" +
            "13,103,1,10,1200.00,-250.00,0\n" +
            "14,102,2,2,375.00,-25.00,1\n" +
            "15,104,4,8,3200.00,-600.00,3\n";

    Dataframe df;


    @BeforeEach
    void initialize()
    {
        df = Serializer.fromCsv(true, csv);
        df.setColumn("Index", df.apply(Mapper.mapByIndex(i -> i)));
        df.reorderColumns("Index");
    }

    @Test
    void print()
    {
        df.print();
    }
    /*
    Here is a list of example tasks you could perform on the dataset to gain insights or apply relevant data processing techniques. These tasks range from basic exploration to more advanced analytics:

---

### **Exploratory Data Analysis (EDA):**
1. **Summarize the dataset:**
   - Calculate the mean, median, and standard deviation of key numerical features (`TotalAmount`, `Nights`, `NumGuests`, `DiscountApplied`).
   - Find the count of distinct `RoomTypeCode` and `PaymentMethodCode` values.
2. **Distribution analysis:**
   - Plot histograms or bar charts for `TotalAmount`, `DiscountApplied`, `Nights`, and `NumGuests`.
   - Analyze the distribution of `RoomTypeCode` and `PaymentMethodCode`.

*/
    @Test
    void eda()
    {
        df.printStatistics();
        double size = df.size();
        df.groupAggregate(
                        Grouper.groupByColumnValues("PaymentMethodCode"),
                        new Aggregator()
                                .addAggregation("PaymentMethodCode", df -> df.getColumn("PaymentMethodCode").average())
                                .addAggregation("frequency", df -> df.size() / size)
                )
                .sort(true, Sorter.sortByColumns(true, "PaymentMethodCode"))
                .print();
        df.groupAggregate(
                        Grouper.groupByColumnValues("RoomTypeCode"),
                        new Aggregator()
                                .addAggregation("RoomTypeCode", df -> df.getColumn("RoomTypeCode").average())
                                .addAggregation("frequency", df -> df.size() / size)
                )
                .sort(true, Sorter.sortByColumns(true, "RoomTypeCode"))
                .print();
    }

    /*
---

### **Data Cleaning:**
3. **Check for missing or inconsistent data:**
   - Validate that all columns have proper values and no null entries.
   - Ensure `DiscountApplied` values are negative or zero but not positive.
4. **Detect outliers:**
   - Use box plots or z-scores to identify extreme outliers in `TotalAmount` or `Nights`.

*/

    @Test
    void testClean()
    {
        df.filter(Filter.hasNan()).print();
        df.filter(Filter.rowTest(row -> row.get("DiscountApplied") > 0)).print();

        df.setColumn("TotalAmountZScore", df.apply(Mapper.mapByColumn("TotalAmount", v -> v)).zscore()).print();

        df.setColumn("Outliers", df.apply(Mapper.mapByColumn("TotalAmountZScore", v -> Math.abs(v) > 1 ? 1 : 0))).print();

    }

    /*

---

### **Data Aggregation:**
5. **Calculate total revenue and discounts:**
   - Sum up `TotalAmount` and `DiscountApplied` across all receipts.
6. **Group performance analysis:**
   - Calculate total revenue per `RoomTypeCode` or `PaymentMethodCode`.
   - Determine the most frequent `RoomTypeCode` and `PaymentMethodCode`.
   - Calculate the average `TotalAmount` for different room types and nights of stay.
7. **Analyze guest groups:**
   - Find the average number of guests (`NumGuests`) per receipt grouped by `RoomTypeCode`.
*/


    @Test
    void testAggregate()
    {
        double totalAmountSum = df.getColumn("TotalAmount").sum();
        double discountAppliedSum = df.getColumn("DiscountApplied").sum();

        df.print();

        df.groupAggregate(
                Grouper.groupByColumnValues("RoomTypeCode", "PaymentMethodCode"),
                new Aggregator()
                        .addAggregation("PaymentMethodCode", df -> df.getColumn("PaymentMethodCode").average())
                        .addAggregation("RoomTypeCode", df -> df.getColumn("RoomTypeCode").average())
                        .addAggregation("TotalAmount", df -> df.getColumn("TotalAmount").sum())
                        .addAggregation("DiscountApplied", df -> df.getColumn("DiscountApplied").sum())
        )
                .sort(true, Sorter.sortByColumns(true, "PaymentMethodCode", "RoomTypeCode"))
                .print();

        double size = df.size();
        Dataframe df0 = df.groupAggregate(
                Grouper.groupByColumnValues("RoomTypeCode"),
                new Aggregator()
                        .addAggregation("RoomTypeCode", df -> df.getColumn("RoomTypeCode").average())
                        .addAggregation("Frequency", df -> df.size() / size)
                        .addAggregation("NumGuests", df -> df.getColumn("NumGuests").average())
        );

        System.out.println("Max frequency index=" + df0.getColumn("Frequency").argmax());
        df0.print();
    }


    /*
---

### **Visual Analysis:**
8. **Create visualizations:**
   - Plot a bar graph showing the total revenue (`TotalAmount`) for each `RoomTypeCode`.
   - Create a pie chart to show the usage share of different `PaymentMethodCode` values.
   - Plot the relationship between `NumGuests` and `TotalAmount`.

---

### **Advanced Analytics:**
9. **Predictive modeling:**
   - Predict the `TotalAmount` based on features like `RoomTypeCode`, `NumGuests`, and `Nights` (regression analysis).
   - Predict the likelihood of a specific payment method (`PaymentMethodCode`) being selected based on the `RoomTypeCode` and `NumGuests` (classification task).
10. **Discount impact analysis:**
    - Determine how `DiscountApplied` affects `TotalAmount` across different room types.
11. **Stay duration vs spending:**
    - Analyze the correlation between the number of nights stayed (`Nights`) and the total bill (`TotalAmount`).

*/

    @Test
    void testPredictive()
    {
        df.print();
        Series uniquePaymentMethods = df.getColumn("PaymentMethodCode").unique().sort(v -> v);
        //df.group(Grouper.groupByColumnValues("RoomTypeCode", "NumGuests"));
        Aggregator agg = new Aggregator();
        agg.addAggregation("RoomTypeCode", df -> df.getColumn("RoomTypeCode").average());
        agg.addAggregation("NumGuests", df -> df.getColumn("NumGuests").average());

        for(Double pm : uniquePaymentMethods)
            agg.addAggregation("pm_"+pm, df -> df.getColumn("PaymentMethodCode").count(pm) / df.size());

        df.groupAggregate(
                Grouper.groupByColumnValues("RoomTypeCode", "NumGuests"),
                agg)
                .sort(true, Sorter.sortByColumns(true, "RoomTypeCode", "NumGuests"))
                .print();

        df.setColumn("DiscountPerc", df.apply(Mapper.mapByRow(row -> 1 - (row.get("TotalAmount") + row.get("DiscountApplied")) / row.get("TotalAmount" ))))
                .print();

        df.groupAggregate(
                Grouper.groupByColumnValues("RoomTypeCode"),
                new Aggregator()
                        .addAggregation("RoomTypeCode", df -> df.getColumn("RoomTypeCode").average())
                        .addAggregation("AvgDiscount", df -> df.getColumn("DiscountPerc").average())
        ).print();

    }

    /*

---

### **Clustering and Segmentation:**
12. **Customer segmentation:**
    - Cluster receipts into groups (e.g., small vs. large spenders) based on `TotalAmount`, `Nights`, and `NumGuests` using k-means clustering or other clustering algorithms.
13. **Room popularity analysis:**
    - Identify clusters of high-demand room types based on `RoomTypeCode` and calculate the most common features for those room types (average `Nights` or `NumGuests`).

---

### **Optimization Tasks:**
14. **Maximize discounts:**
    - Calculate the optimal discount values to incentivize longer stays or larger groups while maintaining profitability.
15. **Identify underperforming features:**
    - Find room types (`RoomTypeCode`) or payment methods (`PaymentMethodCode`) that generate the least revenue and recommend potential optimizations.

---

### **Real-world Simulation Scenarios:**
16. **Revenue forecast:**
    - Simulate how revenue changes with additional variables like seasonal demands or varying room booking rates.
17. **Discount scenarios:**
    - Simulate the impact of an increased or reduced discount policy on overall revenue (`TotalAmount`).

---

### **Data Transformation:**
18. **Normalize data:**
    - Apply feature scaling (standardization or normalization) for features like `Nights`, `TotalAmount`, and `DiscountApplied` to prepare for predictive modeling or clustering tasks.
19. **Add calculated columns:**
    - Add a column for the **EffectiveAmount** (`TotalAmount` + `DiscountApplied`) for accurate revenue measurement.
    - Create a feature to calculate **Revenue per Night** (TotalAmount / Nights).

*/

    @Test
    void testTransform()
    {
        df
                .setColumn("EffectiveAmount", df.apply(Mapper.mapByRow(row -> row.get("TotalAmount") + row.get("DiscountApplied"))))
                .setColumn("RevenuePerNight", df.apply(Mapper.mapByRow(row -> row.get("TotalAmount") / row.get("Nights"))))
                .print();
    }

    /*

These tasks should provide a great starting point for analysis, data processing, and applying statistical or machine learning techniques to the dataset. Let me know if you want to explore any specific task in detail!
     */
}
