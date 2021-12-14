import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Version {
    private static final String TMP = "./tmp.desc";

    private final Path path;

    private final ConcurrentHashMap<Path, DescriptorSet> descriptorSets;

    public Version(String directory) {
        path = Paths.get(directory);
        descriptorSets = new ConcurrentHashMap<>();

        List<String> includePaths = new ArrayList<>();
        List<Path> paths = new ArrayList<>();

        StringBuilder includeParameters = new StringBuilder();

        try {
            Files.find(Paths.get(directory), Integer.MAX_VALUE, (filePath, fileAttr) -> Utils.isValid(filePath)).forEach(protoPath -> {
                String path = String.valueOf(protoPath.getParent());
                if (!includePaths.contains(path)) {
                    includePaths.add(path);
                }
                includeParameters.append("-I").append(path).append(" ");
                paths.add(protoPath);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        paths.forEach(path -> {
            String compilationCommand = "protoc " + includeParameters + "--descriptor_set_out=" + TMP + " " + path;
            Utils.runCommand(compilationCommand);
            if (new File(TMP).exists()) {
                descriptorSets.put(Paths.get(directory).relativize(path), new DescriptorSet(TMP));
                Utils.runCommand("rm " + TMP);
            } else {
                System.out.println("\u001B[31m" + "Error happened when compiling" + path +" !\n" + "\033[0m");
            }
        });
    }

    public Path getPath() {
        return path;
    }

    public ConcurrentHashMap<Path, DescriptorSet> getDescriptorSets() {
        return descriptorSets;
    }
}
