import com.google.protobuf.DescriptorProtos;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoBufFile {
    private final ConcurrentHashMap<String Message> Message
    public ProtoBufFile(String path) {
        DescriptorProtos.FileDescriptorSet fileDescriptorSet = null;
        try {
            FileInputStream in = new FileInputStream(path);
            fileDescriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert fileDescriptorSet != null;

        for (DescriptorProtos.FileDescriptorProto fileDescriptorProto: fileDescriptorSet.getFileList()) {
            for (DescriptorProtos.DescriptorProto descriptorProto: fileDescriptorProto.getMessageTypeList()) {
                Message message = new Message(descriptorProto);
                message.see("");
            }
        }
    }
}
