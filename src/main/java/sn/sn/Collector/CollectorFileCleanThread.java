package sn.sn.Collector;

import sn.sn.Basic.Other;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static sn.sn.Sn.bins;
import static sn.sn.Sn.data_folder;

public class CollectorFileCleanThread extends Thread {

    public static final File rubbishes_folder = new File(data_folder, "rubbishes");

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
                do{
                    Other.sendDebug("尝试删除文件"+file.getName());
                } while(!file.delete());
            }
        } catch (Exception e) {
            Other.sendError(e.getLocalizedMessage());
        }
    }
}
