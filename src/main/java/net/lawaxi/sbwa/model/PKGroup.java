package net.lawaxi.sbwa.model;

import cn.hutool.json.JSONObject;

import java.math.BigDecimal;

public class PKGroup {
    public final String name;
    public final String title;
    public final BigDecimal coefficient;
    public final boolean coefficientEquals1;
    public long total;
    public String message;

    public PKGroup(String name, String title, BigDecimal coefficient, long total, String message) {
        this.name = name;
        this.title = title;
        this.coefficient = coefficient;
        this.total = total;
        this.message = message;
        this.coefficientEquals1 = this.coefficient.compareTo(new BigDecimal(1)) == 0;
    }

    public static PKGroup construct(String name, JSONObject groups) {
        if (groups == null || !groups.containsKey(name)) {
            return new PKGroup(name, name, new BigDecimal(1), 0L, "");
        } else {
            return new PKGroup(
                    name,
                    groups.getJSONObject(name).getStr("title", name),
                    groups.getJSONObject(name).getBigDecimal("coefficient", new BigDecimal(1)),
                    0L,
                    ""
            );
        }
    }

    public void addMessage(String message) {
        this.message += message;
    }

    public void addMessage(String name, long price) {
        addMessage("\n" + name + ": ");
        if (this.coefficientEquals1) {
            addMessage("" + (price / 100.0));
        } else {
            addMessage((getPriceInCoefficient(price) / 100.0) + " (" + (price / 100.0) + ")");
        }
    }

    public String getMessage() {
        if (this.coefficientEquals1) {
            return "\n【" + title + "】 总额: " + total + message;
        } else {
            return "\n【" + title + "】 总额: " + getTotalInCoefficient() + " (" + this.total + "," + this.coefficient.toPlainString() + ")" + message;
        }
    }

    public void appendPrice(long price) {
        this.total += price;
    }

    public long getTotalInCoefficient() {
        return getPriceInCoefficient(this.total);
    }

    public long getPriceInCoefficient(long price) {
        return this.coefficient.multiply(new BigDecimal(price)).longValue();
    }
}
