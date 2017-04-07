package me.morpheus.dtpunishment.commands.banpoints;

import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.utils.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.UUID;

public class CommandBanpointsRemove implements CommandExecutor {

    private final DTPunishment main;

    public CommandBanpointsRemove(DTPunishment main){
        this.main = main;
    }


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<User> user = Util.getUser(args.<String>getOne("player").get());
        if (!user.isPresent()) {
            src.sendMessage(Util.getWatermark().append(Text.of(args.<String>getOne("player").get() + " never joined your server ")).build());
            return CommandResult.empty();
        }

        UUID uuid = user.get().getUniqueId();
        String name = user.get().getName();
        int actual = main.getDatastore().getBanpoints(uuid);
        int amount = args.<Integer>getOne("amount").get();

        if (actual - amount < 0) amount = actual;
        int total = actual - amount;

        main.getDatastore().removeBanpoints(uuid, amount);
        main.getDatastore().finish();

        if (user.get().isOnline()) {
            user.get().getPlayer().get().sendMessage(Util.getWatermark().append(Text.of(TextColors.AQUA, amount + " banpoints have been removed; you now have " + total)).build());
        }

    	Text adminMessage = Util.getWatermark().append(
				Text.of(TextColors.AQUA, String.format("%s has removed %d banpoint(s) from %s; they now have %d", src.getName(), amount, name, total))).build();

    	if(src instanceof ConsoleSource)
    		src.sendMessage(adminMessage);
    	
        for (Player p : Sponge.getServer().getOnlinePlayers()) {
            if (p.hasPermission("dtpunishment.staff.notify") || p.getPlayer().get() == src) {
                p.sendMessage(adminMessage);
            }
        }

        return CommandResult.success();

    }
}
