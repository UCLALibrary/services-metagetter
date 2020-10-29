package edu.ucla.library.services.metadata;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

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
   * Path where modified CSV file(s) will be written.
  */
    private static final int SUCCESS = 0;

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
}
