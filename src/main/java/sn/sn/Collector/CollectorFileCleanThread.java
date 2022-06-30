package sn.sn.Collector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static sn.sn.Sn.*;

public class CollectorFileCleanThread extends Thread {

    public static File rubbishes_folder = new File(data_folder,"rubbishes");

    @Override
    public void run() {
        List<File> remove = new ArrayList<>();
        try {
            for (File file : Objects.requireNonNull(rubbishes_folder.listFiles())) {
                if (!bins.contains(file.getName())) {
                    remove.add(file);
                }
            }
            for (File file : remove) {
                while(!file.delete()){
                    sendDebug("尝试删除文件"+file.getName());
                }
            }
        } catch (Exception e) {
            sendError(e.getLocalizedMessage());
        }
    }
}
