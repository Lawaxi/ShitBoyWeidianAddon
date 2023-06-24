package net.lawaxi.sbwa.model;

import cn.hutool.json.JSONObject;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.sbwa.handler.WeidianHandler;

public class PKOpponent {
    public final String name;
    public final long feeAmount;
    public final boolean hasCookie;
    public String group;

    public PKOpponent(String name, long feeAmount, boolean hasCookie) {
        this.name = name;
        this.feeAmount = feeAmount;
        this.hasCookie = hasCookie;
    }

    public PKOpponent setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getGroup() {
        return group == null ? "未分组" : group;
    }

    public static PKOpponent construct(JSONObject opponent) {
        String name = opponent.getStr("name", "");
        PKOpponent out = null;

        //有cookie获取金额的方式
        String cookie = opponent.getStr("cookie", "");
        if (!cookie.equals("")) {
            long fee = 0;
            for (Long item_id : opponent.getBeanList("item_id", Long.class)) {
                fee += WeidianHandler.INSTANCE.getTotalFee(WeidianCookie.construct(
                        opponent.getStr("cookie")
                ), item_id);
            }
            out = new PKOpponent(name, fee, true);
        }


        //无cookie获取金额的方式（不准确）
        else if (opponent.containsKey("stock")) {
            long f = 0;
            for (Long item_id : opponent.getBeanList("item_id", Long.class)) {
                f += WeidianHandler.INSTANCE.getTotalStock(item_id);
            }
            out = new PKOpponent(name, opponent.getLong("stock") - f, false);
        } else {
            out = new PKOpponent(name, 1L, false);
        }
        return opponent.containsKey("pk_group") ? out.setGroup(opponent.getStr("pk_group")) : out;
    }
}
