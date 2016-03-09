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
        for (ShopContainer shopContainer : shopMap.get(worldUUID)) {
            if (shopContainer.isShop(location)) {
                return Optional.of(shopContainer);
            }
        }
        return Optional.empty();
    }

    public Optional<ShopContainer> getShop(Player player) {
        return getShop(player.getWorld().getUniqueId(), player.getLocation().getBlockPosition());
    }

    public void addShop(UUID worldUUID, ShopContainer shopContainter){
        Set<ShopContainer> shopContainers = shopMap.getOrDefault(worldUUID, new HashSet<>());
        shopContainers.add(shopContainter);
    }

    public Optional<ShopContainer> removeShop(UUID worldUUID, Vector3i location){
        Optional<ShopContainer> shopOptional = getShop(worldUUID, location);
        if(shopOptional.isPresent()){
            shopMap.get(worldUUID).remove(shopOptional.get());
        }
        return shopOptional;
    }
}
