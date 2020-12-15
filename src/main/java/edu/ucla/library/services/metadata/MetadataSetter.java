
package edu.ucla.library.services.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import info.freelibrary.util.I18nRuntimeException;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

/**
 * An application that adds A/V metadata values to a CSV file.
 */
public final class MetadataSetter implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataSetter.class, MessageCodes.BUNDLE);

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
    private String myCsvPath;

    /**
     * Path to media files to be read.
     */
    @Parameters(index = "1", split = "\\,", splitSynopsisLabel = ",",
            description = {
                "The parent directory/mount point holding media files (this is prepended the file name in the CSV). ",
                "For example: If file name in the CSV is /ephraim/audio/audio.wav and the full path to audio.wav is ",
                "/mount/dlcs/masters/ephraim/audio/audio.wav, then enter \"/mount/dlcs/masters/\". If a CSV file has ",
                "multiple possible directories/mount points, separate them in your input with a comma. For example: ",
                "\"/mount/dlcs/masters/,/mount/dlcs/othermasters\"." })
    private List<String> myMediaPath;

    /**
     * Path to ffmpeg probe utility.
     */
    @Parameters(index = "2", description = "The path to ffprobe executable.")
    private String myFfmpegPath;

    /**
     * Path where modified CSV file(s) will be written.
     */
    @Parameters(index = "3", description = "The directory where output file(s) are written.")
    private String myOutputPath;

    /**
     * Header metadata from the CSV file.
     */
    private CsvHeaders myCsvHeaders;

    /**
     * Private constructor for MetadataSetter class.
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
        System.exit(new CommandLine(new MetadataSetter()).execute(args));
    }

    /**
     * Callable method executed by Picoli framework.
     */
    @Override
    public Integer call() {
        if (!fileDirExists(Arrays.asList(myCsvPath))) {
            return ExitCodes.FILE_DOESNT_EXIST;
        }
        if (!fileDirExists(myMediaPath)) {
            return ExitCodes.FILE_DOESNT_EXIST;
        }
        if (!validFFProbe(myFfmpegPath)) {
            return ExitCodes.PROBE_DOESNT_EXIST;
        }

        try {
            final Path basePath = FileSystems.getDefault().getPath(myCsvPath);

            if (!Files.exists(FileSystems.getDefault().getPath(myOutputPath))) {
                Files.createDirectories(Paths.get(myOutputPath));
            }

            if (Files.isDirectory(basePath)) {
                Files.find(Paths.get(myCsvPath), Integer.MAX_VALUE,
                        (filePath, fileAttr) -> fileAttr.isRegularFile() && filePath.toFile().getName().endsWith("csv"))
                        .forEach(this::addMetaToCsv);
            } else if (Files.isRegularFile(basePath)) {
                addMetaToCsv(basePath);
            }
        } catch (final I18nRuntimeException details) { // Exceptions from addMetaToCsv()
            System.err.println(details.getMessage());
            return ExitCodes.READ_WRITE_ERROR;
        } catch (final IOException details) { // IOException from Files' methods
            System.err.println(LOGGER.getMessage(MessageCodes.MG_101, details.getMessage()));
            return ExitCodes.READ_WRITE_ERROR;
        }

        return ExitCodes.SUCCESS;
    }

    /**
     * Verify a directory or file exists.
     *
     * @param aFilePathList A list of file paths to test
     * @return True/false for file or directory's existence
     */
    public boolean fileDirExists(final List<String> aFilePathList) {
        final StringBuilder stringBuilder = new StringBuilder();
        final int length;

        for (final String filePath : aFilePathList) {
            if (!Files.exists(FileSystems.getDefault().getPath(filePath))) {
                stringBuilder.append(LOGGER.getMessage(MessageCodes.MG_100, filePath)).append(System.lineSeparator());
            }
        }

        if ((length = stringBuilder.length()) == 0) {
            return true;
        } else {
            System.err.println(stringBuilder.delete(length - System.lineSeparator().length(), length).toString());
            return false;
        }
    }

    /**
     * Verify ffprobe executable exists.
     *
     * @param aFileName The file path of executable
     * @return True/false for valid ffprobe executable.
     */
    public boolean validFFProbe(final String aFileName) {
        try {
            new FFprobe(myFfmpegPath).version();
        } catch (final IOException details) {
            System.err.println(LOGGER.getMessage(MessageCodes.MG_102, myFfmpegPath));
            return false;
        }

        return true;
    }

    /**
     * Method to process CSV file(s) to add columns for media metadata.
     *
     * @param aPath Path to file to be read and copied/updated
     * @throws I18nRuntimeException A wrapped exception thrown while adding A/V metadata
     */
    private void addMetaToCsv(final Path aPath) throws I18nRuntimeException {
        final File outputFile = Paths.get(myOutputPath, aPath.toFile().getName()).toFile();

        try (CSVReader reader = new CSVReader(new FileReader(aPath.toFile()));
                CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {
            final List<String[]> input = reader.readAll();
            final List<String[]> output = new ArrayList<>(input.size());
            final boolean hasAllMetas = allMetaFieldsPresent(input.get(0));

            // Informational message that lets the user know which CSV file is being processed
            System.out.println(LOGGER.getMessage(MessageCodes.MG_103, aPath));

            myCsvHeaders = new CsvHeaders(input.get(0));

            if (!hasAllMetas) {
                output.add(buildHeaderRow(input.get(0)));
            } else {
                output.add(input.get(0));
            }

            for (int index = 1; index < input.size(); index++) {
                output.add(buildARow(hasAllMetas, input.get(index)));
            }

            for (final String[] row : output) {
                writer.writeNext(row);
            }
        } catch (final FfProbeException details) {
            throw new I18nRuntimeException(details, MessageCodes.BUNDLE, MessageCodes.MG_000, details.getMessage());
        } catch (final IOException details) { // Catches FileNotFoundException(s) and other IOException(s), too
            throw new I18nRuntimeException(details, MessageCodes.BUNDLE, MessageCodes.MG_104, details.getMessage());
        } catch (final FileFormatException details) {
            throw new I18nRuntimeException(details, MessageCodes.BUNDLE, MessageCodes.MG_000, details.getMessage());
        }
    }

    /**
     * Method to copy base headers and add new columns.
     *
     * @param aSource The original headers from the source file.
     * @return Array of header names.
     */
    private String[] buildHeaderRow(final String... aSource) {
        final String[] headers = Arrays.copyOf(aSource, aSource.length + 4);

        headers[headers.length - WIDTH_OFFSET] = HEADER_WIDTH;
        headers[headers.length - HEIGHT_OFFSET] = HEADER_HEIGHT;
        headers[headers.length - DURATION_OFFSET] = HEADER_DURATION;
        headers[headers.length - FORMAT_OFFSET] = HEADER_FORMAT;

        return headers;
    }

    /**
     * Method to copy/modify data rows from source file.
     *
     * @param aSource The original row from the source file
     * @return The modified CSV row
     * @throws FileNotFoundException If a media file could not be found
     * @throws FfProbeException If FFProbe encounters an error while reading the media file
     * @throws FileFormatException If the media file doesn't have a file extension
     */
    private String[] buildARow(final boolean aHasColumns, final String... aSource)
            throws FileNotFoundException, FfProbeException, FileFormatException {
        final int fileColumnIndex = myCsvHeaders.getFileNameIndex();
        final String[] line = Arrays.copyOf(aSource, aHasColumns ? aSource.length : aSource.length + 4);
        final String fileName = line[fileColumnIndex];

        if (fileColumnIndex != -1) {
            if (fileExpected(line)) {
                if (!fileName.contains(".")) {
                    throw new FileFormatException(line[fileColumnIndex]); // Check that file has an extension
                }

                if (fileName.contains("~")) {
                    getFullFilePath(fileName); // Throws FileNotFoundException if path doesn't exist
                }

                addMetadata(line);
            }
        } else {
            throw new FileNotFoundException(LOGGER.getMessage(MessageCodes.MG_107)); // No media files found
        }

        return line;
    }

    /**
     * Tests whether a file is expected.
     *
     * @param aRow A CSV row
     * @return True if a file is expected for this supplied row
     */
    private boolean fileExpected(final String[] aRow) {
        return !aRow[myCsvHeaders.getObjectTypeIndex()].equals("Collection");
    }

    /**
     * Method to extract metadata from media file and add to output row.
     *
     * @param aRow The row from the output file.
     * @throws FileNotFoundException If a media file could be found
     * @throws FfProbeException If FFProbe encounters an error while reading the media file
     */
    private void addMetadata(final String... aRow) throws FileNotFoundException, FfProbeException {
        final String filePath = aRow[myCsvHeaders.getFileNameIndex()];

        try {
            final FFprobe ffprobe = new FFprobe(myFfmpegPath);
            final FFmpegProbeResult probeResult = ffprobe.probe(getFullFilePath(filePath));
            final FFmpegFormat format = probeResult.getFormat();

            aRow[aRow.length - DURATION_OFFSET] = String.valueOf(format.duration);
            aRow[aRow.length - FORMAT_OFFSET] = format.format_name;

            if (probeResult.getStreams() != null) {
                for (final FFmpegStream stream : probeResult.getStreams()) {
                    if (stream.width != 0) {
                        aRow[aRow.length - WIDTH_OFFSET] = String.valueOf(stream.width);
                    }
                    if (stream.height != 0) {
                        aRow[aRow.length - HEIGHT_OFFSET] = String.valueOf(stream.height);
                    }
                }
            }
        } catch (final IOException details) {
            // We want to distinguish between this IOException and one that comes from reading/writing the CSV file
            throw new FfProbeException(details, filePath);
        }
    }

    /**
     * Gets the full path from the supplied partial path.
     *
     * @param aPartialPath A latter part of a file path
     * @return The full path to a file
     * @throws FileNotFoundException If the file could not be found at any of the possible paths
     */
    private String getFullFilePath(final String aPartialPath) throws FileNotFoundException {
        for (final String mediaPath : myMediaPath) {
            final Path path = Path.of(mediaPath, aPartialPath);

            if (Files.exists(path)) {
                return path.toString();
            }
        }

        throw new FileNotFoundException(LOGGER.getMessage(MessageCodes.MG_105, StringUtils.toString(',', myMediaPath)));
    }

    /**
     * Tests whether all the expected metadata fields are present.
     *
     * @param aHeaderRow A CSV header row
     * @return True if all required metadata fields are present; else, false
     */
    private boolean allMetaFieldsPresent(final String... aHeaderRow) {
        return Arrays.stream(aHeaderRow).anyMatch(HEADER_WIDTH::equals) &&
                Arrays.stream(aHeaderRow).anyMatch(HEADER_HEIGHT::equals) &&
                Arrays.stream(aHeaderRow).anyMatch(HEADER_DURATION::equals) &&
                Arrays.stream(aHeaderRow).anyMatch(HEADER_FORMAT::equals);
    }
}
