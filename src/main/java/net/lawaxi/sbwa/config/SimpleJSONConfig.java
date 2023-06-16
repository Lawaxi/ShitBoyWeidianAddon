package net.lawaxi.sbwa.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.io.File;
import java.nio.charset.Charset;

public class SimpleJSONConfig {
    protected final File configFile;

    public SimpleJSONConfig(File file) {
        this.configFile = file;

        if (!file.exists()) {
            FileUtil.touch(file);
            construct(file);
        }
        init();
    }

    public void construct(File file) {
        FileUtil.writeString("{}", file, Charset.defaultCharset());
    }

    public JSONObject init() {
        return JSONUtil.readJSONObject(this.configFile, Charset.defaultCharset());
    }

    public void write(String document) {
        FileUtil.writeString(document, this.configFile, Charset.defaultCharset());
    }
}
