import iped.parsers.util.MemoryPluginBase;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymlinkScanFileExtractor extends MemoryPluginBase {

    @Override
    public void runPlugin() {
        String inputString = "Offset\tCreate Time\tFrom Name\tTo Name\n";
        InputStream is = null;
        try {
            is = getV3PluginOutput("windows.symlinkscan.SymlinkScan", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Pattern pattern = Pattern.compile("(0x[\\S\\s]+)");

        try {
            reader.readLine();
            reader.readLine();
            reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
                String result = matcher.group(1);

                inputString += result + "\n";
            }
        }

        String name = "SymlinkScan.txt";
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
        return OSystems.WINDOWS;
    }
}
