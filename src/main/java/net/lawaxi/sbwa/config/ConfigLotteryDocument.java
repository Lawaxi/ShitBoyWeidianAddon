package net.lawaxi.sbwa.config;

import cn.hutool.json.JSONObject;
import net.lawaxi.sbwa.model.Lottery2;
import net.lawaxi.sbwa.util.Common;

import java.io.File;

public class ConfigLotteryDocument extends SimpleJSONConfig {

    public final String id;
    private Lottery2 lottery;
    private ConfigLotteryData data;

    public ConfigLotteryDocument(String id, File file) {
        super(file);
        this.id = id;
        readDocument();
        initData();
    }

    public ConfigLotteryDocument(String id, File file, Lottery2 lottery) {
        super(file);
        this.id = id;
        this.lottery = lottery;
        initData();
    }

    @Override
    public JSONObject init() {
        return null;
    }

    public void initData() {
        File dataFolder = new File(Common.I.dataFolder, id);
        if (!dataFolder.exists())
            dataFolder.mkdir();
        this.data = new ConfigLotteryData(new File(dataFolder, "data.json"));
    }

    public void readDocument() {
        this.lottery = Lottery2.construct(id, this, super.init());
    }

    public Lottery2 writeDocument(JSONObject object) {
        Lottery2 n = Lottery2.construct(id, this, object);
        if (n != null) {
            write(object.toStringPretty());
            this.lottery = n;
        }
        return n;
    }

    public Lottery2 getLottery() {
        return lottery;
    }

    public ConfigLotteryData getData() {
        return data;
    }
}
