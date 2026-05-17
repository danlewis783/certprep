package acme.certprep.ui;

import acme.certprep.ReviewConfig;
import acme.certprep.Session;
import acme.certprep.SessionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class ReviewUI {
    private static final Logger logger = LoggerFactory.getLogger(ReviewUI.class);

    final JFrame frame;
    final ScalableImageLabel qImgLabel;
    final ScalableImageLabel aImgLabel;
    final JLabel infoLabel;
    final JCheckBox reviewedBox;
    final JToggleButton fitQBtn;
    final JToggleButton fitABtn;
    final JButton prevBtn;
    final JButton nextBtn;
    final JButton exitBtn;
    final JScrollPane qScroll;
    final JScrollPane aScroll;
    final List<SessionRow> rows;
    final ReviewConfig config;
    final Session session;

    int ptr = 0;

    ReviewUI(ReviewConfig config, Session session, List<SessionRow> rows) {
        this.config = config;
        this.session = session;
        this.rows = rows;
        frame = new JFrame("JavaPractice Review Mode - " + config.getSessionFile().getFileName());
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
                session.updateReviewed(rows.get(ptr), reviewedBox.isSelected());
            } catch (IOException ex) {
                logger.warn("Unable to update reviewed flag for row {}", ptr, ex);
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
        qImgLabel.setIcon(new ImageIcon(config.getDataDir().resolve(String.format("ch%02d-q%02d.png", r.getChapter(), r.getQuestion())).toString()));
        Path aF = config.getDataDir().resolve(String.format("ch%02d-q%02d-answer.png", r.getChapter(), r.getQuestion()));
        Path aS = config.getDataDir().resolve(String.format("ch%02d-q%02d-ans.png", r.getChapter(), r.getQuestion()));
        aImgLabel.setIcon(new ImageIcon(Files.exists(aF) ? aF.toString() : aS.toString()));
        infoLabel.setText(String.format("[%s] Ch%02d Q%02d | Answer: [%s] | Time: %d:%02d", r.isCorrect() ? "OK" : "FAIL", r.getChapter(), r.getQuestion(), r.getUserAnswer(), r.getTime() / 60, r.getTime() % 60));
        infoLabel.setForeground(r.isCorrect() ? Color.GREEN : Color.RED);
        reviewedBox.setSelected(r.isReviewed());
        prevBtn.setEnabled(ptr > 0);
        nextBtn.setEnabled(ptr < rows.size() - 1);
    }
}
