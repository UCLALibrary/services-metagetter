
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
     * Creates a new CSV headers object.
     *
     * @param aCsvHeaderRow The header row from the CSV file
     */
    public CsvHeaders(final String[] aCsvHeaderRow) {
        for (int index = 0; index < aCsvHeaderRow.length; index++) {
            switch (aCsvHeaderRow[index]) {
                case "File Name":
                    myFileNameIndex = index;
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
}
