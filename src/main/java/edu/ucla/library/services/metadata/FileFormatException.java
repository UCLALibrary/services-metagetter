
package edu.ucla.library.services.metadata;

import info.freelibrary.util.I18nException;

/**
 * An exception throw when the media file does not have an extension indicating media type.
 */
public class FileFormatException extends I18nException {

    /**
     * The <code>serialVersionUID</code> for FileFormatException.
     */
    private static final long serialVersionUID = 5080967189701020786L;

    /**
     * Creates a new FileFormatException.
     *
     * @param aFileName The name of the file missing the media type extension
     */
    public FileFormatException(final String aFileName) {
        super(MessageCodes.BUNDLE, MessageCodes.MG_108, aFileName);
    }

}
