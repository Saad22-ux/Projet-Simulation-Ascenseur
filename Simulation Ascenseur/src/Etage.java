import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;

public class Etage {
    private int numero;
    private Queue<Personne> fileAttente;

    public Etage(int numero) {
        this.numero = numero;
        this.fileAttente = new LinkedList<>();
    }

    // Utilisé par le Générateur (ton binôme)
    public synchronized void ajouterPersonne(Personne p) {
        fileAttente.add(p);
        notifyAll(); // Réveille l'ascenseur s'il attendait des passagers
    }

    // Utilisé par l'Ascenseur (toi)
    public synchronized Personne recupererPassager(Direction directionAscenseur) {
        Iterator<Personne> it = fileAttente.iterator();
        while (it.hasNext()) {
            Personne p = it.next();
            // L'ascenseur prend la personne si elle va dans le même sens,
            // ou si l'ascenseur est à l'arrêt
            if (directionAscenseur == Direction.ARRET || p.getDirection() == directionAscenseur) {
                it.remove();
                return p;
            }
        }
        return null; // Personne ne va dans cette direction
    }

    public synchronized int getNombrePersonnesEnAttente() {
        return fileAttente.size();
    }

    public int getNumero() {
        return numero;
    }

    public synchronized List<Personne> getFileAttente() {
        return new ArrayList<>(fileAttente);
    }
}
