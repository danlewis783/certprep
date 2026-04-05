package acme.certprep;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class ReviewUI {
    JFrame frame;
    ScalableImageLabel qImgLabel, aImgLabel;
    JLabel infoLabel;
    JCheckBox reviewedBox;
    JToggleButton fitQBtn, fitABtn;
    JButton prevBtn, nextBtn, exitBtn;
    JScrollPane qScroll, aScroll;
    List<SessionRow> rows;
    int ptr = 0;
    ArgParser config;

    ReviewUI(ArgParser config, List<SessionRow> rows) {
        this.config = config;
        this.rows = rows;
        frame = new JFrame("JavaPractice Review Mode - " + config.reviewSession);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 900);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());

        JPanel split = new JPanel(new GridLayout(1, 2));
        qImgLabel = new ScalableImageLabel();
        aImgLabel = new ScalableImageLabel();
        qScroll = new JScrollPane(qImgLabel);
        aScroll = new JScrollPane(aImgLabel);
        split.add(qScroll);
        split.add(aScroll);
        frame.add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        infoLabel = new JLabel("", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Monospaced", Font.BOLD, 18));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        prevBtn = new JButton("<< Prev");
        nextBtn = new JButton("Next >>");
        fitQBtn = new JToggleButton("Fit Q");
        fitABtn = new JToggleButton("Fit A");
        reviewedBox = new JCheckBox("Reviewed");
        exitBtn = new JButton("Exit");

        nav.add(prevBtn);
        nav.add(fitQBtn);
        nav.add(reviewedBox);
        nav.add(fitABtn);
        nav.add(nextBtn);
        nav.add(exitBtn);
        bottom.add(infoLabel, BorderLayout.NORTH);
        bottom.add(nav, BorderLayout.SOUTH);
        frame.add(bottom, BorderLayout.SOUTH);

        fitQBtn.setSelected(true);
        fitABtn.setSelected(true);
        toggleFit(qImgLabel, qScroll, true);
        toggleFit(aImgLabel, aScroll, true);

        prevBtn.addActionListener(e -> {
            if (ptr > 0) {
                ptr--;
                loadCurrent();
            }
        });
        nextBtn.addActionListener(e -> {
            if (ptr < rows.size() - 1) {
                ptr++;
                loadCurrent();
            }
        });
        exitBtn.addActionListener(e -> System.exit(0));
        reviewedBox.addActionListener(e -> {
            try {
                SessionManager.upd(config, rows.get(ptr), reviewedBox.isSelected());
            } catch (IOException ex) {
            }
        });
        fitQBtn.addActionListener(e -> toggleFit(qImgLabel, qScroll, fitQBtn.isSelected()));
        fitABtn.addActionListener(e -> toggleFit(aImgLabel, aScroll, fitABtn.isSelected()));

        loadCurrent();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void toggleFit(ScalableImageLabel label, JScrollPane scroll, boolean fit) {
        label.setFitMode(fit);
        scroll.setHorizontalScrollBarPolicy(fit ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(fit ? ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER : ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.revalidate();
    }

    private void loadCurrent() {
        SessionRow r = rows.get(ptr);
        qImgLabel.setIcon(new ImageIcon(Paths.get(config.dataDir, String.format("ch%02d-q%02d.png", r.chapter, r.question)).toString()));
        Path aF = Paths.get(config.dataDir, String.format("ch%02d-q%02d-answer.png", r.chapter, r.question));
        Path aS = Paths.get(config.dataDir, String.format("ch%02d-q%02d-ans.png", r.chapter, r.question));
        aImgLabel.setIcon(new ImageIcon(Files.exists(aF) ? aF.toString() : aS.toString()));
        infoLabel.setText(String.format("[%s] Ch%02d Q%02d | Answer: [%s] | Time: %d:%02d", r.correct ? "OK" : "FAIL", r.chapter, r.question, r.userAnswer, r.time / 60, r.time % 60));
        infoLabel.setForeground(r.correct ? Color.GREEN : Color.RED);
        reviewedBox.setSelected(r.reviewed);
        prevBtn.setEnabled(ptr > 0);
        nextBtn.setEnabled(ptr < rows.size() - 1);
    }
}
