import iped.parsers.util.MemoryPluginBase;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeventsFilesExtractor extends MemoryPluginBase {

    @Override
    public void runPlugin() {
        Map<String, String> pidInputs = new HashMap<>();
        InputStream is = null;
        try {
            is = getV3PluginOutput("mac.kevents.Kevents", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Pattern pattern = Pattern.compile("(\\d+)\\s+([\\S]+)\\s+([\\d]+)\\s+([\\S]+)\\s+([\\S]*)");

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
                String processName = matcher.group(2);
                String ident = matcher.group(3);
                String filter = matcher.group(4);
                String context = matcher.group(5);

                String inputString = processName + " " + ident + " " + filter + " " + context + "\n";

                if(pidInputs.get(pid) != null) {
                    // Escreve para um dado pid, uma nova linha com nome e valor de um processo abaixo do
                    // valor atual
                    pidInputs.replace(pid, pidInputs.get(pid) + inputString);
                }
                else {
                    // Escreve para um dado pid, a primeira linha com nome e valor de um processo
                    pidInputs.put(pid, inputString);
                }
            }
        }

        Set pidset = pidInputs.keySet();

        // Escreve um arquivo de eventos de kernel para cada pid, o inserindo no IPED
        for(Object opid: pidset) {
            String header = "Process\tIndent\tFilter\tContext\n";
            String pid = opid.toString();
            String name = pid + "_Kevents.txt";
            String istream = header + pidInputs.get(pid);
            try {
                addFile(name, name, pid, new ByteArrayInputStream(istream.getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public OSystems runOS() {
        return OSystems.MACOS;
    }
}
