import iped.parsers.util.MemoryPluginBase;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MLsmodFileExtractor extends MemoryPluginBase {

    @Override
    public void runPlugin() {
        String inputString = "Offset\tName\tSize\n";
        InputStream is = null;
        try {
            is = getV3PluginOutput("mac.lsmod.Lsmod", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Pattern pattern = Pattern.compile("(0x\\S+)\\s+([\\S\\.]+)\\s+(\\d+)");

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
                String offset = matcher.group(1);
                String moduleName = matcher.group(2);
                String size = matcher.group(3);

                inputString += offset + " " + moduleName + " " + size + "\n";
            }
        }

        String name = "Kernel_Modules_Lsmod.txt";
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
