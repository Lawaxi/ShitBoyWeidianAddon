package net.lawaxi.sbwa;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.Shitboy;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.model.WeidianItem;
import net.lawaxi.sbwa.config.ConfigConfig;
import net.lawaxi.sbwa.handler.NewWeidianSenderHandler;
import net.lawaxi.sbwa.handler.WeidianHandler;
import net.lawaxi.sbwa.model.Gift2;
import net.lawaxi.sbwa.model.Lottery2;
import net.lawaxi.util.CommandOperator;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class listener extends SimpleListenerHost {

    public listener() {
        CommandOperator.INSTANCE.addHelp(getHelp(1));
        CommandOperator.INSTANCE.addHelp(getHelp(2));
    }

    @EventHandler()
    public ListeningStatus onGroupMessage(GroupMessageEvent event) {
        Group group = event.getGroup();
        String message = event.getMessage().contentToString();

        if (message.startsWith("/")) {
            String[] args = message.split(" ");
            if (args[0].equals("/抽卡")) {
                if (ShitBoyWeidianAddon.INSTANCE_SHITBOY.getConfig().isAdmin(group, event.getSender().getId())) {
                    if (args.length == 4 && args[1].equals("抽")) {
                        String id = args[2];
                        int pay = Integer.valueOf(args[3]);
                        Lottery2[] lotteries = ConfigConfig.INSTANCE.getLotteryByGroupId(group.getId());
                        for (Lottery2 lottery : lotteries) {
                            if (lottery.lottery_id.equals(id)) {
                                List<Gift2> a = lottery.draw(pay, 1L);
                                if (a.size() > 0) {
                                    group.sendMessage(new At(event.getSender().getId())
                                            .plus(NewWeidianSenderHandler.getOutput(a, group)));
                                }
                            }
                        }
                    } else {
                        group.sendMessage(getHelp(1));
                    }
                } else {
                    group.sendMessage(new At(event.getSender().getId()).plus("权限不足喵~"));
                }
            }
        } else if (message.equalsIgnoreCase("pk")) {
            JSONObject[] pks = ConfigConfig.INSTANCE.getPkByGroupId(group.getId());
            for (JSONObject pk : pks) {
                Message a = new PlainText("【PK】" + pk.getStr("name") + "\n");

                long itemid = pk.getLong("item_id");
                WeidianCookie cookie = Shitboy.INSTANCE.getProperties().weidian_cookie.get(group.getId());
                WeidianItem item = WeidianHandler.INSTANCE.searchItem(cookie, itemid);
                if (item != null) {
                    a = a.plus(NewWeidianSenderHandler.INSTANCE.executeItemMessages(
                            item,
                            group,
                            10
                    ));
                } else {
                    a = a.plus("获取失败");
                }

                group.sendMessage(a);
            }
        }

        return ListeningStatus.LISTENING;
    }


    @EventHandler()
    public ListeningStatus onUserMessageEvent(UserMessageEvent event) {
        String message = event.getMessage().contentToString();
        User sender = event.getSender();

        if (message.startsWith("/")) {
            String[] args = splitPrivateCommand(message); //分三份
            if (args[0].equals("/抽卡")) {

                if (args[1].equals("新建")) {
                    sender.sendMessage(newDocument(args[2], sender.getId(), event.getBot()));
                } else if (args[1].equals("修改") && args[2].contains(" ")) {
                    String arg2 = args[2].substring(0, args[2].indexOf(" "));
                    String arg3 = args[2].substring(args[2].indexOf(" ") + 1);

                    List<Lottery2> lotteries = getLotteryAdministrating(sender.getId(), event.getBot(), arg2);
                    if (lotteries.size() == 0) {
                        sender.sendMessage("无对应此id的抽卡或您不可以管理");
                    } else {
                        sender.sendMessage(editDocument(lotteries.get(0), arg3, sender.getId(), event.getBot()));
                    }

                } else if (args[1].equals("获取")) {
                    List<Lottery2> lotteries = getLotteryAdministrating(sender.getId(), event.getBot(), args[2]);
                    if (lotteries.size() == 0) {
                        sender.sendMessage("无对应此id的抽卡或您不可以管理");
                    } else {
                        JSONObject object = ConfigConfig.INSTANCE.getJsonByLotteryId(lotteries.get(0).lottery_id);
                        if (object == null) {
                            sender.sendMessage("查询错误");
                        } else {
                            sender.sendMessage(object.toString());
                        }
                    }
                } else if (args[1].equals("删除")) {
                    List<Lottery2> lotteries = getLotteryAdministrating(sender.getId(), event.getBot(), args[2]);
                    if (lotteries.size() == 0) {
                        sender.sendMessage("无对应此id的抽卡或您不可以管理");
                    } else {
                        ConfigConfig.INSTANCE.rmLottery(lotteries.get(0).document);
                        sender.sendMessage("删除成功");
                    }
                } else if (args[1].equals("全部")) {
                    List<Lottery2> lotteries = getLotteryAdministrating(sender.getId(), event.getBot(), null);
                    String a = "您可以管理的抽卡共" + lotteries.size() + "个：\n";
                    for (int i = 0; i < lotteries.size(); i++) {
                        a += (i + 1) + ".(" + lotteries.get(i).lottery_id + ")" + lotteries.get(i).name + "\n";
                    }
                    sender.sendMessage(a);
                } else if (args[1].equals("绑定")) {
                    try {
                        Long buyerId = Long.valueOf(args[2]);
                        ConfigConfig.INSTANCE.bindBuyerId("" + sender.getId(), buyerId);
                        sender.sendMessage("绑定微店id: " + buyerId);
                    } catch (Exception e) {
                        sender.sendMessage("请输入正确的微店id");
                    }
                } else if (args[1].equals("解绑")) {
                    if (ConfigConfig.INSTANCE.unbindBuyerId("" + sender.getId())) {
                        sender.sendMessage("解绑成功");
                    } else {
                        sender.sendMessage("您没有绑定过微店id");
                    }
                } else if (args[1].equals("查卡")) {
                    long buyerId = ConfigConfig.INSTANCE.getBindingBuyerId("" + sender.getId());
                    if (buyerId != 0L) {
                        //当前抽卡
                        String current = "在当前正在进行的抽卡中：";
                        for (Lottery2 lottery : ConfigConfig.INSTANCE.getAllNonNullLotterys()) {
                            current += "\n【" + lottery.name + "】";
                            for (Gift2 gift : lottery.getOwnedGifts(buyerId)) {
                                current += "\n" + gift.getTitle();
                            }
                        }

                        //历史抽卡记录暂不支持查看
                        sender.sendMessage(current);

                    } else {
                        sender.sendMessage("您没有绑定过微店id，请使用“/抽卡 绑定 <微店ID>”绑定");
                    }
                } else {
                    sender.sendMessage(getHelp(1));
                }


            } else if (args[0].equals("/pk")) {

                if (args[1].equals("新建")) {
                    sender.sendMessage(newPK(args[2], sender.getId(), event.getBot()));
                } else if (args[1].equals("修改") && args[2].contains(" ")) {
                    String arg2 = args[2].substring(0, args[2].indexOf(" "));
                    String arg3 = args[2].substring(args[2].indexOf(" ") + 1);

                    List<Map.Entry<String, JSONObject>> pks = getPkAdministrating(sender.getId(), event.getBot(), arg2);
                    if (pks.size() == 0) {
                        sender.sendMessage("无对应此id的PK或您不可以管理");
                    } else {
                        try {
                            if (ConfigConfig.INSTANCE.editPkByJson(pks.get(0).getKey(), JSONUtil.parseObj(arg3))) {
                                sender.sendMessage("修改成功");
                            } else {
                                sender.sendMessage("json格式错误或无法获取对手金额");
                            }
                        } catch (Exception e) {
                            sender.sendMessage("请输入json");
                        }
                    }
                } else if (args[1].equals("获取")) {
                    List<Map.Entry<String, JSONObject>> pks = getPkAdministrating(sender.getId(), event.getBot(), args[2]);
                    if (pks.size() == 0) {
                        sender.sendMessage("无对应此id的PK或您不可以管理");
                    } else {
                        sender.sendMessage(pks.get(0).getValue().toString());
                    }
                } else if (args[1].equals("删除")) {
                    List<Map.Entry<String, JSONObject>> pks = getPkAdministrating(sender.getId(), event.getBot(), args[2]);
                    if (pks.size() == 0) {
                        sender.sendMessage("无对应此id的PK或您不可以管理");
                    } else {
                        ConfigConfig.INSTANCE.rmPk(pks.get(0).getKey());
                        sender.sendMessage("删除成功");
                    }
                } else if (args[1].equals("全部")) {
                    List<Map.Entry<String, JSONObject>> pks = getPkAdministrating(sender.getId(), event.getBot(), null);
                    String a = "您可以管理的PK共" + pks.size() + "个：\n";
                    for (int i = 0; i < pks.size(); i++) {
                        a += (i + 1) + ".(" + pks.get(i).getKey() + ")" + pks.get(i).getValue().getStr("name") + "\n";
                    }
                    sender.sendMessage(a);
                } else {
                    sender.sendMessage(getHelp(2));
                }
            }
        }
        return ListeningStatus.LISTENING;
    }

    public String getHelp(int code) {
        if (code == 1) {
            return "【微店抽卡相关】\n"
                    + "/抽卡 抽 <抽卡ID> <金额> (管理员模拟抽卡命令，奖品数据计入模拟账户)\n"
                    + "(私信 管理员)/抽卡 新建 <json>\n"
                    + "(私信 管理员)/抽卡 修改 <抽卡ID> <json>\n"
                    + "(私信 管理员)/抽卡 获取 <抽卡ID>\n"
                    + "(私信 管理员)/抽卡 删除 <抽卡ID>\n"
                    + "(私信 管理员)/抽卡 全部\n"
                    + "(私信)/抽卡 绑定 <个人ID>"
                    + "(私信)/抽卡 解绑"
                    + "(私信)/抽卡 查卡";
        }
        return "【微店PK相关】\n"
                + "(私信)/pk 新建 <json>\n"
                + "(私信)/pk 修改 <pkID> <json>\n"
                + "(私信)/pk 获取 <pkID>\n"
                + "(私信)/pk 删除 <pkID>\n"
                + "(私信)/pk 全部\n";
    }

    public String newDocument(String arg2, long qqId, Bot bot) {
        try {
            JSONObject o = JSONUtil.parseObj(arg2);
            long g = administratingValidJson(o, qqId, bot);
            if (g != 0L)
                return "您在json中涉及的群" + g + "中没有管理权限";

            String id = ConfigConfig.INSTANCE.addLotteryByJSON(o);
            if (!id.equals("null")) {
                return "创建成功，id: " + id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "请按规定格式输入json";
    }

    public String editDocument(Lottery2 lottery, String arg2, long qqId, Bot bot) {
        try {
            JSONObject o = JSONUtil.parseObj(arg2);
            long g = administratingValidJson(o, qqId, bot);
            if (g != 0L)
                return "您在json中涉及的群" + g + "中没有管理权限";

            if (lottery.document.writeDocument(o) != null) {
                return "修改成功";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "修改失败，请按规定格式输入json";
    }

    //可以管理的抽卡
    public List<Lottery2> getLotteryAdministrating(long qqId, Bot bot, String id) {
        List<Lottery2> lotteries = new ArrayList<>();
        for (Lottery2 lottery : (id == null ?
                ConfigConfig.INSTANCE.getAllNonNullLotterys() :
                new Lottery2[]{ConfigConfig.INSTANCE.getLotteryById(id)})) {
            if (lottery.groupIds.length == 0)
                continue;

            boolean n = true;
            for (long group : lottery.groupIds) {
                if (!ShitBoyWeidianAddon.INSTANCE_SHITBOY.getConfig().isAdmin(bot.getGroup(group), qqId)) {
                    n = false;
                }
            }
            if (n)
                lotteries.add(lottery);
        }
        return lotteries;
    }


    public String newPK(String arg2, long qqId, Bot bot) {
        try {
            JSONObject o = JSONUtil.parseObj(arg2);
            long g = administratingValidJson(o, qqId, bot);
            if (g != 0L)
                return "您在json中涉及的群" + g + "中没有管理权限";

            String id = ConfigConfig.INSTANCE.addPkByJson(o);
            switch (id) {
                case "failed":
                    return "获取对手金额失败，请检查PK对手商品id是否填写正确并已上架";
                case "null":
                    return "请按规定格式输入json";
                default:
                    return "创建成功，id: " + id;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "请按规定格式输入json";
    }

    public List<Map.Entry<String, JSONObject>> getPkAdministrating(long qqId, Bot bot, String id) {
        List<Map.Entry<String, JSONObject>> pks = new ArrayList<>();
        for (Map.Entry<String, JSONObject> pk : ConfigConfig.INSTANCE.getAllValidPk()) {
            if (administratingValidJson(pk.getValue(), qqId, bot) == 0 && (id == null || pk.getKey().equals(id))) {
                pks.add(pk);
            }
        }
        return pks;
    }

    public long administratingValidJson(JSONObject json, long qqId, Bot bot) {
        for (Long g : json.getBeanList("groups", Long.class)) {
            if (!ShitBoyWeidianAddon.INSTANCE_SHITBOY.getConfig().isAdmin(bot.getGroup(g), qqId)) {
                return g.longValue();
            }
        }
        return 0L;
    }

    private String[] splitPrivateCommand(String command) {
        if (command.contains(" ")) {
            String[] out = new String[3];
            out[0] = command.substring(0, command.indexOf(" "));
            command = command.substring(command.indexOf(" ") + 1);
            if (command.contains(" ")) { //三份
                out[1] = command.substring(0, command.indexOf(" "));
                out[2] = command.substring(command.indexOf(" ") + 1);
                return out;
            } else { //两份
                out[1] = command;
                return out;
            }
        }
        return null;
    }
}
