package acme.certprep;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class TestUI {
    JFrame frame;
    ScalableImageLabel imageLabel;
    JPanel controlPanel, checkboxPanel, pacingPanel;
    JScrollPane scrollPane;
    JProgressBar qBar, tBar, cBar;
    JButton actionBtn;
    JToggleButton wbBtn, fitBtn;
    WhiteboardPanel wb;
    List<JCheckBox> boxes = new ArrayList<>();
    ArgParser config;
    QuestionBank bank;
    SessionManager session;
    int idx = 0, qSec = 0, tSec = 0, totalT;
    Timer timer;

    TestUI(ArgParser config, QuestionBank bank, SessionManager session) {
        this.config = config;
        this.bank = bank;
        this.session = session;
        this.totalT = bank.questions.size() * 108;
        frame = new JFrame();
        frame.setUndecorated(true);
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
        frame.setLayout(new BorderLayout());
        imageLabel = new ScalableImageLabel();
        scrollPane = new JScrollPane(imageLabel);
        frame.add(scrollPane, BorderLayout.CENTER);
        wb = new WhiteboardPanel();
        wb.setVisible(false);
        frame.add(wb, BorderLayout.EAST);
        controlPanel = new JPanel(new BorderLayout());
        pacingPanel = new JPanel(new GridLayout(3, 1));
        qBar = new JProgressBar(0, 108);
        qBar.setStringPainted(true);
        tBar = new JProgressBar(0, totalT);
        tBar.setStringPainted(true);
        tBar.setForeground(new Color(100, 100, 255));
        cBar = new JProgressBar(0, bank.questions.size());
        cBar.setStringPainted(true);
        pacingPanel.add(qBar);
        pacingPanel.add(tBar);
        pacingPanel.add(cBar);
        controlPanel.add(pacingPanel, BorderLayout.NORTH);
        checkboxPanel = new JPanel();
        controlPanel.add(checkboxPanel, BorderLayout.CENTER);
        JPanel bp = new JPanel();
        actionBtn = new JButton("Answer");
        wbBtn = new JToggleButton("Whiteboard");
        fitBtn = new JToggleButton("Fit");
        JButton ex = new JButton("Exit");
        bp.add(actionBtn);
        bp.add(fitBtn);
        bp.add(wbBtn);
        bp.add(ex);
        controlPanel.add(bp, BorderLayout.SOUTH);
        frame.add(controlPanel, BorderLayout.SOUTH);
        timer = new Timer(1000, e -> update());
        actionBtn.addActionListener(e -> next());
        wbBtn.addActionListener(e -> wb.setVisible(wbBtn.isSelected()));
        fitBtn.addActionListener(e -> {
            boolean fit = fitBtn.isSelected();
            imageLabel.setFitMode(fit);
            scrollPane.setHorizontalScrollBarPolicy(fit ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(fit ? ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER : ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.revalidate();
        });
        ex.addActionListener(e -> System.exit(0));
        frame.setVisible(true);
        load();
        timer.start();
    }

    private void update() {
        qSec++;
        tSec++;
        qBar.setValue(Math.min(qSec, 108));
        qBar.setString("Question: " + qSec + "s");
        qBar.setForeground(qSec <= 64 ? Color.GREEN : (qSec <= 100 ? Color.YELLOW : Color.RED));
        tBar.setValue(Math.min(tSec, totalT));
        tBar.setString("Total: " + tSec + "s");
        cBar.setValue(idx);
        cBar.setString("Done: " + idx + "/" + bank.questions.size());
        double cP = (double) idx / bank.questions.size();
        double tP = (double) tSec / totalT;
        cBar.setForeground(cP > tP ? Color.GREEN : (tP - cP <= (1.0 / bank.questions.size()) ? Color.YELLOW : Color.RED));
    }

    private void load() {
        QuestionInfo q = bank.questions.get(idx);
        imageLabel.setIcon(new ImageIcon(Paths.get(config.dataDir, String.format("ch%02d-q%02d.png", q.ch, q.q)).toString()));
        checkboxPanel.removeAll();
        boxes.clear();
        for (String o : q.p) {
            JCheckBox b = new JCheckBox(o);
            b.setFocusPainted(false);
            boxes.add(b);
            checkboxPanel.add(b);
        }
        checkboxPanel.revalidate();
        checkboxPanel.repaint();
        qSec = 0;
    }

    private void next() {
        QuestionInfo q = bank.questions.get(idx);
        List<String> s = new ArrayList<>();
        for (JCheckBox b : boxes) if (b.isSelected()) s.add(b.getText());
        session.logAnswer(q, String.join(",", s), qSec, String.join(",", s).equals(q.a));
        if (idx == bank.questions.size() - 1) System.exit(0);
        else {
            idx++;
            load();
        }
    }
}
