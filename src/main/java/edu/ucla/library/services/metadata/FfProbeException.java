/**
 *
 */

package edu.ucla.library.services.metadata;

import java.io.IOException;

import info.freelibrary.util.I18nException;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * An exception thrown when FfProbe fails to read a media file.
 */
public class FfProbeException extends I18nException {

    private static final Logger LOGGER = LoggerFactory.getLogger(FfProbeException.class, MessageCodes.BUNDLE);

    /**
     * The <code>serialVersionUID</code> of FfProbeException.
     */
    private static final long serialVersionUID = 7352024916950415073L;

    /**
     * Creates a new exception related to a failure from FFProbe.
     *
     * @param aFilePath The path of the file FFProbe tried to read
     * @param aIOException An underlying exception thrown by FFProbe
     */
    public FfProbeException(final IOException aIOException, final String aFilePath) {
        // We edit the error message because FFProbe swallows STDOUT (so there is nothing for the user to check)
        super(aIOException, MessageCodes.BUNDLE, MessageCodes.MG_106, aFilePath, aIOException.getMessage()
                .replace(" Check stdout.", System.lineSeparator() + LOGGER.getMessage(MessageCodes.MG_109)));
    }

}
