package com.expensesplitter.uifactory;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.text.SimpleDateFormat;

import com.formdev.flatlaf.*;
import com.formdev.flatlaf.themes.*;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

public class SideBar extends JPanel {

    private JPanel         menuItemsContainer;
    private JPanel         contentPanel;
    private JScrollPane    scrollPane;
    private JPanel         sidebarWrapper;
    private JPanel         divider;
    private boolean        isExpanded      = true;
    private int            expandedWidth   = 230;
    private int            collapsedWidth  = 65;
    private int            minSidebarWidth = 160;
    private int            maxSidebarWidth = 400;
    private List<MenuItem> menuItems;
    private MenuItem       selectedItem;
    private AnimatedHeaderBar headerBar;
    private JLabel         headerTitle;
    private JLabel         dateTimeLabel;
    private Timer          clockTimer;
    private ActionListener logoutAction;
    private String         userName    = "User Name";
    private String         userStatus  = "Online";
    private BufferedImage  avatarImage = null;

    private static boolean isDarkTheme() {
        Color bg = UIManager.getColor("Panel.background");
        if (bg == null) return false;
        return (bg.getRed() * 299 + bg.getGreen() * 587 + bg.getBlue() * 114) / 1000 < 128;
    }

    private static Color getThemeAccentColor() {
        String[] keys = {
                "Component.accentColor", "Button.default.background",
                "Focus.color", "TabbedPane.underlineColor",
                "CheckBox.icon.selectedBackground", "ProgressBar.foreground"
        };
        for (String key : keys) {
            Color c = UIManager.getColor(key);
            if (c != null) return c;
        }
        return new Color(37, 99, 235);
    }

    private static Color getAdaptiveIconColor(Color base) {
        if (base == null) base = new Color(37, 99, 235);
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        float hue = hsb[0], saturation = hsb[1], brightness = hsb[2];
        if (isDarkTheme()) {
            return Color.getHSBColor(hue, Math.min(saturation + 0.10f, 0.75f), Math.max(brightness, 0.82f));
        } else {
            return Color.getHSBColor(hue, Math.min(saturation + 0.08f, 0.90f), Math.min(brightness, 0.72f));
        }
    }

    public static class MenuColors {
        public static Color lighten(Color c, float amt) {
            return new Color(
                    Math.min(255, (int)(c.getRed()   + (255 - c.getRed())   * amt)),
                    Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * amt)),
                    Math.min(255, (int)(c.getBlue()  + (255 - c.getBlue())  * amt))
            );
        }
        public static Color darken(Color c, float amt) {
            return new Color(
                    Math.max(0, (int)(c.getRed()   * (1f - amt))),
                    Math.max(0, (int)(c.getGreen() * (1f - amt))),
                    Math.max(0, (int)(c.getBlue()  * (1f - amt)))
            );
        }
    }

    class AnimatedHeaderBar extends JPanel {

        private Color currentColor;
        private Color targetColor;
        private float animProgress = 1f;
        private Timer colorAnimTimer;

        private float[] rippleAlpha  = {};
        private float[] rippleRadius = {};
        private int[]   rippleX      = {};
        private int[]   rippleY      = {};
        private Timer   rippleTimer;

        private float shimmerOffset = -1f;
        private Timer shimmerTimer;

        private float  titleAlpha   = 1f;
        private String pendingTitle = null;
        private Timer  titleFadeTimer;

        private static final int PARTICLE_COUNT = 22;
        private float[] px     = new float[PARTICLE_COUNT];
        private float[] py     = new float[PARTICLE_COUNT];
        private float[] pvx    = new float[PARTICLE_COUNT];
        private float[] pvy    = new float[PARTICLE_COUNT];
        private float[] psize  = new float[PARTICLE_COUNT];
        private float[] palpha = new float[PARTICLE_COUNT];
        private int[]   pshape = new int[PARTICLE_COUNT];
        private Timer   particleTimer;

        private float waveOffset = 0f;
        private Timer waveTimer;

        private float orbitAngle = 0f;
        private Timer orbitTimer;

        AnimatedHeaderBar() {
            currentColor = getThemeAccentColor();
            targetColor  = currentColor;

            setOpaque(true);
            setPreferredSize(new Dimension(0, 60));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 40)),
                    new EmptyBorder(12, 20, 12, 20)
            ));

            initParticles();

            particleTimer = new Timer(16, e -> {
                int w = getWidth(), h = getHeight();
                if (w <= 0 || h <= 0) return;
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    px[i] += pvx[i]; py[i] += pvy[i];
                    if (px[i] < -10)  px[i] = w + 10;
                    if (px[i] > w+10) px[i] = -10;
                    if (py[i] < -10)  py[i] = h + 10;
                    if (py[i] > h+10) py[i] = -10;
                }
                repaint();
            });
            particleTimer.start();

            waveTimer = new Timer(18, e -> { waveOffset += 0.018f; repaint(); });
            waveTimer.start();

            orbitTimer = new Timer(20, e -> { orbitAngle += 0.012f; repaint(); });
            orbitTimer.start();

            shimmerTimer = new Timer(22, e -> {
                shimmerOffset += 0.008f;
                if (shimmerOffset > 2f) shimmerOffset = -1f;
                repaint();
            });
            shimmerTimer.start();

            colorAnimTimer = new Timer(14, e -> {
                animProgress = Math.min(1f, animProgress + 0.045f);
                currentColor = interpolateColor(currentColor, targetColor, 0.045f * 3.5f);
                if (animProgress >= 1f) { currentColor = targetColor; ((Timer) e.getSource()).stop(); }
                repaint();
            });

            rippleTimer = new Timer(16, e -> {
                boolean any = false;
                for (int i = 0; i < rippleAlpha.length; i++) {
                    rippleRadius[i] += 9f;
                    rippleAlpha[i]  -= 0.028f;
                    if (rippleAlpha[i] > 0) any = true;
                }
                if (!any) ((Timer) e.getSource()).stop();
                repaint();
            });
        }

        private void initParticles() {
            Random rnd = new Random();
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                px[i]    = rnd.nextFloat() * 1400;
                py[i]    = rnd.nextFloat() * 60;
                float spd = 0.25f + rnd.nextFloat() * 0.55f;
                float ang = rnd.nextFloat() * (float)(Math.PI * 2);
                pvx[i]   = (float)(Math.cos(ang) * spd);
                pvy[i]   = (float)(Math.sin(ang) * spd);
                psize[i] = 2.5f + rnd.nextFloat() * 5.5f;
                palpha[i]= 0.08f + rnd.nextFloat() * 0.22f;
                pshape[i]= rnd.nextInt(3);
            }
        }

        void transitionToColor(Color target, int clickX, int clickY) {
            if (target.equals(targetColor)) return;
            targetColor  = target;
            animProgress = 0f;
            colorAnimTimer.restart();
            spawnRipple(clickX, clickY);
        }

        void syncToTheme() {
            Color accent = getThemeAccentColor();
            currentColor = accent;
            targetColor  = accent;
            repaint();
        }

        void transitionTitle(String newTitle) {
            if (newTitle.equals(headerTitle.getText())) return;
            pendingTitle = newTitle;
            if (titleFadeTimer != null) titleFadeTimer.stop();
            titleFadeTimer = new Timer(14, null);
            titleFadeTimer.addActionListener(e -> {
                if (pendingTitle != null) {
                    titleAlpha -= 0.12f;
                    if (titleAlpha <= 0f) {
                        titleAlpha = 0f;
                        headerTitle.setText(pendingTitle);
                        pendingTitle = null;
                    }
                } else {
                    titleAlpha += 0.12f;
                    if (titleAlpha >= 1f) { titleAlpha = 1f; ((Timer) e.getSource()).stop(); }
                }
                headerTitle.setForeground(new Color(1f, 1f, 1f, Math.max(0f, Math.min(1f, titleAlpha))));
                repaint();
            });
            titleFadeTimer.start();
        }

        private void spawnRipple(int x, int y) {
            int n = rippleAlpha.length;
            rippleAlpha  = Arrays.copyOf(rippleAlpha,  n + 1);
            rippleRadius = Arrays.copyOf(rippleRadius, n + 1);
            rippleX      = Arrays.copyOf(rippleX,      n + 1);
            rippleY      = Arrays.copyOf(rippleY,      n + 1);
            rippleAlpha[n]  = 0.45f;
            rippleRadius[n] = 10f;
            rippleX[n] = x;
            rippleY[n] = y;
            if (!rippleTimer.isRunning()) rippleTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,       RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            int w = getWidth(), h = getHeight();
            Color base  = currentColor;
            Color dark  = MenuColors.darken(base, 0.28f);
            Color light = MenuColors.lighten(base, 0.18f);

            g2.setPaint(new GradientPaint(0, 0, light, w, h, dark));
            g2.fillRect(0, 0, w, h);

            drawWave(g2, w, h, base);
            drawOrbitingRings(g2, w, h);
            drawParticles(g2);

            g2.setPaint(new GradientPaint(
                    (int)(w * (shimmerOffset - 0.35f)), 0, new Color(255, 255, 255, 0),
                    (int)(w * shimmerOffset),           0, new Color(255, 255, 255, 28), false));
            g2.fillRect(0, 0, w, h);

            for (int i = 0; i < rippleAlpha.length; i++) {
                if (rippleAlpha[i] <= 0) continue;
                int r = (int) rippleRadius[i];
                g2.setColor(new Color(1f, 1f, 1f, Math.min(1f, rippleAlpha[i])));
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawOval(rippleX[i] - r, rippleY[i] - r, r * 2, r * 2);
            }

            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillRect(0, h - 1, w, 1);
            g2.dispose();
        }

        private void drawWave(Graphics2D g2, int w, int h, Color base) {
            for (int wave = 0; wave < 2; wave++) {
                float phaseShift  = wave * 1.1f;
                float ampFraction = wave == 0 ? 0.38f : 0.22f;
                int   alpha       = wave == 0 ? 18 : 12;
                GeneralPath path  = new GeneralPath();
                path.moveTo(0, h);
                for (int x = 0; x <= w; x += 3) {
                    double y = h * (1 - ampFraction)
                            + Math.sin((x / (double) w) * Math.PI * 3.5 + waveOffset + phaseShift) * (h * ampFraction * 0.55)
                            + Math.cos((x / (double) w) * Math.PI * 2.0 + waveOffset * 0.7 + phaseShift) * (h * ampFraction * 0.3);
                    path.lineTo(x, (float) y);
                }
                path.lineTo(w, h);
                path.closePath();
                Color wc = MenuColors.lighten(base, 0.35f);
                g2.setColor(new Color(wc.getRed(), wc.getGreen(), wc.getBlue(), alpha));
                g2.fill(path);
            }
        }

        private void drawOrbitingRings(Graphics2D g2, int w, int h) {
            int[][] centers = { {w / 6, h / 2}, {w - w / 5, h / 2} };
            float[] radii   = { h * 0.55f, h * 0.40f };
            float[] alphas  = { 0.13f, 0.10f };
            float[] speeds  = { 1f, -1.4f };
            for (int c = 0; c < centers.length; c++) {
                int   cx = centers[c][0], cy = centers[c][1];
                float r  = radii[c];
                g2.setColor(new Color(255, 255, 255, (int)(alphas[c] * 80)));
                g2.setStroke(new BasicStroke(0.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 5}, 0));
                g2.drawOval((int)(cx - r), (int)(cy - r), (int)(r * 2), (int)(r * 2));
                g2.setStroke(new BasicStroke(1.5f));
                for (int d = 0; d < 2; d++) {
                    float angle = orbitAngle * speeds[c] + d * (float) Math.PI;
                    int   dx    = (int)(cx + Math.cos(angle) * r);
                    int   dy    = (int)(cy + Math.sin(angle) * r * 0.45f);
                    g2.setColor(new Color(1f, 1f, 1f, Math.min(1f, alphas[c])));
                    g2.fillOval(dx - 5, dy - 5, 10, 10);
                    g2.setColor(new Color(1f, 1f, 1f, Math.min(1f, alphas[c] * 2f)));
                    g2.fillOval(dx - 3, dy - 3, 6, 6);
                }
            }
        }

        private void drawParticles(Graphics2D g2) {
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                float a = palpha[i];
                float s = psize[i];
                int   x = (int) px[i], y = (int) py[i];
                g2.setColor(new Color(1f, 1f, 1f, Math.min(1f, a)));
                switch (pshape[i]) {
                    case 0 -> g2.fillOval(x, y, (int) s, (int) s);
                    case 1 -> {
                        int hs = (int)(s / 2);
                        g2.fillPolygon(new int[]{x, x+hs, x+(int)s, x+hs}, new int[]{y+hs, y, y+hs, y+(int)s}, 4);
                    }
                    case 2 -> {
                        int hs = (int)(s / 2), th = Math.max(1, (int)(s / 4));
                        g2.fillRect(x + hs - th, y, th * 2, (int) s);
                        g2.fillRect(x, y + hs - th, (int) s, th * 2);
                    }
                }
            }
        }

        private Color interpolateColor(Color a, Color b, float t) {
            t = Math.max(0, Math.min(1, t));
            return new Color(
                    (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
                    (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                    (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t)
            );
        }
    }

    public SideBar(ActionListener logoutAction) {
        this.logoutAction = logoutAction;
        setLayout(new BorderLayout());
        setOpaque(false);
        menuItems = new ArrayList<>();

        sidebarWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIManager.getColor("Panel.background"));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebarWrapper.setPreferredSize(new Dimension(expandedWidth, 0));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(8, 8, 5, 8));
        JPanel userProfilePanel = createUserProfilePanel();
        userProfilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(userProfilePanel);

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        menuItemsContainer = new JPanel();
        menuItemsContainer.setLayout(new BoxLayout(menuItemsContainer, BoxLayout.Y_AXIS));
        menuItemsContainer.setOpaque(false);
        menuItemsContainer.setBorder(new EmptyBorder(5, 0, 5, 0));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(separator,          BorderLayout.NORTH);
        centerPanel.add(menuItemsContainer, BorderLayout.CENTER);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.add(topPanel,    BorderLayout.NORTH);
        contentWrapper.add(centerPanel, BorderLayout.CENTER);

        scrollPane = new JScrollPane(contentWrapper);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JScrollBar vsb = scrollPane.getVerticalScrollBar();
        vsb.setPreferredSize(new Dimension(0, 0));
        vsb.setOpaque(false);
        vsb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {}
            @Override protected JButton createDecreaseButton(int o) { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
            @Override protected JButton createIncreaseButton(int o) { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
            @Override public void paintTrack(Graphics g, JComponent c, Rectangle r) {}
            @Override public void paintThumb(Graphics g, JComponent c, Rectangle r) {}
        });

        sidebarWrapper.add(scrollPane, BorderLayout.CENTER);

        divider      = createDivider();
        headerBar    = createHeaderBar();
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setOpaque(false);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(headerBar,    BorderLayout.NORTH);
        rightPanel.add(contentPanel, BorderLayout.CENTER);

        add(sidebarWrapper, BorderLayout.WEST);
        add(divider,        BorderLayout.CENTER);
        add(rightPanel,     BorderLayout.EAST);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { revalidate(); }
        });

        startClock();
        setupGlobalKeyboardShortcuts();
    }

    public void setAvatarImage(BufferedImage img) {
        this.avatarImage = img;
        updateUserProfileVisibility();
    }

    @Override
    public void doLayout() {
        int totalW = getWidth(), totalH = getHeight();
        int divW   = 8;
        int sideW  = sidebarWrapper.getPreferredSize().width;
        sidebarWrapper.setBounds(0, 0, sideW, totalH);
        divider.setBounds(sideW, 0, divW, totalH);
        Component rightPanel = getComponent(2);
        rightPanel.setBounds(sideW + divW, 0, Math.max(0, totalW - sideW - divW), totalH);
    }

    private JPanel createDivider() {
        JPanel d = new JPanel() {
            private boolean hovered  = false;
            private boolean dragging = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e)  { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)   { if (!dragging) { hovered = false; repaint(); } }
                    @Override public void mousePressed(MouseEvent e)  { dragging = true; }
                    @Override public void mouseReleased(MouseEvent e) { dragging = false; hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                if (hovered || dragging) {
                    Color accent = getThemeAccentColor();
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(accent);
                    g2.fillRect(cx - 1, 0, 2, getHeight());
                    int dotY = getHeight() / 2 - 12;
                    for (int i = 0; i < 3; i++) g2.fillOval(cx - 2, dotY + i * 10, 5, 5);
                } else {
                    g2.setColor(UIManager.getColor("Separator.foreground"));
                    g2.fillRect(cx, 0, 1, getHeight());
                }
                g2.dispose();
            }
        };
        d.setPreferredSize(new Dimension(8, 0));
        d.setOpaque(false);
        d.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));

        MouseAdapter resizeHandler = new MouseAdapter() {
            private int pressX, pressedSidebarWidth;
            @Override public void mousePressed(MouseEvent e) {
                pressX = SwingUtilities.convertPoint(d, e.getPoint(), SideBar.this).x;
                pressedSidebarWidth = sidebarWrapper.getPreferredSize().width;
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (!isExpanded) return;
                int cur = SwingUtilities.convertPoint(d, e.getPoint(), SideBar.this).x;
                int nw  = Math.max(minSidebarWidth, Math.min(maxSidebarWidth, pressedSidebarWidth + (cur - pressX)));
                expandedWidth = nw;
                sidebarWrapper.setPreferredSize(new Dimension(nw, sidebarWrapper.getHeight()));
                refreshSidebar();
                revalidate();
                repaint();
            }
        };
        d.addMouseListener(resizeHandler);
        d.addMouseMotionListener(resizeHandler);
        return d;
    }

    private AnimatedHeaderBar createHeaderBar() {
        AnimatedHeaderBar header = new AnimatedHeaderBar();
        header.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        headerTitle = new JLabel("") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerTitle.setForeground(Color.WHITE);
        leftPanel.add(headerTitle);

        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dateTimeLabel.setForeground(new Color(255, 255, 255, 225));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(dateTimeLabel);
        rightPanel.add(buildLogoutButton());

        header.add(leftPanel,  BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JButton buildLogoutButton() {
        JButton btn = new JButton() {
            private float hoverAmt = 0f;
            private Timer hoverTimer;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { animHov(true);  }
                    @Override public void mouseExited(MouseEvent e)  { animHov(false); }
                });
            }
            void animHov(boolean in) {
                if (hoverTimer != null) hoverTimer.stop();
                hoverTimer = new Timer(12, ev -> {
                    hoverAmt += in ? 0.12f : -0.12f;
                    hoverAmt = Math.max(0f, Math.min(1f, hoverAmt));
                    if ((in && hoverAmt >= 1f) || (!in && hoverAmt <= 0f)) ((Timer) ev.getSource()).stop();
                    repaint();
                });
                hoverTimer.start();
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                int r  = (int)(180 + (220 - 180) * hoverAmt);
                int gr = (int)(30  + (38  - 30)  * hoverAmt);
                int b  = (int)(30  + (38  - 30)  * hoverAmt);
                int a  = (int)(200 + (240 - 200) * hoverAmt);
                g2.setColor(new Color(r, gr, b, a));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                FontIcon.of(FontAwesomeSolid.SIGN_OUT_ALT, 14, Color.WHITE).paintIcon(this, g2, 10, (getHeight() - 14) / 2);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("Logout", 32, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(98, 36); }
        };
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(logoutAction);
        return btn;
    }

    private void selectMenuItem(MenuItem item) {
        selectMenuItem(item, headerBar.getWidth() / 2, headerBar.getHeight() / 2);
    }

    private void selectMenuItem(MenuItem item, int clickX, int clickY) {
        selectedItem = item;
        headerBar.transitionToColor(getThemeAccentColor(), clickX, clickY);
        headerBar.transitionTitle(item.title);
        if (item.content != null && item.content.getName() != null)
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, item.content.getName());
        menuItemsContainer.repaint();
    }

    private void startClock() {
        updateDateTime();
        clockTimer = new Timer(1000, e -> updateDateTime());
        clockTimer.start();
    }

    private void updateDateTime() {
        dateTimeLabel.setText(new SimpleDateFormat("EEE, MMM dd yyyy  |  hh:mm:ss a").format(new Date()));
    }

    private static class MenuItem {
        String title, colorKey;
        Ikon   icon;
        Color  iconColor;
        JComponent content;
        int    level;
        List<MenuItem> children = new ArrayList<>();
        MenuItem parent;
        boolean isExpanded = false;
        JButton button;

        MenuItem(String title, String colorKey, Ikon icon, Color iconColor, JComponent content, int level) {
            this.title = title; this.colorKey = colorKey; this.icon = icon;
            this.iconColor = iconColor; this.content = content; this.level = level;
        }

        void addChild(MenuItem child) { child.parent = this; children.add(child); }

        boolean isAncestorOf(MenuItem other) {
            MenuItem cur = other.parent;
            while (cur != null) { if (cur == this) return true; cur = cur.parent; }
            return false;
        }
    }

    public void addMenuItem(String title, String colorKey, Ikon icon, Color iconColor, JComponent content) {
        MenuItem item = new MenuItem(title, colorKey, icon, iconColor, content, 1);
        menuItems.add(item);
        if (content != null) {
            content.setName(String.valueOf(System.identityHashCode(item)));
            contentPanel.add(content, content.getName());
        }
        refreshSidebar();
        if (menuItems.size() == 1 && item.children.isEmpty()) selectMenuItem(item);
    }

    public void addSubMenuItem(String parentPath, String title, String colorKey, Ikon icon, Color iconColor, JComponent content, int level) {
        MenuItem parent = findMenuItem(parentPath.split("/"), 0, menuItems);
        if (parent != null) {
            MenuItem sub = new MenuItem(title, colorKey, icon, iconColor, content, level);
            parent.addChild(sub);
            if (content != null) {
                sub.content.setName(String.valueOf(System.identityHashCode(sub)));
                contentPanel.add(sub.content, sub.content.getName());
            }
            refreshSidebar();
        }
    }

    private MenuItem findMenuItem(String[] path, int index, List<MenuItem> items) {
        if (index >= path.length) return null;
        for (MenuItem item : items) {
            if (item.title.equals(path[index]))
                return index == path.length - 1 ? item : findMenuItem(path, index + 1, item.children);
        }
        return null;
    }

    private void addMenuItemsRecursive(List<MenuItem> items) {
        for (MenuItem item : items) {
            JButton btn = createMenuButton(item);
            item.button = btn;
            menuItemsContainer.add(btn);
            menuItemsContainer.add(Box.createRigidArea(new Dimension(0, 3)));
            if (item.isExpanded && !item.children.isEmpty() && isExpanded)
                addMenuItemsRecursive(item.children);
        }
    }

    private Color getSidebarBg() {
        Color bg = UIManager.getColor("Panel.background");
        return bg != null ? bg : Color.WHITE;
    }

    private Color getHoverColor() {
        Color bg = getSidebarBg();
        int avg  = (bg.getRed() + bg.getGreen() + bg.getBlue()) / 3;
        return avg < 128
                ? new Color(Math.min(255, bg.getRed() + 30), Math.min(255, bg.getGreen() + 30), Math.min(255, bg.getBlue() + 30), 180)
                : new Color(Math.max(0, bg.getRed() - 22),   Math.max(0, bg.getGreen() - 22),   Math.max(0, bg.getBlue() - 22),   200);
    }

    public void setUserProfile(String name, String status) {
        this.userName   = name;
        this.userStatus = status;
        refreshUserProfile();
    }

    private void refreshUserProfile() {
        try {
            JPanel cw  = (JPanel) ((JScrollPane) sidebarWrapper.getComponent(0)).getViewport().getView();
            JPanel top = (JPanel) cw.getComponent(0);
            JPanel txt = (JPanel) ((JPanel) top.getComponent(0)).getComponent(1);
            ((JLabel) txt.getComponent(0)).setText(userName);
            ((JLabel) txt.getComponent(1)).setText(userStatus);
        } catch (Exception ignored) {}
    }

    private JButton createMenuButton(MenuItem item) {
        JButton button = new JButton() {
            private float hoverAmt = 0f;
            private Timer hoverTimer;

            void animHov(boolean in) {
                if (hoverTimer != null) hoverTimer.stop();
                hoverTimer = new Timer(12, ev -> {
                    hoverAmt += in ? 0.14f : -0.14f;
                    hoverAmt  = Math.max(0f, Math.min(1f, hoverAmt));
                    if ((in && hoverAmt >= 1f) || (!in && hoverAmt <= 0f)) ((Timer) ev.getSource()).stop();
                    repaint();
                });
                hoverTimer.start();
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                boolean selected = (selectedItem == item);

                if (selected) {
                    g2.setColor(getThemeAccentColor());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                } else if (hoverAmt > 0f) {
                    Color hc = getHoverColor();
                    g2.setColor(new Color(hc.getRed(), hc.getGreen(), hc.getBlue(), (int)(hc.getAlpha() * hoverAmt)));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                }

                Color iconClr = selected ? Color.WHITE : getAdaptiveIconColor(item.iconColor);
                Color textClr;
                if (selected) {
                    textClr = Color.WHITE;
                } else {
                    Color fg = UIManager.getColor("Label.foreground");
                    textClr = fg != null ? fg : (isDarkTheme() ? new Color(220, 220, 220) : new Color(30, 30, 30));
                }

                if (isExpanded) {
                    int x = (item.level - 1) * 16 + 10;
                    if (item.icon != null) {
                        int sz = selected ? 19 : 17;
                        FontIcon.of(item.icon, sz, iconClr).paintIcon(this, g2, x, (getHeight() - sz) / 2);
                        x += 28;
                    }
                    g2.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 13));
                    g2.setColor(textClr);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(item.title, x, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    if (!item.children.isEmpty()) {
                        Ikon arrow = item.isExpanded ? FontAwesomeSolid.CHEVRON_DOWN : FontAwesomeSolid.CHEVRON_RIGHT;
                        Color arrC = selected
                                ? new Color(255, 255, 255, 200)
                                : new Color(textClr.getRed(), textClr.getGreen(), textClr.getBlue(), 160);
                        FontIcon.of(arrow, 11, arrC).paintIcon(this, g2, getWidth() - 26, (getHeight() - 11) / 2);
                    }
                } else {
                    if (item.icon != null) {
                        int sz = selected ? 22 : 20;
                        FontIcon.of(item.icon, sz, iconClr).paintIcon(this, g2, (getWidth() - sz) / 2 - 7, (getHeight() - sz) / 2);
                    }
                }
                g2.dispose();
            }

            @Override public Dimension getPreferredSize() { return new Dimension(isExpanded ? expandedWidth - 20 : collapsedWidth, 42); }
            @Override public Dimension getMaximumSize()   { return new Dimension(Integer.MAX_VALUE, 42); }

            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { animHov(true);  }
                public void mouseExited(MouseEvent e)  { animHov(false); }
            }); }
        };

        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.addActionListener(e -> {
            int cx = headerBar.getWidth() / 2;
            int cy = headerBar.getHeight() / 2;
            if (!item.children.isEmpty()) {
                if (!isExpanded) { selectMenuItem(getFirstLeaf(item), cx, cy); }
                else {
                    item.isExpanded = !item.isExpanded;
                    if (item.isExpanded) selectMenuItem(getFirstLeaf(item), cx, cy);
                    refreshSidebar();
                }
            } else {
                selectMenuItem(item, cx, cy);
            }
        });
        return button;
    }

    private MenuItem getFirstLeaf(MenuItem item) {
        return item.children.isEmpty() ? item : getFirstLeaf(item.children.get(0));
    }

    private JLabel buildAvatarLabel(int size) {
        return new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (avatarImage != null) {
                    BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D sg = scaled.createGraphics();
                    sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    sg.setClip(new Ellipse2D.Float(0, 0, size, size));
                    sg.drawImage(avatarImage, 0, 0, size, size, null);
                    sg.dispose();
                    g2.drawImage(scaled, 0, 0, null);
                    g2.setColor(getThemeAccentColor());
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(1, 1, size - 2, size - 2);
                } else {
                    Color accent = getThemeAccentColor();
                    g2.setPaint(new GradientPaint(0, 0, accent, size, size, MenuColors.lighten(accent, 0.3f)));
                    g2.fillOval(0, 0, size, size);
                    FontIcon.of(FontAwesomeSolid.USER, size / 2, Color.WHITE).paintIcon(this, g2, size / 4, size / 4);
                }
                g2.setColor(new Color(34, 197, 94));
                g2.fillOval(size - 11, size - 11, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(size - 11, size - 11, 10, 10);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(size, size); }
        };
    }

    private JPanel createUserProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0)) {
            private boolean hovered = false;
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (hovered) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getHoverColor());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                }
            }
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                public void mouseClicked(MouseEvent e) { toggleSidebar(); }
            }); }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 1, 10, 10));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(userName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusLabel = new JLabel(userStatus);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(100, 160, 100));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(nameLabel);
        textPanel.add(statusLabel);

        JLabel toggleIcon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Color fg = UIManager.getColor("Label.foreground");
                Color c  = fg != null ? new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 160) : new Color(120, 120, 120);
                FontIcon.of(isExpanded ? FontAwesomeSolid.CHEVRON_LEFT : FontAwesomeSolid.CHEVRON_RIGHT, 14, c).paintIcon(this, g2, 0, 0);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(14, 14); }
        };

        panel.add(buildAvatarLabel(36), BorderLayout.WEST);
        panel.add(textPanel,            BorderLayout.CENTER);
        panel.add(toggleIcon,           BorderLayout.EAST);
        return panel;
    }

    private JPanel createCollapsedUserProfile() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6)) {
            @Override protected void paintComponent(Graphics g) { super.paintComponent(g); }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(collapsedWidth, 52));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        p.add(buildAvatarLabel(38));
        p.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { toggleSidebar(); } });
        return p;
    }

    private void updateUserProfileVisibility() {
        try {
            JPanel cw  = (JPanel) ((JScrollPane) sidebarWrapper.getComponent(0)).getViewport().getView();
            JPanel top = (JPanel) cw.getComponent(0);
            top.removeAll();
            JPanel profile = isExpanded ? createUserProfilePanel() : createCollapsedUserProfile();
            profile.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(profile);
            top.revalidate();
            top.repaint();
        } catch (Exception ignored) {}
    }

    public void refreshSidebar() {
        menuItemsContainer.removeAll();
        menuItemsContainer.setBorder(isExpanded ? new EmptyBorder(5, 5, 5, 4) : new EmptyBorder(5, 0, 5, 0));
        addMenuItemsRecursive(menuItems);
        menuItemsContainer.revalidate();
        menuItemsContainer.repaint();
        if (headerBar != null) headerBar.syncToTheme();
    }

    private void toggleSidebar() {
        isExpanded = !isExpanded;
        int targetWidth = isExpanded ? expandedWidth : collapsedWidth;
        divider.setCursor(isExpanded
                ? Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)
                : Cursor.getDefaultCursor());

        Timer timer = new Timer(0, null);
        timer.addActionListener(new ActionListener() {
            int current = sidebarWrapper.getPreferredSize().width;
            final int step = isExpanded ? 10 : -10;
            @Override public void actionPerformed(ActionEvent e) {
                current += step;
                if ((isExpanded && current >= targetWidth) || (!isExpanded && current <= targetWidth)) {
                    current = targetWidth;
                    timer.stop();
                    updateUserProfileVisibility();
                }
                sidebarWrapper.setPreferredSize(new Dimension(current, sidebarWrapper.getHeight()));
                refreshSidebar();
                revalidate();
                repaint();
            }
        });
        timer.start();
    }

    private void handleMenuItemKeyPress(MenuItem item) {
        if (!item.children.isEmpty()) {
            if (!isExpanded) selectMenuItem(getFirstLeaf(item));
            else { item.isExpanded = !item.isExpanded; if (item.isExpanded) selectMenuItem(getFirstLeaf(item)); refreshSidebar(); }
        } else { selectMenuItem(item); }
    }

    private void navigateMenuItems(int dir) {
        List<MenuItem> flat = new ArrayList<>();
        collectVisible(menuItems, flat);
        if (flat.isEmpty()) return;
        if (selectedItem == null) { selectMenuItem(flat.get(0)); return; }
        int next = flat.indexOf(selectedItem) + dir;
        if (next >= 0 && next < flat.size()) selectMenuItem(flat.get(next));
    }

    private void collectVisible(List<MenuItem> items, List<MenuItem> result) {
        for (MenuItem item : items) {
            if      (item.children.isEmpty())       result.add(item);
            else if (item.isExpanded && isExpanded)  collectVisible(item.children, result);
            else                                     result.add(item);
        }
    }

    private void setupGlobalKeyboardShortcuts() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED) return false;
            if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == 0) return false;
            Component f = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (f instanceof JTextField || f instanceof JTextArea
                    || f instanceof JFormattedTextField || f instanceof JPasswordField) return false;
            int key = e.getKeyCode();
            if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_9) {
                int idx = key - KeyEvent.VK_1;
                if (idx < menuItems.size()) { handleMenuItemKeyPress(menuItems.get(idx)); return true; }
            }
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) { toggleSidebar();       return true; }
            if (key == KeyEvent.VK_UP)                                { navigateMenuItems(-1); return true; }
            if (key == KeyEvent.VK_DOWN)                              { navigateMenuItems(1);  return true; }
            return false;
        });
    }

    public void selectMenuItemByName(String name) {
        MenuItem found = findMenuItemByTitle(name, menuItems);
        if (found != null) selectMenuItem(found);
    }

    private MenuItem findMenuItemByTitle(String name, List<MenuItem> items) {
        for (MenuItem item : items) {
            if (item.title.equals(name)) return item;
            MenuItem found = findMenuItemByTitle(name, item.children);
            if (found != null) return found;
        }
        return null;
    }

    private static JPanel createPanel(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}