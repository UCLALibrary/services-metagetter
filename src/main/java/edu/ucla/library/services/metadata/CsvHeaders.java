
package edu.ucla.library.services.metadata;

/**
 * The header metadata from the CSV file.
 */
public class CsvHeaders {

    /**
     * The index position of the 'File Name' column.
     */
    private int myFileNameIndex = -1;

    /**
     * The index position of the 'Object Type' column.
     */
    private int myObjectTypeIndex = -1;

    /**
     * The index position of the 'Format.extent' column.
     */
    private int myFormatExtentIndex = -1;

    /**
     * The index position of the 'media.format' column.
     */
    private int myMediaFormatIndex = -1;

    /**
     * The index position of the 'media.width' column.
     */
    private int myMediaWidthIndex = -1;

    /**
     * The index position of the 'media.height' column.
     */
    private int myMediaHeightIndex = -1;

    /**
     * The index position of the 'media.duration' column.
     */
    private int myMediaDurationIndex = -1;

    /**
     * Creates a new CSV headers object.
     *
     * @param aCsvHeaderRow The header row from the CSV file
     */
    public CsvHeaders(final String... aCsvHeaderRow) {
        for (int index = 0; index < aCsvHeaderRow.length; index++) {
            switch (aCsvHeaderRow[index]) {
                case "File Name":
                    myFileNameIndex = index;
                    break;
                case "Object Type":
                    myObjectTypeIndex = index;
                    break;
                case "Format.extent":
                    myFormatExtentIndex = index;
                    break;
                case "media.width":
                    myMediaWidthIndex = index;
                    break;
                case "media.height":
                    myMediaHeightIndex = index;
                    break;
                case "media.duration":
                    myMediaDurationIndex = index;
                    break;
                case "media.format":
                    myMediaFormatIndex = index;
                    break;
                // Other cases for the A/V fields we add (when we work that ticket)
                default:
                    // Do nothing for fields we don't care about
            }
        }
    }

    /**
     * Gets the index position of the 'File Name' column from the CSV's header row.
     *
     * @return The index position of the 'File Name' column
     */
    public int getFileNameIndex() {
        return myFileNameIndex;
    }

    /**
     * Whether the CSV metadata has a 'File Name' column.
     *
     * @return True if a 'File Name' column was found; else, false
     */
    public boolean hasFileNameIndex() {
        return myFileNameIndex != -1;
    }

    /**
     * Gets the index position of the 'Object Type' column from the CSV's header row.
     *
     * @return The index position of the 'Object Type' column
     */
    public int getObjectTypeIndex() {
        return myObjectTypeIndex;
    }

    /**
     * Whether the CSV metadata has a 'Object Type' column.
     *
     * @return True if a 'Object Type' column was found; else, false
     */
    public boolean hasObjectTypeIndex() {
        return myObjectTypeIndex != -1;
    }

    /**
     * Whether the CSV metadata has a 'Format.extent' column.
     *
     * @return True if a 'Format.extent' column was found; else, false
     */
    public boolean hasFormatExtentIndex() {
        return myFormatExtentIndex != -1;
    }

    /**
     * Gets the index position of the 'Format.extent' column from the CSV's header row.
     *
     * @return The index position of the 'Format.extent' column
     */
    public int getFormatExtentIndex() {
        return myFormatExtentIndex;
    }

    /**
     * Whether the CSV metadata has a 'media.format' column.
     *
     * @return True if a 'media.format' column was found; else, false
     */
    public boolean hasMediaFormatIndex() {
        return myMediaFormatIndex != -1;
    }

    /**
     * Gets the index position of the 'media.format' column from the CSV's header row.
     *
     * @return The index position of the 'media.format' column
     */
    public int getMediaFormatIndex() {
        return myMediaFormatIndex;
    }

    /**
     * Whether the CSV metadata has a 'media.width' column.
     *
     * @return True if a 'media.width' column was found; else, false
     */
    public boolean hasMediaWidthIndex() {
        return myMediaWidthIndex != -1;
    }

    /**
     * Gets the index position of the 'media.width' column from the CSV's header row.
     *
     * @return The index position of the 'media.width' column
     */
    public int getMediaWidthIndex() {
        return myMediaWidthIndex;
    }

    /**
     * Whether the CSV metadata has a 'media.height' column.
     *
     * @return True if a 'media.height' column was found; else, false
     */
    public boolean hasMediaHeightIndex() {
        return myMediaHeightIndex != -1;
    }

    /**
     * Gets the index position of the 'media.height' column from the CSV's header row.
     *
     * @return The index position of the 'media.height' column
     */
    public int getMediaHeightIndex() {
        return myMediaHeightIndex;
    }

    /**
     * Whether the CSV metadata has a 'media.duration' column.
     *
     * @return True if a 'media.duration' column was found; else, false
     */
    public boolean hasMediaDurationIndex() {
        return myMediaDurationIndex != -1;
    }

    /**
     * Gets the index position of the 'media.duration' column from the CSV's header row.
     *
     * @return The index position of the 'media.duration' column
     */
    public int getMediaDurationIndex() {
        return myMediaDurationIndex;
    }
}
