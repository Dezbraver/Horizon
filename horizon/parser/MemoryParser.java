package iped.parsers.misc;

import java.io.*;
import java.util.*;

import iped.parsers.util.MemoryPlugin;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.ParsingEmbeddedDocumentExtractor;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.swing.*;

/**
 * Parser para arquivos de Memória.
 *
 * @author João Pedro Barbosa Medeiros (Dezbraver)
 *
 */

public class MemoryParser extends AbstractParser {

    private static Logger LOGGER = LoggerFactory.getLogger(MemoryParser.class);

    public static final String ENABLED_PROP = "memoryparser.enabled"; //$NON-NLS-1$

    private boolean ENABLED = Boolean.valueOf(System.getProperty(ENABLED_PROP, "true")); //$NON-NLS-1$

    public static final String MEMORY_MIME_TYPE = "application/x-memory";

    private static final Set<MediaType> SUPPORTED_TYPES = Collections
        .singleton(MediaType.parse(MEMORY_MIME_TYPE));

    public boolean isEnabled() {
        return this.ENABLED;
    }

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context){
        return SUPPORTED_TYPES;
    }

    @Override
    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
        throws IOException, SAXException, TikaException {

        if (ENABLED) {
            XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
            xhtml.startDocument();

            TemporaryResources tmp = new TemporaryResources();

            File rootFile = TikaInputStream.get(stream, tmp).getFile();

            try {
                MemoryPlugin.rootFilePath = rootFile.getAbsolutePath();
                MemoryPlugin.rootFileName = rootFile.getName();
                MemoryPlugin.executor = new DefaultExecutor();
                MemoryPlugin.handler = handler;
                MemoryPlugin.xhtml = xhtml;
                MemoryPlugin.extractor = context.get(EmbeddedDocumentExtractor.class,
                    new ParsingEmbeddedDocumentExtractor(context));

                MemoryPlugin.startPlugins();
            } finally {
                tmp.close();
                xhtml.endDocument();
            }
        }
    }
}
