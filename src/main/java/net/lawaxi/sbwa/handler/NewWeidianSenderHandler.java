package net.lawaxi.sbwa.handler;

import cn.hutool.json.JSONObject;
import net.lawaxi.handler.WeidianSenderHandler;
import net.lawaxi.model.WeidianItem;
import net.lawaxi.model.WeidianItemMessage;
import net.lawaxi.model.WeidianOrder;
import net.lawaxi.model.WeidianOrderMessage;
import net.lawaxi.sbwa.ShitBoyWeidianAddon;
import net.lawaxi.sbwa.config.ConfigConfig;
import net.lawaxi.sbwa.model.Gift2;
import net.lawaxi.sbwa.model.Lottery2;
import net.lawaxi.sbwa.util.PKUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class NewWeidianSenderHandler extends WeidianSenderHandler {

    public static NewWeidianSenderHandler INSTANCE;
    private final MiraiLogger logger;

    public NewWeidianSenderHandler() {
        this.logger = ShitBoyWeidianAddon.INSTANCE_SHITBOY.getLogger();
        INSTANCE = this;
    }

    @Override
    public WeidianItemMessage executeItemMessages(WeidianItem item, Group group, int pickAmount) {
        WeidianItemMessage m = super.executeItemMessages(item, group, pickAmount);
        long feeAmount_me = m.amountTotal;

        JSONObject[] pks = ConfigConfig.INSTANCE.getPkByGroupIdAndItemId(group.getId(), item.id);
        for (JSONObject pk : pks) {
            m.setMessage(m.getMessage().plus(PKUtil.getOutput(pk.getStr("pk_group", null), feeAmount_me, pk)));
        }
        return m;
    }

    @Override
    public WeidianOrderMessage executeOrderMessage(WeidianOrder order, Group group) {
        WeidianOrderMessage m = super.executeOrderMessage(order, group);
        Lottery2[] lotteries = ConfigConfig.INSTANCE.getLotterysByGroupIdAndItemId(group.getId(), order.itemID);
        for (Lottery2 lottery : lotteries) {
            List<Gift2> a = lottery.draw(order.price, order.buyerID);
            if (a.size() > 0) {
                m.setMessage(m.getMessage().plus("\n---------\n").plus(getLotteryOutput(a, group)));
            }
        }
        return m;
    }

    public static Message getLotteryOutput(List<Gift2> a, Group group) {
        Message m = new PlainText("");

        try {
            InputStream i = getGiftsFrontPic(a);
            if (i != null) {
                m = m.plus(group.uploadImage(ExternalResource.create(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        m = m.plus("\n抽卡" + a.size() + "张，收获：\n");
        for (Gift2 g : a) {
            m = m.plus(g.getTitle() + "\n");
        }
        return m;
    }

    //寻找有图的gift
    private static InputStream getGiftsFrontPic(List<Gift2> gifts) {
        for (Gift2 g : gifts) {
            InputStream i = g.getPic();
            if (i != null)
                return i;
        }
        return null;
    }
}
