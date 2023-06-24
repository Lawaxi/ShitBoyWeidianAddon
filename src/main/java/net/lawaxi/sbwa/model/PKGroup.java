package net.lawaxi.sbwa.model;

import cn.hutool.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PKGroup {
    public final String name;
    public final String title;
    public final BigDecimal coefficient;
    public final boolean coefficientEquals1;
    public long total;
    public ArrayList<PKOpponent> opponents = new ArrayList<>();

    public PKGroup(String name, String title, BigDecimal coefficient, long total) {
        this.name = name;
        this.title = title;
        this.coefficient = coefficient;
        this.total = total;
        this.coefficientEquals1 = this.coefficient.compareTo(new BigDecimal(1)) == 0;
    }

    public static PKGroup construct(String name, JSONObject groups) {
        if (groups == null || !groups.containsKey(name)) {
            return new PKGroup(name, name, new BigDecimal(1), 0L);
        } else {
            return new PKGroup(
                    name,
                    groups.getJSONObject(name).getStr("title", name),
                    groups.getJSONObject(name).getBigDecimal("coefficient", new BigDecimal(1)),
                    0L
            );
        }
    }

    public void addOpponent(PKOpponent opponent) {
        this.opponents.add(opponent);
        appendPrice(opponent.feeAmount);
    }

    public String getMessage() {
        String a = "\n【" + title + "】 总额: ";
        this.opponents.sort((a1, a2) -> (a2.feeAmount - a1.feeAmount > 0 ? 1 : -1));
        if (this.coefficientEquals1) {
            a += "" + (total / 100.0);
            for (PKOpponent opponent : this.opponents) {
                a += "\n" + opponent.name + ": " + (opponent.feeAmount / 100.0);
            }
        } else {

            a += "" + (getTotalInCoefficient() / 100.0) + " (" + (this.total / 100.0) + "," + this.coefficient.toPlainString() + ")";
            for (PKOpponent opponent : this.opponents) {
                a += "\n" + opponent.name + ": " + (getPriceInCoefficient(opponent.feeAmount) / 100.0) + " (" + (opponent.feeAmount / 100.0) + ")";
            }
        }
        return a;
    }

    private void appendPrice(long price) {
        this.total += price;
    }

    public long getTotalInCoefficient() {
        return getPriceInCoefficient(this.total);
    }

    public long getPriceInCoefficient(long price) {
        return this.coefficient.multiply(new BigDecimal(price)).longValue();
    }
}
