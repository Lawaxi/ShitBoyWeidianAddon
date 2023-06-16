package net.lawaxi.sbwa.config;

import cn.hutool.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigLotteryData extends SimpleJSONConfig {
    private JSONObject data;

    public ConfigLotteryData(File file) {
        super(file);
    }

    @Override
    public JSONObject init() {
        JSONObject l = super.init();
        this.data = l;
        return l;
    }

    public String[] getGiftIds(long buyerID) {
        if (this.data.containsKey("" + buyerID)) {
            return this.data.getBeanList("" + buyerID, String.class).toArray(new String[0]);
        }
        return new String[0];
    }

    public void addGiftId(long buyerID, String giftId) {
        List<String> a;
        if (this.data.containsKey("" + buyerID)) {
            a = this.data.getBeanList("" + buyerID, String.class);
        } else {
            a = new ArrayList<>();
        }
        a.add(giftId);
        this.data.set("" + buyerID, a);
        write(this.data.toStringPretty());
    }
}
