package net.lawaxi.sbwa.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.sbwa.model.Lottery2;
import net.lawaxi.sbwa.util.Common;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ConfigConfig extends SimpleSettingConfig {
    public static ConfigConfig INSTANCE;

    public ArrayList<ConfigLotteryDocument> lotteryDocuments = new ArrayList<>();

    public ConfigConfig(File file) {
        super(file);
        INSTANCE = this;
    }

    @Override
    public void construct(Setting setting) {
        setting.setByGroup("documents", "lottery", "[]");
    }

    @Override
    public void init() {
        File documentFolder = Common.I.documentFolder;
        if (!documentFolder.exists())
            documentFolder.mkdir();

        for (Object o : JSONUtil.parseArray(setting.getStr("documents", "lottery", "[]")).toArray()) {
            String id = (String) o;
            File d = new File(documentFolder, id + ".json");
            ConfigLotteryDocument document = new ConfigLotteryDocument(id, d);
            if (document.getLottery() != null)
                lotteryDocuments.add(document);
        }
    }

    public String addLotteryByJSON(JSONObject json) {
        String id = null;
        if (json.containsKey("id")) {
            id = json.getStr("id");
            json.remove("id");
        }
        id = getNewRandomId();
        File d = new File(Common.I.documentFolder, id + ".json");

        Lottery2 l = Lottery2.construct(id, null, json);
        if (l == null) {
            return "null";
        } else {
            lotteryDocuments.add(new ConfigLotteryDocument(id, d, l));

            FileUtil.writeString(json.toStringPretty(), d, Charset.defaultCharset());

            JSONArray a = JSONUtil.parseArray(setting.getStr("documents", "lottery", "[]"));
            a.add(id);
            setting.setByGroup("documents", "lottery", a.toString());

            return id;
        }
    }

    public Lottery2[] getLotterysByGroupIdAndItemId(long groupId, long item_id) {
        Lottery2[] a = {};
        for (ConfigLotteryDocument document : lotteryDocuments) {
            Lottery2 l = document.getLottery();
            if (l != null) {
                if (ArrayUtil.contains(l.groupIds, groupId) && ArrayUtil.contains(l.item_ids, item_id)) {
                    a[a.length] = document.getLottery();
                }
            }
        }
        return a;
    }

    public Lottery2[] getLotteryByGroupId(long groupId) {
        Lottery2[] a = {};
        for (ConfigLotteryDocument document : lotteryDocuments) {
            Lottery2 l = document.getLottery();
            if (l != null) {
                if (ArrayUtil.contains(l.groupIds, groupId)) {
                    a[a.length] = document.getLottery();
                }
            }
        }
        return a;
    }

    public Lottery2[] getAllNonNullLotterys() {
        Lottery2[] a = {};
        for (ConfigLotteryDocument document : lotteryDocuments) {
            if (document.getLottery() != null)
                a[a.length] = document.getLottery();
        }
        return a;
    }

    public String getNewRandomId() {
        String a = RandomUtil.randomString(5);
        while (getLotteryById(a) != null) {
            a = RandomUtil.randomString(5);
        }
        return a;
    }

    public Lottery2 getLotteryById(String id) {
        for (ConfigLotteryDocument document : lotteryDocuments) {
            Lottery2 l = document.getLottery();
            if (l != null) {
                if (l.lottery_id.equals(id))
                    return l;
            }
        }
        return null;
    }
}
