import dpf.sp.gpinf.indexer.parsers.util.MemoryPluginBase;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetstatFilesExtractor extends MemoryPluginBase {

    @Override
    public void runPlugin() {
        Map<String, String> pidInputs = new HashMap<>();
        InputStream is = null;
        try {
            is = getV3PluginOutput("mac.netstat.Netstat", null);
            JOptionPane.showMessageDialog(null, is.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Pattern pattern = Pattern.compile("(0x[\\S]+)\\s+([\\S]+)\\s+([\\S\\d]+)\\s+([\\d]+)\\s+([\\S\\d]+)\\s+([\\d]+)\\s+([\\S]*)\\s*([\\S]+)/([\\d]+)");

        while(true) {
            try {
                if (!reader.ready()) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String l = null;
            try {
                l = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Matcher matcher = pattern.matcher(l);
            if(matcher.find()) {
                String offset = matcher.group(1);
                String proto = matcher.group(2);
                String localIP = matcher.group(3);
                String localPort = matcher.group(4);
                String remoteIP = matcher.group(5);
                String remotePort = matcher.group(6);
                String state = matcher.group(7);
                String processName = matcher.group(8);
                String pid = matcher.group(9);

                String inputString = offset + " " + proto + " " + localIP + " " + localPort + " " + remoteIP + " " + remotePort + " " + state + " " + processName + "/" + pid + "\n";

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
            String header = "Offset\tProto\tLocal IP\tLocal Port\tRemote IP\tRemote Port\tState\tProcess\n";
            String pid = opid.toString();
            String name = pid + "_Netstat.txt";
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
