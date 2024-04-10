    package org.loony.dataflow;


    import org.apache.beam.sdk.Pipeline;
    import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;
    import org.apache.beam.sdk.extensions.avro.io.AvroIO;
    import org.apache.beam.sdk.options.PipelineOptions;
    import org.apache.beam.sdk.options.PipelineOptionsFactory;
    import org.apache.beam.sdk.transforms.DoFn;
    import org.apache.beam.sdk.transforms.ParDo;
    import org.apache.beam.sdk.values.PCollection;
    import org.apache.avro.Schema;
    import org.apache.avro.generic.GenericData;
    import org.apache.avro.generic.GenericRecord;

    import java.io.File;
    import java.io.IOException;
    import java.util.UUID;



    public class MockData {
            public static void main(String[] args) {
                PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();

                runProductDetails(options);

            }


            static void runProductDetails(PipelineOptions options) {
            
                Pipeline p = Pipeline.create(options);
        
        
                String inputAvroFile = "gs://my_dataflow_bucket_2610/input_data/twitter3.avro";
                String outputAvroFile = "gs://my_dataflow_bucket_2610/output_data/twitter3.avro";


                Schema schema = null;
                try {
                    schema = new Schema.Parser().parse(new File("src/main/resources/schema2.avsc"));
                } catch (IOException e){
                    System.err.println("Error reading Avro schema file: " + e.getMessage());
                    e.printStackTrace();   
                    return; 
                }


                PCollection<GenericRecord> records = p.apply("ReadFromAvro", AvroIO.readGenericRecords(schema).from(inputAvroFile)).setCoder(AvroCoder.of(schema))
                                                    .apply(ParDo.of(new CalculateTotalPriceFn()))
                                                    .apply(ParDo.of(new MultiplyPriceFn()))
                                                    .apply(ParDo.of(new FilterTweetsFn()));

                records.apply("WriteToAvro", AvroIO.writeGenericRecords(schema).to(outputAvroFile).withSuffix(".avro"));


                p.run().waitUntilFinish();
            }
            
            public static class FilterTweetsFn extends DoFn<GenericRecord, GenericRecord> {
                @ProcessElement
                public void processElement(ProcessContext c) {
                GenericRecord record = c.element();
                String tweet = record.get("tweet").toString();
                if (tweet.contains("scissors")) {
                    c.output(record);
                    }
                }
            }

            public static class CalculateTotalPriceFn extends DoFn<GenericRecord, GenericRecord> {
                @ProcessElement
                public void processElement(@Element GenericRecord input, OutputReceiver<GenericRecord> out) {

                    GenericRecord outputRecord = new GenericData.Record(input.getSchema());
            
                    for (Schema.Field field : input.getSchema().getFields()) {
                        outputRecord.put(field.name(), input.get(field.name()));
                    }
            
                    long originalPrice = (long) input.get("price");
                    long updatedPrice = originalPrice + (long) (originalPrice * 0.13);
            
                    outputRecord.put("price", updatedPrice);
            
                    out.output(outputRecord);
                }
            }     

            public static class MultiplyPriceFn extends DoFn<GenericRecord, GenericRecord> {
                @ProcessElement
                public void processElement(@Element GenericRecord input, OutputReceiver<GenericRecord> out) {

                    GenericRecord outputRecord = new GenericData.Record(input.getSchema());
            
                    for (Schema.Field field : input.getSchema().getFields()) {
                        outputRecord.put(field.name(), input.get(field.name()));
                    }
            
                    long originalPrice = (long) input.get("price");
                    long updatedPrice = originalPrice * 5;
            
                    outputRecord.put("price", updatedPrice);
            
                    out.output(outputRecord);
                }
            }              


    }

        


            