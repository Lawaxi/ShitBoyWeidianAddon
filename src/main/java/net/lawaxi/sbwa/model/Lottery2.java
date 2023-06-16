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
    public final BigDecimal fee;
    public final long[] groupIds;
    public final long[] item_ids;
    public final Map.Entry<BigDecimal, Gift2>[] map;
    public ConfigLotteryDocument document;

    public Lottery2(String lotteryId, BigDecimal fee, long[] groupIds, long[] itemIds, Map.Entry<BigDecimal, Gift2>[] map, ConfigLotteryDocument document) {
        this.lottery_id = lotteryId;
        this.fee = fee;
        this.groupIds = groupIds;
        this.item_ids = itemIds;
        this.map = map;
        this.document = document;
    }

    public Gift2[] draw(double pay, long buyerId) {
        int time = new BigDecimal(pay).divide(fee).intValue();//向下取整
        List<Gift2> a = new ArrayList<>();
        for (int i = 0; i < time; i++) {
            a.add(draw(buyerId));
        }
        a.sort(Comparator.comparingInt(b -> b.index));//由小到大排序
        return (Gift2[]) a.toArray();
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

    public Gift2[] getGifts(long buyerId) {
        List<Gift2> a = new ArrayList<>();
        for (String id : this.document.getData().getGiftIds(buyerId)) {
            Gift2 g = getGiftById(id);
            if (g != null)
                a.add(g);
        }
        return a.toArray(new Gift2[0]);
    }

    public Gift2 getRandomGift() {
        double a = RandomUtil.randomDouble(0, getMaxNum());//不包含尾
        for (Map.Entry<BigDecimal, Gift2> g : map) {
            if (g.getKey().doubleValue() > a)
                return g.getValue();
        }
        return null;//应该不会出现的情况
    }

    public double getMaxNum() {
        return map[map.length - 1].getKey().doubleValue();
    }

    public Gift2 getGiftById(String id) {
        for (Map.Entry<BigDecimal, Gift2> g : map) {
            if (g.getValue().id.equals(id))
                return g.getValue();
        }
        return null;
    }

    public static Lottery2 construct(String lottery_id, ConfigLotteryDocument document, JSONObject lottery) {
        try {
            long[] groups = lottery.getJSONArray("groups").stream().mapToLong((t) -> {
                return (Long) t;
            }).toArray();
            long[] item_ids = lottery.getJSONArray("item_ids").stream().mapToLong((t) -> {
                return (Long) t;
            }).toArray();
            BigDecimal fee = new BigDecimal(lottery.getStr("fee"));
            HashMap<BigDecimal, Gift2> a = new HashMap<>();
            BigDecimal b = new BigDecimal(0);

            for (Object o : lottery.getJSONArray("qualities").stream().toArray()) {
                //quality
                JSONObject quality = JSONUtil.parseObj(o);
                String q = quality.getStr("qlty");
                BigDecimal probability = new BigDecimal(quality.getStr("pr"));
                int index = quality.getInt("index");

                for (Object o2 : quality.getJSONArray("gifts").stream().toArray()) {
                    //quality
                    JSONObject gift = JSONUtil.parseObj(o2);
                    String id = gift.getStr("id");
                    String name = gift.getStr("name");
                    String pic = gift.getStr("pic", name + ".jpg");

                    b.add(probability);
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

            return new Lottery2(
                    lottery_id,
                    fee, groups,
                    item_ids,
                    (Map.Entry<BigDecimal, Gift2>[]) a.entrySet().stream().sorted((e1, e2) -> e1.getKey().subtract(e2.getKey()).compareTo(BigDecimal.ZERO) > 0 ? 1 : -1).toArray(),
                    document);
        } catch (Exception e) {
            ShitBoyWeidianAddon.INSTANCE.getLogger().info("格式错误");
            return null;
        }
    }

    public Lottery2 setDocument(ConfigLotteryDocument document) {
        this.document = document;
        return this;
    }
}
