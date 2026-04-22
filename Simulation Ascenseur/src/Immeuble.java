public class Immeuble {
    private Etage[] etages;
    private Ascenseur ascenseur;

    public Immeuble() {
        etages = new Etage[5];
        for (int i = 0; i < 5; i++) {
            etages[i] = new Etage(i);
        }
        ascenseur = new Ascenseur(etages);
    }

    public Etage[] getEtages() {
        return etages;
    }

    public Etage getEtage(int i) {
        if (i >= 0 && i < 5) {
            return etages[i];
        }
        return null;
    }

    public Ascenseur getAscenseur() {
        return ascenseur;
    }
}
