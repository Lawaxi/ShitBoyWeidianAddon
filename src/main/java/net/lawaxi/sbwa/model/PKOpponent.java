package net.lawaxi.sbwa.model;

import cn.hutool.json.JSONObject;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.sbwa.handler.WeidianHandler;

public class PKOpponent {
    public final String name;
    public final long feeAmount;
    public final boolean hasCookie;

    public PKOpponent(String name, long feeAmount, boolean hasCookie) {
        this.name = name;
        this.feeAmount = feeAmount;
        this.hasCookie = hasCookie;
    }

    public static PKOpponent construct(JSONObject opponent) {
        String name = opponent.getStr("name");

        //有cookie获取金额的方式
        String cookie = opponent.getStr("cookie", "");
        if (!cookie.equals("")) {
            long fee = 0;
            for(Long item_id : opponent.getBeanList("item_id", Long.class)){
                fee += WeidianHandler.INSTANCE.getTotalFee(WeidianCookie.construct(
                        opponent.getStr("cookie")
                ), item_id);
            }
            return new PKOpponent(name, fee, true);
        }


        //无cookie获取金额的方式（不准确）
        if (opponent.containsKey("stock")) {
            long f = 0;
            for(Long item_id : opponent.getBeanList("item_id", Long.class)){
                f+=WeidianHandler.INSTANCE.getTotalStock(item_id);
            }
            return new PKOpponent(name, opponent.getLong("stock") - f, false);
        }

        return new PKOpponent(name, 1L, false);
    }
}
