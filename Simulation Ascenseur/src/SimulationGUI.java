import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class SimulationGUI extends JFrame {
    private Immeuble immeuble;
    private GenerateurPersonnes generateur;
    private AscenseurPanel panelSimulation;

    // Éléments du tableau de bord
    private JLabel lblTotalTransported;
    private JLabel lblAverageWait;
    private JLabel lblOccupancy;
    private JProgressBar barOccupancy;
    private JLabel lblMotorStatus;
    private JButton btnPauseResume;
    private JSlider sliderFrequency;
    private JComboBox<Integer> comboDepart;
    private JComboBox<Integer> comboDestination;

    // Constantes de couleur
    private static final Color COLOR_BG_DARK = new Color(20, 20, 27);
    private static final Color COLOR_PANEL_BG = new Color(30, 30, 42);
    private static final Color COLOR_ACCENT_CYAN = new Color(0, 210, 255);
    private static final Color COLOR_ACCENT_CRIMSON = new Color(255, 64, 129);
    private static final Color COLOR_TEXT_LIGHT = new Color(240, 240, 245);
    private static final Color COLOR_TEXT_MUTED = new Color(150, 150, 170);
    private static final Color COLOR_SUCCESS_GREEN = new Color(50, 205, 50);

    public SimulationGUI(Immeuble immeuble, GenerateurPersonnes generateur) {
        this.immeuble = immeuble;
        this.generateur = generateur;

        // Configuration de la fenêtre principale
        this.setTitle("Simulation d'Ascenseur Intelligent - Cyber-Dashboard");
        this.setSize(1100, 750);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout(10, 10));
        this.getContentPane().setBackground(COLOR_BG_DARK);

        // Application du Look and Feel "Nimbus" s'il est disponible pour des boutons plus propres
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Ignorer, conserve le L&F par défaut
        }

        // 1. Initialisation du Panneau de Simulation (Droite)
        panelSimulation = new AscenseurPanel();
        this.add(panelSimulation, BorderLayout.CENTER);

        // 2. Initialisation du Tableau de Bord (Gauche)
        JPanel sidebar = createSidebar();
        this.add(sidebar, BorderLayout.WEST);

        // 3. Timer global à 60 FPS pour le rendu graphique
        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Mise à jour de l'affichage
                panelSimulation.updateAnimation();
                updateDashboardData();
                panelSimulation.repaint();
            }
        });
        timer.start();
    }

    /**
     * Crée la barre latérale contenant les contrôles et statistiques.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(340, 750));
        sidebar.setBackground(COLOR_PANEL_BG);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(50, 50, 70)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // --- EN-TÊTE ---
        JLabel title = new JLabel("CONTRÔLEUR");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(COLOR_ACCENT_CYAN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("SIMULATION ASCENSEUR");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        subtitle.setForeground(COLOR_TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(title);
        sidebar.add(subtitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));

        // --- SECTION 1: STATISTIQUES ---
        sidebar.add(createSectionTitle("STATISTIQUES LIVE"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        lblTotalTransported = createStatLabel("Passagers Transportés :", "0", COLOR_SUCCESS_GREEN);
        lblAverageWait = createStatLabel("Attente Moyenne :", "0.0s", COLOR_ACCENT_CYAN);
        lblMotorStatus = createStatLabel("État Moteur :", "■ ARRET", COLOR_TEXT_LIGHT);

        sidebar.add(lblTotalTransported);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(lblAverageWait);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(lblMotorStatus);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));

        // Jauge d'occupation de la cabine
        lblOccupancy = new JLabel("Occupation Cabine : 0 / 4");
        lblOccupancy.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblOccupancy.setForeground(COLOR_TEXT_LIGHT);
        lblOccupancy.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblOccupancy);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));

        barOccupancy = new JProgressBar(0, 4);
        barOccupancy.setValue(0);
        barOccupancy.setPreferredSize(new Dimension(300, 15));
        barOccupancy.setForeground(COLOR_ACCENT_CYAN);
        barOccupancy.setBackground(COLOR_BG_DARK);
        barOccupancy.setBorderPainted(false);
        barOccupancy.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(barOccupancy);

        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- SECTION 2: CONTRÔLE CONCURRENT ---
        sidebar.add(createSectionTitle("CONTRÔLES DU FLUX"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // Bouton Pause / Reprise
        btnPauseResume = new JButton("METTRE EN PAUSE");
        btnPauseResume.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPauseResume.setForeground(Color.WHITE);
        btnPauseResume.setBackground(COLOR_ACCENT_CRIMSON);
        btnPauseResume.setPreferredSize(new Dimension(300, 40));
        btnPauseResume.setMaximumSize(new Dimension(300, 40));
        btnPauseResume.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPauseResume.setFocusPainted(false);
        btnPauseResume.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isPaused = generateur.isEnPause();
                generateur.setEnPause(!isPaused);
                if (isPaused) {
                    btnPauseResume.setText("METTRE EN PAUSE");
                    btnPauseResume.setBackground(COLOR_ACCENT_CRIMSON);
                } else {
                    btnPauseResume.setText("REPRENDRE LE FLUX");
                    btnPauseResume.setBackground(COLOR_SUCCESS_GREEN);
                }
            }
        });
        sidebar.add(btnPauseResume);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        // Slider de Fréquence
        JLabel lblSlider = new JLabel("Fréquence d'apparition (secondes) :");
        lblSlider.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSlider.setForeground(COLOR_TEXT_LIGHT);
        lblSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblSlider);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));

        sliderFrequency = new JSlider(JSlider.HORIZONTAL, 1, 10, 3);
        sliderFrequency.setBackground(COLOR_PANEL_BG);
        sliderFrequency.setForeground(COLOR_TEXT_MUTED);
        sliderFrequency.setMajorTickSpacing(1);
        sliderFrequency.setPaintTicks(true);
        sliderFrequency.setPaintLabels(true);
        sliderFrequency.setAlignmentX(Component.LEFT_ALIGNMENT);
        sliderFrequency.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = sliderFrequency.getValue();
                generateur.setDelaiMoyenMs(value * 1000);
            }
        });
        sidebar.add(sliderFrequency);

        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- SECTION 3: APPEL MANUEL ---
        sidebar.add(createSectionTitle("DEMANDE MANUELLE"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBackground(COLOR_PANEL_BG);
        formPanel.setMaximumSize(new Dimension(300, 70));
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDepart = new JLabel("Départ :");
        lblDepart.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDepart.setForeground(COLOR_TEXT_LIGHT);
        
        JLabel lblDest = new JLabel("Destination :");
        lblDest.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDest.setForeground(COLOR_TEXT_LIGHT);

        Integer[] floors = {0, 1, 2, 3, 4};
        comboDepart = new JComboBox<>(floors);
        comboDestination = new JComboBox<>(floors);
        comboDepart.setSelectedIndex(0);
        comboDestination.setSelectedIndex(4);

        formPanel.add(lblDepart);
        formPanel.add(comboDepart);
        formPanel.add(lblDest);
        formPanel.add(comboDestination);

        sidebar.add(formPanel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton btnCall = new JButton("APPELER L'ASCENSEUR");
        btnCall.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCall.setForeground(Color.BLACK);
        btnCall.setBackground(COLOR_ACCENT_CYAN);
        btnCall.setPreferredSize(new Dimension(300, 40));
        btnCall.setMaximumSize(new Dimension(300, 40));
        btnCall.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCall.setFocusPainted(false);
        btnCall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dep = (int) comboDepart.getSelectedItem();
                int dest = (int) comboDestination.getSelectedItem();
                
                if (dep == dest) {
                    JOptionPane.showMessageDialog(SimulationGUI.this, 
                        "L'étage de départ doit être différent de l'étage de destination !", 
                        "Erreur d'appel", JOptionPane.WARNING_MESSAGE);
                } else {
                    Personne p = new Personne(dep, dest);
                    immeuble.getEtage(dep).ajouterPersonne(p);
                    immeuble.getAscenseur().signalerNouvelleDemande();
                }
            }
        });
        sidebar.add(btnCall);

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(COLOR_TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createStatLabel(String labelText, String valueText, Color valueColor) {
        JLabel label = new JLabel("<html><font color='#9696aa'>" + labelText + "</font> <font color='" + 
            String.format("#%02x%02x%02x", valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue()) + 
            "'><b>" + valueText + "</b></font></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Met à jour les valeurs affichées dans la barre latérale en temps réel.
     */
    private void updateDashboardData() {
        Ascenseur asc = immeuble.getAscenseur();
        if (asc == null) return;

        // Total passagers
        lblTotalTransported.setText("<html><font color='#9696aa'>Passagers Transportés :</font> <font color='#32cd32'><b>" + 
            asc.getPassagersTransportes() + "</b></font></html>");

        // Attente moyenne
        lblAverageWait.setText("<html><font color='#9696aa'>Attente Moyenne :</font> <font color='#00d2ff'><b>" + 
            String.format("%.1fs", asc.getTempsAttenteMoyenSecondes()) + "</b></font></html>");

        // État moteur
        Direction dir = asc.getDirectionCourante();
        String strDir = "■ ARRET";
        Color colorDir = COLOR_TEXT_LIGHT;
        if (dir == Direction.MONTEE) {
            strDir = "▲ MONTEE";
            colorDir = COLOR_ACCENT_CYAN;
        } else if (dir == Direction.DESCENTE) {
            strDir = "▼ DESCENTE";
            colorDir = COLOR_ACCENT_CRIMSON;
        }
        lblMotorStatus.setText("<html><font color='#9696aa'>État Moteur :</font> <font color='" + 
            String.format("#%02x%02x%02x", colorDir.getRed(), colorDir.getGreen(), colorDir.getBlue()) + 
            "'><b>" + strDir + "</b></font></html>");

        // Jauge d'occupation
        int count = asc.getNombrePassagers();
        lblOccupancy.setText("Occupation Cabine : " + count + " / 4");
        barOccupancy.setValue(count);
        if (count == 4) {
            barOccupancy.setForeground(COLOR_ACCENT_CRIMSON);
        } else if (count >= 2) {
            barOccupancy.setForeground(new Color(255, 165, 0)); // Orange
        } else {
            barOccupancy.setForeground(COLOR_ACCENT_CYAN);
        }
    }

    /**
     * Panneau de rendu graphique 2D de la simulation.
     */
    private class AscenseurPanel extends JPanel {
        private int hauteurEtage = 120;
        private double yAscenseurActuel = -1;

        // Gestion de l'animation des portes (0.0 = complètement ouverte, 1.0 = complètement fermée)
        private double doorWidth = 1.0;
        private int stationaryFrames = 0;

        public AscenseurPanel() {
            this.setBackground(COLOR_BG_DARK);
        }

        public void updateAnimation() {
            Ascenseur asc = immeuble.getAscenseur();
            if (asc == null) return;

            // Calcul cible: étage 0 est en bas, étage 4 est en haut
            double cibleY = (4 - asc.getEtageCourant()) * hauteurEtage;

            if (yAscenseurActuel == -1) {
                yAscenseurActuel = cibleY;
            }

            // Interpolation pour mouvement très fluide de la cabine
            double diff = cibleY - yAscenseurActuel;
            if (Math.abs(diff) > 0.5) {
                yAscenseurActuel += diff * 0.08; // Facteur d'amortissement
                // Cabine en mouvement -> Les portes se ferment immédiatement
                doorWidth = Math.min(1.0, doorWidth + 0.12);
                stationaryFrames = 0;
            } else {
                yAscenseurActuel = cibleY;
                
                // Cabine arrêtée à l'étage -> Gestionnaire d'animation des portes
                stationaryFrames++;
                
                // 1. Phase d'ouverture (frames 0 à 15)
                if (stationaryFrames <= 15) {
                    doorWidth = Math.max(0.0, doorWidth - 0.08);
                } 
                // 2. Phase de maintien ouvert (frames 15 à 65)
                else if (stationaryFrames > 15 && stationaryFrames <= 65) {
                    doorWidth = 0.0; // Reste ouvert
                } 
                // 3. Phase de fermeture (frames 65 à 80)
                else if (stationaryFrames > 65 && stationaryFrames <= 80) {
                    doorWidth = Math.min(1.0, doorWidth + 0.08);
                } 
                // 4. Portes fermées
                else {
                    doorWidth = 1.0;
                }
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

            // Dimensions de la gaine et cabine
            int ascenseurWidth = 110;
            int ascenseurX = width / 2 - ascenseurWidth / 2 - 40;

            // --- 1. DESSINER LES ÉTAGES (Structure de l'immeuble) ---
            for (int i = 0; i < 5; i++) {
                int y = (4 - i) * hauteurEtage;

                // Ligne du sol avec gradient métallique sombre
                g2d.setPaint(new GradientPaint(0, y + hauteurEtage - 5, new Color(40, 40, 55),
                                              width, y + hauteurEtage, new Color(20, 20, 30)));
                g2d.fillRect(0, y + hauteurEtage - 6, width, 6);

                // Texte de l'étage
                g2d.setColor(COLOR_TEXT_LIGHT);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2d.drawString("ÉTAGE " + i, 30, y + hauteurEtage / 2 + 6);

                // --- DESSINER LA FILE D'ATTENTE DE CHAQUE ÉTAGE ---
                Etage etage = immeuble.getEtage(i);
                if (etage != null) {
                    List<Personne> file = etage.getFileAttente();
                    int xPersonne = 140;
                    for (Personne p : file) {
                        // Dessiner le passager comme une pilule stylisée avec effet de dégradé
                        Color pillColor = (p.getDirection() == Direction.MONTEE) ? COLOR_ACCENT_CYAN : COLOR_ACCENT_CRIMSON;
                        
                        // Bulle d'ombre
                        g2d.setColor(new Color(0, 0, 0, 80));
                        g2d.fillOval(xPersonne + 2, y + hauteurEtage - 43, 26, 26);
                        
                        // Corps de la pilule
                        g2d.setPaint(new GradientPaint(xPersonne, y + hauteurEtage - 45, pillColor,
                                                      xPersonne + 26, y + hauteurEtage - 19, pillColor.darker()));
                        g2d.fillOval(xPersonne, y + hauteurEtage - 45, 26, 26);
                        
                        // Bordure brillante
                        g2d.setColor(new Color(255, 255, 255, 100));
                        g2d.setStroke(new BasicStroke(1.5f));
                        g2d.drawOval(xPersonne, y + hauteurEtage - 45, 26, 26);

                        // Texte de destination
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        g2d.drawString(String.valueOf(p.getEtageDestination()), xPersonne + 9, y + hauteurEtage - 28);
                        
                        xPersonne += 32;
                    }
                }
            }

            // --- 2. DESSINER LA GAINE DE L'ASCENSEUR (Fond de l'arbre) ---
            // Arrière-plan transparent brillant
            g2d.setColor(new Color(15, 15, 25, 180));
            g2d.fillRect(ascenseurX - 15, 0, ascenseurWidth + 30, height);

            // Câbles métalliques de suspension au milieu
            g2d.setColor(new Color(60, 60, 80));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(ascenseurX + ascenseurWidth / 3, 0, ascenseurX + ascenseurWidth / 3, height);
            g2d.drawLine(ascenseurX + (ascenseurWidth * 2) / 3, 0, ascenseurX + (ascenseurWidth * 2) / 3, height);

            // Rails latéraux de guidage
            g2d.setColor(new Color(80, 80, 100));
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(ascenseurX - 15, 0, ascenseurX - 15, height);
            g2d.drawLine(ascenseurX + ascenseurWidth + 15, 0, ascenseurX + ascenseurWidth + 15, height);

            // --- 3. DESSINER LA CABINE D'ASCENSEUR ---
            int yCabine = (int) yAscenseurActuel;

            // Ombre portée de la cabine
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRect(ascenseurX - 5, yCabine + 5, ascenseurWidth + 10, hauteurEtage);

            // Fond intérieur de la cabine avec lueur néon bleue
            g2d.setPaint(new GradientPaint(ascenseurX, yCabine, new Color(28, 28, 48),
                                          ascenseurX + ascenseurWidth, yCabine + hauteurEtage, new Color(14, 14, 26)));
            g2d.fillRect(ascenseurX, yCabine, ascenseurWidth, hauteurEtage);

            // Bordure extérieure de la cabine
            g2d.setColor(new Color(0, 210, 255, 180)); // Cyan brillant
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(new RoundRectangle2D.Double(ascenseurX, yCabine, ascenseurWidth, hauteurEtage, 8, 8));

            // Dessiner les passagers DANS la cabine
            Ascenseur asc = immeuble.getAscenseur();
            if (asc != null) {
                List<Personne> passagers = asc.getPassagers();
                int px = ascenseurX + 15;
                int py = yCabine + 40;
                
                for (Personne p : passagers) {
                    Color pillColor = (p.getDirection() == Direction.MONTEE) ? COLOR_ACCENT_CYAN : COLOR_ACCENT_CRIMSON;

                    // Bulle d'ombre
                    g2d.setColor(new Color(0, 0, 0, 80));
                    g2d.fillOval(px + 1, py + 1, 24, 24);
                    
                    // Badge passager
                    g2d.setPaint(new GradientPaint(px, py, pillColor, px + 24, py + 24, pillColor.darker()));
                    g2d.fillOval(px, py, 24, 24);
                    
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2d.drawString(String.valueOf(p.getEtageDestination()), px + 8, py + 17);
                    
                    px += 28;
                    // Retour à la ligne si beaucoup de passagers
                    if (px + 24 > ascenseurX + ascenseurWidth) {
                        px = ascenseurX + 15;
                        py += 30;
                    }
                }

                // --- 4. DESSINER LES PORTES COULISSANTES DE LA CABINE ---
                // Les portes glissent de l'extérieur vers le centre.
                int midX = ascenseurX + ascenseurWidth / 2;
                int baseDoorWidth = ascenseurWidth / 2;
                int animatedDoorWidth = (int) (baseDoorWidth * doorWidth);

                // Gradient métallique pour les portes en verre blindé semi-transparent
                GradientPaint glassGradientLeft = new GradientPaint(
                        midX - animatedDoorWidth, yCabine, new Color(75, 85, 110, 220),
                        midX, yCabine, new Color(110, 125, 155, 225)
                );
                GradientPaint glassGradientRight = new GradientPaint(
                        midX, yCabine, new Color(110, 125, 155, 225),
                        midX + animatedDoorWidth, yCabine, new Color(75, 85, 110, 220)
                );

                // Dessin de la porte gauche
                if (animatedDoorWidth > 0) {
                    g2d.setPaint(glassGradientLeft);
                    g2d.fillRect(midX - animatedDoorWidth, yCabine + 3, animatedDoorWidth, hauteurEtage - 6);
                    
                    // Bordure de porte
                    g2d.setColor(new Color(200, 210, 230, 240));
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRect(midX - animatedDoorWidth, yCabine + 3, animatedDoorWidth, hauteurEtage - 6);
                }

                // Dessin de la porte droite
                if (animatedDoorWidth > 0) {
                    g2d.setPaint(glassGradientRight);
                    g2d.fillRect(midX, yCabine + 3, animatedDoorWidth, hauteurEtage - 6);
                    
                    // Bordure de porte
                    g2d.setColor(new Color(200, 210, 230, 240));
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRect(midX, yCabine + 3, animatedDoorWidth, hauteurEtage - 6);
                }
            }
        }
    }
}
