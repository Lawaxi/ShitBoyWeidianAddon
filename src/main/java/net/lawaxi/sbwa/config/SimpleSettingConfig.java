package net.lawaxi.sbwa.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.Setting;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class SimpleSettingConfig {
    protected final File configFile;
    protected final Setting setting;

    public SimpleSettingConfig(File file) {
        this.configFile = file;

        if (!file.exists()) {
            FileUtil.touch(file);
            Setting setting = new Setting(file, StandardCharsets.UTF_8, false);
            construct(setting);
            setting.store();
        }

        this.setting = new Setting(file, StandardCharsets.UTF_8, false);
        init();
    }

    public void construct(Setting setting) {

    }

    public void init() {

    }

    public void save() {
        setting.store();
    }
}
