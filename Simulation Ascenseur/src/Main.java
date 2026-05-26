import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Initialisation du modèle
        Immeuble immeuble = new Immeuble();
        
        // Threads
        GenerateurPersonnes generateur = new GenerateurPersonnes(immeuble);
        
        // Démarrage de la logique
        immeuble.getAscenseur().start();
        generateur.start();
        
        // Démarrage de l'interface graphique sur l'Event Dispatch Thread (Swing)
        SwingUtilities.invokeLater(() -> {
            SimulationGUI gui = new SimulationGUI(immeuble, generateur);
            gui.setVisible(true);
        });
    }
}