package net.lawaxi.sbwa.model;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.InputStream;

public class OwnedGift implements Gift {

    public final File picFile;
    public final String title;
    public int amount = 0;

    public OwnedGift(File picFile, String title) {
        this.picFile = picFile;
        this.title = title;
    }

    @Override
    public InputStream getPic() {
        if (this.picFile.exists()) {
            return FileUtil.getInputStream(this.picFile);
        }
        return null;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public OwnedGift setAmount(int amount) {
        this.amount = amount;
        return this;
    }
}
