package net.lawaxi.sbwa;

import net.lawaxi.handler.WeidianSenderHandler;
import net.lawaxi.model.WeidianItem;
import net.lawaxi.model.WeidianOrder;
import net.lawaxi.sbwa.config.ConfigConfig;
import net.lawaxi.sbwa.model.Gift2;
import net.lawaxi.sbwa.model.Lottery2;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.IOException;
import java.io.InputStream;

public class NewHandler extends WeidianSenderHandler {

    private final MiraiLogger logger;

    public NewHandler() {
        this.logger = ShitBoyWeidianAddon.INSTANCE_SHITBOY.getLogger();
    }

    @Override
    public Message executeItemMessages(WeidianItem item, Group group) {
        return super.executeItemMessages(item, group);
    }

    @Override
    public Message executeOrderMessage(WeidianOrder order, Group group) {
        Message m = super.executeOrderMessage(order, group);
        Lottery2[] lotteries = ConfigConfig.INSTANCE.getLotterysByGroupIdAndItemId(group.getId(), order.itemID);
        for (Lottery2 lottery : lotteries) {
            Gift2[] a = lottery.draw(order.price, order.buyerID);
            if (a.length > 0) {
                m = m.plus("\n---------\n").plus(getOut(a, group));
            }
        }
        return m;
    }

    public static Message getOut(Gift2[] a, Group group) {
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
