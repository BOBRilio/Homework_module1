import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TestLogParser {


    enum Status {PASSED, FAILED, SKIPPED}

    record TestResult(String name, Status status, long durationMs) {
    }

    private final List<String> errors = new ArrayList<>();

    //T1
    public List<TestResult> parse(List<String> lines) {
        Map<String, TestResult> lastTest = new LinkedHashMap<>();
        for (String line : lines) {
            if (line.isBlank()) {
                errors.add(line);
                continue;
            }
            String[] lineParts = line.split("\\|", -1);
            if (lineParts.length != 3) {
                errors.add(line);
                continue;
            }
            String nameTest = lineParts[0].trim();
            String statusTest = lineParts[1].trim();
            String durationTest = lineParts[2].trim();
            if (nameTest.isBlank()) {
                errors.add(nameTest + statusTest + durationTest);
                continue;
            }
            Status status;// переменная для манипуляций и сохранения в нужном типе
            try {
                status = Status.valueOf(statusTest.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add(line);
                continue;
            }

            long duration;// переменная для манипуляций и сохранения в нужном типе
            try {
                duration = Long.parseLong(durationTest);
            } catch (NumberFormatException e) {// проверка на буквы
                errors.add(line);
                continue;
            }
            if (Long.parseLong(durationTest) < 0) {// проверка на отрицательное время
                errors.add(line);
                continue;
            }

            TestResult result = new TestResult(nameTest, status, duration);// собрали все данные без ошибок орфографии

            lastTest.put(nameTest, result);// мапа сохранит по ключу только последний тест
        }


        return new ArrayList<>(lastTest.values());// тут долго думал как вернуть чтобы корректно отображалось


    }

    //T3
    public Optional<TestResult> firstFailed(List<TestResult> results) {
        return results.stream()
                .filter(r -> r.status() == Status.FAILED)
                .findFirst();
    }
    public List<String> getErrors() {
        return List.copyOf(errors);
    }

    //T4
    public static void main(String[] args) throws IOException {
        Path logPath = Path.of("run.log");

        List<String> lines;
        try (var reader = Files.newBufferedReader(logPath)) {
            lines = reader.lines().toList();
        }
        TestLogParser parser = new TestLogParser();
        List<TestResult> results = parser.parse(lines);

        System.out.println("Результаты:");
        for (TestResult r : results) {
            System.out.println("  " + r.name() + " — " + r.status()
                    + " (" + r.durationMs() + " ms)");
        }

        List<String> errs = parser.getErrors();
        if (errs.isEmpty()) {
            System.out.println("Битые строки: нет");
        } else {
            System.out.println("Битые строки: " + errs.get(0));
            for (int i = 1; i < errs.size(); i++) {
                System.out.println("  " + errs.get(i));
            }
        }

        Optional<TestResult> failed = parser.firstFailed(results);
        System.out.println("Первый упавший: "
                + failed.map(r -> r.name() + " (" + r.durationMs() + " ms)")
                .orElse("Упадений нет"));
    }
}

