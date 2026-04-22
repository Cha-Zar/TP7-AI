# Tunisia Pathfinding Java / JavaFX

## Requirements
- Java 17+
- Maven 3.6+

## Run (development)
```bash
mvn javafx:run
```

## Data files
All CSV files are loaded from `src/main/resources/` (bundled in the JAR)
or from the `data/` folder next to the JAR when running standalone.

- `cities.csv`: governorates with GPS coordinates
- `distances.csv`: road distances between city pairs (km)

## Algorithms
- **BFS**: breadth-first, finds shortest hop path
- **DFS**: depth-first, fast but not optimal
- **UCS**: uniform cost, optimal by road distance
- **Best-First**: greedy by heuristic, fast but not optimal
- **Bidir-A***: optimal and efficient, searches from both start and goal simultaneously

## Heuristics
- **Haversine (GPS)**: exact great-circle distance from precise GPS coordinates

## GUI
Left panel: choose algorithm, start/goal cities, heuristic type.
Centre: Tunisia map with edges and city dots. Click a city to set start/goal.
Right panel: open list and closed set at each search iteration.
Bottom bar: step controls (Prev / Next / Play) with a slider.
Results bar: algorithm, distance, nodes explored, time, full path.
