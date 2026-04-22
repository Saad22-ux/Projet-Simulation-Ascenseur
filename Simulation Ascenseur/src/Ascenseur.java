import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CopyOnWriteArrayList;

public class Ascenseur extends Thread {
    private int etageCourant = 0;
    private Direction directionCourante = Direction.ARRET;
    private List<Personne> passagers = new CopyOnWriteArrayList<>();

    // Sémaphore limitant la capacité à 4 places
    private Semaphore capacite = new Semaphore(4, true);
    private Etage[] etages;

    public Ascenseur(Etage[] etages) {
        this.etages = etages;
    }

    @Override
    public void run() {
        while (true) {
            try {
                deposerPassagers();
                embarquerPassagers();
                mettreAJourDirection();

                // Déplacement
                if (directionCourante != Direction.ARRET) {
                    // Simule le temps de trajet entre deux étages (à ajuster pour l'animation)
                    Thread.sleep(1500);

                    if (directionCourante == Direction.MONTEE) {
                        etageCourant++;
                    } else if (directionCourante == Direction.DESCENTE) {
                        etageCourant--;
                    }

                    // Pour voir la simulation dans la console avant que ton binôme ne fasse le GUI
                    System.out.println("Ascenseur à l'étage " + etageCourant +
                            " | Direction: " + directionCourante +
                            " | Passagers: " + passagers.size());
                } else {
                    // Si on est à l'arrêt et qu'il n'y a pas de demandes, on attend (wait)
                    synchronized (this) {
                        if (!demandesEnAttente()) {
                            this.wait(); // L'ascenseur se met en veille
                        }
                    }
                }

            } catch (InterruptedException e) {
                System.out.println("L'ascenseur a été interrompu.");
                break;
            }
        }
    }

    private void deposerPassagers() {
        List<Personne> aDescendre = new ArrayList<>();
        // Identifier les personnes arrivées à destination
        for (Personne p : passagers) {
            if (p.getEtageDestination() == etageCourant) {
                aDescendre.add(p);
            }
        }

        // Les faire descendre et libérer les places dans le sémaphore
        for (Personne p : aDescendre) {
            passagers.remove(p);
            capacite.release();
            System.out.println("Une personne descend à l'étage " + etageCourant);
        }
    }

    private void embarquerPassagers() {
        Etage etageActuel = etages[etageCourant];

        // On tente d'acquérir des places disponibles
        while (capacite.tryAcquire()) {
            Personne p = etageActuel.recupererPassager(directionCourante);

            if (p != null) {
                // Si l'ascenseur était à l'arrêt, le premier passager détermine le sens
                if (directionCourante == Direction.ARRET) {
                    directionCourante = p.getDirection();
                }
                passagers.add(p);
                System.out.println("Embarquement à l'étage " + etageCourant +
                        " destination -> " + p.getEtageDestination());
            } else {
                // Si personne ne correspond, on rend la place virtuelle au sémaphore et on
                // arrête l'embarquement
                capacite.release();
                break;
            }
        }
    }

    private void mettreAJourDirection() {
        // Changement de direction aux extrémités de l'immeuble
        if (etageCourant == 4) {
            directionCourante = Direction.DESCENTE;
        } else if (etageCourant == 0 && directionCourante != Direction.ARRET) {
            directionCourante = Direction.MONTEE;
        }

        // S'il est vide et qu'on n'est pas à une extrémité, on vérifie s'il y a des
        // demandes ailleurs
        if (passagers.isEmpty() && directionCourante != Direction.ARRET) {
            if (!demandesEnAttente()) {
                directionCourante = Direction.ARRET;
            }
        }
    }

    // Vérifie s'il y a au moins une personne qui attend dans l'immeuble
    public boolean demandesEnAttente() {
        for (Etage e : etages) {
            if (e.getNombrePersonnesEnAttente() > 0) {
                return true;
            }
        }
        return false;
    }

    public synchronized void signalerNouvelleDemande() {
        this.notifyAll();
    }

    // Getters pour que ton binôme puisse dessiner le GUI
    public int getEtageCourant() {
        return etageCourant;
    }

    public Direction getDirectionCourante() {
        return directionCourante;
    }

    public int getNombrePassagers() {
        return passagers.size();
    }

    public List<Personne> getPassagers() {
        return passagers;
    }
}