package net.lawaxi.sbwa.util;

import java.io.File;

public class Common {
    public static Common I;
    public final File parentFolder;
    public final File documentFolder;
    public final File picFolder;
    public final File dataFolder;
    public final File historyFolder;

    public Common(File parentFolder) {
        this.parentFolder = parentFolder;
        this.documentFolder = new File(parentFolder, "documents");
        this.dataFolder = new File(parentFolder, "data");
        this.picFolder = this.dataFolder;
        this.historyFolder = new File(parentFolder, "histories");
        I = this;
    }

}
