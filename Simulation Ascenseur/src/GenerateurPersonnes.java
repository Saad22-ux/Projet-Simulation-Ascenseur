import java.util.Random;

public class GenerateurPersonnes extends Thread {
    private Immeuble immeuble;
    private Random random;
    
    private volatile int delaiMoyenMs = 3500;
    private volatile boolean enPause = false;

    public GenerateurPersonnes(Immeuble immeuble) {
        this.immeuble = immeuble;
        this.random = new Random();
    }

    @Override
    public void run() {
        while (true) {
            try {
                int delaiActuel = delaiMoyenMs;
                int minSleep = Math.max(500, delaiActuel - 1500);
                int randomRange = Math.max(100, delaiActuel + 1500 - minSleep);
                int tempsSleep = minSleep + random.nextInt(randomRange);

                int elapsed = 0;
                while (elapsed < tempsSleep) {
                    if (enPause) {
                        Thread.sleep(200);
                    } else {
                        Thread.sleep(100);
                        elapsed += 100;
                    }
                }
                
                while (enPause) {
                    Thread.sleep(200);
                }

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

    public void setDelaiMoyenMs(int delaiMoyenMs) {
        this.delaiMoyenMs = delaiMoyenMs;
    }

    public int getDelaiMoyenMs() {
        return delaiMoyenMs;
    }

    public void setEnPause(boolean enPause) {
        this.enPause = enPause;
    }

    public boolean isEnPause() {
        return enPause;
    }
}
