/*
 * Copyright (C) 2017  Zerthick
 *
 * This file is part of PlayerShopsRPG.
 *
 * PlayerShopsRPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * PlayerShopsRPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PlayerShopsRPG.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.zerthick.playershopsrpg.utils.config.sql;

import com.google.common.collect.ImmutableList;
import io.github.zerthick.playershopsrpg.shop.Shop;
import io.github.zerthick.playershopsrpg.shop.ShopItem;
import io.github.zerthick.playershopsrpg.utils.config.serializers.ItemStackHOCONSerializer;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SQLDataUtil {

    public static void createTables(Logger logger) {
        try {
            createShopTable();
            createShopRegionTable();
            createShopItemTable();
            createShopManagerTable();
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    private static void createShopTable() throws SQLException {
        List<String> columns =
                ImmutableList.of("ID UUID PRIMARY KEY", "NAME VARCHAR(256)", "OWNER_ID UUID", "RENTER_ID UUID",
                        "UNLIMITED_MONEY BOOLEAN", "UNLIMITED_STOCK BOOLEAN", "TYPE VARCHAR(32)",
                        "PRICE DECIMAL", "RENT DECIMAL");
        SQLUtil.createTable("SHOP", columns);
    }

    private static void createShopRegionTable() throws SQLException {
        List<String> columns =
                ImmutableList.of("ID UUID PRIMARY KEY", "TYPE VARCHAR(32)", "DATA VARCHAR", "SHOP_ID UUID",
                        "WORLD_ID UUID", "FOREIGN KEY(SHOP_ID) REFERENCES SHOP(ID) ON DELETE CASCADE");
        SQLUtil.createTable("SHOP_REGION", columns);
    }

    private static void createShopItemTable() throws SQLException {
        List<String> columns =
                ImmutableList.of("ID UUID PRIMARY KEY", "ITEMSTACK VARCHAR", "AMOUNT INT", "MAX_AMOUNT INT",
                        "BUY_PRICE DECIMAL", "SELL_PRICE DECIMAL", "SHOP_ID UUID",
                        "FOREIGN KEY(SHOP_ID) REFERENCES SHOP(ID) ON DELETE CASCADE");
        SQLUtil.createTable("SHOP_ITEM", columns);
    }

    private static void createShopManagerTable() throws SQLException {
        List<String> columns =
                ImmutableList.of("SHOP_ID UUID", "MANAGER_ID UUID", "PRIMARY KEY(SHOP_ID, MANAGER_ID)",
                        "FOREIGN KEY(SHOP_ID) REFERENCES SHOP(ID) ON DELETE CASCADE");
        SQLUtil.createTable("SHOP_MANAGERS", columns);
    }

    private static Set<UUID> loadShopManagers(UUID shopUUID, Logger logger) {
        Set<UUID> managerSet = new HashSet<>();
        try {
            SQLUtil.select("SHOP_MANAGERS", "SHOP_ID", shopUUID.toString(), resultSet -> {
                try {
                    while (resultSet.next()) {
                        managerSet.add((UUID) resultSet.getObject("MANAGER_ID"));
                    }
                } catch (SQLException e) {
                    logger.info(e.getMessage());
                }
            });
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
        return managerSet;
    }

    private static void saveShopManagers(UUID shopUUID, Set<UUID> managerSet, Logger logger) {
        List<String> values = managerSet.stream().map(uuid -> "('" + shopUUID + "', '" + uuid.toString() + "')").collect(Collectors.toList());
        if (!values.isEmpty()) {
            try {
                SQLUtil.executeUpdate("MERGE INTO SHOP_MANAGERS VALUES" + values.stream().collect(Collectors.joining(", ")));
            } catch (SQLException e) {
                logger.info(e.getMessage());
            }
        }
    }

    private static List<ShopItem> loadShopItems(UUID shopUUID, Logger logger) {
        List<ShopItem> items = new ArrayList<>();

        try {
            SQLUtil.select("SHOP_ITEM", "SHOP_ID", shopUUID.toString(), resultSet -> {
                try {
                    while (resultSet.next()) {
                        UUID itemUUID = (UUID) resultSet.getObject("ID");
                        ItemStackSnapshot snapshot = ItemStackHOCONSerializer.deserializeSnapShot(resultSet.getString("ITEMSTACK"));
                        int itemAmount = resultSet.getInt("AMOUNT");
                        int itemMaxAmount = resultSet.getInt("MAX_AMOUNT");
                        double itemBuyPrice = resultSet.getBigDecimal("BUY_PRICE").doubleValue();
                        double itemSellPrice = resultSet.getBigDecimal("SELL_PRICE").doubleValue();
                        items.add(new ShopItem(itemUUID, snapshot, itemAmount, itemMaxAmount, itemBuyPrice, itemSellPrice));
                    }
                } catch (IOException | ObjectMappingException | SQLException e) {
                    logger.info(e.getMessage());
                }
            });
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }

        return items;
    }

    private static void saveShopItems(UUID shopUUID, List<ShopItem> items, Logger logger) {
        List<String> values = items.stream().map(shopItem -> {
            String itemID = shopItem.getShopItemUUID().toString();
            String itemStack = "";
            try {
                itemStack = ItemStackHOCONSerializer.serializeSnapShot(shopItem.getItemStackSnapShot());
            } catch (ObjectMappingException | IOException e) {
                logger.info(e.getMessage());
            }
            int amount = shopItem.getItemAmount();
            int maxAmount = shopItem.getItemMaxAmount();
            double itemBuyPrice = shopItem.getItemBuyPrice();
            double itemSellPrice = shopItem.getItemSellPrice();

            return "('" + itemID + "', '" + itemStack + "', " + amount + ", " + maxAmount + ", " + itemBuyPrice + ", "
                    + itemSellPrice + ", '" + shopUUID + "')";
        }).collect(Collectors.toList());
        if (!values.isEmpty()) {
            try {
                SQLUtil.executeUpdate("MERGE INTO SHOP_ITEM VALUES" + values.stream().collect(Collectors.joining(", ")));
            } catch (SQLException e) {
                logger.info(e.getMessage());
            }
        }
    }

    public static void saveShop(Shop shop, Logger logger) {
        String id = shop.getUUID().toString();
        String name = shop.getName();
        String ownerId = shop.getOwnerUUID().toString();
        String renterId = "";
        if (shop.getRenterUUID() != null) {
            renterId = shop.getRenterUUID().toString();
        }
        boolean unlimitedMoney = shop.isUnlimitedMoney();
        boolean unlimitedStock = shop.isUnlimitedStock();
        String type = shop.getType();
        double price = shop.getPrice();
        double rent = shop.getRent();

        try {
            SQLUtil.executeUpdate("MERGE INTO SHOP VALUES ('" + id + "', '" + name + "', '" + ownerId + "', '" +
                    renterId + "', " + unlimitedMoney + ", " + unlimitedStock + ", '" + type + "', " + price + ", " +
                    rent + ")");
            saveShopItems(shop.getUUID(), shop.getItems(), logger);
            saveShopManagers(shop.getUUID(), shop.getManagerUUIDSet(), logger);
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }
}
