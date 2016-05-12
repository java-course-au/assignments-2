package ru.spbau.mit;

import com.google.common.base.Joiner;
import javafx.util.Pair;

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
        return paths.stream().flatMap(fileStr -> {
            try {
                return Files.lines(Paths.get(fileStr));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Stream.of("");
        }).filter(str -> str.contains(sequence)).
                collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать,
    // какова вероятность попасть в мишень.

    public static final int COUNT_OPERATIONS = (int)1e6;

    public static double piDividedBy4() {
        return Stream.generate(() -> Math.pow(Math.random(), 2.0) + Math.pow(Math.random(), 2.0))
                .limit(COUNT_OPERATIONS)
                .filter(r -> r < 1).
                count()*1.0/COUNT_OPERATIONS;
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions.entrySet().stream()
                .map(e -> new Pair<>(e.getKey(), (Joiner.on("").join(e.getValue())).length()))
                .max(Comparator.comparingInt(Pair::getValue)).orElse(new Pair<String, Integer>(null, 0)).getKey();
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders.stream().flatMap(e -> e.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));
    }
}
