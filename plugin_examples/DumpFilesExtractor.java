import dpf.sp.gpinf.indexer.parsers.util.MemoryPluginBase;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class DumpFilesExtractor extends MemoryPluginBase {
    @Override
    public void runPlugin() {
        try {
            getV3PluginOutput("windows.pslist.PsList", "--dump");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File file = new File(getTemporaryDirectory().getAbsolutePath());
        File files[]=  file.listFiles();

        for(File f:files){
            Iterator pids = getProcesses().keySet().iterator();
            while(pids.hasNext()) {
                String pid = pids.next().toString();
                String pName = (getProcesses().get(pid)).get(1);
                if (f.getName().contains("." + pid + ".")) {
                    String fileName = "dump_" + pid + "_" + pName;
                    try {
                        addFile(fileName, fileName, pid, new FileInputStream(f));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (SAXException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public OSystems runOS() {
        return OSystems.WINDOWS;
    }
}
