import iped.parsers.util.MemoryPluginBase;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MountFileExtractor extends MemoryPluginBase {

    @Override
    public void runPlugin() {
        String inputString = "Device\tMount Point\tType\n";
        InputStream is = null;
        try {
            is = getV3PluginOutput("mac.mount.Mount", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Pattern pattern = Pattern.compile("([\\S+\\s]+)\\s+([\\S+\\s]+)\\s+(\\S+)");

        try {
            reader.readLine();
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
                String device = matcher.group(1);
                String mountPoint = matcher.group(2);
                String type = matcher.group(3);

                inputString += device + " " + mountPoint + " " + type + "\n";
            }
        }

        String name = "Mount_Points_Mount.txt";
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
