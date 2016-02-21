package ru.spbau.mit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    // Надо промоделировать этот процесс с помощью класса
    // java.util.Random и посчитать, какова вероятность попасть в мишень.

    private static final int COUNT_REP = 1000000;
    public static double piDividedBy4() {
        return new Random().doubles(COUNT_REP).boxed().map(new Function<Double, ArrayList<Double>>() {
            private Double firstArg;

            @Override
            public ArrayList<Double> apply(Double aDouble) {
                if (firstArg != null) {
                    ArrayList<Double> ar = new ArrayList<Double>();
                    ar.add(firstArg);
                    ar.add(aDouble);
                    firstArg = null;
                    return ar;
                } else {
                    firstArg = aDouble;
                    return null;
                }
            }
        }).filter(a -> a != null)
                .filter(a -> a.get(0) * a.get(0) + a.get(1) * a.get(1) <= 1).count() * 2.0 / COUNT_REP;
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions.entrySet().stream()
                .collect(
                        () -> new HashMap<String, Integer>(),
                        (a, b) -> a.put(b.getKey(), b.getValue().stream().collect(Collectors.joining()).length()),
                        (a, b) -> a.putAll(b))
                .entrySet().stream().max((a, b) -> a.getValue().compareTo(b.getValue())).get().getKey();
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders.stream().flatMap(a -> a.entrySet().stream()).collect(
                Collectors.groupingBy(
                        a -> a.getKey(),
                        Collectors.mapping(
                                a -> a.getValue(),
                                Collectors.reducing(0, (c, d) -> c + d)
                        )
                )
        );
    }
}
