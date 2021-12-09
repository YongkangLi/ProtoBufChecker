public class Main {
    private static final String path = "/Users/yongkang/Documents/Projects/ProtoBufChecker/test/desc/addressbook.desc";

    public static void main(String[] args) {
        DescriptorSet descriptorSet = new DescriptorSet(path);
        descriptorSet.see();
    }
}
