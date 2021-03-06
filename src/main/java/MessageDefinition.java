import com.google.protobuf.DescriptorProtos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MessageDefinition {
    private final String name;

    private final ConcurrentHashMap<String, EnumDefinition> enumDefinitions;
    private final ConcurrentHashMap<String, MessageDefinition> nestedTypes;
    private final ConcurrentHashMap<String, Field> fields;

    public static class Field {
        private final DescriptorProtos.FieldDescriptorProto.Label label;
        private final String type;
        private final String name;
        private final int tag;

        public Field(DescriptorProtos.FieldDescriptorProto fieldDescriptorProto) {
            label = fieldDescriptorProto.getLabel();
            type = fieldDescriptorProto.getTypeName().equals("") ? fieldDescriptorProto.getType().toString() : fieldDescriptorProto.getTypeName();
            name = fieldDescriptorProto.getName();
            tag = fieldDescriptorProto.getNumber();
        }

        public String getName() {
            return name;
        }

        public int getTag() {
            return tag;
        }

        public boolean isRequired() {
            return label.equals(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED);
        }

        public void see(String indent) {
            System.out.println(indent + label + " " + type + " " + name + " = " + tag + ";");
        }
    }

    public static class EnumDefinition {
        private final String name;
        private final List<Value> valueList;

        private static class Value {
            private final String name;
            private final int number;

            public Value(DescriptorProtos.EnumValueDescriptorProto enumValueDescriptorProto) {
                name = enumValueDescriptorProto.getName();
                number = enumValueDescriptorProto.getNumber();
            }
        }

        public EnumDefinition(DescriptorProtos.EnumDescriptorProto enumDescriptorProto) {
            name = enumDescriptorProto.getName();
            valueList = new ArrayList<>();
            for (DescriptorProtos.EnumValueDescriptorProto enumValueDescriptorProto: enumDescriptorProto.getValueList()) {
                valueList.add(new Value(enumValueDescriptorProto));
            }
        }

        public String getName() {
            return name;
        }

        public boolean hasNoDefault() {
            boolean hasNoZero = true;
            for (Value value : valueList) {
                if (value.number == 0) {
                    hasNoZero = false;
                    break;
                }
            }
            return hasNoZero;
        }

        public void see(String indent) {
            System.out.println(indent + "enum " + name + " {");
            for (Value value: valueList) {
                System.out.println(indent + "  " + value.name + " = " + value.number + ";");
            }
            System.out.println(indent + "}");
            System.out.println();
        }
    }

    public MessageDefinition(DescriptorProtos.DescriptorProto descriptorProto) {
        name = descriptorProto.getName();
        enumDefinitions = new ConcurrentHashMap<>();
        nestedTypes = new ConcurrentHashMap<>();
        fields = new ConcurrentHashMap<>();

        for (DescriptorProtos.EnumDescriptorProto enumDescriptorProto: descriptorProto.getEnumTypeList()) {
            EnumDefinition enumDefinition = new EnumDefinition(enumDescriptorProto);
            enumDefinitions.put(enumDefinition.getName(), enumDefinition);
        }

        for (DescriptorProtos.DescriptorProto nestedTypeDescriptorProto: descriptorProto.getNestedTypeList()) {
            MessageDefinition messageDefinition = new MessageDefinition(nestedTypeDescriptorProto);
            nestedTypes.put(messageDefinition.getName(), messageDefinition);
        }
        for (DescriptorProtos.FieldDescriptorProto fieldDescriptorProto: descriptorProto.getFieldList()) {
            Field field = new Field(fieldDescriptorProto);
            fields.put(field.getName(), field);
        }
    }

    public String getName() {
        return name;
    }

    public ConcurrentHashMap<String, Field> getFields() {
        return fields;
    }

    public ConcurrentHashMap<String, EnumDefinition> getEnumDefinitions() {
        return enumDefinitions;
    }

    private void seeEnumDefinitions(String indent) {
        enumDefinitions.values().forEach(enumDefinition -> enumDefinition.see(indent));
    }

    private void seeNestedTypes(String indent) {
        nestedTypes.values().forEach(messageDefinition -> messageDefinition.see(indent));
    }

    private void seeFields(String indent) {
        fields.values().forEach(field -> field.see(indent));
    }

    public void see(String indent) {
        System.out.println(indent + "message " + name + " {");
        seeEnumDefinitions("  " + indent);
        seeNestedTypes("  " + indent);
        seeFields("  " + indent);
        System.out.println(indent + "}");
        System.out.println();
    }
}
