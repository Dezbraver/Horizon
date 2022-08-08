package dpf.sp.gpinf.indexer.parsers.util;

import com.zaxxer.sparsebits.SparseBitSet;
import iped3.util.ExtraProperties;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.logging.log4j.Level;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.XHTMLContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemoryPlugin {
    private static Logger LOGGER = LoggerFactory.getLogger(MemoryPlugin.class);

    public static String rootFilePath;
    public static String rootFileName;
    public static DefaultExecutor executor;
    public static ContentHandler handler;
    public static XHTMLContentHandler xhtml;
    public static EmbeddedDocumentExtractor extractor;

    private static File tmpDir;
    private static String os;
    private static Map<String, ArrayList<String>> processes = new HashMap();

    public static List<String> plugins = new ArrayList<>();

    // ====================================================================
    // Função Para Classe MemoryParser
    // ====================================================================

    // Executa métodos e plugins sequencialmente
    public static void startPlugins()
        throws IOException, SAXException {

        generateTmpDir();

        VerifyOS();
        createFolder("Memory", "Memory", rootFileName);
        GenProcessesTree();

        for (String plugin : plugins) {
            try {
                Class<MemoryPluginBase> c = (Class<MemoryPluginBase>) Class.forName(plugin);
                MemoryPluginBase obj = c.newInstance();
                if (obj.runOS().toString().equals(os)) {
                    obj.runPlugin();
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error("Class " + plugin + " was not found in .../optional_jars/horizon-memory-plugin.jar");
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } finally {
                continue;
            }
        }
    }

    // ====================================================================
    // Funções Privadas do MemoryPlugin
    // ====================================================================

    // Gera diretório onde arquivos temporários serão armazenados
    private static void generateTmpDir()
        throws IOException {
        tmpDir = Files.createTempDirectory("tmp").toFile();
    }

    // Criação de arquivo no IPED
    private static void parseSubitem(String fileName, String iVID, String pVID, InputStream fileIS)
        throws SAXException, IOException {

        final Metadata entrydata = new Metadata();

        entrydata.set(Metadata.RESOURCE_NAME_KEY, fileName);
        entrydata.set(ExtraProperties.ITEM_VIRTUAL_ID, iVID);
        entrydata.set(ExtraProperties.PARENT_VIRTUAL_ID, pVID);

        if (extractor.shouldParseEmbedded(entrydata))
            extractor.parseEmbedded(fileIS, handler, entrydata, true);
    }

    // Execução de plugin no Volatility 3
    private static InputStream execV3Plugin(String pluginName, String pluginArguments)
        throws IOException {

        String commandFix = "python -c " +
            "\"from sys import argv;" +
            "argv.remove('-c');" +
            "from volatility3.cli import main;" +
            "main();\" " +
            "vol.py -q -f \"" + rootFilePath + "\" ";
        String cmd = commandFix + (pluginArguments == null ? pluginName : pluginName + " " + pluginArguments);
        CommandLine cmdLine = CommandLine.parse(cmd);

        org.apache.commons.io.output.ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        executor.setStreamHandler(streamHandler);
        executor.setWorkingDirectory(tmpDir);
        executor.setExitValues(null);

        int exitCode = executor.execute(cmdLine);
        if (exitCode == 0 || exitCode == 1) {
            return new ByteArrayInputStream(outputStream.toByteArray());
        } else {
            return null;
        }
    }

    // Verificação do sistema operacional referente a snapshot de memória sendo analisada
    private static void VerifyOS()
        throws IOException {

        InputStream subIS = execV3Plugin("banners.Banners", null);
        BufferedReader reader = new BufferedReader(new InputStreamReader(subIS));

        while (reader.ready()) {
            String l = reader.readLine();
            if ((l.toUpperCase()).contains("LINUX")) {
                os = "LINUX";
            } else if ((l.toUpperCase()).contains("DARWIN") | (l.toUpperCase()).contains("XNU")) {
                os = "MACOS";
            } else {
                os = "WINDOWS";
            }
        }
    }

    // Criação de pasta no IPED
    private static void createFolder(String folderName, String iVID, String pVID)
        throws SAXException, IOException {

        Metadata entrydata = new Metadata();

        entrydata.set(Metadata.RESOURCE_NAME_KEY, folderName);
        entrydata.set(ExtraProperties.ITEM_VIRTUAL_ID, iVID);
        entrydata.set(ExtraProperties.PARENT_VIRTUAL_ID, pVID);
        entrydata.set(ExtraProperties.EMBEDDED_FOLDER, "true"); //$NON-NLS-1$

        if (extractor.shouldParseEmbedded(entrydata))
            extractor.parseEmbedded(new ByteArrayInputStream(new byte[0]), handler, entrydata, true);
    }

    private static void GenProcessesTree()
        throws IOException, SAXException {

        InputStream subIS;
        Pattern pattern;

        if (os == "LINUX") {
            subIS = execV3Plugin("linux.pstree.PsTree", null);
            pattern = Pattern.compile("(\\*+\\s+)?([0-9]+)\\s+([0-9]+)(.+)");
        } else if (os == "MACOS") {
            subIS = execV3Plugin("mac.pstree.PsTree", null);
            pattern = Pattern.compile("(\\*+\\s+)?([0-9]+)\\s+([0-9]+)(.+)");
        } else {
            subIS = execV3Plugin("windows.pstree.PsTree", null);
            pattern = Pattern.compile("(\\*+\\s+)?([0-9]+)\\s+([0-9]+)\\s+([\\S]+)");
        }

        if (subIS != null) {
            String parent = "Memory";

            createFolder("General", "General", parent);
            createFolder("Processes", "Processes", parent);

            BufferedReader reader = new BufferedReader(new InputStreamReader(subIS));

            if (os == "MACOS") {
                processes.put("1", new ArrayList<>(Arrays.asList("Processes", "launchd")));
                createFolder("launchd", "1", "Processes");
            }

            while (reader.ready()) {
                String l = reader.readLine();
                Matcher matcher = pattern.matcher(l);
                if (matcher.find()) {
                    String pid = matcher.group(2);
                    String ppid = matcher.group(3);
                    String pName = (matcher.group(4)).replace("/", "{bar}");

                    processes.put(pid, new ArrayList<>(Arrays.asList(ppid, pName)));
                    ArrayList<String> recovered = processes.get(ppid);
                    parent = (recovered != null ? ppid : "Processes");

                    createFolder(pName, pid, parent);
                }
            }
        }
    }

    // ====================================================================
    // Funções Para Classe dpf.sp.gpinf.indexer.parsers.util.MemoryPluginBase
    // ====================================================================
    public static File getTmpDir() {
        return tmpDir;
    }

    public static Map<String, ArrayList<String>> getProcesses() {
        return processes;
    }

    public static InputStream getV3PluginOutput(String pluginName, String pluginArguments)
        throws IOException {
        return execV3Plugin(pluginName, pluginArguments);
    }

    public static String getOS() {
        return os;
    }

    public static void addFolder(String folderName, String folderID, String parentFolderName)
        throws SAXException, IOException {
        createFolder(folderName, folderID, parentFolderName);
    }

    public static void addFile(String fileName, String fileVirtualID, String parentFolderVirtualID, InputStream fileIS)
        throws IOException, SAXException {
        parseSubitem(fileName, fileVirtualID, parentFolderVirtualID, fileIS);
    }
}
