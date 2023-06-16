package net.lawaxi.sbwa.model;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.sbwa.ShitBoyWeidianAddon;
import net.lawaxi.sbwa.config.ConfigLotteryDocument;

import java.math.BigDecimal;
import java.util.*;

public class Lottery2 {
    public final String lottery_id;
    public final String name;
    public final BigDecimal fee;
    public final long[] groupIds;
    public final long[] item_ids;
    public final List<Map.Entry<Integer, Gift2>> map;
    public ConfigLotteryDocument document;

    public Lottery2(String lotteryId, String name, BigDecimal fee, long[] groupIds, long[] itemIds, List<Map.Entry<Integer, Gift2>> map, ConfigLotteryDocument document) {
        this.lottery_id = lotteryId;
        this.name = name;
        this.fee = fee;
        this.groupIds = groupIds;
        this.item_ids = itemIds;
        this.map = map;
        this.document = document;
    }

    public List<Gift2> draw(double pay, long buyerId) {
        int time = new BigDecimal(pay).divide(fee).intValue();//向下取整
        List<Gift2> a = new ArrayList<>();
        for (int i = 0; i < time; i++) {
            a.add(draw(buyerId));
        }
        a.sort(Comparator.comparingInt(b -> b.index));//由小到大排序
        return a;
    }

    public Gift2 draw(long buyerId) {
        Gift2 g = getRandomGift();
        if (g != null) {
            this.document.getData().addGiftId(buyerId, g.id);
        } else {
            ShitBoyWeidianAddon.INSTANCE.getLogger().warning("错误的抽卡结果: null");
        }
        return g;
    }

    public Gift2[] getOwnedGifts(long buyerId) {
        List<Gift2> a = new ArrayList<>();
        for (String id : this.document.getData().getGiftIds(buyerId)) {
            Gift2 g = getGiftById(id);
            if (g != null)
                a.add(g);
        }
        a.sort(Comparator.comparingInt(b -> b.index));
        return a.toArray(new Gift2[0]);
    }

    public Gift2 getRandomGift() {
        int a = RandomUtil.randomInt(0, getMaxNum());//不包含尾
        for (Map.Entry<Integer, Gift2> g : map) {
            if (g.getKey().intValue() > a)
                return g.getValue();
        }
        return null;//应该不会出现的情况
    }

    public int getMaxNum() {
        return map.get(map.size() - 1).getKey().intValue();
    }

    public Gift2 getGiftById(String id) {
        for (Map.Entry<Integer, Gift2> g : map) {
            if (g.getValue().id.equals(id))
                return g.getValue();
        }
        return null;
    }

    public static Lottery2 construct(String lottery_id, ConfigLotteryDocument document, JSONObject lottery) {
        try {
            long[] groups = lottery.getBeanList("groups", Long.class).stream().mapToLong((t) -> {
                return t;
            }).toArray();
            long[] item_ids = lottery.getBeanList("item_ids", Long.class).stream().mapToLong((t) -> {
                return t;
            }).toArray();
            String n = lottery.getStr("name", lottery_id);
            BigDecimal fee = new BigDecimal(lottery.getStr("fee"));
            HashMap<Integer, Gift2> a = new HashMap<>();
            int b = 0;

            for (Object o : lottery.getJSONArray("qualities").stream().toArray()) {
                //quality
                JSONObject quality = JSONUtil.parseObj(o);
                String q = quality.getStr("qlty");
                int probability = quality.getInt("pr");
                int index = quality.getInt("index");

                for (Object o2 : quality.getJSONArray("gifts").stream().toArray()) {
                    //quality
                    JSONObject gift = JSONUtil.parseObj(o2);
                    String id = gift.getStr("id");
                    String name = gift.getStr("name");
                    String pic = gift.getStr("pic", id + ".jpg");

                    b += probability;
                    a.put(b, new Gift2(
                            lottery_id,
                            id,
                            name,
                            pic,
                            q,
                            index
                    ));
                }

            }

            //注意Map.Entry不是Object，不可以用toArray()方法转化
            List<Map.Entry<Integer, Gift2>> a1 = new ArrayList<>();
            for (Map.Entry<Integer, Gift2> a0 : a.entrySet())
                a1.add(a0);
            a1.sort(Comparator.comparingInt(Map.Entry::getKey));

            return new Lottery2(
                    lottery_id,
                    n, fee, groups,
                    item_ids,
                    a1,
                    document);
        } catch (Exception e) {
            e.printStackTrace();
            ShitBoyWeidianAddon.INSTANCE.getLogger().info("格式错误");
            return null;
        }
    }

    public Lottery2 setDocument(ConfigLotteryDocument document) {
        this.document = document;
        return this;
    }
}
