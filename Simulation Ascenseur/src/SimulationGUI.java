import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SimulationGUI extends JFrame {
    private Immeuble immeuble;
    private AscenseurPanel panel;

    public SimulationGUI(Immeuble immeuble) {
        this.immeuble = immeuble;
        this.setTitle("Simulation Ascenseur Intelligent");
        this.setSize(800, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        panel = new AscenseurPanel();
        this.add(panel);

        // Timer pour 60 FPS (1000 ms / 60 ≈ 16 ms)
        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.updateAnimation();
                panel.repaint();
            }
        });
        timer.start();
    }

    private class AscenseurPanel extends JPanel {
        private int hauteurEtage = 100;
        private double yAscenseurActuel = -1; // -1 pour initier au premier affichage

        public AscenseurPanel() {
            this.setBackground(new Color(245, 245, 245));
        }

        public void updateAnimation() {
            Ascenseur asc = immeuble.getAscenseur();
            if (asc == null) return;
            
            // Calcul cible: etage 0 est en bas, etage 4 est en haut
            double cibleY = (4 - asc.getEtageCourant()) * hauteurEtage;
            
            if (yAscenseurActuel == -1) {
                yAscenseurActuel = cibleY;
            }

            // Interpolation pour fluidité
            if (Math.abs(yAscenseurActuel - cibleY) > 0.5) {
                yAscenseurActuel += (cibleY - yAscenseurActuel) * 0.05; // Facteur de vitesse
            } else {
                yAscenseurActuel = cibleY;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            hauteurEtage = height / 5;

            // Dessiner les étages (0 en bas, 4 en haut)
            for (int i = 0; i < 5; i++) {
                int y = (4 - i) * hauteurEtage;
                
                // Ligne du sol
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(0, y + hauteurEtage, width, y + hauteurEtage);
                
                // Texte étage
                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2d.drawString("Étage " + i, 20, y + hauteurEtage - 15);

                // File d'attente
                Etage etage = immeuble.getEtage(i);
                if (etage != null) {
                    List<Personne> file = etage.getFileAttente();
                    int xPersonne = 120;
                    for (Personne p : file) {
                        g2d.setColor(new Color(65, 105, 225)); // Royal Blue
                        g2d.fillOval(xPersonne, y + hauteurEtage - 35, 24, 24);
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        g2d.drawString(String.valueOf(p.getEtageDestination()), xPersonne + 8, y + hauteurEtage - 18);
                        xPersonne += 30;
                    }
                }
            }

            int ascenseurX = width / 2 - 50;
            int ascenseurWidth = 100;
            
            // Dessiner la gaine de l'ascenseur (fond)
            g2d.setColor(new Color(220, 220, 230));
            g2d.fillRect(ascenseurX - 10, 0, ascenseurWidth + 20, height);
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(ascenseurX - 10, 0, ascenseurX - 10, height);
            g2d.drawLine(ascenseurX + ascenseurWidth + 10, 0, ascenseurX + ascenseurWidth + 10, height);

            // Dessiner la cabine
            int yCabine = (int) yAscenseurActuel;
            g2d.setColor(new Color(240, 248, 255)); // Alice Blue
            g2d.fillRect(ascenseurX, yCabine, ascenseurWidth, hauteurEtage);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(ascenseurX, yCabine, ascenseurWidth, hauteurEtage);

            // Dessiner direction
            Ascenseur asc = immeuble.getAscenseur();
            if (asc != null) {
                Direction dir = asc.getDirectionCourante();
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
                String ascLabel = "■";
                if (dir == Direction.MONTEE) ascLabel = "▲";
                else if (dir == Direction.DESCENTE) ascLabel = "▼";
                g2d.drawString(ascLabel, ascenseurX + ascenseurWidth / 2 - 8, yCabine + 20);

                // Dessiner les personnes dans la cabine
                List<Personne> passagers = asc.getPassagers();
                int px = ascenseurX + 15;
                int py = yCabine + 35;
                for (Personne p : passagers) {
                    g2d.setColor(new Color(220, 20, 60)); // Crimson
                    g2d.fillOval(px, py, 24, 24);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2d.drawString(String.valueOf(p.getEtageDestination()), px + 8, py + 17);
                    px += 30;
                    if (px + 24 > ascenseurX + ascenseurWidth) {
                        px = ascenseurX + 15;
                        py += 30;
                    }
                }
            }
        }
    }
}
