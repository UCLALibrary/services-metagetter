
package edu.ucla.library.services.metadata;

import static org.junit.Assert.*;

import java.io.FileReader;

import org.junit.Test;

import com.opencsv.CSVReader;

/**
 * Tests of CsvHeaders.
 */
public class CsvHeadersTest {

    private static final String GOOD_CSV = "src/test/resources/sales.csv";

    private static final String MISSING_FILE_NAME_CSV = "src/test/resources/missing-file-name.csv";

    /**
     * Tests getFileNameIndex().
     *
     * @throws Exception If there is trouble reading the CSV test fixture
     */
    @Test
    public void testGetFileNameIndex() throws Exception {
        final CSVReader csvReader = new CSVReader(new FileReader(GOOD_CSV));

        assertEquals(6, new CsvHeaders(csvReader.readNext()).getFileNameIndex());
        csvReader.close();
    }

    /**
     * Tests hasFileNameIndex().
     *
     * @throws Exception If there is trouble reading the CSV test fixture
     */
    @Test
    public void testHasFileNameIndex() throws Exception {
        final CSVReader csvReader = new CSVReader(new FileReader(GOOD_CSV));

        assertTrue(new CsvHeaders(csvReader.readNext()).hasFileNameIndex());
        csvReader.close();
    }

    /**
     * Tests getFileNameIndex() with missing 'File Name' column.
     *
     * @throws Exception If there is trouble reading the CSV test fixture
     */
    @Test
    public void testGetFileNameIndexWithMissingFileName() throws Exception {
        final CSVReader csvReader = new CSVReader(new FileReader(MISSING_FILE_NAME_CSV));

        assertEquals(-1, new CsvHeaders(csvReader.readNext()).getFileNameIndex());
        csvReader.close();
    }

    /**
     * Tests hasFileNameIndex() with missing 'File Name' column.
     *
     * @throws Exception If there is trouble reading the CSV test fixture
     */
    @Test
    public void testHasFileNameIndexWithMissingFileName() throws Exception {
        final CSVReader csvReader = new CSVReader(new FileReader(MISSING_FILE_NAME_CSV));

        assertFalse(new CsvHeaders(csvReader.readNext()).hasFileNameIndex());
        csvReader.close();
    }
}
