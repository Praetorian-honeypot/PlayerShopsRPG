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

package io.github.zerthick.playershopsrpg.cmd.cmdexecutors.shop;

import io.github.zerthick.playershopsrpg.cmd.cmdexecutors.AbstractCmdExecutor;
import io.github.zerthick.playershopsrpg.shop.Shop;
import io.github.zerthick.playershopsrpg.shop.ShopContainer;
import io.github.zerthick.playershopsrpg.shop.ShopTransactionResult;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShopSetUnlimitedExecutor extends AbstractCmdExecutor {

    public ShopSetUnlimitedExecutor(PluginContainer pluginContainer) {
        super(pluginContainer);
    }

    public static Map<String, String> selectChoices() {
        Map<String, String> selectChoices = new HashMap<>();
        selectChoices.put("money", "money");
        selectChoices.put("stock", "stock");
        return selectChoices;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if (src instanceof Player) {
            Player player = (Player) src;

            Optional<String> selectTypeOptional = args.getOne(Text.of("SelectionType"));
            Optional<Boolean> booleanArgumentOptional = args.getOne(Text.of("BooleanArgument"));

            if (selectTypeOptional.isPresent() && booleanArgumentOptional.isPresent()) {
                String selectType = selectTypeOptional.get();
                Boolean booleanArg = booleanArgumentOptional.get();

                Optional<ShopContainer> shopContainerOptional = shopManager.getShop(player);
                if (shopContainerOptional.isPresent()) {
                    ShopContainer shopContainer = shopContainerOptional.get();
                    Shop shop = shopContainer.getShop();
                    ShopTransactionResult transactionResult;

                    switch (selectType) {
                        case "money":
                            transactionResult = shop.setUnlimitedMoney(player, booleanArg);
                            break;
                        case "stock":
                            transactionResult = shop.setUnlimitedStock(player, booleanArg);
                            break;
                        default:
                            transactionResult = new ShopTransactionResult("Unknown shop attribute!");
                    }

                    if (transactionResult != ShopTransactionResult.SUCCESS) {
                        player.sendMessage(ChatTypes.CHAT, Text.of(TextColors.RED, transactionResult.getMessage()));
                    }
                } else {
                    player.sendMessage(ChatTypes.CHAT, Text.of(TextColors.RED, "You are not in a shop!"));
                }

                return CommandResult.success();
            }
        }

        src.sendMessage(Text.of("You cannot set shop attributes from the console!"));
        return CommandResult.success();
    }
}
