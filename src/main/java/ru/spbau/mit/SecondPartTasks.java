package ru.spbau.mit;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SecondPartTasks {
    private static final double RADIUS = 0.5;
    private static final int ITERATIONS_COUNT = 10_000_000;

    private SecondPartTasks() { }

    // Найти строки из переданных файлов,
    // в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths,
                                          CharSequence sequence) {
        return paths
                .stream()
                .filter(path -> {
                    try {
                        return Files.lines(Paths.get(path))
                                .reduce("", String::concat)
                                .contains(sequence);
                    } catch (Exception e) {
                        // error
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает
    // в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса
    // java.util.Random и посчитать, какова вероятность
    // попасть в мишень.
    public static double piDividedBy4() {
        return Stream
                .generate(() -> Math.pow(
                        new Random().nextDouble() - RADIUS, 2.0)
                        + Math.pow(new Random().nextDouble() - RADIUS, 2))
                .limit(ITERATIONS_COUNT)
                .filter(x -> x <= Math.pow(RADIUS, 2.0))
                .count() / (1.0 * ITERATIONS_COUNT);
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions
                .entrySet()
                .stream()
                .max(Comparator.comparingInt(x -> x.getValue().stream().mapToInt(String::length).sum()))
                .orElse(null)
                .getKey();
    }

    // Вы крупный поставщик продуктов.
    // Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String,
            Integer>> orders) {
        return orders
                .stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
    }
}
