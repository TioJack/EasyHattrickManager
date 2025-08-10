package easyhattrickmanager.controller;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import easyhattrickmanager.client.hattrick.ZonedDateTimeDeserializer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.atteo.evo.inflector.English;
import org.mapstruct.ap.internal.util.Strings;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("pojo")
public class PojoController {

    private static final String INPUT_FOLDER = "zzPojoIN/";
    private static final String OUTPUT_FOLDER = "zzPojoOUT/";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final XmlMapper XMLMAPPER = XmlMapper.builder()
        .addModule(new JavaTimeModule().addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer(FORMATTER)))
        .configure(WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build();

    @PostMapping("generate")
    public String generate(@RequestParam("file") MultipartFile file) {
        try {
            prepareDirectory(OUTPUT_FOLDER);
            File convertedFile = convertMultiPartToFile(file);
            JsonNode jsonNode = XMLMAPPER.readTree(convertedFile);
            String generatedCode = generateJavaCode(jsonNode, getFileNameWithoutExtension(convertedFile));
            return generatedCode;
        } catch (Exception e) {
            throw new RuntimeException("Error generate");
        }
    }

    private String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    private File convertMultiPartToFile(MultipartFile file) {
        File convertedFile = new File(INPUT_FOLDER + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile);
            InputStream inputStream = file.getInputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertedFile;
    }

    private void prepareDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                file.delete();
            }
        } else {
            directory.mkdirs();
        }
    }

    private String generateJavaCode(JsonNode jsonNode, String name) {
        StringBuilder codeBuilder = new StringBuilder();
        generateClass(jsonNode, name, codeBuilder);
        return codeBuilder.toString();
    }

    private void generateClass(JsonNode jsonNode, String nodeName, StringBuilder codeBuilder) {
        System.out.println("Generating class: " + nodeName);
        Set<Entry<String, JsonNode>> classesToBeGenerated = new HashSet<>();
        StringBuilder classCodeBuilder = new StringBuilder();
        classCodeBuilder.append("@Data\n");
        classCodeBuilder.append("public class ").append(capitalizeFirstLetter(nodeName)).append(" {\n\n");
        for (Entry<String, JsonNode> property : jsonNode.properties()) {
            Entry<String, JsonNode> listElement = getListElement(property.getValue());
            if (Objects.nonNull(listElement)) {
                generateListProperty(property.getKey(), listElement.getKey(), classCodeBuilder);
                classesToBeGenerated.add(listElement);
            } else {
                generateProperty(property.getValue(), property.getKey(), classCodeBuilder);
                if (property.getValue().getNodeType().equals(OBJECT)) {
                    classesToBeGenerated.add(property);
                }
            }
        }
        classCodeBuilder.append("}\n\n");
        saveToFile(classCodeBuilder.toString(), capitalizeFirstLetter(nodeName) + ".java");
        codeBuilder.append(classCodeBuilder);
        for (Entry<String, JsonNode> classEntry : classesToBeGenerated) {
            generateClass(classEntry.getValue(), classEntry.getKey(), codeBuilder);
        }
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private void saveToFile(String content, String fileName) {
        try {
            Files.writeString(Paths.get(OUTPUT_FOLDER + fileName), content);
        } catch (IOException e) {
            System.err.println("Error saveToFile: " + e.getMessage());
        }
    }

    private Entry<String, JsonNode> getListElement(JsonNode jsonNode) {
        if (jsonNode.getNodeType().equals(OBJECT)) {
            if (jsonNode.properties().size() == 1) {
                JsonNode child = jsonNode.properties().stream().map(Entry::getValue).findFirst().get();
                if (child.getNodeType().equals(ARRAY)) {
                    Entry<String, JsonNode> childEntry = jsonNode.properties().stream().findFirst().get();
                    return new AbstractMap.SimpleEntry<>(childEntry.getKey(), childEntry.getValue().get(0));
                }
            }
        }
        return null;
    }

    private void generateProperty(JsonNode jsonNode, String nodeName, StringBuilder codeBuilder) {
        List<String> attributes = List.of("Id", "Type", "Value", "Label");
        if (attributes.contains(nodeName)) {
            codeBuilder
                .append("\t").append("@JacksonXmlProperty(localName = \"").append(nodeName).append("\", isAttribute = true)\n")
                .append("\t").append("private ").append(getType(jsonNode, nodeName)).append(" ").append(normalizeName(nodeName)).append(";\n\n");
        } else if (Strings.isEmpty(nodeName)) {
            codeBuilder
                .append("\t").append("@JacksonXmlText\n")
                .append("\t").append("private ").append(getType(jsonNode, nodeName)).append(" ").append("text").append(";\n\n");
        } else {
            codeBuilder
                .append("\t").append("@JacksonXmlProperty(localName = \"").append(nodeName).append("\")\n")
                .append("\t").append("private ").append(getType(jsonNode, nodeName)).append(" ").append(normalizeName(nodeName)).append(";\n\n");
        }
    }

    private void generateListProperty(final String xmlProperty, final String classOfList, StringBuilder codeBuilder) {
        codeBuilder
            .append("\t").append("@JacksonXmlProperty(localName = \"").append(xmlProperty).append("\")\n")
            .append("\t").append("private List<").append(classOfList).append("> ").append(normalizeName(pluralize(classOfList))).append(";\n\n");
    }

    private String getType(JsonNode jsonNode, String nodeName) {
        if (jsonNode.getNodeType().equals(OBJECT)) {
            return nodeName;
        }
        if (Objects.isNull(jsonNode.textValue())) {
            return "String";
        }
        if (jsonNode.textValue().equals("True") || jsonNode.textValue().equals("False")) {
            return "boolean";
        }
        if (jsonNode.textValue().matches("\\d+")) {
            return "int";
        }
        if (jsonNode.textValue().matches("[-+]?\\d*\\.?\\d+([eE][-+]?\\d+)?")) {
            return "float";
        }
        if (jsonNode.textValue().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
            return "ZonedDateTime";
        }
        return "String";
    }

    private String pluralize(String input) {
        return English.plural(input);
    }

    private String normalizeName(String input) {
        if (input.startsWith("Is")) {
            input = input.substring(2);
        }
        if (input.endsWith("ID")) {
            input = input.substring(0, input.length() - 1) + "d";
        }
        return lowerCaseFirstChar(input);
    }

    private String lowerCaseFirstChar(String input) {
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }

}
