package acme.certprep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

final class CsvLineParser {
    private static final Logger logger = LoggerFactory.getLogger(CsvLineParser.class);

    private CsvLineParser() {
    }

    static String[] parseLine(String line) {
        List<String> columns = new ArrayList<>();
        boolean quoted = false;
        StringBuilder column = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') {
                quoted = !quoted;
            } else if (c == ',' && !quoted) {
                columns.add(column.toString().trim());
                column.setLength(0);
            } else {
                column.append(c);
            }
        }
        columns.add(column.toString().trim());
        return columns.toArray(new String[0]);
    }
}
