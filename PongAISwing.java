import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class PongAISwing extends JPanel implements ActionListener {

    // --- Spielfeld ---
    private static final int WIDTH = 900;
    private static final int HEIGHT = 600;

    // --- Paddles & Ball ---
    private static final int PADDLE_W = 12;
    private static final int PADDLE_H = 100;
    private static final int BALL_SIZE = 14;

    private static final int PLAYER_X = 40;
    private static final int AI_X = WIDTH - 40 - PADDLE_W;

    // Geschwindigkeiten
    private static final int PLAYER_SPEED = 10;
    private static final double BALL_SPEED_START = 6.0;
    private static final double BALL_SPEED_MAX   = 14.0;
    private static final double BALL_SPEED_STEP  = 0.6; // bei Paddle-Treffern
    private static final double AI_MAX_SPEED_EASY   = 10.0;
    private static final double AI_MAX_SPEED_MEDIUM = 15.5;
    private static final double AI_MAX_SPEED_HARD   = 20.0;

    // AI-Schwierigkeit (wähle eins)
    private static final double AI_MAX_SPEED = AI_MAX_SPEED_MEDIUM;
    private static final double AI_SMOOTHING = 0.3; // 0..1 (je höher, desto „nervöser“)
    private static final int    AI_REACTION_FRAMES = 6; // Reaktionsverzögerung

    // Zustände
    private double playerY = (HEIGHT - PADDLE_H) / 2.0;
    private double aiY     = (HEIGHT - PADDLE_H) / 2.0;

    private double ballX = WIDTH / 2.0 - BALL_SIZE / 2.0;
    private double ballY = HEIGHT / 2.0 - BALL_SIZE / 2.0;
    private double ballVX;
    private double ballVY;

    private int scorePlayer = 0;
    private int scoreAI = 0;

    private boolean upPressed = false;
    private boolean downPressed = false;

    private boolean paused = false;
    private boolean showHelp = true;

    private final Timer timer;
    private final Random rnd = new Random();
    private int frameCount = 0;

    public PongAISwing() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(18, 18, 22));
        setFocusable(true);

        initBall(true);

        // Timer ~60 FPS
        timer = new Timer(1000 / 60, this);
        timer.start();

        // Keyboard
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_UP:    upPressed = true;  break;
                    case KeyEvent.VK_S:
                    case KeyEvent.VK_DOWN:  downPressed = true; break;
                    case KeyEvent.VK_P:
                    case KeyEvent.VK_ESCAPE:
                        paused = !paused;
                        showHelp = false;
                        break;
                    case KeyEvent.VK_R:
                        resetGame();
                        break;
                    case KeyEvent.VK_H:
                        showHelp = !showHelp;
                        break;
                }
            }
            @Override public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_UP:    upPressed = false;  break;
                    case KeyEvent.VK_S:
                    case KeyEvent.VK_DOWN:  downPressed = false; break;
                }
            }
        });

        // Maus-Klick entfernt Help-Overlay
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showHelp = false; requestFocusInWindow(); }
        });
    }

    private void resetGame() {
        scoreAI = scorePlayer = 0;
        playerY = (HEIGHT - PADDLE_H) / 2.0;
        aiY = (HEIGHT - PADDLE_H) / 2.0;
        initBall(true);
        paused = false;
        showHelp = true;
    }

    private void initBall(boolean randomDirectionToPlayer) {
        ballX = WIDTH / 2.0 - BALL_SIZE / 2.0;
        ballY = HEIGHT / 2.0 - BALL_SIZE / 2.0;

        double angle;
        // Winkel zwischen -45° und 45° (aber nicht zu klein)
        do {
            angle = (rnd.nextDouble() * Math.toRadians(90)) - Math.toRadians(45);
        } while (Math.abs(angle) < Math.toRadians(10));

        double speed = BALL_SPEED_START;

        // Richtung initial wahlweise zum Spieler, damit du öfter was zu tun hast
        double dir = randomDirectionToPlayer ? (rnd.nextBoolean() ? -1 : 1) : (rnd.nextBoolean() ? -1 : 1);
        ballVX = Math.cos(angle) * speed * dir;
        ballVY = Math.sin(angle) * speed * (rnd.nextBoolean() ? 1 : -1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused) {
            updatePlayer();
            updateAI();
            updateBall();
        }
        repaint();
        frameCount++;
    }

    private void updatePlayer() {
        if (upPressed)   playerY -= PLAYER_SPEED;
        if (downPressed) playerY += PLAYER_SPEED;
        if (playerY < 0) playerY = 0;
        if (playerY > HEIGHT - PADDLE_H) playerY = HEIGHT - PADDLE_H;
    }

    private void updateAI() {
        // Reaktion nur alle X Frames (künstliche Verzögerung)
        if (frameCount % AI_REACTION_FRAMES != 0) return;

        // Ziel: Ball-Mitte anvisieren
        double target = (ballY + BALL_SIZE / 2.0) - PADDLE_H / 2.0;

        // optionaler kleiner „Fehler“: wenn Ball weit weg, zieh einen Versatz dazu
        double distX = Math.abs(ballX - AI_X);
        double error = Math.min(60, distX / 12.0); // max ~60px Fehler
        target += (rnd.nextDouble() * error - error / 2.0);

        // sanft zum Ziel bewegen (Smoothing)
        double desired = target - aiY;
        double step = desired * AI_SMOOTHING;

        // max Speed begrenzen
        if (step > AI_MAX_SPEED) step = AI_MAX_SPEED;
        if (step < -AI_MAX_SPEED) step = -AI_MAX_SPEED;

        aiY += step;

        if (aiY < 0) aiY = 0;
        if (aiY > HEIGHT - PADDLE_H) aiY = HEIGHT - PADDLE_H;
    }

    private void updateBall() {
        ballX += ballVX;
        ballY += ballVY;

        // Oben/unten abprallen
        if (ballY <= 0) {
            ballY = 0;
            ballVY = Math.abs(ballVY);
        } else if (ballY + BALL_SIZE >= HEIGHT) {
            ballY = HEIGHT - BALL_SIZE;
            ballVY = -Math.abs(ballVY);
        }

        // Kollision mit Player-Paddle
        Rectangle ball = new Rectangle((int)ballX, (int)ballY, BALL_SIZE, BALL_SIZE);
        Rectangle paddlePlayer = new Rectangle(PLAYER_X, (int)playerY, PADDLE_W, PADDLE_H);
        Rectangle paddleAI     = new Rectangle(AI_X,     (int)aiY,     PADDLE_W, PADDLE_H);

        if (ball.intersects(paddlePlayer) && ballVX < 0) {
            // Auftreffpunkt berechnen → Winkel ändern
            double hitPos = (ballY + BALL_SIZE/2.0) - (playerY + PADDLE_H/2.0);
            double norm = hitPos / (PADDLE_H/2.0); // -1..1
            double angle = norm * Math.toRadians(50); // maximaler Auslenkwinkel
            double speed = Math.min(BALL_SPEED_MAX, Math.hypot(ballVX, ballVY) + BALL_SPEED_STEP);
            ballVX = Math.cos(angle) * speed;
            ballVY = Math.sin(angle) * speed;
            // leichte Verschiebung um "Kleben" zu vermeiden
            ballX = PLAYER_X + PADDLE_W + 1;
        }

        // Kollision mit AI-Paddle
        if (ball.intersects(paddleAI) && ballVX > 0) {
            double hitPos = (ballY + BALL_SIZE/2.0) - (aiY + PADDLE_H/2.0);
            double norm = hitPos / (PADDLE_H/2.0); // -1..1
            double angle = norm * Math.toRadians(50);
            double speed = Math.min(BALL_SPEED_MAX, Math.hypot(ballVX, ballVY) + BALL_SPEED_STEP);
            ballVX = -Math.cos(angle) * speed;
            ballVY = Math.sin(angle) * speed;
            ballX = AI_X - BALL_SIZE - 1;
        }

        // Punkt für AI (Ball links raus)
        if (ballX + BALL_SIZE < 0) {
            scoreAI++;
            initBall(false);
        }
        // Punkt für Spieler (Ball rechts raus)
        if (ballX > WIDTH) {
            scorePlayer++;
            initBall(false);
        }
    }

    @Override
    protected void paintComponent(Graphics gRaw) {
        super.paintComponent(gRaw);
        Graphics2D g = (Graphics2D) gRaw.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hintergrund
        g.setColor(new Color(24, 24, 30));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Mittellinie (gestrichelt)
        g.setColor(new Color(60, 60, 70));
        for (int y = 0; y < HEIGHT; y += 24) {
            g.fillRect(WIDTH/2 - 2, y, 4, 14);
        }

        // Paddles
        g.setColor(new Color(230, 230, 240));
        g.fillRoundRect(PLAYER_X, (int)playerY, PADDLE_W, PADDLE_H, 10, 10);
        g.fillRoundRect(AI_X,     (int)aiY,     PADDLE_W, PADDLE_H, 10, 10);

        // Ball (leichter Schein)
        g.setColor(new Color(200, 220, 255));
        g.fillOval((int)ballX, (int)ballY, BALL_SIZE, BALL_SIZE);
        g.setColor(new Color(140, 160, 200, 120));
        g.fillOval((int)ballX - 3, (int)ballY - 3, BALL_SIZE + 6, BALL_SIZE + 6);

        // Score
        g.setFont(new Font("Consolas", Font.BOLD, 56));
        g.setColor(new Color(240, 240, 250));
        String sL = String.valueOf(scorePlayer);
        String sR = String.valueOf(scoreAI);
        int wL = g.getFontMetrics().stringWidth(sL);
        int wR = g.getFontMetrics().stringWidth(sR);
        g.drawString(sL, WIDTH/2 - 50 - wL, 70);
        g.drawString(sR, WIDTH/2 + 50, 70);

        // Bottom-Bar
        g.setFont(new Font("Inter", Font.PLAIN, 14));
        g.setColor(new Color(160,160,180));
        String info = paused ? "PAUSE (P/ESC)" : "W/S oder ↑/↓   |   P/ESC: Pause   |   R: Reset   |   H: Hilfe";
        g.drawString(info, 20, HEIGHT - 16);

        if (showHelp) {
            drawHelpOverlay(g);
        }

        g.dispose();
    }

    private void drawHelpOverlay(Graphics2D g) {
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        g.setColor(new Color(15,15,20));
        g.fillRoundRect(WIDTH/2 - 250, HEIGHT/2 - 140, 500, 240, 20, 20);
        g.setComposite(old);

        g.setColor(new Color(230,230,245));
        g.setFont(new Font("Inter", Font.BOLD, 22));
        g.drawString("Pong – Spieler vs. Computer", WIDTH/2 - 200, HEIGHT/2 - 90);

        g.setFont(new Font("Inter", Font.PLAIN, 16));
        int y = HEIGHT/2 - 50;
        g.drawString("Steuerung:  W/S oder Pfeil ↑/↓", WIDTH/2 - 200, y); y += 24;
        g.drawString("Pause:      P oder ESC", WIDTH/2 - 200, y); y += 24;
        g.drawString("Reset:      R", WIDTH/2 - 200, y); y += 24;
        g.drawString("Hilfe:      H", WIDTH/2 - 200, y); y += 24;

        g.setColor(new Color(160,180,255));
        g.drawString("Klick oder Taste drücken, um zu starten …", WIDTH/2 - 200, HEIGHT/2 + 80);
    }

    // --- Bootstrap ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Pong – Spieler vs. Computer (Swing)");
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            PongAISwing game = new PongAISwing();
            f.setContentPane(game);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
