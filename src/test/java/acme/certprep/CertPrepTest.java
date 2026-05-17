package acme.certprep;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CertPrepTest {
    @Test
    void parsesCsvLineWithQuotedComma() {
        String[] columns = CertPrep.parseCSVLine("9,38,\"A,B\",true,72,false,true");

        assertThat(columns).containsExactly("9", "38", "A,B", "true", "72", "false", "true");
    }

    @Test
    void trimsUnquotedColumns() {
        String[] columns = CertPrep.parseCSVLine(" 9 , 38 , A ");

        assertThat(columns).containsExactly("9", "38", "A");
    }
}
