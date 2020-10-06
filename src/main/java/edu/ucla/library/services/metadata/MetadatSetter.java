
package edu.ucla.library.services.metadata;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

public final class MetadatSetter {

  /**
   * Path to CSV file (or directory of CSV files) to be updated.
  */
    private static String CSV_PATH;

  /**
   * Path to media files to be read.
  */
    private static String MEDIA_PATH;

  /**
   * Path to ffmpeg probe utility.
  */
    private static String FFMPEG_PATH;

  /**
   * Path where modified CSV file(s) will be written.
  */
    private static String OUTPUT_PATH;

  /**
   * Private constructor for MetadataSetter class.
  */
    private MetadatSetter() {
    }

  /**
   * Main method for command-line execution.
   *
   * @param args array of parameters
  */
    @SuppressWarnings("uncommentedmain")
    public static void main(final String[] args) {
        CSV_PATH = args[0];
        MEDIA_PATH = args[1];
        FFMPEG_PATH = args[2];
        OUTPUT_PATH = args[3];

        try {
            final Path basePath;
            basePath = FileSystems.getDefault().getPath(CSV_PATH);
            if (Files.isDirectory(basePath)) {
                Files.find(Paths.get(CSV_PATH), Integer.MAX_VALUE,
                    (filePath, fileAttr) -> fileAttr.isRegularFile()
                     && filePath.toFile()
                                .getName()
                                .endsWith("csv"))
                    .forEach(path -> addMetaToCsv(path));
            } else if (Files.isRegularFile(basePath)) {
                addMetaToCsv(basePath);
            }
        } catch (IOException details) {
            System.err.println("Problem reading file/walking file directory: "
                         + details.getMessage());
        }
    }

  /**
   * Method to process CSV file(s) to add columns for media metadata.
   *
   * @param aPath Path to file to be read and copied/updated
  */
    private static void addMetaToCsv(final Path aPath) {
        final CSVReader reader;
        final CSVWriter writer;
        final List<String[]> input;
        final List<String[]> output;

        try {
            reader = new CSVReader(new FileReader(aPath.toFile()));
            input = reader.readAll();
            reader.close();

            output = new ArrayList<>(input.size());
            output.add(buildHeaderRow(input.get(0)));

            for (int index = 1; index < input.size(); index++) {
                output.add(buildARow(input.get(index)));
            }

            writer = new CSVWriter(new FileWriter(new File(
               OUTPUT_PATH.concat(buildOutputName(
               aPath.toFile().getName())))));
            for (final String[] aLine: output) {
                writer.writeNext(aLine);
            }

            writer.close();
        } catch (IOException details) {
            System.err.println("Problem reading/writing csv file: "
                         + details.getMessage());
        }
    }

  /**
    * Method to copy base headers and add new columns.
    *
    * @param aSource The original headers from the source file.
    * @return Array of header names.
  */
    private static String[] buildHeaderRow(final String... aSource) {
        final String[] headers;

        headers = Arrays.copyOf(aSource, aSource.length + 4);

        headers[headers.length - 4] = "media.width";
        headers[headers.length - 3] = "media.height";
        headers[headers.length - 2] = "media.duration";
        headers[headers.length - 1] = "media.format";

        return headers;
    }

  /**
    * Method to copy/modify data rows from sourve file.
    *
    * @param aSource The original row from the source file.
    * @return The modified CSV row.
  */
    private static String[] buildARow(final String... aSource) {
        final String[] aLine;

        aLine = Arrays.copyOf(aSource, aSource.length + 4);

        if (aLine[6].contains(".") && !aLine[6].contains("~")) {
            addMetaData(aLine);
        }

        return aLine;
    }

  /**
    * Method to extract metadata from media file and add to output row.
    *
    * @param aRow The row from the output file.
  */
    private static void addMetaData(final String... aRow) {
        final FFprobe ffprobe;
        final FFmpegProbeResult probeResult;
        final FFmpegFormat format;

        try {
            ffprobe = new FFprobe(FFMPEG_PATH);
            probeResult = ffprobe.probe(MEDIA_PATH.concat(getMediaFileName(aRow[6])));
            format = probeResult.getFormat();

            aRow[aRow.length - 2] = String.valueOf(format.duration);
            aRow[aRow.length - 1] = format.format_name;

            if (probeResult.getStreams() != null) {
                for (int index = 0; index < probeResult.getStreams().size(); index++) {
                    final FFmpegStream stream;
                    stream = probeResult.getStreams().get(index);
                    if (stream.width != 0) {
                        aRow[aRow.length - 4] = String.valueOf(stream.width);
                    }
                    if (stream.height != 0) {
                        aRow[aRow.length - 3] = String.valueOf(stream.height);
                    }
                }
            }
        } catch (IOException details) {
            System.err.println("Problem reading media file: "
                               + details.getMessage());
        }
    }

  /**
    * Method to extract media file name from CSV column.
    *
    * @param aSource The original column entry from the source file.
    * @return Name of the media file
  */
    private static String getMediaFileName(final String aSource) {
        final StringBuilder mediaFile;

        if (aSource.contains(File.separator)) {
            mediaFile = new StringBuilder(aSource
                      .substring(aSource.lastIndexOf(File.separator) + 1));
        } else {
            mediaFile = new StringBuilder(aSource);
        }

        return mediaFile.toString();
    }

  /**
    * Method to generate name for output file.
    *
    * @param aInputName The original name of the source file.
    * @return The name of the output file.
  */
    private static String buildOutputName(final String aInputName) {
        return new StringBuilder(aInputName)
              .insert(aInputName.lastIndexOf(".csv"), ".meta").toString();
    }

}
