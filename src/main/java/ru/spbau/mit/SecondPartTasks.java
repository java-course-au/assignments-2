package ru.spbau.mit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SecondPartTasks {

    private SecondPartTasks() {
    }

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        return paths.stream().flatMap(s -> {
            try {
                return Files.lines(Paths.get(s));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(s -> s.contains(sequence)).collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать,
    // какова вероятность попасть в мишень.

    private static final int COUNT_REP = 1000000;
    private static final Random RNG = new Random(179);

    public static double piDividedBy4() {
        return Stream.generate(() -> Math.pow(RNG.nextDouble(), 2.) + Math.pow(RNG.nextDouble(), 2.))
                .limit(COUNT_REP)
                .filter(a -> a <= 1.).count() * 1.0 / COUNT_REP;
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions.entrySet().stream()
                .collect(
                        (Supplier<HashMap<String, Integer>>) HashMap::new,
                        (a, b) -> a.put(b.getKey(), b.getValue().stream().collect(Collectors.joining()).length()),
                        HashMap::putAll)
                .entrySet().stream().max((a, b) -> a.getValue().compareTo(b.getValue())).get().getKey();
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders.stream().flatMap(a -> a.entrySet().stream()).collect(
                Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingInt(
                                Map.Entry::getValue
                        )
                )
        );
    }
}
