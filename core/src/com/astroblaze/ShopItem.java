package com.astroblaze;

public class ShopItem {
    public final int iconResource;
    public final String name;
    public final float current;
    public final float next;
    public final float price;
    public final ShopItemType type;

    public ShopItem(int iconResource, String name, float current, float next, float price, ShopItemType type) {
        this.iconResource = iconResource;
        this.name = name;
        this.current = current;
        this.next = next;
        this.price = price;
        this.type = type;
    }
}
