import iped.parsers.util.MemoryPluginBase;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IfconfigFileExtractor extends MemoryPluginBase {

    @Override
    public void runPlugin() {
        String inputString = "Interface\tIP Address\tMac Address\tPromiscuous\n";
        InputStream is = null;
        try {
            is = getV3PluginOutput("mac.ifconfig.Ifconfig", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Pattern pattern = Pattern.compile("(\\S+)\\s+([\\S\\.\\:]*)\\s+([\\S\\.\\:]*)\\s+(\\S+)");

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
                String iface = matcher.group(1);
                if (iface.equalsIgnoreCase("volatility") || iface.equalsIgnoreCase("interface")) continue;
                String IPAddress = matcher.group(2) == null? "": matcher.group(2);
                String MacAddress = matcher.group(3) == null? "": matcher.group(3);
                String Promiscuous = matcher.group(4);

                inputString += iface + " " + IPAddress + " " + MacAddress + " " + Promiscuous + "\n";
            }
        }

        String name = "Interface_Configuration_Ifconfig.txt";
        try {
            addFile(name, name, "General", new ByteArrayInputStream(inputString.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OSystems runOS() {
        return OSystems.MACOS;
    }
}
