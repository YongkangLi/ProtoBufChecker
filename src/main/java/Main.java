import org.apache.commons.cli.*;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("o", "old", true, "path to the directory of old version");
        options.addOption("n", "new", true, "path to the directory of new version");
        options.addOption("app", true, "directory of the software");
        options.addOption("v1", true, "tag of old version");
        options.addOption("v2", true, "tag of new versions");

        String oldVersion = null;
        String newVersion = null;

        CommandLineParser commandLineParser = new DefaultParser();
        try {
            CommandLine commandLine = commandLineParser.parse(options, args);
            if (commandLine.hasOption("app") && commandLine.hasOption("v1") && commandLine.hasOption("v2")) {
                String software = commandLine.getOptionValue("app");
                String directory = "./" + software;
                String oldTag = commandLine.getOptionValue("v1");
                String newTag = commandLine.getOptionValue("v2");
                if (new File(directory).exists()) {
                    Version older = null;
                    Version newer = null;

                    if (Utils.runCommand("cd " + directory + "&& git checkout " + oldTag) == 0) {
                        older = new Version(directory);
                    } else {
                        System.out.println("Error happened when switching to branch " + oldTag + "!");
                    }

                    if (Utils.runCommand("cd " + directory + "&& git checkout " + newTag) == 0) {
                        newer = new Version(directory);
                    } else {
                        System.out.println("Error happened when switching to branch " + newTag + "!");
                    }
                    Utils.runCommand("cd ..");

                    if (older != null && newer != null) {
                        Utils.compareVersions(older, newer);
                    }
                } else {
                    System.out.println("Invalid software name: " + software + "!");
                }
            } else {
                if (commandLine.hasOption("o")) {
                    oldVersion = commandLine.getOptionValue("o");
                    if (!new File(oldVersion).exists()) {

                        System.out.println("Invalid path: " + oldVersion);
                        oldVersion = null;
                    }
                }
                if (commandLine.hasOption("n")) {
                    newVersion = commandLine.getOptionValue("n");
                    if (!new File(newVersion).exists()) {
                        System.out.println("Invalid path: " + newVersion);
                        newVersion = null;
                    }
                }
                if (oldVersion != null && newVersion != null) {
                    Utils.compareVersions(new Version(oldVersion), new Version(newVersion));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
