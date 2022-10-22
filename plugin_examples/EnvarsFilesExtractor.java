import iped.parsers.util.MemoryPluginBase;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvarsFilesExtractor extends MemoryPluginBase {

    @Override
    public void runPlugin() {
        Map<String, String> pidInputs = new HashMap<>();
        InputStream is = null;
        try {
            is = getV3PluginOutput("windows.envars.Envars", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Pattern pattern = Pattern.compile("([0-9]+)\\s+([\\S]+)\\s+([\\S]+)\\s+([\\S]+)\\s+([\\S\\s]+)");

        while(true) {
            String l = null;
            try {
                l = reader.readLine();
                if (l == null) break;
                l = l.replaceAll("%", "%%");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Matcher matcher = pattern.matcher(l);
            if(matcher.find()) {
                String pid = matcher.group(1);
                String variableName = matcher.group(4);
                String value = matcher.group(5);

                String inputString = variableName + " = " + value + "\n";

                if(pidInputs.get(pid) != null) {
                    // Escreve para um dado pid, uma nova linha com nome e valor de uma variável de ambiente abaixo do
                    // valor atual
                    pidInputs.replace(pid, pidInputs.get(pid) + inputString);
                }
                else {
                    // Escreve para um dado pid, a primeira linha com nome e valor de uma variável de ambiente
                    pidInputs.put(pid, inputString);
                }
            }
        }

        Set pidset = pidInputs.keySet();

        // Escreve um arquivo de variáveis de ambiente para cada pid, o inserindo no IPED
        for(Object opid: pidset) {
            String pid = opid.toString();
            String name = pid + "_Environment_Variables.txt";
            try {
                addFile(name, name, pid, new ByteArrayInputStream(pidInputs.get(pid).getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public OSystems runOS() {
        return OSystems.WINDOWS;
    }
}
