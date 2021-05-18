
package edu.ucla.library.services.metadata;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * Unit tests for MetadataSetter.java
 */
public class MetadataSetterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataSetterTest.class, MessageCodes.BUNDLE);

    private static final String TEST_FIXTURES_DIR = "src/test/resources/";

    private static final String EMPTY = "";

    /**
     * Path to CSV file to be updated.
     */
    private static final String CSV_PATH = TEST_FIXTURES_DIR + "good_csvs/";

    /**
     * CSV file name.
     */
    private static final String CSV_NAME = "sales.csv";

    /**
     * A CSV file that references a bad media file.
     */
    private static final String CSV_WITH_BAD_MEDIA = TEST_FIXTURES_DIR + "bad_csvs/bad-media-file.csv";

    /**
     * Path to media files to be read.
     */
    private static final String MEDIA_PATH = TEST_FIXTURES_DIR + "media/";

    /**
     * A program argument that includes two media mounts (or directories) separated by a comma. If the program is
     * operating as it should, both paths will be checked for the media file from the CSV's 'File Name' column.
     */
    private static final String COMBINED_MEDIA_PATHS = TEST_FIXTURES_DIR + "media2/," + MEDIA_PATH;

    /**
     * Path to ffmpeg probe utility.
     */
    private static final String FFMPEG_PATH = "/usr/bin/ffprobe";

    /**
     * Path where modified CSV file(s) will be written.
     */
    private static final String OUTPUT_PATH = "/tmp/";

    /**
     * Fake path for testing missing parameters.
     */
    private static final String FAKE_PATH = "/not/there/";

    /**
     * Rule that allows catching System.err output in tests.
     */
    @Rule
    public final SystemErrRule mySystemErrRule = new SystemErrRule().enableLog();

    /**
     * Cleans up after a MetadataSetter test.
     *
     * @throws IOException If there is a problem deleting the file
     */
    @After
    public void tearDown() throws IOException, SecurityException {
        Files.deleteIfExists(FileSystems.getDefault().getPath(OUTPUT_PATH + CSV_NAME));
    }

    /**
     * Tests happy path with a directory with CSV files supplied.
     */
    @Test
    public void testGetMetaWithDir() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { CSV_PATH, MEDIA_PATH, FFMPEG_PATH, OUTPUT_PATH });
        });
        assertEquals(ExitCodes.SUCCESS, statusCode);
        assertTrue(Files.exists(FileSystems.getDefault().getPath(OUTPUT_PATH + CSV_NAME)));
    }

    /**
     * Tests happy path with a CSV file supplied.
     */
    @Test
    public void testGetMetaWithFile() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { CSV_PATH + CSV_NAME, MEDIA_PATH, FFMPEG_PATH, OUTPUT_PATH });
        });
        assertEquals(ExitCodes.SUCCESS, statusCode);
        assertTrue(Files.exists(FileSystems.getDefault().getPath(OUTPUT_PATH + CSV_NAME)));
    }

    /**
     * Tests populating Format.extent.
     */
    @Test
    public void testPopulateFormatExtentField() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { CSV_PATH + CSV_NAME, MEDIA_PATH, FFMPEG_PATH, OUTPUT_PATH });
        });

        // Nb: value comes from src/test/resources/media/ephraim/audio/21198-zz000954s4-2-submaster.mp3
        final String formattedDuration = "12m 37s";
        final Path updatedCsv = FileSystems.getDefault().getPath(OUTPUT_PATH + CSV_NAME);

        assertEquals(ExitCodes.SUCCESS, statusCode);

        try (CSVReader reader = new CSVReader(new FileReader(updatedCsv.toFile()))) {
            final List<String[]> rows = reader.readAll();
            final CsvHeaders headers = new CsvHeaders(rows.get(0));
            assertEquals(rows.get(2)[headers.getFormatExtentIndex()], formattedDuration);
        } catch (IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Tests populating media.format.
     */
    @Test
    public void testPopulateMediaFormat() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { CSV_PATH + CSV_NAME, MEDIA_PATH, FFMPEG_PATH, OUTPUT_PATH });
        });

        // Nb: value comes from src/test/resources/media/ephraim/audio/21198-zz000954s4-2-submaster.mp3
        final String audioFormat = "audio/mpeg";
        // Nb: value comes from src/test/resources/media/ephraim/video/crowd.mpg
        final String videoFormat = "video/mpeg";
        final Path updatedCsv = FileSystems.getDefault().getPath(OUTPUT_PATH + CSV_NAME);

        assertEquals(ExitCodes.SUCCESS, statusCode);

        try (CSVReader reader = new CSVReader(new FileReader(updatedCsv.toFile()))) {
            final List<String[]> rows = reader.readAll();
            final CsvHeaders headers = new CsvHeaders(rows.get(0));
            assertEquals(rows.get(2)[headers.getMediaFormatIndex()], audioFormat);
            assertEquals(rows.get(4)[headers.getMediaFormatIndex()], videoFormat);
        } catch (IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Tests not populating media.* fields for non-A/V media
     */
    @Test
    public void testDontPopulateImageMetadata() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { CSV_PATH + CSV_NAME, MEDIA_PATH, FFMPEG_PATH, OUTPUT_PATH });
        });

        final Path updatedCsv = FileSystems.getDefault().getPath(OUTPUT_PATH + CSV_NAME);

        assertEquals(ExitCodes.SUCCESS, statusCode);

        try (CSVReader reader = new CSVReader(new FileReader(updatedCsv.toFile()))) {
            final List<String[]> rows = reader.readAll();
            final CsvHeaders headers = new CsvHeaders(rows.get(0));
            //row 4 is image
            final String width = rows.get(3)[headers.getMediaWidthIndex()].trim();
            final String height = rows.get(3)[headers.getMediaHeightIndex()].trim();
            final String duration = rows.get(3)[headers.getMediaDurationIndex()].trim();
            final String format = rows.get(3)[headers.getMediaFormatIndex()].trim();
            assertTrue(width.equals(EMPTY) && height.equals(EMPTY) && duration.equals(EMPTY) && format.equals(EMPTY));
        } catch (IOException details) {
            fail(details.getMessage());
        }
    }

    /**
     * Tests what happens when ffprobe throws an exception message.
     */
    @Test
    public void testGetMetaFromBadFileMultipleMediaPaths() throws Exception {
        final String badMedia = "bad-mp3-file.mp3";
        final String probeError = "/usr/bin/ffprobe returned non-zero exit status. Check stdout.";
        final String errorMessage = LOGGER.getMessage(MessageCodes.MG_106, badMedia, probeError);
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { CSV_WITH_BAD_MEDIA, COMBINED_MEDIA_PATHS, FFMPEG_PATH, OUTPUT_PATH });
        });
        assertEquals(ExitCodes.SUCCESS, statusCode);
        assertEquals(mySystemErrRule.getLog().trim(), errorMessage);
    }

    /**
     * Tests happy path with a CSV file supplied and multiple media mounts.
     */
    @Test
    public void testGetMetaWithFileMultipleMediaPaths() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { CSV_PATH + CSV_NAME, COMBINED_MEDIA_PATHS, FFMPEG_PATH, OUTPUT_PATH });
        });
        assertEquals(ExitCodes.SUCCESS, statusCode);
        assertTrue(Files.exists(FileSystems.getDefault().getPath(OUTPUT_PATH + CSV_NAME)));
    }

    /**
     * Tests happy path with a directory with CSV files supplied and multiple media mounts.
     */
    @Test
    public void testGetMetaWithDirWithMultipleMediaPaths() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { CSV_PATH, COMBINED_MEDIA_PATHS, FFMPEG_PATH, OUTPUT_PATH });
        });
        assertEquals(ExitCodes.SUCCESS, statusCode);
        assertTrue(Files.exists(FileSystems.getDefault().getPath(OUTPUT_PATH + CSV_NAME)));
    }

    /**
     * Tests nonexistent path for CSV file(s).
     */
    @Test
    public void testMissingDir() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { FAKE_PATH, MEDIA_PATH, FFMPEG_PATH, OUTPUT_PATH });
        });
        assertEquals(ExitCodes.FILE_DOESNT_EXIST, statusCode);
        assertTrue(mySystemErrRule.getLog().trim().equals(LOGGER.getMessage(MessageCodes.MG_100, FAKE_PATH)));
    }

    /**
     * Tests nonexistent path for ffprobe utility.
     */
    @Test
    public void testMissingProbe() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { CSV_PATH, MEDIA_PATH, FAKE_PATH, OUTPUT_PATH });
        });
        assertEquals(ExitCodes.PROBE_DOESNT_EXIST, statusCode);
        assertTrue(mySystemErrRule.getLog().trim().equals(LOGGER.getMessage(MessageCodes.MG_102, FAKE_PATH)));
    }
}
