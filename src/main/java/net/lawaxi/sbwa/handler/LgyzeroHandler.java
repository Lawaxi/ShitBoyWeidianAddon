package net.lawaxi.sbwa.handler;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.handler.WebHandler;
import net.lawaxi.sbwa.ShitBoyWeidianAddon;
import net.lawaxi.sbwa.model.OwnedProxyGift;

import java.util.List;

public class LgyzeroHandler extends WebHandler {
    public static final String API = "http://www.lgyzero.top/api/cardLottery/cardInquire";

    public JSONObject inquireCard(long buyerId) {
        try {
            JSONObject object = JSONUtil.parseObj(post(API, String.format("{\"data\":{\"platform\":\"Weidian\",\"userID\":\"%d\"}}", buyerId)));
            if (object.getInt("status") == 0) {
                return object.getJSONObject("content");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String checkOwnedGifts(JSONObject inquired, String key, boolean specifically, List<OwnedProxyGift> owned) {
        JSONObject info = inquired.getJSONObject("progressInfo").getJSONObject(key);
        String out = "【" + key + "】";
        for (String k : info.keySet()) {
            JSONArray a = info.getJSONArray(k);
            out += "\n[" + k + "]" + a.get(0) + "/" + a.get(1);
        }

        if (specifically || owned != null) {
            JSONObject dict = inquired.getJSONObject("cardDict").getJSONObject(key);
            JSONObject image = inquired.getJSONObject("imageUrlDict");
            for (String k : dict.keySet()) {
                if (owned != null) {
                    owned.add(new OwnedProxyGift(
                            image.getStr(k),
                            k,
                            dict.getInt(k)
                    ));
                }
                if (specifically) {
                    out += "\n" + (owned == null ? "" : owned.size() + ".") + k + "*" + dict.getInt(k);
                }
            }
        }

        return out;
    }
}
