package acme.certprep.ui;

import acme.certprep.QuestionInfo;
import acme.certprep.ReviewConfig;
import acme.certprep.Session;
import acme.certprep.SessionRow;
import acme.certprep.ExamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class CertPrepUi {
    private static final Logger logger = LoggerFactory.getLogger(CertPrepUi.class);

    private static boolean themeApplied = false;

    private CertPrepUi() {
    }

    private static void applyDarkTheme() {
        if (themeApplied) {
            return;
        }

        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("Panel.foreground", Color.WHITE);
        UIManager.put("Label.background", Color.BLACK);
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("CheckBox.background", Color.BLACK);
        UIManager.put("CheckBox.foreground", Color.WHITE);
        UIManager.put("Button.background", Color.DARK_GRAY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("ToggleButton.background", Color.DARK_GRAY);
        UIManager.put("ToggleButton.foreground", Color.WHITE);
        UIManager.put("ScrollPane.background", Color.BLACK);
        UIManager.put("Viewport.background", Color.BLACK);
        themeApplied = true;
    }

    public static void showReview(ReviewConfig config, Session session, List<SessionRow> allRows) {
        applyDarkTheme();
        SwingUtilities.invokeLater(() -> new ReviewUI(config, session, allRows));
    }

    public static void showExam(ExamConfig config, List<QuestionInfo> questions, Session session) {
        applyDarkTheme();
        SwingUtilities.invokeLater(() -> new ExamUI(config, questions, session));
    }
}
