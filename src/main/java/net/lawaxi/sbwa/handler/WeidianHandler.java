package net.lawaxi.sbwa.handler;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.model.WeidianBuyer;
import net.lawaxi.model.WeidianCookie;

public class WeidianHandler extends net.lawaxi.handler.WeidianHandler {

    public static WeidianHandler INSTANCE;
    public static final String APIStock = "https://thor.weidian.com/detail/getItemSkuInfo/1.0?param={\"itemId\":\"%s\"}";

    public WeidianHandler() {
        INSTANCE = this;
    }

    public long getTotalStock(long id) {
        String s = get(String.format(APIStock, id));
        JSONObject o = JSONUtil.parseObj(s);
        if (o != null) {
            if (o.getJSONObject("status").getInt("code") == 0) {
                JSONObject r = o.getJSONObject("result");
                long total = 0;
                for (Object o1 : r.getJSONArray("skuInfos")) {
                    JSONObject sku = JSONUtil.parseObj(o1).getJSONObject("skuInfo");
                    int price = sku.getInt("originalPrice");//分为单位
                    int stock = sku.getInt("stock");
                    total += (long) price * stock;
                }
                return total;
            }
        }
        return 0L;
    }

    public long getTotalFee(WeidianCookie cookie, long id) {
        long total = 0;
        for (WeidianBuyer buyer : getItemBuyer(cookie, id)) {
            total += (int) (buyer.contribution * 100);
        }
        return total;
    }

    @Override
    protected HttpRequest setHeader(HttpRequest request) {
        return request;
    }
}
