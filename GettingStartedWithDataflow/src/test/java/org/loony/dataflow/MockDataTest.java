package org.loony.dataflow;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;


public class MockDataTest {

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    @Test
    public void testFilterTweetsFn() throws Exception {

        Schema schema = null;
        try {
            schema = new Schema.Parser().parse(new File("src/test/resources/schema2.avsc"));
        } catch (IOException e){
            System.err.println("Error reading Avro schema file: " + e.getMessage());
            e.printStackTrace();   
            return; 
        }
        
        GenericRecord inputRecord = new GenericData.Record(schema);
        inputRecord.put("tweet", "This tweet mentions scissors");
        inputRecord.put("price", 113L); // Assume this is the price after CalculateTotalPriceFn
        
        GenericRecord expectedOutputRecord = new GenericData.Record(schema);
        expectedOutputRecord.put("tweet", "This tweet mentions scissors");
        expectedOutputRecord.put("price", 113L);
        
        PCollection<GenericRecord> input = pipeline.apply(Create.of(inputRecord));
        
        PCollection<GenericRecord> output = input.apply("FilterTweets", ParDo.of(new MockData.FilterTweetsFn()));
        
        PAssert.that(output).containsInAnyOrder(expectedOutputRecord);
        
        pipeline.run().waitUntilFinish();
    }
}



