package org.loony.dataflow;

import org.loony.dataflow.ReadAvro.FilterTweetsFn;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.extensions.avro.io.AvroIO;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.io.File;
import java.io.IOException;

import org.apache.avro.Schema;

@RunWith(JUnit4.class)
public class ReadAvroTest {

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    @Test
    public void testFilterTweetsFn() {

        Schema schema = null;
        try {
            schema = new Schema.Parser().parse(new File("src/test/resources/schema.avsc"));
        } catch (IOException e){
            System.err.println("Error reading Avro schema file: " + e.getMessage());
            e.printStackTrace();   
            return; 
        }

        GenericRecord tweet1 = new GenericData.Record(schema);
        tweet1.put("username", "user1");
        tweet1.put("tweet", "This contains Scissors");
        tweet1.put("timestamp", 123456789L);

        GenericRecord tweet2 = new GenericData.Record(schema);
        tweet2.put("username", "user2");
        tweet2.put("tweet", "This does not");
        tweet2.put("timestamp", 987654321L);

        PCollection<GenericRecord> input = pipeline.apply(Create.of(tweet1, tweet2).withCoder(AvroCoder.of(schema)));

        PCollection<GenericRecord> filteredTweets = input.apply(ParDo.of(new FilterTweetsFn()));

        PAssert.that(filteredTweets).containsInAnyOrder(tweet1);
        pipeline.run().waitUntilFinish();
    }


    @Test
    public void testavrodatawithexpectedoutput() throws IOException {

        Schema schema = null;
        try {
            schema = new Schema.Parser().parse(new File("src/test/resources/schema.avsc"));
        } catch (IOException e){
            System.err.println("Error reading Avro schema file: " + e.getMessage());
            e.printStackTrace();   
            return; 
        }        
    
        String expectedOutputPath = "gs://my_dataflow_bucket_2610/output_data/twitter.avro-00001-of-00002.avro";

        PCollection<GenericRecord> expectedOutput = pipeline.apply("ReadExpectedOutput",
            AvroIO.readGenericRecords(schema).from(expectedOutputPath));

        GenericRecord tweet1 = new GenericData.Record(schema);
        tweet1.put("username", "miguno");
        tweet1.put("tweet", "Rock: Nerf paper, scissors is fine.");
        tweet1.put("timestamp", 1366150681);

        PAssert.that(expectedOutput).containsInAnyOrder(tweet1);
        pipeline.run().waitUntilFinish();
}



}
