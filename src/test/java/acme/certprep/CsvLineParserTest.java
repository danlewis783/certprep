package acme.certprep;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CsvLineParserTest {
    @Test
    void parsesCsvLineWithQuotedComma() {
        String[] columns = CsvLineParser.parseLine("9,38,\"A,B\",true,72,false,true");

        assertThat(columns).containsExactly("9", "38", "A,B", "true", "72", "false", "true");
    }

    @Test
    void trimsUnquotedColumns() {
        String[] columns = CsvLineParser.parseLine(" 9 , 38 , A ");

        assertThat(columns).containsExactly("9", "38", "A");
    }
}
