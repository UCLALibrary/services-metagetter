package edu.ucla.library.services.metadata;

/**
 * A class for package constants.
 */
public final class Constants {

    /**
     * Constant for width column name.
     */
    public static final String HEADER_WIDTH = "media.width";

    /**
     * Constant for height column name.
     */
    public static final String HEADER_HEIGHT = "media.height";

    /**
     * Constant for duration column name.
     */
    public static final String HEADER_DURATION = "media.duration";

    /**
     * Constant for format column name.
     */
    public static final String HEADER_FORMAT = "media.format";

    /**
     * Constant for file name column name.
     */
    public static final String HEADER_NAME = "File Name";

    /**
     * Constant for object type column name.
     */
    public static final String HEADER_TYPE = "Object Type";

    /**
     * Constant for extent column name.
     */
    public static final String HEADER_EXTENT = "Format.extent";

    /**
     * Constant for column position of media.width.
     */
    public static final int WIDTH_OFFSET = 4;

    /**
     * Constant for column position of media.height.
     */
    public static final int HEIGHT_OFFSET = 3;

    /**
     * PConstant for column position of media.duration.
     */
    public static final int DURATION_OFFSET = 2;

    /**
     * Constant for column position of media.format.
     */
    public static final int FORMAT_OFFSET = 1;

    /**
     * Constant for one hour in seconds, used for formatting Format.extent content.
     */
    public static final int ONE_HOUR = 3600;

    /**
     * Constant for one minute in seconds, used for formatting Format.extent content.
     */
    public static final int ONE_MINUTE = 60;

    /**
     * Private constructor for Constants class.
     */
    private Constants() {
    }

}
