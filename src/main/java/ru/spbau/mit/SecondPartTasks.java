package ru.spbau.mit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SecondPartTasks {

    private SecondPartTasks() {
    }

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        return paths.stream()
                .flatMap(path -> {
                    try {
                        return Files.lines(Paths.get(path));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .filter(s -> s.contains(sequence.toString()))
                .collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать,
    // какова вероятность попасть в мишень.
    public static double piDividedBy4() {
        Random random = new Random(System.currentTimeMillis());
        int num = 10_000_000;
        return (double) (Stream.generate(() -> {
            double x = random.nextDouble() / 2;
            double y = random.nextDouble() / 2;
            return (x * x + y * y <= 0.25) ? 1 : 0;
        })
                .limit(num)
                .filter(a -> a == 1)
                .collect(Collectors.counting())) / num;
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions.entrySet()
                .stream()
                .collect(Collectors.maxBy(
                    Comparator.comparingInt(e -> e.getValue().stream().collect(Collectors.joining()).length())))
                .get()
                .getKey();
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders.stream()
                .flatMap(orderList -> orderList.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)));
    }
}
