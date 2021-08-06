package io.github.arraybench.main;

import java.io.File;

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

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("ArrayBench").build()
            .defaultHelp(true)
            .description("Benching program for ArrayV");

        parser.addArgument("sortFile").metavar("SORT_FILE")
            .type(ArrayBench::existingFile)
            .help("The .java file of the sort");

        Namespace ns = parser.parseArgsOrFail(args);
        System.out.println(ns);
    }
}
