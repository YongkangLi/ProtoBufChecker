import org.apache.commons.cli.*;

public class Main {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("o", "old", false, "path to the directory of old version");
        options.addOption("n", "new", false, "path to the directory of new version");

        String oldVersion = null;
        String newVersion = null;

        CommandLineParser commandLineParser = new DefaultParser();
        try {
            CommandLine commandLine = commandLineParser.parse(options, args);
            if (commandLine.hasOption("o")) {
                oldVersion = commandLine.getOptionValue("o");
            }
            if (commandLine.hasOption("n")) {
                newVersion = commandLine.getOptionValue("n");
            }
            if (oldVersion != null && newVersion != null) {
                Utils.compareVersions(new Version(oldVersion), new Version(newVersion));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
