package ru.spbau.mit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SecondPartTasks {

    private SecondPartTasks() {}

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        return paths
                .stream()
                .flatMap(path -> {
                    try {
                        return Files.lines(Paths.get(path));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(s -> s.contains(sequence))
                .collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать,
    // какова вероятность попасть в мишень.
    private static final long ITER_CNT = 10000000;
    private static final double RADIUS = 0.5;
    private static final Random RNG = new Random(123);

    public static double piDividedBy4() {
        return Stream.generate(() -> Math.pow(RNG.nextDouble() - RADIUS, 2)
                + Math.pow(RNG.nextDouble() - RADIUS, 2))
                .limit(ITER_CNT)
                .mapToDouble(dist -> {
                    if (dist <= Math.pow(RADIUS, 2)) {
                        return 1;
                    } else {
                        return 0;
                    }
                })
                .average()
                .getAsDouble();
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions
                .entrySet()
                .stream()
                .max(Comparator.comparing(entry ->
                        entry
                                .getValue()
                                .stream()
                                .collect(Collectors.summingLong(String::length))))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders
                .stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (lhs, rhs) -> lhs + rhs));
    }
}
