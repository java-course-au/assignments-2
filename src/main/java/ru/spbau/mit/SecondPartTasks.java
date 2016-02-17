package ru.spbau.mit;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public final class SecondPartTasks {

    private SecondPartTasks() {}

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        return paths
                .stream()
                .filter(path -> {
                    try {
                        return Files.lines(Paths.get(path)).reduce("", String::concat).contains(sequence);
                    } catch (Exception e ) {
                        // error
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать, какова вероятность попасть в мишень.
    public static double piDividedBy4() {
        return new Random()
                .doubles(10000)
                .mapToObj(v1 -> Arrays.asList(v1, new Random().nextDouble()))
                .mapToInt(p -> (p.get(0) - 0.5) * (p.get(0) - 0.5) + (p.get(1) - 0.5) * (p.get(1) - 0.5) <= 0.25 ? 1 : 0)
                .average()
                .orElse(0);
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions
                .entrySet()
                .stream()
                .max(
                    (e1, e2) ->  Integer.compare(
                            e1.getValue().stream().mapToInt(String::length).sum(),
                            e2.getValue().stream().mapToInt(String::length).sum()
                    )).orElse(null).getKey();
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders
                .stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a + b));
    }
}
