package io.github.arraybench.main;

import java.io.File;
import java.util.Random;

import io.github.arraybench.sorts.templates.Sort;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class ArrayBench {
    static File existingFile(ArgumentParser parser, Argument arg, String value) throws ArgumentParserException {
        File file = new File(value);
        if (!file.exists()) {
            throw new ArgumentParserException(file + " does not exist", parser);
        }
        return file;
    }

    static Sort createSortInstance(Class<? extends Sort> sortClass, ArrayVisualizer arrayVisualizer) {
        try {
            Sort inst = sortClass.getConstructor(ArrayVisualizer.class).newInstance(arrayVisualizer);
            return inst;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    static double runSort(int[] array, int arrayLength, Class<? extends Sort> sortClass, ArrayVisualizer arrayVisualizer) {
        for (int i = 0; i < arrayLength; i++) {
            array[i] = i;
        }

        Random random = new Random();
        for (int i = arrayLength - 1; i > 0; i--) {
            int dest = random.nextInt(i);
            int tmp = array[i];
            array[i] = array[dest];
            array[dest] = tmp;
        }

        Sort inst = createSortInstance(sortClass, arrayVisualizer);

        double start = System.currentTimeMillis();
        try {
            inst.runSort(array, arrayLength, 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("The sort raised an exception. Terminating...");
            System.exit(1);
        }
        double end = System.currentTimeMillis();
        return end - start;
    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("ArrayBench").build()
            .defaultHelp(true)
            .description("Benching program for ArrayV");

        parser.addArgument("sortFile").metavar("SORT_FILE")
            .type(ArrayBench::existingFile)
            .help("The .java file of the sort");
        parser.addArgument("-l", "--length").metavar("LENGTH").dest("arrayLength")
            .type(int.class).setDefault(1024)
            .help("The length of the array. Default is 1024");
        parser.addArgument("-p", "--pre-reps").metavar("COUNT").dest("preReps")
            .type(int.class).setDefault(1)
            .help("The number of times to run the sort without benching. This helps the JIT. Default is 1");
        parser.addArgument("-r", "--reps").metavar("COUNT").dest("reps")
            .type(int.class).setDefault(3)
            .help("The number of times to run the sort benching. Default is 3");

        Namespace ns = parser.parseArgsOrFail(args);

        File sortFile = ns.get("sortFile");
        File packageRoot = sortFile.getParentFile();
        while (packageRoot != null && !packageRoot.getName().equals("sorts")) {
            packageRoot = packageRoot.getParentFile();
        }

        ArrayVisualizer arrayVisualizer = new ArrayVisualizer();
        SortAnalyzer analyzer = new SortAnalyzer(arrayVisualizer);
        Sort sort = analyzer.importSort(packageRoot, sortFile, true);
        if (sort == null) {
            String invalidMessage = analyzer.getInvalidSorts();
            if (invalidMessage != null) {
                System.err.println(invalidMessage);
            }
            System.exit(1);
        }

        int arrayLength = ns.getInt("arrayLength");
        System.out.println("Sort: " + sort.getRunSortName() + "     Length: " + arrayLength);

        String suggestions = analyzer.getSuggestions();
        if (suggestions != null) {
            System.out.println(suggestions);
        }

        int[] array = new int[arrayLength];

        Class<? extends Sort> sortClass = sort.getClass();

        System.out.println("\nPrerunning");
        int preReps = ns.getInt("preReps");
        for (int i = 0; i < preReps; i++) {
            runSort(array, arrayLength, sortClass, arrayVisualizer);
            System.out.print(".");
        }

        double total = 0;
        double min = Double.MAX_VALUE;

        System.out.println("\nRunning");
        int reps = ns.getInt("reps");
        for (int i = 0; i < reps; i++) {
            double time = runSort(array, arrayLength, sortClass, arrayVisualizer);
            total += time;
            if (time < min) {
                min = time;
            }
            System.out.print(".");
        }

        double mean = total / reps;
        System.out.println("\n\nSummary\n-------------");
        System.out.println("Total time: " + total / 1000);
        System.out.println("Average time: " + mean / 1000);
        System.out.println("Minimum time: " + min / 1000);
    }
}
