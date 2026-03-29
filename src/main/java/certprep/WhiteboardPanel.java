package certprep;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

class WhiteboardPanel extends JPanel {
    BufferedImage canvas;
    Graphics2D g2d;
    int lx, ly;
    JToggleButton tBtn;
    JPanel cp;

    WhiteboardPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 0));
        setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.DARK_GRAY));
        JPanel tools = new JPanel();
        tools.setBackground(Color.BLACK);
        tBtn = new JToggleButton("T");
        JButton dBtn = new JButton("Delete");
        tools.add(tBtn);
        tools.add(dBtn);
        add(tools, BorderLayout.NORTH);
        cp = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (canvas == null) {
                    canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                    g2d = canvas.createGraphics();
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.setFont(new Font("Arial", Font.PLAIN, 18));
                }
                g.drawImage(canvas, 0, 0, null);
            }
        };
        cp.setLayout(null);
        cp.setBackground(Color.BLACK);
        cp.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (tBtn.isSelected()) {
                    addText(e.getX(), e.getY());
                    tBtn.setSelected(false);
                } else {
                    lx = e.getX();
                    ly = e.getY();
                }
            }
        });
        cp.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (!tBtn.isSelected() && canvas != null) {
                    g2d.drawLine(lx, ly, e.getX(), e.getY());
                    lx = e.getX();
                    ly = e.getY();
                    cp.repaint();
                }
            }
        });
        dBtn.addActionListener(e -> {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g2d.setColor(Color.WHITE);
            cp.removeAll();
            cp.repaint();
        });
        add(cp, BorderLayout.CENTER);
    }

    private void addText(int x, int y) {
        JTextField f = new JTextField();
        f.setBounds(x, y, 200, 30);
        f.addActionListener(e -> {
            g2d.drawString(f.getText(), f.getX() + 4, f.getY() + 22);
            cp.remove(f);
            cp.repaint();
        });
        cp.add(f);
        cp.repaint();
        f.requestFocus();
    }
}
