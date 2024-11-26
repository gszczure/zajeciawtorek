package org.example;

public class MazeSolverConfig {

    // Maksymalna liczba wykonań
    public static int maxTries = 10;

    // Mutacja
    public static double mutation = 0.1;

    // Krzyżowanie
    public static double crossover = 0.7;

    // Rozmiar populacji
    public static int populationSize = 400;

    // Liczba pokoleń
    public static int generations = 100;

    // Maksymalna liczba kroków
    public static int maxSteps = 200;

    // Start [0, 0]
    public static int startX = 0;
    public static int startY = 0;

    // Meta [10, 10]
    public static int endX = 10;
    public static int endY = 10;

}
