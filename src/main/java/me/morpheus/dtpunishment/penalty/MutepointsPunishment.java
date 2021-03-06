package me.morpheus.dtpunishment.penalty;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import me.morpheus.dtpunishment.configuration.MainConfig;
import me.morpheus.dtpunishment.configuration.Punishment;
import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.utils.Util;

@Singleton
public class MutepointsPunishment {

	private MainConfig mainConfig;

	private DataStore dataStore;

	private BanpointsPunishment banPunish;

	private Logger logger;

	private Server server;

	@Inject
	public MutepointsPunishment(MainConfig mainConfig, DataStore dataStore, BanpointsPunishment banPunish,
			Logger logger, Server server) {
		this.mainConfig = mainConfig;
		this.dataStore = dataStore;
		this.banPunish = banPunish;
		this.logger = logger;
		this.server = server;
	}

	public void check(UUID uuid, int amount) {

		User user = Util.getUser(uuid).get();

		Punishment punishment = mainConfig.punishments.getApplicableMutepointsPunishment(amount);

		if (punishment == null) {
			logger.info(String.format("Could not find punishment for %d mutepoints", amount));
			return;
		}

		if (punishment.banpoints > 0) {
			dataStore.addBanpoints(uuid, punishment.banpoints);
			banPunish.check(uuid, dataStore.getBanpoints(uuid));
		}

		Instant expiration = Instant.now().plus(punishment.length.duration);

		dataStore.mute(uuid, expiration);

		Text message = Util.withWatermark(TextColors.RED,
				String.format("%s has been muted for %s for exceeding %d mutepoint(s)", user.getName(),
						Util.durationToString(punishment.length.duration), punishment.threshold));

		server.getConsole().sendMessage(message);

		for (Player pl : server.getOnlinePlayers()) {
			pl.sendMessage(message);
		}

		if (user.isOnline()) {
			user.getPlayer().get()
					.sendMessage(Util.withWatermark(TextColors.RED,
							String.format("You have been muted for %s for exceeding %d points",
									Util.durationToString(punishment.length.duration), punishment.threshold)));
		}
	}
}
