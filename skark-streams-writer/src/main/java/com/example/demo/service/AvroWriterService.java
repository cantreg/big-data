package com.example.demo.service;

import com.example.demo.dto.Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.net.URI;

@Slf4j
public class AvroWriterService {

    private static volatile DataFileWriter<Message> dataFileWriter;

    public static DataFileWriter<Message> getDataFileWriter() {
        if (dataFileWriter == null) {
            synchronized (SparkService.class) {
                if (dataFileWriter == null) {
                    try {
                        DatumWriter<Message> messageDatumWriter = new SpecificDatumWriter<>(Message.class);
                        dataFileWriter = new DataFileWriter<>(messageDatumWriter);
                        dataFileWriter.setFlushOnEveryBlock(true);
                        String pathConfString = SparkService.getAvroPath();
                        FileSystem fileSystem = FileSystem.get(URI.create(pathConfString), new Configuration());
                        Path path = new Path(pathConfString);
                        if (fileSystem.exists(path)) {
                            FSDataOutputStream fsDataOutputStream = fileSystem.append(path);
                            FSDataInputStream fsDataInputStream = fileSystem.open(path);
                            long fileSize = fileSystem.getFileStatus(path).getLen();
                            dataFileWriter.appendTo(new AvroFSInput(fsDataInputStream, fileSize), fsDataOutputStream);
                        } else {
                            FSDataOutputStream fsDataOutputStream = fileSystem.create(path);
                            dataFileWriter.create(Message.SCHEMA$, fsDataOutputStream);
                        }
                    } catch (Exception e) {
                        log.error("HDFS AVRO file writer create fail", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return dataFileWriter;
    }

    @SneakyThrows
    public static void write(Message message) {
        try {
            log.info("Append message to avro: {}", message);
            getDataFileWriter().append(message);
        } catch (Exception e) {
            log.error("AVRO file write fail", e);
        }
    }

    public static void setDataFileWriter(DataFileWriter<Message> dataFileWriter) {
        synchronized (SparkService.class) {
            AvroWriterService.dataFileWriter = dataFileWriter;
        }
    }

}
