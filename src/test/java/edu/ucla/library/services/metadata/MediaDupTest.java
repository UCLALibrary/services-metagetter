
package edu.ucla.library.services.metadata;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * A test of media column duplication.
 */
public class MediaDupTest {

    /** A logger for the tests. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataSetterTest.class, MessageCodes.BUNDLE);

    /** The location of the test resources directory. */
    private static final String TEST_FIXTURES_DIR = "src/test/resources/";

    /** The name of the pre-existing media metadata file. */
    private static final String BAD_MEDIA_CSV_FILE = "pre-existing_media.csv";

    /** Path to CSV file to be updated. */
    private static final String CSV_PATH = TEST_FIXTURES_DIR + "bad_csvs/";

    /** The test resource with pre-existing media metadata. */
    private static final String CSV_WITH_BAD_MEDIA = CSV_PATH + BAD_MEDIA_CSV_FILE;

    /** Path where modified CSV file(s) will be written. */
    private static final String OUTPUT_PATH = "/tmp/";

    /** Path to media files to be read. */
    private static final String MEDIA_PATH = TEST_FIXTURES_DIR + "media/";

    /** The location of ffprobe. */
    private static final String FFMPEG_PATH = "/usr/bin/ffprobe";

    /** Rule that allows catching System.err output in tests. */
    @Rule
    public final SystemErrRule mySystemErrRule = new SystemErrRule().enableLog();

    /**
     * Cleans up after a MetadataSetter test.
     *
     * @throws IOException If there is a problem deleting the file
     */
    @After
    public void tearDown() throws IOException, SecurityException {
        Files.deleteIfExists(FileSystems.getDefault().getPath(OUTPUT_PATH + BAD_MEDIA_CSV_FILE));
    }

    /**
     * Tests pre-existing media metadata processing.
     *
     * @throws Exception If there is trouble reading the CSV file
     */
    @Test
    public void testPreExistingMetadata() throws Exception {
        final Path output = FileSystems.getDefault().getPath(OUTPUT_PATH + BAD_MEDIA_CSV_FILE);
        final int statusCode = catchSystemExit(() -> {
            MetadataSetter.main(new String[] { CSV_WITH_BAD_MEDIA, MEDIA_PATH, FFMPEG_PATH, OUTPUT_PATH });
        });

        assertEquals(ExitCodes.SUCCESS, statusCode);
        assertTrue(Files.exists(output));

        final String csv = FileUtils.readFileToString(output.toFile(), StandardCharsets.UTF_8);

        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            String line;

            while ((line = reader.readLine()) != null) {
                final String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                final String[] lastSix = Arrays.copyOfRange(fields, fields.length - 6, fields.length);

                if (stripQuotes("Waveform").equals(stripQuotes(lastSix[0]))) {
                    assertEquals("media.width", stripQuotes(lastSix[1]));
                    assertEquals("media.height", stripQuotes(lastSix[2]));
                    assertEquals("media.duration", stripQuotes(lastSix[3]));
                    assertEquals("media.format", stripQuotes(lastSix[4]));
                    assertEquals("IIIF Manifest URL", stripQuotes(lastSix[5]));
                } else {
                    assertEquals("", stripQuotes(lastSix[0]));
                    assertEquals("320", stripQuotes(lastSix[1]));
                    assertEquals("240", stripQuotes(lastSix[2]));
                    assertEquals("video/mpeg", stripQuotes(lastSix[4]));
                    assertTrue(lastSix[5].startsWith("\"https"));
                }
            }
        }
    }

    /**
     * Strips quotes out of a supplied string value.
     *
     * @param aValue A value to strip
     * @return The supplied value without any quotes
     */
    private String stripQuotes(final String aValue) {
        return aValue.replace("\"", "");
    }
}
