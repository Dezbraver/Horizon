import iped.parsers.util.MemoryPluginBase;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCheckSyscallFileExtractor extends MemoryPluginBase {

    @Override
    public void runPlugin() {
        String inputString = "Table Address\tTable Name\tIndex\tHandler Address\tHandler Module\tHandler Symbol\n";
        InputStream is = null;
        try {
            is = getV3PluginOutput("mac.check_syscall.Check_syscall", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Pattern pattern = Pattern.compile("(0x\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(0x\\S+)\\s+([\\S_]+)\\s+([\\S_]+)");

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
                String tableAddress = matcher.group(1);
                String tableName = matcher.group(2);
                String index = matcher.group(3);
                String handlerAddress = matcher.group(4);
                String handlerModule = matcher.group(5);
                String handlerSymbol = matcher.group(6);

                inputString += tableAddress + " " + tableName + " " + index + " " + handlerAddress + " " + handlerModule + " " + handlerSymbol + "\n";
            }
        }

        String name = "System_Calls_Check_syscall.txt";
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
