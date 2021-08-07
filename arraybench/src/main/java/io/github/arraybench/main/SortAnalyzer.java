package io.github.arraybench.main;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

import javax.tools.ToolProvider;
import javax.tools.JavaCompiler;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import io.github.arraybench.sorts.templates.Sort;

/*
 * 
The MIT License (MIT)

Copyright (c) 2021 Gaming32

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 *
 */

final public class SortAnalyzer {
    private String invalidMessage;
    private String suggestions;
    
    private String sortErrorMsg;
    
    private ArrayVisualizer arrayVisualizer;
    
    public SortAnalyzer(ArrayVisualizer arrayVisualizer) {
        this.arrayVisualizer = arrayVisualizer;
    }

    private Sort compileSingle(String name, ClassLoader loader, boolean initialize) {
        Sort sort;
        try {
            Class<?> sortClass = Class.forName(name, true, loader);
            if (!initialize) {
                return null;
            }
            Constructor<?> newSort = sortClass.getConstructor(new Class[] {ArrayVisualizer.class});
            sort = (Sort) newSort.newInstance(this.arrayVisualizer);
            
            try {
                if(verifySort(sort)) {
                    String suggestion = checkForSuggestions(sort);
                    if(!suggestion.isEmpty()) {
                        suggestions = suggestion;
                    }
                }
                else {
                    throw new Exception();
                }
            }
            catch(Exception e) {
                invalidMessage = sort.getClass().getName() + " (" + this.sortErrorMsg + ")";
                return null;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.err.println("Could not compile " + name);
            invalidMessage = name + " (failed to compile)";
            return null;
        }
        return sort;
    }

    public Sort importSort(File packageRoot, File file, boolean initialize) {
        String contents;
        try {
            contents = new String(Files.readAllBytes(file.toPath()));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Pattern packagePattern = Pattern.compile("^\\s*package io\\.github\\.arrayv\\.sorts\\.([a-zA-Z\\.]+);");
        boolean legacy = false, isTemplate = false;
        Matcher matcher = packagePattern.matcher(contents);
        if (!matcher.find()) {
            Pattern packagePatternLegacy = Pattern.compile("^\\s*package sorts\\.([a-zA-Z\\.]+);");
            matcher = packagePatternLegacy.matcher(contents);
            if (!matcher.find()) {
                System.err.println("No package io.github.arrayv.sorts specifed");
                return null;
            }
            legacy = true;
            if (matcher.group(1).startsWith("templates")) {
                isTemplate = true;
                contents = contents.replaceAll("package sorts\\.templates", "package io.github.arraybench.sorts.templates");
            } else {
                contents = contents.replaceAll("package sorts", "package io.github.arrayv.sorts");
            }
        }

        String root = legacy ? "" : "io.github.arrayv.";
        for (String subPackage : new String[] {"main", "utils"}) {
            contents = contents.replaceAll("import " + root + subPackage, "import io.github.arraybench." + subPackage);
        }

        Pattern findSortImport = Pattern.compile("^\\s*import " + root + "sorts\\.([a-zA-Z.]+);", Pattern.MULTILINE);
        Matcher importMatcher = findSortImport.matcher(contents);
        while (importMatcher.find()) {
            String rel = importMatcher.group(1);
            if (rel.endsWith("templates.Sort")) {
                continue;
            }
            importSort(packageRoot, new File(packageRoot, rel.replaceAll("\\.", "/") + ".java"), false);
        }
        contents = contents.replaceAll("import " + root + "sorts.templates", "import io.github.arraybench.sorts.templates");
        contents = contents.replaceAll("import " + root + "sorts", "import io.github.arrayv.sorts");

        String packageName = "io.github." + (isTemplate ? "arraybench" : "arrayv") + ".sorts." + matcher.group(1);
        String name = packageName + "." + file.getName().split("\\.")[0];
        File tempPath = new File("./cache/" + String.join("/", packageName.split("\\.")));
        tempPath.mkdirs();
        File destPath = new File(tempPath.getAbsolutePath() + "/" + file.getName());
        try {
            FileWriter writer = new FileWriter(destPath);
            writer.write(contents);
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int success = compiler.run(null, null, null, destPath.getAbsolutePath());
        if (success != 0) {
            System.err.println("Failed to compile: " + destPath.getPath() + "\nError code " + success);
            return null;
        }

        Sort sort;
        try {
            if ((sort = compileSingle(name, URLClassLoader.newInstance(new URL[] { new File("./cache/").toURI().toURL() }), initialize)) == null && initialize)
                return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        System.out.println("Successfully imported sort " + name);
        return sort;
    }
    
    private boolean verifySort(Sort sort) {
        if(!sort.isSortEnabled()) {
            this.sortErrorMsg = "manually disabled";
            return false;
        }
        if(sort.getSortListName().equals("")) {
            this.sortErrorMsg = "missing 'Choose Sort' name";
            return false;
        }
        if(sort.getRunAllSortsName().equals("")) {
            this.sortErrorMsg = "missing 'Run All' name";
            return false;
        }
        if(sort.getRunSortName().equals("")) {
            this.sortErrorMsg = "missing 'Run Sort' name";
            return false;
        }
        if(sort.getCategory().equals("")) {
            this.sortErrorMsg = "missing category";
            return false;
        }
        
        return true;
    }
    
    private static String checkForSuggestions(Sort sort) {
        StringBuilder suggestions = new StringBuilder();
        boolean warned = false;
        
        if(sort.isBogoSort() && !sort.isUnreasonablySlow()) {
            suggestions.append("- " + sort.getRunSortName() + " is a bogosort. It should be marked 'unreasonably slow'.\n");
            warned = true;
        }
        if(sort.isUnreasonablySlow() && sort.getUnreasonableLimit() == 0) {
            suggestions.append("- A warning will pop up every time you select " + sort.getRunSortName() + ". You might want to change its 'unreasonable limit'.\n");
            warned = true;
        }
        if(!sort.isUnreasonablySlow() && sort.getUnreasonableLimit() != 0) {
            suggestions.append("- You might want to set " + sort.getRunSortName() + "'s 'unreasonable limit' to 0. It's not marked 'unreasonably slow'.\n");
            warned = true;
        }
        if(sort.isRadixSort() && !sort.usesBuckets()) {
            suggestions.append("- " + sort.getRunSortName() + " is a radix sort and should also be classified as a bucket sort.\n");
            warned = true;
        }
        if(sort.isRadixSort() && sort.isComparisonBased()) {
            suggestions.append("- " + sort.getRunSortName() + " is a radix sort. It probably shouldn't be labelled as a comparison-based sort.\n");
            warned = true;
        }
        
        if(warned) {
            suggestions.deleteCharAt(suggestions.length() - 1);
        }
        return suggestions.toString();
    }

    public String getInvalidSorts() {
        return invalidMessage;
    }

    public String getSuggestions() {
        return suggestions;
    }
}