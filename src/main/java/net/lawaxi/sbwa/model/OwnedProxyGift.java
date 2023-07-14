package net.lawaxi.sbwa.model;

import cn.hutool.http.HttpRequest;

import java.io.InputStream;

public class OwnedProxyGift implements Gift {
    public final String pic;
    public final String title;
    public final int amount;

    public OwnedProxyGift(String pic, String title, int amount) {
        this.pic = pic;
        this.title = title;
        this.amount = amount;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public InputStream getPic() {
        return HttpRequest.get("http://www.lgyzero.top/static/cards/" + this.pic).execute().bodyStream();
    }

}
