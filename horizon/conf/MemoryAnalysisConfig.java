package iped.engine.config;

import iped.parsers.misc.MemoryParser;
import iped.parsers.util.MemoryPlugin;
import iped.utils.UTF8Properties;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.util.*;

public class MemoryAnalysisConfig extends AbstractPropertiesConfigurable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String CONFIG_FILE = "conf/MemoryAnalysisConfig.txt"; //$NON-NLS-1$
    private Boolean wDumpFiles = true;
    private Boolean wEnvars = true;

    public static final Filter<Path> filter = new Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return entry.endsWith(CONFIG_FILE);
        }
    };

    @Override
    public Filter<Path> getResourceLookupFilter() {
        return filter;
    }

    @Override
    public void processProperties(UTF8Properties properties) {
        String value = properties.getProperty("EnableMemoryAnalysis"); //$NON-NLS-1$

        if (value != null && !value.trim().isEmpty()) {
            System.setProperty(MemoryParser.ENABLED_PROP, value.trim());
        } else {
            System.setProperty(MemoryParser.ENABLED_PROP, "false");
        }

        List plugins = Arrays.asList(properties.keySet().toArray());

        for(Object plugin : plugins) {
            String p = plugin.toString();

            if(!p.equals("EnableMemoryAnalysis")) {
                value = properties.getProperty(p);
                if(value != null && !value.trim().isEmpty()) {
                    if(value.trim().equalsIgnoreCase("true") && !MemoryPlugin.plugins.contains(p)) {
                        MemoryPlugin.plugins.add(p);
                    }
                }
            }
        }
    }

    public Boolean getwDumpFiles() {
        return wDumpFiles;
    }

    public Boolean getwEnvars() {
        return wEnvars;
    }

}
