
package edu.ucla.library.services.metadata;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.UUID;

import org.junit.Test;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * Tests of FfProbeException.
 */
public class FfProbeExceptionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FfProbeExceptionTest.class, MessageCodes.BUNDLE);

    private static final String MESSAGE = UUID.randomUUID().toString();

    private static final String FILE_NAME = "my-file.mp3";

    /**
     * Tests the exception's constructor.
     */
    @Test
    public void testFfProbeException() {
        assertEquals(LOGGER.getMessage(MessageCodes.MG_106, FILE_NAME, MESSAGE),
                new FfProbeException(new IOException(MESSAGE), FILE_NAME).getMessage());
    }

}
