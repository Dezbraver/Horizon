import dpf.sp.gpinf.indexer.parsers.util.MemoryPluginBase;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BashFileExtractor extends MemoryPluginBase {

    @Override
    public void runPlugin() {
        String inputString = "PID\tProcess\tCommand Time\tCommand\n";
        InputStream is = null;
        try {
            is = getV3PluginOutput("linux.bash.Bash", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Pattern pattern = Pattern.compile("(\\d[\\S\\s]+)");

        try {
            reader.readLine();
            reader.readLine();
            reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
                String result = matcher.group(1);

                inputString += result + "\n";
            }
        }

        String name = "Commands_Bash.txt";
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
        return OSystems.LINUX;
    }
}
