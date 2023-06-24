package net.lawaxi.sbwa.util;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.Shitboy;
import net.lawaxi.sbwa.model.PKGroup;
import net.lawaxi.sbwa.model.PKOpponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PKUtil {

    public static String getOutput(String group_me, long feeAmount_me, JSONObject pk) {
        String a = "\n---------\n【PK】" + pk.getStr("name");

        List<PKOpponent> opponents = PKUtil.getOpponents(pk.getJSONArray("opponents"));
        boolean group_game = false;

        for (PKOpponent opponent : opponents) {
            if (opponent.group != null) {
                group_game = true;
                break;
            }
        }

        opponents.add(new PKOpponent("我", feeAmount_me, false)
                .setGroup(group_me));

        if (group_game) {
            JSONObject group_properties = pk.getJSONObject("pk_groups");

            HashMap<String, PKGroup> groups2 = new HashMap<>();
            for (PKOpponent opponent : opponents) {
                if (!groups2.containsKey(opponent.getGroup())) {
                    groups2.put(opponent.getGroup(), PKGroup.construct(opponent.getGroup(), group_properties));
                }
                groups2.get(opponent.getGroup()).appendPrice(opponent.feeAmount);
                groups2.get(opponent.getGroup()).addMessage(opponent.name, opponent.feeAmount);
            }


            //整理组
            for (PKGroup g : (PKGroup[]) groups2.values().stream().sorted((a1, a2) -> (a2.getTotalInCoefficient() - a1.getTotalInCoefficient() > 0 ? 1 : -1)).toArray()) {
                a += g.getMessage();
            }

            return a;

        } else {
            //整理各人
            opponents.sort((a1, a2) -> (a2.feeAmount - a1.feeAmount > 0 ? 1 : -1));
            for (PKOpponent opponent : opponents) {
                a += "\n" + opponent.name + ": " + (opponent.feeAmount / 100.0);
            }
            return a;
        }
    }

    public static List<PKOpponent> getOpponents(JSONArray opponents) {
        List<PKOpponent> a = new ArrayList();
        for (Object o : opponents) {
            JSONObject o1 = JSONUtil.parseObj(o);
            a.add(PKOpponent.construct(o1));
        }
        return a;
    }

    public static boolean doGroupsHaveCookie(JSONObject pk) {
        for (Long group : pk.getBeanList("groups", Long.class)) {
            //为未提交cookie的群代理pk查询
            if (!Shitboy.INSTANCE.getProperties().weidian_cookie.containsKey(group)) {
                return false;
            }
        }
        return true;
    }

    public static PKOpponent meAsOpponent(JSONObject pk) {
        JSONObject asOpponent = new JSONObject();
        asOpponent.set("name", null);
        asOpponent.set("stock", pk.getLong("stock"));
        if (pk.containsKey("group")) {
            asOpponent.set("group", pk.getStr("group"));
        }
        return PKOpponent.construct(asOpponent);
    }
}
