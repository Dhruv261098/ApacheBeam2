package org.loony.dataflow;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.values.PCollection;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

public class PipelineTestUtils {

    /**
     * Reads pipeline output from a specified Avro file into a PCollection.
     * This simulates running the pipeline without the FilterTweetsFn transformation,
     * using pre-generated data.
     * 
     * @param pipeline The Beam pipeline to use for creating the PCollection.
     * @param outputPath The path to the Avro file containing the pipeline's output data.
     * @param schema The Avro schema corresponding to the data in the file.
     * @return A PCollection<GenericRecord> containing the data from the Avro file.
     */
    public static PCollection<GenericRecord> readPipelineOutput(
            Pipeline pipeline, String outputPath, Schema schema) {
        return pipeline.apply("ReadPipelineOutput",
                AvroIO.readGenericRecords(schema).from(outputPath));
    }
}
