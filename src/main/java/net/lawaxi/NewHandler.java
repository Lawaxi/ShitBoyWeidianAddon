package net.lawaxi;

import net.lawaxi.handler.WeidianSenderHandler;
import net.lawaxi.model.WeidianItem;
import net.lawaxi.model.WeidianOrder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.utils.MiraiLogger;

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
        logger.info("executeOrderMessage");
        return super.executeOrderMessage(order, group);
    }
}
