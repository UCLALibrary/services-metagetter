/**
 *
 */

package edu.ucla.library.services.metadata;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * Tests of the FileFormatException class.
 */
public class FileFormatExceptionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileFormatExceptionTest.class, MessageCodes.BUNDLE);

    /**
     * Test method for {@link FileFormatException#FileFormatException(String)}.
     */
    @Test
    public void testFileFormatException() {
        final String fileName = UUID.randomUUID().toString();
        assertEquals(LOGGER.getMessage(MessageCodes.MG_108, fileName), new FileFormatException(fileName).getMessage());
    }

}
