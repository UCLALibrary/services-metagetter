
package edu.ucla.library.services.metadata;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

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

    /**
     * Path to CSV file to be updated.
     */
    private static final String CSV_PATH = TEST_FIXTURES_DIR + "good_csvs/";

    /**
     * CSV file name.
     */
    private static final String CSV_NAME = "sales.csv";

    /**
     * Path to media files to be read.
     */
    private static final String MEDIA_PATH = TEST_FIXTURES_DIR + "media/";

    /**
     * A secondary path from which media files can be read.
     */
    private static final String COMBINED_MEDIA_PATH = TEST_FIXTURES_DIR + "media2/," + MEDIA_PATH;

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
     * Tests happy path with a CSV file supplied and multiple media mounts.
     */
    @Test
    public void testGetMetaWithFileMultipleMediaPaths() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { CSV_PATH + CSV_NAME, COMBINED_MEDIA_PATH, FFMPEG_PATH, OUTPUT_PATH });
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
            MetadataSetter.main(new String[] { CSV_PATH, COMBINED_MEDIA_PATH, FFMPEG_PATH, OUTPUT_PATH });
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
