package net.lawaxi.sbwa.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.sbwa.handler.WeidianHandler;
import net.lawaxi.sbwa.model.Lottery2;
import net.lawaxi.sbwa.util.Common;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigConfig extends SimpleSettingConfig {
    public static ConfigConfig INSTANCE;
    //抽卡数据总表
    public ArrayList<ConfigLotteryDocument> lotteryDocuments;
    //pk数据总表
    public HashMap<String, JSONObject> pk;

    //-----------------//
    public JSONObject binding;
    public HashMap<String, Long> buyerIdBinded;

    public ConfigConfig(File file) {
        super(file);
        INSTANCE = this;
    }

    @Override
    public void construct(Setting setting) {
        setting.setByGroup("documents", "lottery", "[]");
        setting.setByGroup("binding", "lottery", "{}");
    }

    @Override
    public void init() {
        File documentFolder = Common.I.documentFolder;

        lotteryDocuments = new ArrayList<>();
        buyerIdBinded = new HashMap<>();
        pk = new HashMap<>();

        //抽卡
        for (Object o : JSONUtil.parseArray(setting.getStr("documents", "lottery", "[]")).toArray()) {
            String id = (String) o;
            File d = new File(documentFolder, id + ".json");
            ConfigLotteryDocument document = new ConfigLotteryDocument(id, d);
            if (document.getLottery() != null)
                lotteryDocuments.add(document);
        }

        this.binding = JSONUtil.parseObj(setting.getStr("binding", "lottery", "{}"));
        for (String qqId : this.binding.keySet()) {
            buyerIdBinded.put(qqId, this.binding.getLong(qqId));
        }

        //pk
        for (String id : setting.keySet("pk")) {
            JSONObject o = JSONUtil.parseObj(setting.getStr(id, "pk", "{}"));
            pk.put(id, o);
        }
    }

    //抽卡

    public String addLotteryByJSON(JSONObject json) {
        String id = json.getStr("id", "");
        if (!(!id.equals("") && getLotteryById(id) == null)) {
            id = generateLotteryId();
        }
        File d = new File(Common.I.documentFolder, id + ".json");

        Lottery2 l = Lottery2.construct(id, null, json);
        if (l == null) {
            return "null";
        } else {
            ConfigLotteryDocument document = new ConfigLotteryDocument(id, d, l);
            lotteryDocuments.add(document);
            l.setDocument(document);

            FileUtil.writeString(json.toStringPretty(), d, Charset.defaultCharset());
            JSONArray a = JSONUtil.parseArray(setting.getStr("documents", "lottery", "[]"));
            a.add(id);
            setting.setByGroup("documents", "lottery", a.toString());
            save();

            return id;
        }
    }

    public void rmLottery(ConfigLotteryDocument document) {
        String id = document.id;
        FileUtil.moveContent(new File(Common.I.documentFolder, id + ".json"),
                new File(Common.I.historyFolder, id + ".json"), false);

        if (!new File(Common.I.historyFolder, "id").exists())
            new File(Common.I.historyFolder, "id").mkdir();

        FileUtil.move(new File(Common.I.picFolder, "id"),
                new File(Common.I.historyFolder, "id"), true);

        FileUtil.move(new File(Common.I.dataFolder, "id"),
                new File(Common.I.historyFolder, "id"), true);

        lotteryDocuments.remove(document);
        JSONArray a = JSONUtil.parseArray(setting.getStr("documents", "lottery", "[]"));
        a.add(id);
        setting.setByGroup("documents", "lottery", a.toString());
        save();
    }

    public JSONObject getJsonByLotteryId(String id) {
        File d = new File(Common.I.documentFolder, id + ".json");
        try {
            return JSONUtil.readJSONObject(d, Charset.defaultCharset());
        } catch (Exception e) {
            return null;
        }
    }

    public Lottery2[] getLotterysByGroupIdAndItemId(long groupId, long item_id) {
        List<Lottery2> a = new ArrayList<>();
        for (ConfigLotteryDocument document : lotteryDocuments) {
            Lottery2 l = document.getLottery();
            if (l != null) {
                if (ArrayUtil.contains(l.groupIds, groupId) && ArrayUtil.contains(l.item_ids, item_id)) {
                    a.add(document.getLottery());
                }
            }
        }
        return a.toArray(new Lottery2[0]);
    }

    public Lottery2[] getLotteryByGroupId(long groupId) {
        List<Lottery2> a = new ArrayList<>();
        for (ConfigLotteryDocument document : lotteryDocuments) {
            Lottery2 l = document.getLottery();
            if (l != null) {
                if (ArrayUtil.contains(l.groupIds, groupId)) {
                    a.add(document.getLottery());
                }
            }
        }
        return a.toArray(new Lottery2[0]);
    }

    public Lottery2[] getAllNonNullLotterys() {
        List<Lottery2> a = new ArrayList<>();
        for (ConfigLotteryDocument document : lotteryDocuments) {
            if (document.getLottery() != null)
                a.add(document.getLottery());
        }
        return a.toArray(new Lottery2[0]);
    }

    public String generateLotteryId() {
        String a = RandomUtil.randomString(5).toUpperCase();
        while (getLotteryById(a) != null) {
            a = RandomUtil.randomString(5).toUpperCase();
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

    public long getBindingBuyerId(String qqId) {
        return buyerIdBinded.getOrDefault(qqId, 0L);
    }

    public void bindBuyerId(String qqId, long buyerId) {
        buyerIdBinded.put(qqId, buyerId);
        binding.set(qqId, buyerId);
        setting.setByGroup("binding", "lottery", binding.toString());
        save();
    }

    public boolean unbindBuyerId(String qqId) {
        if (buyerIdBinded.containsKey(qqId)) {
            buyerIdBinded.remove(qqId);
            binding.remove(qqId);
            setting.setByGroup("binding", "lottery", binding.toString());
            save();
            return true;
        }
        return false;
    }

    //pk

    public String addPkByJson(JSONObject json) {
        String id = json.getStr("id", "");
        if (!(!id.equals("") && !pk.containsKey(json.getStr("id")))) {
            id = generatePkId();
        }

        if (!isValidPK(json))
            return "null";

        JSONArray opponents = new JSONArray();
        for (Object o : json.getJSONArray("opponents").toArray()) {
            JSONObject opponent = JSONUtil.parseObj(o);
            if (opponent.containsKey("item_id")) {
                boolean success = opponent.containsKey("cookie");
                if (!success) {
                    long stock = 0;
                    for (Long item_id : opponent.getBeanList("item_id", Long.class)) {
                        stock += WeidianHandler.INSTANCE.getTotalStock(item_id);
                    }

                    if (stock != 0L) {
                        opponent.set("stock", stock);
                        opponents.add(opponent);
                        success = true;
                    }
                }

                if (success) {
                    continue;
                }
            }

            return "failed";
        }

        json.set("opponents", opponents);
        pk.put(id, json);
        setting.setByGroup(id, "pk", json.toString());
        save();
        return id;
    }

    public boolean rmPk(String id) {
        if (pk.containsKey(id)) {
            pk.remove(id);
            setting.remove("pk", id);
            save();
            return true;
        } else
            return false;
    }

    public boolean editPkByJson(String id, JSONObject json) {
        if (rmPk(id)) {
            switch (addPkByJson(json.set("id", id))) {
                case "null":
                case "failed":
                    return false;
                default:
                    return true;
            }
        }
        return false;
    }

    public JSONObject getPkOpponent(String id, String opponent_name){
        try {
            if (pk.containsKey(id)) {
                JSONObject o = pk.get(id);
                JSONArray a = o.getJSONArray("opponents");
                for (Object op : a.toArray()) {
                    JSONObject op1 = JSONUtil.parseObj(op);
                    if (op1.getStr("name").equals(opponent_name)) {
                        return op1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean editStock(String id, String opponent, long stock) {
        try {
            if (pk.containsKey(id)) {
                JSONObject o = pk.get(id);
                JSONArray a = o.getJSONArray("opponents");
                JSONArray a2 = new JSONArray();
                for (Object op : a.toArray()) {
                    JSONObject op1 = JSONUtil.parseObj(op);
                    if (op1.getStr("name").equals(opponent)) {
                        op1.set("stock", stock);
                    }
                    a2.add(op1);
                }
                o.set("opponents", a2);
                pk.put(id, o);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String generatePkId() {
        String a = RandomUtil.randomString(5).toUpperCase();
        while (pk.containsKey(a)) {
            a = RandomUtil.randomString(5).toUpperCase();
        }
        return a;
    }

    public boolean isValidPK(JSONObject json) {
        return json.containsKey("groups")
                && json.containsKey("name")
                && json.containsKey("item_id")
                && json.containsKey("opponents");
    }

    public JSONObject[] getPkByGroupId(long groupId) {
        List<JSONObject> j = new ArrayList<>();
        for (JSONObject pk : pk.values()) {
            if (isValidPK(pk)) {
                for (Long g : pk.getBeanList("groups", Long.class)) {
                    if (g.longValue() == groupId) {
                        j.add(pk);
                        break;
                    }
                }
            }
        }
        return j.toArray(new JSONObject[0]);
    }

    public JSONObject[] getPkByGroupIdAndItemId(long groupId, long item_id) {
        List<JSONObject> j = new ArrayList<>();
        for (JSONObject pk : pk.values()) {
            if (isValidPK(pk) && pk.getLong("item_id").longValue() == item_id) {
                for (Long g : pk.getBeanList("groups", Long.class)) {
                    if (g.longValue() == groupId) {
                        j.add(pk);
                        break;
                    }
                }
            }
        }
        return j.toArray(new JSONObject[0]);
    }

    public List<Map.Entry<String, JSONObject>> getAllValidPk() {
        List<Map.Entry<String, JSONObject>> j = new ArrayList<>();
        for (Map.Entry<String, JSONObject> pk : pk.entrySet()) {
            if (isValidPK(pk.getValue()))
                j.add(pk);
        }

        return j;
    }

}
