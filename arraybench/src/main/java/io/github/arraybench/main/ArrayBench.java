package io.github.arraybench.main;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Random;

import io.github.arraybench.sorts.templates.Sort;
import io.github.arraybench.utils.Delays;
import io.github.arraybench.utils.Highlights;
import io.github.arraybench.utils.Writes;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class ArrayBench {
    static ArrayVisualizer arrayVisualizer;
    static Object distribution, shuffle;
    static Method distMethod, shuffleMethod;

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

    static boolean isArrayVDir(File dir) {
        // Check for visuals to verify this is ArrayV, not ArrayBench
        for (String subDir : new String[] {"visuals", "sorts", "utils"}) {
            File subFile = new File(dir, subDir);
            if (!subFile.exists()) {
                return false;
            }
        }
        return true;
    }

    static void distribute(int[] array) {
        if (distMethod == null) {
            for (int i = 0; i < arrayVisualizer.getCurrentLength(); i++) {
                array[i] = i;
            }
        } else {
            try {
                distMethod.invoke(distribution, array, arrayVisualizer);
            } catch (Exception e) {
                System.err.println("Distribution method unusable: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    static void shuffle(int[] array) {
        if (shuffleMethod == null) {
            int arrayLength = arrayVisualizer.getCurrentLength();
            Random random = new Random();
            for (int i = arrayLength - 1; i > 0; i--) {
                int dest = random.nextInt(i);
                int tmp = array[i];
                array[i] = array[dest];
                array[dest] = tmp;
            }
        } else {
            try {
                shuffleMethod.invoke(shuffle, array, arrayVisualizer, 
                                     arrayVisualizer.getDelays(),
                                     arrayVisualizer.getHighlights(),
                                     arrayVisualizer.getWrites());
            } catch (Exception e) {
                System.err.println("Shuffle method unusable: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    static double runSort(int[] array, int arrayLength, Class<? extends Sort> sortClass) {
        distribute(array);
        shuffle(array);

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
            .help("The length of the array");
        parser.addArgument("-p", "--pre-reps").metavar("COUNT").dest("preReps")
            .type(int.class).setDefault(1)
            .help("The number of times to run the sort without benching. This helps the JIT.");
        parser.addArgument("-r", "--reps").metavar("COUNT").dest("reps")
            .type(int.class).setDefault(3)
            .help("The number of times to run the sort benching");
        parser.addArgument("-a", "--arrayv").metavar("PATH").dest("arrayv")
            .type(ArrayBench::existingFile).setDefault((File)null)
            .help("The path to a directory in which to find ArrayV (not required)");
        parser.addArgument("-d", "--distribution").metavar("NAME").dest("distribution")
            .setDefault((String)null)
            .help("The distribution to use");
        parser.addArgument("-s", "--shuffle").metavar("NAME").dest("shuffle")
            .setDefault((String)null)
            .help("The shuffle to use");

        Namespace ns = parser.parseArgsOrFail(args);

        File sortFile = ns.get("sortFile");

        File arrayvDirectory = ns.get("arrayv");
        if (arrayvDirectory == null) {
            File packageRoot = sortFile.getParentFile();
            while (packageRoot != null && !packageRoot.getName().equals("sorts")) {
                packageRoot = packageRoot.getParentFile();
            }
            if (packageRoot != null) {
                arrayvDirectory = packageRoot.getParentFile();
            }
        } else {
            if (!isArrayVDir(arrayvDirectory)) {
                File testFile;
                if (isArrayVDir(testFile = new File(arrayvDirectory, "src"))) {
                    arrayvDirectory = testFile;
                } else if (isArrayVDir(testFile = new File(arrayvDirectory, "main/java/io/github/arrayv"))) {
                    arrayvDirectory = testFile;
                } else if (isArrayVDir(testFile = new File(arrayvDirectory, "src/main/java/io/github/arrayv"))) {
                    arrayvDirectory = testFile;
                }
            }
        }

        if (arrayvDirectory == null) {
            System.out.println("ArrayV directory not found: Some features (such as shuffles) will be disabled\n");
        } else {
            System.out.println("ArrayV directory found: " + arrayvDirectory + "\n");
        }

        int arrayLength = ns.getInt("arrayLength");
        arrayVisualizer = new ArrayVisualizer(arrayLength);
        SortAnalyzer analyzer = new SortAnalyzer(arrayVisualizer);
        File sortsDir = new File(arrayvDirectory, "sorts");

        distMethod = null;
        if (arrayvDirectory != null) {
            try {
                analyzer.importClass(sortsDir, new File(arrayvDirectory, "utils/Distributions.java"), false, "util");
                Class<?> distributionClass = Class.forName("io.github.arrayv.utils.Distributions");
                distMethod = distributionClass.getMethod("initializeArray", int[].class, ArrayVisualizer.class);
                distribution = distributionClass.getMethod("valueOf", String.class).invoke(null, ns.getString("distribution"));
            } catch (Exception e) {
                if (!(e instanceof ClassNotFoundException)) {
                    System.err.println("Failed to load distribution: " + e);
                }
            }
        }

        shuffleMethod = null;
        if (arrayvDirectory != null) {
            try {
                analyzer.importClass(sortsDir, new File(arrayvDirectory, "utils/Shuffles.java"), false, "util");
                Class<?> shuffleClass = Class.forName("io.github.arrayv.utils.Shuffles");
                shuffleMethod = shuffleClass.getMethod("shuffleArray", int[].class, ArrayVisualizer.class, Delays.class, Highlights.class, Writes.class);
                shuffle = shuffleClass.getMethod("valueOf", String.class).invoke(null, ns.getString("shuffle"));
            } catch (Exception e) {
                if (!(e instanceof ClassNotFoundException)) {
                    System.err.println("Failed to load shuffle: " + e);
                }
            }
        }

        Sort sort = analyzer.importClass(sortsDir, sortFile, true, "sort");
        if (sort == null) {
            String invalidMessage = analyzer.getInvalidSorts();
            if (invalidMessage != null) {
                System.err.println(invalidMessage);
            }
            System.exit(1);
        }

        System.out.println("\nSort: " + sort.getRunSortName() + "     Length: " + arrayLength);

        String suggestions = analyzer.getSuggestions();
        if (suggestions != null) {
            System.out.println(suggestions);
        }

        int[] array = new int[arrayLength];

        Class<? extends Sort> sortClass = sort.getClass();

        System.out.println("\nPrerunning");
        int preReps = ns.getInt("preReps");
        for (int i = 0; i < preReps; i++) {
            runSort(array, arrayLength, sortClass);
            System.out.print(".");
        }

        double total = 0;
        double min = Double.MAX_VALUE;

        System.out.println("\nRunning");
        int reps = ns.getInt("reps");
        for (int i = 0; i < reps; i++) {
            double time = runSort(array, arrayLength, sortClass);
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
