package certprep.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import javax.imageio.ImageIO;

public class GenerateTestData {

    // --- ANSI COLOR DEFINITIONS ---
    // Format: \033[<STYLE>;<COLOR>m
    // \033    : The ESC (Escape) character (Octal 33)
    // [       : The Control Sequence Introducer
    // 0       : Reset/Normal style
    // 36, 32  : The foreground color code
    // m       : The character that ends the sequence

    private static final String RESET  = "\033[0m";      // Returns terminal to default colors
    private static final String CYAN   = "\033[0;36m";   // Standard Cyan
    private static final String GREEN  = "\033[0;32m";   // Standard Green
    private static final String YELLOW = "\033[0;33m";   // Standard Yellow
    private static final String GRAY   = "\033[0;90m";   // "Bright Black" / Dark Gray

    public static void main(String[] args) {
        boolean cleanMode = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-c") || arg.equalsIgnoreCase("--clean")) {
                cleanMode = true;
                break;
            }
        }

        System.out.println(CYAN + "Initializing JavaPractice Complete Test Environment..." + RESET);

        try {
            Path baseDir = Paths.get(".").toAbsolutePath().normalize();
            Path dataDir = baseDir.resolve("data");
            Path sessionsDir = baseDir.resolve("sessions");

            Files.createDirectories(dataDir);
            Files.createDirectories(sessionsDir);

            if (cleanMode) {
                cleanDataDirectory(dataDir);
            }

            // Generate the master-answer-key.csv
            String csvContent = "Chapter,Question,Answer,Possible\n" +
                    "9,38,\"F\",\"A,B,C,D,E,F\"\n" +
                    "9,39,\"A,B,E\",\"A,B,C,D,E,F\"\n" +
                    "9,40,\"C\",\"A,B,C,D,E\"\n" +
                    "10,1,\"B\",\"A,B,C,D,E\"\n" +
                    "10,2,\"C,E\",\"A,B,C,D,E,F\"";

            Files.writeString(dataDir.resolve("master-answer-key.csv"), csvContent);
            System.out.println(GREEN + "Success: Created master-answer-key.csv" + RESET);

            List<TestData> data = List.of(
                    new TestData(9, 38, "F"),
                    new TestData(9, 39, "A,B,E"),
                    new TestData(9, 40, "C"),
                    new TestData(10, 1, "B"),
                    new TestData(10, 2, "C,E")
            );

            for (TestData item : data) {
                String cPad = String.format("%02d", item.chapter);
                String qPad = String.format("%02d", item.question);

                // Question Image
                String qFile = String.format("ch%s-q%s.png", cPad, qPad);
                generateImage(dataDir.resolve(qFile),
                        Color.LIGHT_GRAY,
                        Color.BLUE.darker(),
                        "Simulated Question Image\n\nChapter: " + item.chapter + "\nQuestion: " + item.question);
                System.out.println(GRAY + "Created Question: " + qFile + RESET);

                // Answer Image
                String aFile = String.format("ch%s-q%s-ans.png", cPad, qPad);
                generateAnswerImage(dataDir.resolve(aFile), item);
                System.out.println(GREEN + "Created Answer:   " + aFile + RESET);
            }

            System.out.println("\n" + CYAN + "Environment Setup Complete!" + RESET);
            System.out.println("Total images generated: " + (data.size() * 2));
            System.out.println(GREEN + "Ready for Test and Review modes." + RESET);

        } catch (IOException e) {
            System.err.println("Error setting up environment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void cleanDataDirectory(Path dataDir) throws IOException {
        System.out.println(YELLOW + "Cleaning data directory..." + RESET);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir)) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString().toLowerCase();
                if (name.endsWith(".png") || name.endsWith(".csv")) {
                    Files.delete(entry);
                }
            }
        }
        System.out.println(GREEN + "Clean complete." + RESET);
    }

    private static void generateImage(Path path, Color bg, Color fg, String text) throws IOException {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(bg);
        g2d.fillRect(0, 0, 800, 600);
        g2d.setColor(fg);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));

        int y = 200;
        for (String line : text.split("\n")) {
            g2d.drawString(line, 50, y);
            y += 50;
        }
        g2d.dispose();
        ImageIO.write(img, "png", path.toFile());
    }

    private static void generateAnswerImage(Path path, TestData item) throws IOException {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(152, 251, 152));
        g2d.fillRect(0, 0, 800, 600);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(new Color(0, 100, 0));
        g2d.drawString("CORRECT ANSWER", 50, 100);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Ch " + item.chapter + " Q " + item.question, 50, 180);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 72));
        g2d.setColor(new Color(0, 100, 0));
        g2d.drawString(item.answer, 50, 350);
        g2d.dispose();
        ImageIO.write(img, "png", path.toFile());
    }

}