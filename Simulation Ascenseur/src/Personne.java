public class Personne {
    private int etageDepart;
    private int etageDestination;
    private final long tempsCreation;

    public Personne(int etageDepart, int etageDestination) {
        this.etageDepart = etageDepart;
        this.etageDestination = etageDestination;
        this.tempsCreation = System.currentTimeMillis();
    }

    public int getEtageDepart() {
        return etageDepart;
    }

    public int getEtageDestination() {
        return etageDestination;
    }

    public long getTempsCreation() {
        return tempsCreation;
    }

    // Détermine la direction souhaitée par la personne
    public Direction getDirection() {
        return (etageDestination > etageDepart) ? Direction.MONTEE : Direction.DESCENTE;
    }
}
