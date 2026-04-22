import java.util.Random;

public class GenerateurPersonnes extends Thread {
    private Immeuble immeuble;
    private Random random;

    public GenerateurPersonnes(Immeuble immeuble) {
        this.immeuble = immeuble;
        this.random = new Random();
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Temps d'apparition aléatoire entre 2s et 5s
                Thread.sleep(2000 + random.nextInt(3000));
                
                int etageDepart = random.nextInt(5);
                int etageArrivee;
                do {
                    etageArrivee = random.nextInt(5);
                } while (etageDepart == etageArrivee);
                
                Personne p = new Personne(etageDepart, etageArrivee);
                immeuble.getEtage(etageDepart).ajouterPersonne(p);
                immeuble.getAscenseur().signalerNouvelleDemande();
                
                System.out.println("Nouvelle personne générée à l'étage " + etageDepart + " vers l'étage " + etageArrivee);

            } catch (InterruptedException e) {
                System.out.println("Le générateur de personnes a été interrompu.");
                break;
            }
        }
    }
}
