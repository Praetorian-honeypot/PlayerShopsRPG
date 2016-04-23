/*
 * Copyright (C) 2016  Zerthick
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

package io.github.zerthick.playershopsrpg.shop;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.entity.living.player.Player;

import java.util.*;

public class ShopManager {

    private Map<UUID, Set<ShopContainer>> shopMap;

    public ShopManager(Map<UUID, Set<ShopContainer>> shopMap) {
        this.shopMap = shopMap;
    }

    public Optional<ShopContainer> getShop(UUID worldUUID, Vector3i location){
        for (ShopContainer shopContainer : shopMap.getOrDefault(worldUUID, new HashSet<>())) {
            if (shopContainer.isShop(location)) {
                return Optional.of(shopContainer);
            }
        }
        return Optional.empty();
    }

    public Optional<ShopContainer> getShop(Player player) {
        return getShop(player.getWorld().getUniqueId(), player.getLocation().getBlockPosition());
    }

    public void addShop(UUID worldUUID, ShopContainer shopContainer) {
        Set<ShopContainer> shopContainers = shopMap.getOrDefault(worldUUID, new HashSet<>());
        shopContainers.add(shopContainer);
        shopMap.put(worldUUID, shopContainers);
    }

    public Optional<ShopContainer> removeShop(UUID worldUUID, Vector3i location){
        Optional<ShopContainer> shopOptional = getShop(worldUUID, location);
        if(shopOptional.isPresent()){
            shopMap.get(worldUUID).remove(shopOptional.get());
        }
        return shopOptional;
    }

    public Optional<ShopContainer> removeShop(Player player) {
        return removeShop(player.getWorld().getUniqueId(), player.getLocation().getBlockPosition());
    }

    public Map<UUID, Set<ShopContainer>> getShopMap() {
        return shopMap;
    }
}
