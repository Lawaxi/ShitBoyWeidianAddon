package net.lawaxi.sbwa.util;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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
}
