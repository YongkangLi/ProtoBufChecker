import java.io.IOException;
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

    public static int runCommand(String command) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith(WINDOWS);

        ProcessBuilder processBuilder = new ProcessBuilder();
        if (isWindows) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }

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
//        assert exitCode == 0;
        return exitCode;
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
                                      ConcurrentHashMap<String, MessageDefinition.Field> newFields,
                                      Path relativePath) {
        for (String fieldName : oldFields.keySet()) {
            if (newFields.containsKey(fieldName)) {
                // Both Versions have this field.
                MessageDefinition.Field oldField = oldFields.get(fieldName);
                MessageDefinition.Field newField = newFields.get(fieldName);

                // Compare their tag numbers.
                if (oldField.getTag() != newField.getTag()) {
                    // Different tag number!
                    System.out.println("\u001B[31m" + "ERROR: tag number of field '" + fieldName +
                            "' has changed across versions!!! " +
                            "(" + relativePath + ") " + "\033[0m");
                }

                // Compare their labels;
                if (oldField.isRequired() ^ newField.isRequired()) {
                    // Different required properties!
                    System.out.println("\033[0;33m" + "WARNING: field '" + fieldName +
                            "' is required in one version but non-required in another! " +
                            "(" + relativePath + ") " + " \033[0m");
                }
            } else if (oldFields.get(fieldName).isRequired()){
                // Old version has this required field, but new version does not have this field at all!
                System.out.println("\u001B[31m" + "ERROR: a required field '" + fieldName +
                        "' is deleted in new version!!! " +
                        "(" + relativePath + ") " + "\033[0m");
            }
        }

        for (String fieldName : newFields.keySet()) {
            if ((!oldFields.containsKey(fieldName)) && (newFields.get(fieldName).isRequired())) {
                // New version has this required field, but old version does not have this field at all!
                System.out.println("\u001B[31m" + "ERROR: a required field '" + fieldName +
                        "' is added in new version!!! " +
                        "(" + relativePath + ") " + "\033[0m");
            }
        }
    }

    private static void compareMessageDefinition(MessageDefinition oldMessageDefinition,
                                                 MessageDefinition newMessageDefinition,
                                                 Path relativePath) {
        compareFields(oldMessageDefinition.getFields(), newMessageDefinition.getFields(), relativePath);
        compareEnumDefinitions(oldMessageDefinition.getEnumDefinitions(), newMessageDefinition.getEnumDefinitions());
    }

    public static void compareDescriptorSets(DescriptorSet older, DescriptorSet newer, Path relativePath) {
        for (String filename : older.getProtoBufFiles().keySet()) {
            DescriptorSet.ProtoBufFile oldProtoBufFile = older.getProtoBufFiles().get(filename);
            if (newer.getProtoBufFiles().containsKey(filename)) {
               DescriptorSet.ProtoBufFile newProtoBufFile = newer.getProtoBufFiles().get(filename);
               for (String messageType : oldProtoBufFile.getMessageDefinitions().keySet()) {
                   if (newProtoBufFile.getMessageDefinitions().containsKey(messageType)) {
                       MessageDefinition oldMessageDefinition = oldProtoBufFile.getMessageDefinitions().get(messageType);
                       MessageDefinition newMessageDefinition = newProtoBufFile.getMessageDefinitions().get(messageType);
                       compareMessageDefinition(oldMessageDefinition, newMessageDefinition, relativePath);
                   } else {
                       System.out.println("INFO: a message type " + messageType + " was deleted in new version. (" + relativePath + ")");
                   }
               }
               for (String messageType : newProtoBufFile.getMessageDefinitions().keySet()) {
                   if (!oldProtoBufFile.getMessageDefinitions().containsKey(messageType)) {
                       System.out.println("INFO: a message type " + messageType + " was added in new version. (" + relativePath + ")");
                   }
               }
            }
        }
    }

    private static void check(DescriptorSet descriptorSet, String action, String reason, Path relativePath) {
        for (String fileName : descriptorSet.getProtoBufFiles().keySet()) {
            for (String messageType : descriptorSet.getProtoBufFiles().get(fileName).getMessageDefinitions().keySet()) {
                System.out.println("INFO: a message type " + messageType + " was " + action + " due to a/an " + reason + " file: " + relativePath);
            }
        }
    }
    public static void compareVersions(Version older, Version newer) {
        for (Path relativePath : older.getDescriptorSets().keySet()) {
            if (newer.getDescriptorSets().containsKey(relativePath)) {
                compareDescriptorSets(older.getDescriptorSets().get(relativePath), newer.getDescriptorSets().get(relativePath), relativePath);
            } else {
                check(older.getDescriptorSets().get(relativePath), "deleted", "removed", relativePath);
            }
        }
        for (Path relativePath : newer.getDescriptorSets().keySet()) {
            if (!older.getDescriptorSets().containsKey(relativePath)) {
                check(newer.getDescriptorSets().get(relativePath), "added", "added", relativePath);
            }
        }
    }
}