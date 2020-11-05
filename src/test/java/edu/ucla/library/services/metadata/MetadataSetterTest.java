package edu.ucla.library.services.metadata;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import org.junit.contrib.java.lang.system.SystemErrRule;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

/*
 * Unit tests for MetadataSetter.java
 */
public class MetadataSetterTest {

  /**
   * Path to CSV file to be updated.
  */
    private static final String CSV_PATH = "src/test/resources/";

  /**
   * CSV file name.
  */
    private static final String CSV_NAME = "sales.csv";

  /**
   * Path to media files to be read.
  */
    private static final String MEDIA_PATH = "src/test/resources/media/";

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
   * Return code for app success.
  */
    private static final int SUCCESS = 0;

  /**
   * Return code for nonexitent file/directory.
  */
    private static final int NO_FILE = 101;

  /**
   * Return code for nonexistent ffprope.
  */
    private static final int NO_PROBE = 102;

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
        // delete the output file if left over from test run
        Files.deleteIfExists(FileSystems.getDefault().getPath(OUTPUT_PATH + CSV_NAME));
    }

  /**
   * Tests happy path for app execution.
   *
  */
    @Test
    public void testGetMeta() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            final String[] args = {CSV_PATH, MEDIA_PATH, FFMPEG_PATH, OUTPUT_PATH};
            MetadataSetter.main(args);
        });
        assertEquals(SUCCESS, statusCode);
        assertTrue(Files.exists(FileSystems.getDefault().getPath(OUTPUT_PATH + CSV_NAME)));
    }

  /**
   * Tests nonexistent path for CSV file(s).
   *
  */
    @Test
    public void testMissingDir() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            final String[] args = {FAKE_PATH, MEDIA_PATH, FFMPEG_PATH, OUTPUT_PATH};
            MetadataSetter.main(args);
        });
        assertEquals(NO_FILE, statusCode);
        assertTrue(mySystemErrRule.getLog().contains("file must exist"));
    }

  /**
   * Tests nonexistent path for ffprobe utility.
   *
  */
    @Test
    public void testMissingProbe() throws Exception {
        final int statusCode = catchSystemExit(() -> {
            final String[] args = {CSV_PATH, MEDIA_PATH, FAKE_PATH, OUTPUT_PATH};
            MetadataSetter.main(args);
        });
        assertEquals(NO_PROBE, statusCode);
        assertTrue(mySystemErrRule.getLog().contains("is not valid path to ffprobe"));
    }
}
