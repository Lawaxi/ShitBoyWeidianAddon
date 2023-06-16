package net.lawaxi.sbwa.model;

import cn.hutool.json.JSONObject;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.sbwa.handler.WeidianHandler;

public class PKOpponent {
    public final String name;
    public final long feeAmount;

    public PKOpponent(String name, long feeAmount) {
        this.name = name;
        this.feeAmount = feeAmount;
    }

    public static PKOpponent construct(JSONObject opponent) {
        String name = opponent.getStr("name");
        long item_id = opponent.getLong("item_id");
        long fee = 0;
        //有cookie获取金额的方式
        if (opponent.containsKey("cookie")) {
            fee = WeidianHandler.INSTANCE.getTotalFee(WeidianCookie.construct(
                    opponent.getStr("cookie")
            ), item_id);
        }

        //无cookie获取金额的方式（不准确）
        if (opponent.containsKey("stock")) {
            fee = opponent.getLong("stock") - WeidianHandler.INSTANCE.getTotalStock(item_id);
        }

        return new PKOpponent(name, fee);
    }
}
