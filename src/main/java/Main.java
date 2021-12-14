import org.apache.commons.cli.*;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("o", "old", true, "path to the directory of old version");
        options.addOption("n", "new", true, "path to the directory of new version");

        String oldVersion = null;
        String newVersion = null;

        CommandLineParser commandLineParser = new DefaultParser();
        try {
            CommandLine commandLine = commandLineParser.parse(options, args);
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
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
