package net.lawaxi.sbwa.util;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.Shitboy;
import net.lawaxi.sbwa.model.PKOpponent;

import java.util.ArrayList;
import java.util.List;

public class PKUtil {
    public static PKOpponent[] getOpponents(JSONArray opponents) {
        List<PKOpponent> a = new ArrayList();
        for (Object o : opponents) {
            JSONObject o1 = JSONUtil.parseObj(o);
            a.add(PKOpponent.construct(o1));
        }
        return a.toArray(new PKOpponent[0]);
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
        return PKOpponent.construct(asOpponent);
    }
}
