package io.github.zerthick.playershopsrpg.cmd;

import io.github.zerthick.playershopsrpg.cmd.cmdexecutors.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

public class PlayerShopsRPGCommandRegister {

    private PluginContainer container;

    public PlayerShopsRPGCommandRegister(PluginContainer container) {
        this.container = container;
    }

    public void registerCmds() {

        // shop browse
        CommandSpec shopBrowseCommand = CommandSpec.builder()
                .description(Text.of("Browses the shop you are currently standing in"))
                .permission("playershopsrpg.command.browse")
                .executor(new ShopBrowseExecutor(container))
                .build();

        // shop destroy
        CommandSpec shopDestroyCommand = CommandSpec.builder()
                .description(Text.of("Destroys the shop you are currently standing in"))
                .permission("playershopsrpg.command.destroy")
                .executor(new ShopDestroyExecutor(container))
                .build();

        // shop create <Name>
        CommandSpec shopCreateCommand = CommandSpec.builder()
                .description(Text.of("Creates a shop in the region selected by shop select command"))
                .permission("playershopsrpg.command.create")
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("ShopName")))
                .executor(new ShopCreateExecutor(container))
                .build();

        // shop select
        CommandSpec shopSelectCommmand = CommandSpec.builder()
                .description(Text.of("Selects a region to create a shop"))
                .permission("playershopsrpg.command.select")
                .arguments(GenericArguments.optional(GenericArguments.choices(Text.of("SelectionType"), ShopSelectExecutor.selectChoices())))
                .executor(new ShopSelectExecutor(container))
                .build();

        // shop
        CommandSpec shopCommand = CommandSpec.builder()
                .permission("playershopsrpg.command.help")
                .executor(new ShopExecutor(container))
                .child(shopSelectCommmand, "select")
                .child(shopCreateCommand, "create")
                .child(shopDestroyCommand, "destroy")
                .child(shopBrowseCommand, "browse")
                .build();

        Sponge.getGame().getCommandManager().register(container.getInstance().get(), shopCommand, "shop");
    }
}
