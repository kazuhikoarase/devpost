package devpost;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * XMLResourceBundle
 * @author Kazuhiko Arase
 */
public class XMLResourceBundle extends ResourceBundle {

    private final Properties properties;

    public XMLResourceBundle(final InputStream stream) throws IOException {
        properties = new Properties();
        properties.loadFromXML(stream);
    }
 
    @Override
    public Object handleGetObject(final String key) {
        return properties.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<String> getKeys() {
        return (Enumeration<String>)properties.propertyNames();
    }

    public static class Control extends ResourceBundle.Control {

        public static final String FORMAT_XML = "xml";
        public static final List<String> FORMATS = Arrays.asList(FORMAT_XML);

        @Override
        public List<String> getFormats(final String baseName) {
            return FORMATS;
        }

        @Override
        public ResourceBundle newBundle(
            final String baseName,
            final Locale locale,
            final String format,
            final ClassLoader loader,
            final boolean reload
        ) throws 
                IllegalAccessException,
                InstantiationException,
                IOException {
            if (!format.equals(FORMAT_XML) ) {
                throw new IllegalArgumentException(
                        "unsupported format:" + format);
            }
            final InputStream in  = loader.getResource(
                    toResourceName(toBundleName(baseName, locale),
                            format) ).openStream();
            try {
                return new XMLResourceBundle(in);
            } finally {
                in.close();
            }
        }
    }
}
