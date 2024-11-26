package org.example;

import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.*;
import java.util.*;

public class MazeSolver {

    private int[][] maze;
    private int startX, startY, endX, endY;
    private int maxSteps;

    public MazeSolver(int[][] maze, int startX, int startY, int endX, int endY, int maxSteps) {
        this.maze = maze;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.maxSteps = maxSteps;
    }

    // Funkcja oceny (fitness)
    private Integer evaluate(Genotype<IntegerGene> genotype) {
        int x = startX;
        int y = startY;

        IntegerChromosome chromosome = genotype.chromosome().as(IntegerChromosome.class);
        int fitness = 0;

        for (IntegerGene gene : chromosome) {
            int direction = gene.intValue();

            int newX = x, newY = y;

            // Ustalanie nowej pozycji na podstawie ruchu
            if (direction == 1) { // Ruch w dół
                newX = x + 1;
            } else if (direction == 3) { // Ruch w prawo
                newY = y + 1;
            } else if (direction == 0) { // Ruch w górę
                newX = x - 1;
            } else if (direction == 2) { // Ruch w lewo
                newY = y - 1;
            }

            // Sprawdzamy, czy ruch jest poprawny
            if (newX < 0 || newX >= maze.length || newY < 0 || newY >= maze[0].length || maze[newX][newY] == 1) {
                fitness -= 10;  // Kara za wejście w ściane
            } else {
                x = newX;
                y = newY;
                fitness += 5;  // Nagroda za poprawny ruch
            }

            // Jeśli osiągnęliśmy cel
            if (x == endX && y == endY) {
                fitness += 200;
                break;
            }
        }

        // Kara za zbyt dużą liczbę kroków
        int steps = chromosome.length();
        fitness -= steps;

        // Kara za odległość od celu
        int distance = Math.abs(x - endX) + Math.abs(y - endY);
        fitness -= distance * 5;

        return fitness;
    }


    // Główna funkcja rozwiązująca problem
    public void solve(int populationSize, int generations, int maxTries) {
        int bestSteps = Integer.MAX_VALUE;
        int attempts = 0;
        List<String> bestPath = new ArrayList<>();
        Genotype<IntegerGene> bestGenotype = null;
        EvolutionStatistics<Integer, ?> statistics = EvolutionStatistics.ofNumber();

        while (attempts < maxTries) {
            attempts++;

            Factory<Genotype<IntegerGene>> genotypeFactory = Genotype.of(
                    IntegerChromosome.of(0, 4, maxSteps)  // Cztery kierunki (góra, dół, lewo, prawo)
            );

            Engine<IntegerGene, Integer> engine = Engine.builder(this::evaluate, genotypeFactory)
                    .populationSize(populationSize)
                    .selector(new TournamentSelector<>())
                    .alterers(
                            new Mutator<>(MazeSolverConfig.mutation),
                            new SinglePointCrossover<>(MazeSolverConfig.crossover)
                    )
                    .build();

            Phenotype<IntegerGene, Integer> best = engine.stream()
                    .limit(generations)
                    .peek(statistics)
                    .collect(EvolutionResult.toBestPhenotype());

            // Sprawdzamy liczbę kroków najlepszego osobnika
            IntegerChromosome chromosome = best.genotype().chromosome().as(IntegerChromosome.class);
            int x = startX, y = startY;
            int steps = 0;
            List<String> path = new ArrayList<>();  // Ścieżka dla bieżącego chromosomu

            // Przemierzanie labiryntu z wykorzystaniem najlepszego chromosomu
            for (IntegerGene gene : chromosome) {
                int direction = gene.intValue();

                int newX = x, newY = y;

                if (direction == 1 && x < maze.length - 1 && maze[x + 1][y] != 1) {
                    newX = x + 1;  // Ruch w dół
                } else if (direction == 3 && y < maze[0].length - 1 && maze[x][y + 1] != 1) {
                    newY = y + 1;  // Ruch w prawo
                } else if (direction == 0 && x > 0 && maze[x - 1][y] != 1) {
                    newX = x - 1;  // Ruch w górę
                } else if (direction == 2 && y > 0 && maze[x][y - 1] != 1) {
                    newY = y - 1;  // Ruch w lewo
                }

                // Jeśli po ruchu zmieniła się pozycja, dodajemy ją do ścieżki
                if (newX != x || newY != y) {
                    path.add("(" + x + ", " + y + ")");
                    x = newX;
                    y = newY;
                }

                steps++;

                if (x == endX && y == endY) {
                    path.add("(" + x + ", " + y + ")");
                    break;
                }
            }

            System.out.println("Próba " + attempts + ":");
            System.out.println("Chromosom: " + best.genotype());
            System.out.println("Liczba kroków: " + steps);
            System.out.println("Ścieżka: " + path);

            if (steps < bestSteps) {
                bestSteps = steps;
                bestGenotype = best.genotype();
                bestPath = path;
            }
        }
        List<String> uniquePath = new ArrayList<>();
        Set<String> visitedPositions = new HashSet<>();

        for (String position : bestPath) {
            if (visitedPositions.add(position)) { // Dodaje do zbioru tylko unikalne pozycje
                uniquePath.add(position);
            }
        }

        System.out.println("\nNajlepsze rozwiązanie:");
        System.out.println("Chromosom: " + bestGenotype);
        System.out.println("Liczba kroków: " + bestSteps);
        System.out.println("Najlepsza ścieżka: " + uniquePath);

        // Wyświetlanie wartości fitness
        int bestFitness = evaluate(bestGenotype);
        System.out.println("Wartość fitness najlepszego rozwiązania: " + bestFitness);

        System.out.println("\nStatystyki ewolucji:");
        System.out.println(statistics);
    }


    public static void main(String[] args) {
        int[][] maze = {
                {0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0},
                {0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 0},
                {0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1},
                {0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0},
                {1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1},
                {0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0},
                {1, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1},
                {0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0},
                {0, 1, 1, 0, 1, 1, 0, 0, 0, 1, 0},
                {0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0}
        };

        MazeSolver solver = new MazeSolver(maze, MazeSolverConfig.startX, MazeSolverConfig.startY, MazeSolverConfig.endX, MazeSolverConfig.endY, MazeSolverConfig.maxSteps);
        solver.solve(MazeSolverConfig.populationSize, MazeSolverConfig.generations, MazeSolverConfig.maxTries);
    }
}
