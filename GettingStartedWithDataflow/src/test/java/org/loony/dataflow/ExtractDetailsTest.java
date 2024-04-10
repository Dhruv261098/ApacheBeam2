package org.loony.dataflow;

import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.io.TextIO;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import java.util.Arrays; // Import statement for Arrays



@RunWith(JUnit4.class)
public class ExtractDetailsTest {

    @Rule
    public final transient TestPipeline testPipeline = TestPipeline.create();

    @Test
    public void testEntirePipeline() {
        // Define your input data and expected output
        final String[] inputLines = new String[]{
            "Order_ID,Product,Quantity_Ordered,Price_Each,Order_Date,Purchase_Address",
            "176560,Google Phone,1,600,04/12/19 14:38,669 Spruce St, Los Angeles, CA 90001",
            "176561,Wired Headphones,1,11.99,04/12/19 14:38,669 Spruce St, Los Angeles, CA 90001",
            "176561,Wired Headphones,2,12,04/12/19 14:38,669 Spruce St, Los Angeles, CA 90001",



        };
        final String[] expectedOutput = new String[]{
            "Google Phone,600.0,04/12/19 14:38",
            "Wired Headphones,11.99,04/12/19 14:38",
            "Wired Headphones,24.0,04/12/19 14:38"


        };

        PCollection<String> input = testPipeline.apply("CreateInput", Create.of(Arrays.asList(inputLines)).withCoder(StringUtf8Coder.of()));

        PCollection<String> output = input
            .apply("FilterHeader", ParDo.of(new ExtractDetails.FilterHeaderFn(ExtractDetails.CSV_HEADER)))
            .apply("ExtractDetails", ParDo.of(new ExtractDetails.ExtractSalesDetailsFn()));

        PAssert.that(output).containsInAnyOrder(expectedOutput);

        testPipeline.run().waitUntilFinish();

    }


    @Test
    public void testMalformedData() {
        final String[] inputLines = {
            ExtractDetails.CSV_HEADER,
            "176562,USB-C Charging Cable,,11.95,04/12/19 14:38,672 Spruce St, Los Angeles, CA 90001", // Missing Quantity_Ordered
            "176563,Vareebadd Phone,1,,04/12/19 14:38,673 Spruce St, Los Angeles, CA 90001" // Missing Price_Each

        };
        final String[] expectedOutput = {
            // Since both rows are malformed and we're skipping them, expect no output.
        };

        PCollection<String> input = testPipeline.apply("CreateInputForMalformedData", Create.of(Arrays.asList(inputLines)).withCoder(StringUtf8Coder.of()))
            .apply("FilterHeaderForMalformedData", ParDo.of(new ExtractDetails.FilterHeaderFn(ExtractDetails.CSV_HEADER)))
            .apply("ExtractDetailsForMalformedData", ParDo.of(new ExtractDetails.ExtractSalesDetailsFn()));

        PAssert.that(input).empty();

        testPipeline.run().waitUntilFinish();
    }
}
