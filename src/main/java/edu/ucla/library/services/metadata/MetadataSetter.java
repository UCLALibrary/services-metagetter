
package edu.ucla.library.services.metadata;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

//import java.io.File;
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
import java.util.concurrent.Callable;
//import java.util.stream.Stream;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;

public final class MetadataSetter implements Callable<Integer> {

  /**
   * File/directory doesn't exist.
  */
    public static final int FILE_DOESNT_EXIST = 101;

  /**
   * ffprobe executable doesn't exist.
  */
    public static final int PROBE_DOESNT_EXIST = 102;

  /**
   * Constant for width column name.
  */
    private static final String HEADER_WIDTH = "media.width";

  /**
   * Constant for height column name.
  */
    private static final String HEADER_HEIGHT = "media.height";

  /**
   * Constant for duration column name.
  */
    private static final String HEADER_DURATION = "media.duration";

  /**
   * Constant for format column name.
  */
    private static final String HEADER_FORMAT = "media.format";

  /**
   * Constant for column position of media.width.
  */
    private static final int WIDTH_OFFSET = 4;

  /**
   * Constant for column position of media.height.
  */
    private static final int HEIGHT_OFFSET = 3;

  /**
   * PConstant for column position of media.duration.
  */
    private static final int DURATION_OFFSET = 2;

  /**
   * Constant for column position of media.format.
  */
    private static final int FORMAT_OFFSET = 1;


  /**
   * Path to CSV file (or directory of CSV files) to be updated.
  */
    @Parameters(index = "0", description = "The CSV file/directory to process.")
    private static String CSV_PATH;

  /**
   * Path to media files to be read.
  */
    @Parameters(index = "1", description = "The parent directory/mount point holding media files;"
        + " this is prepended to file name in CSV.  If file name in CSV is /ephraim/audio/audio.wav, and"
        + " full path to audio.wav is /mount/dlcs/masters/ephraim/audio/audio.wav, enter /mount/dlcs/masters/")
    private static String MEDIA_PATH;

  /**
   * Path to ffmpeg probe utility.
  */
    @Parameters(index = "2", description = "The path to ffprobe executable.")
    private static String FFMPEG_PATH;

  /**
   * Path where modified CSV file(s) will be written.
  */
    @Parameters(index = "3", description = "The directory where output file(s) are written.")
    private static String OUTPUT_PATH;

  /**
   * Private constructor for MetaDataSetter class.
  */
    private MetadataSetter() {
    }

  /**
   * Main method for command-line execution.
   *
   * @param args array of parameters
  */
    @SuppressWarnings("uncommentedmain")
    public static void main(final String[] args) {
        final int exitCode = new CommandLine(new MetadataSetter()).execute(args);
        System.exit(exitCode);
    }

  /**
   * Callable method executed by picoli framework.
   *
  */
    @Override
    public Integer call() {
        if (!fileDirExists(CSV_PATH)) {
            return FILE_DOESNT_EXIST;
        }
        if (!fileDirExists(MEDIA_PATH)) {
            return FILE_DOESNT_EXIST;
        }
        if (!fileDirExists(OUTPUT_PATH)) {
            return FILE_DOESNT_EXIST;
        }
        if (!validFFProbe(FFMPEG_PATH)) {
            return PROBE_DOESNT_EXIST;
        }

        try {
            final Path basePath = FileSystems.getDefault().getPath(CSV_PATH);
            if (Files.isDirectory(basePath)) {
                Files.find(Paths.get(CSV_PATH), Integer.MAX_VALUE, (filePath, fileAttr) -> {
                    return fileAttr.isRegularFile() && filePath.toFile().getName().endsWith("csv");
                }).forEach(path -> addMetaToCsv(path));
            } else if (Files.isRegularFile(basePath)) {
                addMetaToCsv(basePath);
            }
        } catch (IOException details) {
            System.err.println("Problem reading file/walking file directory: "
                         + details.getMessage());
        }
        return 0;
    }

  /**
   * Verify a directory/file exists.
   *
   * @param aFileName pathed name of file to test
   * @return true/false for file/directory existence.
  */
    public static boolean fileDirExists(final String aFileName) {
        if (!Files.exists(FileSystems.getDefault().getPath(aFileName))) {
            System.err.println("Directory/file must exist: " + aFileName);
            return false;
        }
        return true;
    }

  /**
   * Verify ffprobe executable exists.
   *
   * @param aFileName pathed name of executable to test
   * @return true/false for valid ffprobe executable.
  */
    public static boolean validFFProbe(final String aFileName) {
        try {
            new FFprobe(FFMPEG_PATH).version();
        } catch (IOException e) {
            System.err.println(FFMPEG_PATH + " is not valid path to ffprobe");
            return false;
        }
        return true;
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
        final boolean hasAllMetas;

        System.out.println("working with file " + aPath);
        try {
            reader = new CSVReader(new FileReader(aPath.toFile()));
            input = reader.readAll();
            reader.close();

            output = new ArrayList<>(input.size());
            hasAllMetas = allMetaFieldsPresent(input.get(0));
            if (!hasAllMetas) {
                output.add(buildHeaderRow(input.get(0)));
            } else {
                output.add(input.get(0));
            }

            for (int index = 1; index < input.size(); index++) {
                output.add(buildARow(hasAllMetas, input.get(index)));
            }

            if (!Files.exists(FileSystems.getDefault().getPath(OUTPUT_PATH))) {
                Files.createDirectories(Paths.get(OUTPUT_PATH));
            }
            writer = new CSVWriter(new FileWriter(
               Paths.get(OUTPUT_PATH, aPath.toFile().getName()).toFile()));
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

        headers[headers.length - WIDTH_OFFSET] = HEADER_WIDTH;
        headers[headers.length - HEIGHT_OFFSET] = HEADER_HEIGHT;
        headers[headers.length - DURATION_OFFSET] = HEADER_DURATION;
        headers[headers.length - FORMAT_OFFSET] = HEADER_FORMAT;

        return headers;
    }

  /**
    * Method to copy/modify data rows from sourve file.
    *
    * @param aSource The original row from the source file.
    * @return The modified CSV row.
  */
    private static String[] buildARow(final boolean aHasColumns, final String... aSource) {
        final int fileColumn = 6;
        final String[] line;

        line = Arrays.copyOf(aSource, aHasColumns ? aSource.length : aSource.length + 4);

        if (line[fileColumn].contains(".") && !line[fileColumn].contains("~")) {
            addMetaData(line);
        }

        return line;
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
            //probeResult = ffprobe.probe(MEDIA_PATH.concat(getMediaFileName(aRow[6])));
            probeResult = ffprobe.probe(MEDIA_PATH.concat(aRow[6]));
            format = probeResult.getFormat();

            aRow[aRow.length - DURATION_OFFSET] = String.valueOf(format.duration);
            aRow[aRow.length - FORMAT_OFFSET] = format.format_name;

            if (probeResult.getStreams() != null) {
                for (int index = 0; index < probeResult.getStreams().size(); index++) {
                    final FFmpegStream stream;
                    stream = probeResult.getStreams().get(index);
                    if (stream.width != 0) {
                        aRow[aRow.length - WIDTH_OFFSET] = String.valueOf(stream.width);
                    }
                    if (stream.height != 0) {
                        aRow[aRow.length - HEIGHT_OFFSET] = String.valueOf(stream.height);
                    }
                }
            }
        } catch (IOException details) {
            System.err.println("Problem reading media file: "
                               + details.getMessage());
        }
    }

    private static boolean allMetaFieldsPresent(final String... aHeaderRow) {
        //final Stream<String> stream = Arrays.stream(aHeaderRow);
        return Arrays.stream(aHeaderRow).anyMatch(HEADER_WIDTH::equals)
               && Arrays.stream(aHeaderRow).anyMatch(HEADER_HEIGHT::equals)
               && Arrays.stream(aHeaderRow).anyMatch(HEADER_DURATION::equals)
               && Arrays.stream(aHeaderRow).anyMatch(HEADER_FORMAT::equals);
    }
  /**
    * Method to extract media file name from CSV column.
    *
    * @param aSource The original column entry from the source file.
    * @return Name of the media file
  */
    /*private static String getMediaFileName(final String aSource) {
        final StringBuilder mediaFile;

        if (aSource.contains(File.separator)) {
            mediaFile = new StringBuilder(aSource
                      .substring(aSource.lastIndexOf(File.separator) + 1));
        } else {
            mediaFile = new StringBuilder(aSource);
        }

        return mediaFile.toString();
    }*/

  /**
    * Method to generate name for output file.
    *
    * @param aInputName The original name of the source file.
    * @return The name of the output file.
  */
    /*private static String buildOutputName(final String aInputName) {
        return new StringBuilder(aInputName)
              .insert(aInputName.lastIndexOf(".csv"), ".meta").toString();
    }*/

}
