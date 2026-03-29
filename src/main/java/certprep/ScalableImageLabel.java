package certprep;

import javax.swing.*;
import java.awt.*;

class ScalableImageLabel extends JLabel {
    private ImageIcon originalIcon;
    private boolean fitMode = false;

    public ScalableImageLabel() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
    }

    @Override
    public void setIcon(Icon icon) {
        super.setIcon(icon);
        if (icon instanceof ImageIcon) this.originalIcon = (ImageIcon) icon;
    }

    @Override
    public Dimension getPreferredSize() {
        if (fitMode) return new Dimension(10, 10);
        return super.getPreferredSize();
    }

    public void setFitMode(boolean fit) {
        this.fitMode = fit;
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (fitMode && originalIcon != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int imgW = originalIcon.getIconWidth();
            int imgH = originalIcon.getIconHeight();
            int panelW = getWidth();
            int panelH = getHeight();
            if (imgW > 0 && imgH > 0) {
                double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);
                int nW = (int) (imgW * scale);
                int nH = (int) (imgH * scale);
                g2d.drawImage(originalIcon.getImage(), (panelW - nW) / 2, (panelH - nH) / 2, nW, nH, this);
            }
            g2d.dispose();
        } else {
            super.paintComponent(g);
        }
    }
}
