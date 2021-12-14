import com.google.protobuf.DescriptorProtos;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class DescriptorSet {
    private final ConcurrentHashMap<String, ProtoBufFile> protoBufFiles;

    public static class ProtoBufFile {
        private final String name;
        private final ConcurrentHashMap<String, MessageDefinition> messageDefinitions;

        public ProtoBufFile(DescriptorProtos.FileDescriptorProto fileDescriptorProto) {
            name = fileDescriptorProto.getName();
            messageDefinitions = new ConcurrentHashMap<>();

            for (DescriptorProtos.DescriptorProto descriptorProto: fileDescriptorProto.getMessageTypeList()) {
                MessageDefinition messageDefinition = new MessageDefinition(descriptorProto);
                messageDefinitions.put(messageDefinition.getName(), messageDefinition);
            }
        }

        public ConcurrentHashMap<String, MessageDefinition> getMessageDefinitions() {
            return messageDefinitions;
        }

        public String getName() {
            return name;
        }

        public void see() {
            System.out.println("<" + name + ">");
            messageDefinitions.values().forEach(messageDefinition -> messageDefinition.see(""));
        }
    }

    public DescriptorSet(String path) {
        DescriptorProtos.FileDescriptorSet fileDescriptorSet = null;
        try {
            FileInputStream in = new FileInputStream(path);
            fileDescriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert fileDescriptorSet != null;

        protoBufFiles = new ConcurrentHashMap<>();

        for (DescriptorProtos.FileDescriptorProto fileDescriptorProto : fileDescriptorSet.getFileList()) {
            ProtoBufFile protoBufFile = new ProtoBufFile(fileDescriptorProto);
            protoBufFiles.put(protoBufFile.getName(), protoBufFile);
        }
    }

    public void see() {
        protoBufFiles.values().forEach(ProtoBufFile::see);
    }

    public ConcurrentHashMap<String, ProtoBufFile> getProtoBufFiles() {
        return protoBufFiles;
    }
}