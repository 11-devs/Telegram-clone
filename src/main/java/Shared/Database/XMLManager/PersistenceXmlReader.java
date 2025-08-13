package Shared.Database.XMLManager;

import Shared.Database.Database;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URI;

public class PersistenceXmlReader {
    public static String getDatabaseNameFromPersistenceXml(String persistenceUnitName , String fileName) throws Exception {
        InputStream is = Database.class.getClassLoader().getResourceAsStream("META-INF/" + fileName);
        if (is == null) throw new RuntimeException("persistence.xml not found");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);

        NodeList units = doc.getElementsByTagName("persistence-unit");
        for (int i = 0; i < units.getLength(); i++) {
            Element unit = (Element) units.item(i);
            if (persistenceUnitName.equals(unit.getAttribute("name"))) {
                NodeList props = unit.getElementsByTagName("property");
                for (int j = 0; j < props.getLength(); j++) {
                    Element prop = (Element) props.item(j);
                    if ("jakarta.persistence.jdbc.url".equals(prop.getAttribute("name")) ||
                            "javax.persistence.jdbc.url".equals(prop.getAttribute("name"))) {
                        String url = prop.getAttribute("value");
                        return extractDatabaseNameFromJdbcUrl(url);
                    }
                }
            }
        }
        throw new RuntimeException("Database URL not found in persistence.xml");
    }

    private static String extractDatabaseNameFromJdbcUrl(String jdbcUrl) throws Exception {
        URI uri = new URI(jdbcUrl.substring(5));
        String path = uri.getPath();
        if (path != null && path.length() > 1) {
            return path.substring(1);
        }
        throw new RuntimeException("Cannot parse DB name from URL: " + jdbcUrl);
    }
}
