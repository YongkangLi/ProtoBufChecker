import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {
    private static final String WINDOWS = "windows";
    private static final String PROTO = ".proto";
    private static final String TARGET = "target";
    private static final String SHADED = "shaded";

    public static boolean isValid(Path path) {
        String s = path.toString();
        return s.endsWith(PROTO) && !s.contains(TARGET) && !s.contains(SHADED);
    }
    public static void runCommand(String command) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith(WINDOWS);

        ProcessBuilder processBuilder = new ProcessBuilder();
        if (isWindows) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.inheritIO().command("sh", "-c", command);
        }
        processBuilder.redirectErrorStream(true);

        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int exitCode = 0;
        try {
            assert process != null;
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assert exitCode == 0;
    }

    private static void compareEnumDefinitions(ConcurrentHashMap<String, MessageDefinition.EnumDefinition> oldEnumDefinitions,
                                               ConcurrentHashMap<String, MessageDefinition.EnumDefinition> newEnumDefinitions) {
        for (String enumTypeName : oldEnumDefinitions.keySet()) {
            if (!newEnumDefinitions.containsKey(enumTypeName)) {
                // An deleted enum definition.
                if (oldEnumDefinitions.get(enumTypeName).hasNoDefault()) {
                    // This enum definition does not have default 0 value.
                    System.out.println("INFO: an deleted enum '" + enumTypeName + "' with no 0 value.");
                }
            }
        }

        for (String enumTypeName : newEnumDefinitions.keySet()) {
            if (!oldEnumDefinitions.containsKey(enumTypeName)) {
                // An added enum definition.
                if (newEnumDefinitions.get(enumTypeName).hasNoDefault()) {
                    // This enum definition does not have default 0 value.
                    System.out.println("INFO: an added enum '" + enumTypeName + "' with no 0 value.");
                }
            }
        }
    }

    private static void compareFields(ConcurrentHashMap<String, MessageDefinition.Field> oldFields,
                                      ConcurrentHashMap<String, MessageDefinition.Field> newFields) {
        for (String fieldName : oldFields.keySet()) {
            if (newFields.containsKey(fieldName)) {
                // Both Versions have this field.
                MessageDefinition.Field oldField = oldFields.get(fieldName);
                MessageDefinition.Field newField = newFields.get(fieldName);

                // Compare their tag numbers.
                if (oldField.getTag() != newField.getTag()) {
                    // Different tag number!
                    System.out.println("\u001B[31m" + "ERROR: tag number of field '" + fieldName + "' has changed across versions!!!" + "\033[0m");
                }

                // Compare their labels;
                if (oldField.isRequired() ^ newField.isRequired()) {
                    // Different required properties!
                    System.out.println("\033[0;33m" + "WARNING: field '" + fieldName + "' is required in one version but non-required in another!" + "\033[0m");
                }
            } else if (oldFields.get(fieldName).isRequired()){
                // Old version has this required field, but new version does not have this field at all!
                System.out.println("\u001B[31m" + "ERROR: a required field '" + fieldName + "' is deleted in new version!!!" + "\033[0m");
            }
        }

        for (String fieldName : newFields.keySet()) {
            if ((!oldFields.containsKey(fieldName)) && (newFields.get(fieldName).isRequired())) {
                // New version has this required field, but old version does not have this field at all!
                System.out.println("\u001B[31m" + "ERROR: a required field '" + fieldName + "' is added in new version!!!" + "\033[0m");
            }
        }
    }

    private static void compareMessageDefinition(MessageDefinition oldMessageDefinition, MessageDefinition newMessageDefinition) {
        compareFields(oldMessageDefinition.getFields(), newMessageDefinition.getFields());
        compareEnumDefinitions(oldMessageDefinition.getEnumDefinitions(), newMessageDefinition.getEnumDefinitions());
    }

    public static void compareDescriptorSets(DescriptorSet older, DescriptorSet newer) {
        for (String filename : older.getProtoBufFiles().keySet()) {
            if (newer.getProtoBufFiles().containsKey(filename)) {
               DescriptorSet.ProtoBufFile oldProtoBufFile = older.getProtoBufFiles().get(filename);
               DescriptorSet.ProtoBufFile newProtoBufFile = newer.getProtoBufFiles().get(filename);
               for (String messageType : oldProtoBufFile.getMessageDefinitions().keySet()) {
                   if (newProtoBufFile.getMessageDefinitions().containsKey(messageType)) {
                       MessageDefinition oldMessageDefinition = oldProtoBufFile.getMessageDefinitions().get(messageType);
                       MessageDefinition newMessageDefinition = newProtoBufFile.getMessageDefinitions().get(messageType);
                       compareMessageDefinition(oldMessageDefinition, newMessageDefinition);
                   }
               }
            }
        }
    }

    public static void compareDescriptorSets(String older, String newer) {
        compareDescriptorSets(new DescriptorSet(older), new DescriptorSet(newer));
    }

    public static void compareVersions(Version older, Version newer) {
        String oldDescriptor = "./test/old/tmp.desc";
        String newDescriptor = "./test/new/tmp.desc";
        try {
            Files.find(older.getPath(), Integer.MAX_VALUE, (filePath, fileAttr) -> isValid(filePath)).forEach(protoPath -> {
                System.out.println(protoPath);
                if (protoPath.toString().contains("target")) {
                    System.out.println(protoPath);
                }
                Path relativePath = older.getPath().relativize(protoPath);
                Path newProtoPath = newer.getPath().resolve(relativePath);
                if (new File(String.valueOf(newProtoPath)).exists()) {
                    String compileOld = "protoc " + older.getIncludes() + "--descriptor_set_out=" + oldDescriptor + " " + protoPath;
                    String compileNew = "protoc " + newer.getIncludes() + "--descriptor_set_out=" + newDescriptor + " " + newProtoPath;
                    runCommand(compileOld);
                    runCommand(compileNew);
                    System.out.println("<" + relativePath + ">");
                    if (new File(oldDescriptor).exists() && new File(newDescriptor).exists()) {
                        compareDescriptorSets(oldDescriptor, newDescriptor);
                        System.out.println();
                        runCommand("rm " + oldDescriptor);
                        runCommand("rm " + newDescriptor);
                    } else {
                        System.out.println("Error during compilation!\n");
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
