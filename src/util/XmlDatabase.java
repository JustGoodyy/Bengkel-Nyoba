package util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class XmlDatabase<T> {

    private final XStream xstream;
    private final String filePath;

    public XmlDatabase(String filePath, Class<T> clazz) {
        this.filePath = filePath;
        this.xstream = new XStream(new StaxDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.alias(clazz.getSimpleName().toLowerCase(), clazz);
        xstream.alias("list", ArrayList.class);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<T> loadAll() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            Object result = xstream.fromXML(reader);
            return result != null ? (ArrayList<T>) result : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("[XmlDatabase] Failed to read " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveAll(ArrayList<T> data) {
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                xstream.toXML(data, writer);
            }
        } catch (Exception e) {
            System.err.println("[XmlDatabase] Failed to write " + filePath + ": " + e.getMessage());
        }
    }
}
