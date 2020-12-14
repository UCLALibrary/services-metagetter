
package edu.ucla.library.services.metadata;

/**
 * Error codes for the Metagetter application.
 */
final class ExitCodes {

    /**
     * A successful execution.
     */
    public static final int SUCCESS = 0;

    /**
     * File/directory doesn't exist.
     */
    public static final int FILE_DOESNT_EXIST = 101;

    /**
     * The ffprobe executable doesn't exist.
     */
    public static final int PROBE_DOESNT_EXIST = 102;

    /**
     * An error that happened during reading or writing.
     */
    public static final int READ_WRITE_ERROR = 103;

    /**
     * Creates a new error codes object.
     */
    private ExitCodes() {
    }

}
