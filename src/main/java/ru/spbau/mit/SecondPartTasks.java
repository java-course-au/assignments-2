package ru.spbau.mit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public final class SecondPartTasks {

    private SecondPartTasks() {}

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) throws IOException {
        return paths.stream()
                    .map(Paths::get)
                    .flatMap((p) -> {
                        try {
                            return Files.lines(p);
                        } catch (IOException e) {
                            return Stream.empty();
                        }
                    })
                    .filter(s -> s.contains(sequence))
                    .collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать,
    // какова вероятность попасть в мишень.
    private static final double RAD = 0.5;
    private static final double RAD2 = 0.25;
    private static final long SHOTS_NUMBER = 1_000_000;
    public static double piDividedBy4() {
        Random random = new Random();

        return DoubleStream.generate(() ->
                Math.pow(random.nextDouble() - RAD, 2) + Math.pow(random.nextDouble() - RAD, 2))
                           .limit(SHOTS_NUMBER)
                           .filter(d2 -> d2 < RAD2)
                           .count()
                    / (double) SHOTS_NUMBER;
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions.entrySet()
                           .stream()
                           .collect(Collectors.maxBy(Comparator.comparing(
                                        e -> e.getValue()
                                              .stream()
                                              .mapToLong(String::length)
                                              .sum())))
                           .get()
                           .getKey();
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders.stream()
                     .map(Map::entrySet)
                     .flatMap(Set::stream)
                     .collect(Collectors.groupingBy(Map.Entry::getKey,
                                                    Collectors.summingInt(Map.Entry::getValue)));
    }
}
