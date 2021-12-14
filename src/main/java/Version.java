import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Version {

    private final Path path;
    private final String includes;

    public Version(String directory) {
        path = Paths.get(directory);

        List<String> includePaths = new ArrayList<>();

        StringBuilder includeParameters = new StringBuilder();

        try {
            Files.find(Paths.get(directory), Integer.MAX_VALUE, (filePath, fileAttr) -> Utils.isValid(filePath)).forEach(protoPath -> {
                String path = String.valueOf(protoPath.getParent());
                if (!includePaths.contains(path)) {
                    includePaths.add(path);
                }
                includeParameters.append("-I").append(path).append(" ");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        includes = String.valueOf(includeParameters);
    }

    public Path getPath() {
        return path;
    }

    public String getIncludes() {
        return includes;
    }
}
