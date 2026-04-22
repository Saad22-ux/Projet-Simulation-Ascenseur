# Projet de Simulation d'Ascenseur Intelligent (Multi-Threading)

##  Présentation Globale
Ce projet est une simulation graphique interactive et multi-threadée d'un système d'ascenseur intelligent. Développé en Java avec l'interface graphique **Swing**, le projet illustre visuellement la gestion concurrente des ressources, en représentant un bâtiment de **5 étages** équipé d'**un seul ascenseur** (limité à une capacité stricte de 4 personnes).

Ce projet s'inscrit dans le cadre de l'apprentissage de l'**architecture orientée objet**, et représente une mise en pratique très complète de la **programmation concurrente** (Multithreading : Mutex, Semaphore, Notifications inter-thread).

---

## Fonctionnalités Clés
- **Génération Aléatoire Autonome** : Des instances de personnes (passagers) apparaissent de façon aléatoire à différents étages avec des destinations choisies au hasard pour alimenter le flux du bâtiment.
- **Logique d'Ascenseur Intelligent** :
  - L'ascenseur conserve un sens de progression (Montée ou Descente) tant qu'il y a des passagers qui patientent dans ce sens.
  - L'ascenseur ne change de sens que lorsqu'il se vide de ses passagers, ou lorsqu'il arrive aux extrémités du parcours (l'Étage 0 et l'Étage 4).
- **Contrôle Graphique et Fluidité** : Le projet inclut une vue Swing tournant à haute cadence de framerate (environ 60 FPS) qui va lire subtilement l'état de l'application et interpoler la position d'une cabine visuelle pour offrir une fluidité parfaite et agréable pour l'utilisateur.
- **Multithreading et Synchronisation Évoluée** : 
  - Files d'attentes synchronisées garantissant qu'aucune donnée de passager n'est écrasée ou en conflit lors des embarquements.
  - Gestion efficace de l'énergie et des calculs CPU : L'ascenseur entre dans un état de sommeil profond (via l'instruction `wait()`) en l'absence totale de requêtes sur les files d'attentes. Dès qu'un nouveau passager surgit, un déclencheur le réveille immédiatement à l'aide de l'instruction `notifyAll()`.

---

##  Workflow et Architecture
Le projet exploite une séparation très nette de la couche Vue et de la logique Métier. Le flux s'articule ainsi :

1. **Le Modèle de Données (`Ascenseur`, `Etage`, `Personne`, `Immeuble`) :** 
   C'est la dimension physique. Chaque objet `Etage` emmagasine dans une file un groupe de `Personnes`. L'objet encapsulant `Immeuble` regroupe l'ensemble du système.

2. **Les Moteurs Autonomes (Les Threads Java) :**
   - **Le Thread `Ascenseur`** : C'est le coeur applicatif. Il tourne de manière active dans sa propre boucle pour déceler, récupérer, et déposer les participants. Il orchestre les accès concurrentiels aux listes.
   - **Le Thread `GenerateurPersonnes`** : Moteur "divin" qui va générer et instancier occasionnellement des passagers sur le bâtiment et déclencher un événement système pour sortir l'ascenseur de sa maintenance le cas échéant.

3. **L'Interface Utilisateur (L'EDT Java Swing `SimulationGUI`) :**
   Un thread exclusé, l'*Event Dispatch Thread* (EDT), va régulièrement analyser une base de lecture copiée et "thread-safe" (`CopyOnWriteArrayList`) produite par le code principal. La GUI produit une restitution des objectifs de passagers dans le couloir sans jamais brider ou affecter la robustesse du noyau.

---

## Classes et Fonctions Utilisées
* `Personne.java` : Classe porte-drapeau comportant (Étages de départ et d'arrivée, et la Direction espérée).
* `Ascenseur.java` : Classe clé qui implémente `Thread`.
  *  *`deposerPassagers()`* : Exclut les passagers qui ont atteint leur objectif final.
  *  *`embarquerPassagers()`* : Assure l'admission des passagers correspondants à l'orientation dans un cadre protégé par un garde-fou de places permises  (objet `Semaphore`) via sa fonction `tryAcquire()`. 
  *  *`mettreAJourDirection()`* : L'algorithme intelligent calculant son objectif final en balayant les flux extérieurs.
* `GenerateurPersonnes.java` : Routine réveillant l'ascenseur après sa pause avec l'emploi de son pont synchronisé *`signalerNouvelleDemande()`*.
* `Etage.java` : Verrou de distribution des queues de l'immeuble.
* `SimulationGUI.java` : Gère le lissage et le visuel et fait usage de librairies comme `javax.swing.Timer` et `Graphics2D` de l'AWT.

---

## Exécution du code :
1. Placez votre terminal de commande dans le dossier comportant vos sources (`/src`) : 
    ```bash
    cd "Simulation Ascenseur/src"
    ```
2. Mettez en route la compilation générale :
    ```bash
    javac -d ../out *.java
    ```
3. Exécutez le point d'amorce du programme `Main.java` avec la charge du classpath.
    ```bash
    java -cp ../out Main
    ```
4. Le resultat de chaque etape de la simulation sera affiché dans la console ansi que dans une fenetre graphique.
