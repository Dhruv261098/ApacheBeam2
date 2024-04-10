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
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;
import org.apache.beam.sdk.extensions.avro.io.AvroIO;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;


public class MockDataTest2 {

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
        inputRecord.put("price", 100L); 
        
        GenericRecord expectedOutputRecord = new GenericData.Record(schema);
        expectedOutputRecord.put("tweet", "This tweet mentions scissors");
        expectedOutputRecord.put("price", 500L);
        
        PCollection<GenericRecord> input = pipeline.apply(Create.of(inputRecord));
        
        PCollection<GenericRecord> output = input.apply(ParDo.of(new CalculateTotalPriceFn(100))) // Set price for testing
        .apply(ParDo.of(new MockData.MultiplyPriceFn())) // Continue with the multiplication
        .apply(ParDo.of(new MockData.FilterTweetsFn())); // Apply filtering        
        PAssert.that(output).containsInAnyOrder(expectedOutputRecord);
        
        pipeline.run().waitUntilFinish();
    }

    public static class CalculateTotalPriceFn extends DoFn<GenericRecord, GenericRecord> {
        private final long priceToSet;
    
        public CalculateTotalPriceFn(long priceToSet) {
            this.priceToSet = priceToSet;
        }
    
        @ProcessElement
        public void processElement(@Element GenericRecord input, OutputReceiver<GenericRecord> out) {
            GenericRecord outputRecord = new GenericData.Record(input.getSchema());
    
            for (Schema.Field field : input.getSchema().getFields()) {
                outputRecord.put(field.name(), input.get(field.name()));
            }
    
            // Set the price to a specific value for testing
            outputRecord.put("price", this.priceToSet);
    
            out.output(outputRecord);
        }
    }
    
}



