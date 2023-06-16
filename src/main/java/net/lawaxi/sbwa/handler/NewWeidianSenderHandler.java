package net.lawaxi.sbwa.handler;

import cn.hutool.json.JSONObject;
import net.lawaxi.handler.WeidianSenderHandler;
import net.lawaxi.model.WeidianItem;
import net.lawaxi.model.WeidianOrder;
import net.lawaxi.sbwa.ShitBoyWeidianAddon;
import net.lawaxi.sbwa.config.ConfigConfig;
import net.lawaxi.sbwa.model.Gift2;
import net.lawaxi.sbwa.model.Lottery2;
import net.lawaxi.sbwa.model.PKOpponent;
import net.lawaxi.sbwa.util.PKUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.IOException;
import java.io.InputStream;

public class NewWeidianSenderHandler extends WeidianSenderHandler {

    public static NewWeidianSenderHandler INSTANCE;
    private final MiraiLogger logger;

    public NewWeidianSenderHandler() {
        this.logger = ShitBoyWeidianAddon.INSTANCE_SHITBOY.getLogger();
        INSTANCE = this;
    }

    @Override
    public Message executeItemMessages(WeidianItem item, Group group, int pickAmount) {
        Message m = super.executeItemMessages(item, group, pickAmount);
        JSONObject[] pks = ConfigConfig.INSTANCE.getPkByGroupIdAndItemId(group.getId(), item.id);
        for (JSONObject pk : pks) {
            String a = "\n---------\n【PK】" + pk.getStr("name");
            for (PKOpponent opponent : PKUtil.getOpponents(pk.getJSONArray("opponents"))) {
                a += "\n" + opponent + ": " + opponent.feeAmount;
            }

            m = m.plus(a);
        }
        return m;
    }

    @Override
    public Message executeOrderMessage(WeidianOrder order, Group group) {
        Message m = super.executeOrderMessage(order, group);
        Lottery2[] lotteries = ConfigConfig.INSTANCE.getLotterysByGroupIdAndItemId(group.getId(), order.itemID);
        for (Lottery2 lottery : lotteries) {
            Gift2[] a = lottery.draw(order.price, order.buyerID);
            if (a.length > 0) {
                m = m.plus("\n---------\n").plus(getOutput(a, group));
            }
        }
        return m;
    }

    public static Message getOutput(Gift2[] a, Group group) {
        Message m = new PlainText("");

        try {
            InputStream i = getFrontPic(a);
            if (i != null) {
                m = m.plus(group.uploadImage(ExternalResource.create(i)));
            }
        } catch (IOException e) {

        }

        m = m.plus("\n抽卡收获：\n");
        for (Gift2 g : a) {
            m.plus(g.getTitle() + "\n");
        }
        return m;
    }

    //寻找有图的gift
    private static InputStream getFrontPic(Gift2[] gifts) {
        for (Gift2 g : gifts) {
            InputStream i = g.getPic();
            if (i != null)
                return i;
        }
        return null;
    }
}
