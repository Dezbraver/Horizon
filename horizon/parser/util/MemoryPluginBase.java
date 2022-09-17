package iped.parsers.util;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

public abstract class MemoryPluginBase {

    protected File getTemporaryDirectory() { return MemoryPlugin.getTmpDir(); }

    protected Map<String, ArrayList<String>> getProcesses() { return MemoryPlugin.getProcesses(); }

    protected String getOS() { return MemoryPlugin.getOS(); }

    protected InputStream getV3PluginOutput(String pluginName, String pluginArguments)
        throws IOException { return MemoryPlugin.getV3PluginOutput(pluginName, pluginArguments); }

    protected void addFolder(String folderName, String folderID, String parentFolderName)
        throws IOException, SAXException { MemoryPlugin.addFolder(folderName, folderID, parentFolderName); }

    protected void addFile(String fileName, String fileVirtualID, String parentFolderVirtualID, InputStream fileIS)
        throws IOException, SAXException { MemoryPlugin.addFile(fileName, fileVirtualID, parentFolderVirtualID, fileIS); }

    public abstract void runPlugin() throws IOException;

    protected enum OSystems { LINUX, MACOS, WINDOWS }

    public abstract OSystems runOS();
}

