package ru.spbau.mit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SecondPartTasks {
    private static final int ITERATIONS_NUMBER = 10000000;
    private static final double RADIUS = 0.5;

    private SecondPartTasks() {}

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        try {
            return paths.
                    stream().
                    flatMap(path -> {
                        try {
                            return Files.lines(Paths.get(path));
                        } catch (IOException e) {
                            throw new UncheckedIOException("No file " + path + " exists", e);
                        }
                    }).
                    filter(string -> string.contains(sequence)).
                    collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать,
    // какова вероятность попасть в мишень.
    public static double piDividedBy4() {
        class Point {
            private double x, y;

            Point(double x, double y) {
                this.x = x;
                this.y = y;
            }

            public double getDistance() {
                return Math.sqrt(x * x + y * y);
            }
        }
        Random random = new Random();
        return Stream.
                generate(() -> new Point(random.nextDouble() - RADIUS, random.nextDouble() - RADIUS)).
                limit(ITERATIONS_NUMBER).
                filter(point -> point.getDistance() <= RADIUS).count() * 1.0 / ITERATIONS_NUMBER;
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions.
                entrySet().
                stream().
                collect(Collectors.maxBy(Comparator.comparing(
                        entry -> entry.getValue().
                                stream().
                                mapToInt(String::length).
                                sum()))).map(Map.Entry::getKey).orElse(null);
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders.
                stream().
                flatMap(map -> map.entrySet().stream()).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a + b));
    }
}
