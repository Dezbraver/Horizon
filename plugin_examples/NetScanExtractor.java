import iped.parsers.util.MemoryPluginBase;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetScanExtractor extends MemoryPluginBase {

    @Override
    public void runPlugin() {
        Map<String, String> pidInputs = new HashMap<>();
        InputStream is = null;
        try {
            is = getV3PluginOutput("windows.netscan.NetScan", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Pattern pattern = Pattern.compile("([\\S]+)\\s+([\\S]+)\\s+([\\S]+)\\s+([\\S]+)\\s+([\\S]+)\\s+([\\S]+)\\s+([\\S]*)\\s+([\\S]+)\\s+([\\S]+)\\s+([\\S]+)\\s+([\\S]+)");

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
                String pid = matcher.group(8);
                String processName = matcher.group(9);
                String protocol = matcher.group(2);
                String localAddr = matcher.group(3);
                String localPort = matcher.group(4);
                String remoteAddr = matcher.group(5);
                String remotePort = matcher.group(6);
                String state = (matcher.group(7) == ""? "\t\t": matcher.group(7));
                String date = matcher.group(10);
                String time = matcher.group(11);

                String inputString = String.format("%-14s\t%-8s\t%-39s\t%-10s\t%-39s\t%-11s\t%-11s\t%-10s\t%-15s\n",
                        processName, protocol, localAddr, localPort, remoteAddr, remotePort, state, date, time);

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

        String header = String.format("%-14s\t%-8s\t%-39s\t%-10s\t%-39s\t%-11s\t%-11s\t%-10s\t%-15s\n", "PROCESS NAME",
                "PROTOCOL", "LOCAL ADDRESS", "LOCAL PORT", "REMOTE ADDRESS", "REMOTE PORT", "STATE","DATE", "TIME");
        Set pidset = pidInputs.keySet();

        // Escreve um arquivo de variáveis de ambiente para cada pid, o inserindo no IPED
        for(Object opid: pidset) {
            String pid = opid.toString();
            String name = pid + "_NetScan.txt";
            try {
                String inputS = header + pidInputs.get(pid);
                addFile(name, name, pid, new ByteArrayInputStream(inputS.getBytes()));
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