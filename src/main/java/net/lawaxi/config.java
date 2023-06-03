package net.lawaxi;

import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.Setting;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class config {
    private final Setting setting;

    public config(File file) {
        if (!file.exists()) {
            FileUtil.touch(file);
            Setting setting = new Setting(file, StandardCharsets.UTF_8, false);
            setting.store();
        }

        this.setting = new Setting(file, StandardCharsets.UTF_8, false);
        init();
    }

    private void init() {

    }
}
