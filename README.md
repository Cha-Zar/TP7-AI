# Tunisia Pathfinding Java / JavaFX

## Requirements
- Java 17+
# Tunisia Pathfinding (TP7-AI)

Ce projet est une application JavaFX permettant de visualiser et comparer différents algorithmes de recherche de chemin sur un graphe routier de la Tunisie.

## Fonctionnalités
- Chargement d'un graphe routier (villes et routes) depuis des fichiers CSV
- Visualisation graphique de la carte et des étapes de recherche
- Algorithmes disponibles :
	- BFS (Breadth-First Search)
	- DFS (Depth-First Search)
	- UCS (Uniform Cost Search)
	- Best-First
	- A*
	- Bidirectional A*
- Choix de l’heuristique (Haversine ou manuelle) pour les algorithmes informés
- Deux modes de jeu :
	- **Version complète** : toutes les villes et routes
	- **Version TP7** : sous-graphe réduit Tunis → Tozeur (6 villes)

## Structure des données
- `data/cities.csv`, `data/distances.csv`, `data/heuristics.csv` : version complète
- `data/cities_tp7.csv`, `data/distances_tp7.csv`, `data/heuristics_tp7.csv` : version TP7

## Lancement du projet

1. Assurez-vous d’avoir Java 17+ et Maven installés.
2. Placez-vous dans le dossier du projet :

		cd TP7-AI

3. Compilez le projet :

		mvn clean compile

4. Lancez l’application JavaFX :

		mvn javafx:run -q

5. Choisissez la version du graphe au démarrage (complète ou TP7).

## Auteurs
- Projet universitaire IA, 2026
- Contact : BEN SALAH Chahine, chahinebensalah511@gmail.com

mvn javafx:run
```

## Data files
All CSV files are loaded from `src/main/resources/` (bundled in the JAR)
or from the `data/` folder next to the JAR when running standalone.

- `cities.csv`: governorates with GPS coordinates
- `distances.csv`: road distances between city pairs (km)

- **DFS**: depth-first, fast but not optimal
- **UCS**: uniform cost, optimal by road distance

## GUI
Centre: Tunisia map with edges and city dots. Click a city to set start/goal.
Right panel: open list and closed set at each search iteration.
Bottom bar: step controls (Prev / Next / Play) with a slider.
Results bar: algorithm, distance, nodes explored, time, full path.
