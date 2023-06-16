package net.lawaxi.sbwa;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.sbwa.config.ConfigConfig;
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

import java.util.ArrayList;
import java.util.List;

public class listener extends SimpleListenerHost {

    public listener() {
        CommandOperator.INSTANCE.addHelp(getHelp());
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
                                Gift2[] a = lottery.draw(pay, 1L);
                                if (a.length > 0) {
                                    group.sendMessage(new At(event.getSender().getId())
                                            .plus(NewHandler.getOut(a, group)));
                                }
                            }
                        }
                    } else {
                        group.sendMessage(getHelp());
                    }
                } else {
                    group.sendMessage(new At(event.getSender().getId()).plus("权限不足喵~"));
                }
            }
        }
        return ListeningStatus.LISTENING;
    }


    @EventHandler()
    public ListeningStatus onUserMessageEvent(UserMessageEvent event) {
        String message = event.getMessage().contentToString();
        User sender = event.getSender();

        if (message.startsWith("/")) {
            String[] args = message.split(" ");
            if (args[0].equals("/抽卡")) {

                if (args.length == 3 && args[1].equals("新建")) {
                    sender.sendMessage(newDocument(args[2], sender.getId(), event.getBot()));
                    return ListeningStatus.LISTENING;
                } else if (args.length == 4 && args[1].equals("修改")) {
                    List<Lottery2> lotteries = getLotteryAdministrating(sender.getId(), event.getBot(), args[2]);
                    if (lotteries.size() == 0) {
                        sender.sendMessage("无对应此id的抽卡或您不可以管理");
                    } else {
                        Lottery2 l = lotteries.get(0);
                        if (l.document.writeDocument(JSONUtil.parseObj(args[3])) == null) {
                            sender.sendMessage("修改失败，请按规定格式输入json");
                        } else
                            sender.sendMessage("修改成功");
                    }

                } else if (args.length == 2 && args[1].equals("列表")) {
                    List<Lottery2> lotteries = getLotteryAdministrating(sender.getId(), event.getBot(), null);
                    String a = "您可以管理的抽卡共" + lotteries.size() + "个：\n";
                    for (int i = 0; i < lotteries.size(); i++) {
                        a += i + "." + lotteries.get(i).lottery_id + "\n";
                    }
                    sender.sendMessage(a);
                } else {
                    sender.sendMessage(getHelp());
                }


            }
        }
        return ListeningStatus.LISTENING;
    }

    public String getHelp() {
        return "【微店抽卡相关】\n"
                + "/抽卡 抽 <抽卡ID> <金额> (管理员模拟抽卡命令，奖品数据计入模拟账户)\n"
                + "(私信)/抽卡 新建 <json>\n"
                + "(私信)/抽卡 修改 <抽卡ID> <json>\n"
                + "(私信)/抽卡 列表\n";
    }

    public String newDocument(String arg2, long qqId, Bot bot) {
        try {
            JSONObject o = JSONUtil.parseObj(arg2);
            if (o != null) {
                for (Long g : (Long[]) o.getJSONArray("groups").stream().toArray()) {
                    if (!ShitBoyWeidianAddon.INSTANCE_SHITBOY.getConfig().isAdmin(bot.getGroup(g), qqId)) {
                        return "您在json中涉及的群" + g + "中没有管理权限";
                    }
                }

                String id = ConfigConfig.INSTANCE.addLotteryByJSON(o);
                if (!id.equals("null")) {
                    return "创建成功，id: " + id;
                }
            }
        } catch (Exception e) {

        }
        return "请按规定格式输入json";
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


}
