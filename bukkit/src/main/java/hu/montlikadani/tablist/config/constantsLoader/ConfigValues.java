package hu.montlikadani.tablist.config.constantsLoader;

import java.time.format.DateTimeFormatter;
import java.util.*;

import hu.montlikadani.api.IPacketNM;
import hu.montlikadani.tablist.utils.ServerVersion;
import hu.montlikadani.tablist.utils.reflection.ComponentParser;
import org.bukkit.configuration.ConfigurationSection;

import hu.montlikadani.tablist.Objects;
import hu.montlikadani.tablist.config.CommentedConfig;

public final class ConfigValues {

	private static boolean logConsole, perWorldPlayerList, fakePlayers, countFakePlayersToOnlinePlayers, removeGrayColorFromTabInSpec, ignoreVanishedPlayers,
			countVanishedStaff, hidePlayerFromTabAfk, afkStatusEnabled, afkStatusShowInRightLeftSide, afkStatusShowPlayerGroup, afkSortLast, pingFormatEnabled,
			tpsFormatEnabled, prefixSuffixEnabled, useDisabledWorldsAsWhiteList, syncPluginsGroups, hideGroupInVanish, preferPrimaryVaultGroup, assignGlobalGroup,
			followNameTagVisibility, useLPWeightToOrderGroupsFirst;

	private static TimeZone timeZone;

	private static String afkFormatYes, afkFormatNo, customObjectSetting;

	private static DateTimeFormatter timeFormat, dateFormat;

	private static Objects.ObjectTypes objectType = Objects.ObjectTypes.PING;
	private static IPacketNM.ObjectiveFormat objectiveFormat;
	private static Object objectiveFormatText;

	private static List<String> tpsColorFormats, pingColorFormats, groupsDisabledWorlds, healthObjectRestricted, objectsDisabledWorlds;

	private static int tpsDigits, groupsRefreshInterval, objectRefreshInterval;

	private static double tpsObservationValue;

	public static final List<List<String>> PER_WORLD_LIST_NAMES = new java.util.ArrayList<>();

	public static void loadValues(CommentedConfig c) {
		org.bukkit.configuration.file.YamlConfigurationOptions options = c.options();
		options.copyDefaults(true);

		try {
			options.parseComments(false);
		} catch (NoSuchMethodError ignore) {
		}

		PER_WORLD_LIST_NAMES.clear();

		c.addComment("tps-performance-observation-value",
				"This option monitors server performance. If the server's TPS is less than the set value,",
				"TabList will cancels all currently running schedulers to improve server performance.", " ",
				"TabList will not restart these schedulers (ie animations, group updates, etc.),",
				"so you have to do it manually, by reconnecting to the server or by reloading the plugin.", " ",
				"At values below 8-5, TabList is almost unable to stop its own running processes,",
				"as the server is already under a very heavy load.", " ",
				"The values should be between 5-18", "If the value is below 5 or above 18, the default value will be 16.0",
				"To disable this feature set to -1");

		c.addComment("fake-players", "Fake players that can be added to the player list.");
		c.addComment("fake-players.count-fake-players-to-online-players", "Count the added fake players to the %online-players% placeholder?");
		c.addComment("remove-gray-color-from-tab-in-spectator",
				"If enabled, the gray color will not appear to other players when the player's game mode is spectator.",
				"The gray color will only show for the spectator player.");
		c.addComment("ignore-vanished-players-in-online-players",
				"If enabled, vanished players in %online-players% placeholder won't be counted.",
				"Requires Essentials, SuperVanish, PremiumVanish or CMI plugin");
		c.addComment("count-vanished-staffs",
				"If enabled, vanished players with \"tablist.onlinestaff\" permission added will be counted in %staff-online% placeholder",
				"Requires Essentials, SuperVanish, PremiumVanish or CMI plugin");
		c.addComment("hide-player-from-tab-when-afk",
				"Hide player from player list when a player is AFK?", "Requires Essentials or CMI plugin");

		c.addComment("per-world-player-list", "Different player list in different world.");
		c.addComment("per-world-player-list.world-groups", "You can specify worlds, which will share the same list of players");
		c.addComment("per-world-player-list.world-groups.example1", "The key name, can be anything");

		c.addComment("placeholder-format", "Placeholders formatting");
		c.addComment("placeholder-format.afk-status", "Applied when the player's afk state changed, this will result to include",
				"a prefix or suffix after or before the player name");
		c.addComment("placeholder-format.afk-status.show-in-right-or-left-side", "Should the AFK format display in right or left side?",
				"true - displays in right side (after the player name)",
				"false - displays in left side (before the player name)");
		c.addComment("placeholder-format.afk-status.show-player-group", "Also display player's group together with the afk status");
		c.addComment("placeholder-format.afk-status.format-yes", "Format when the player is AFK.");
		c.addComment("placeholder-format.afk-status.format-no", "Format when the player is not AFK.");
		c.addComment("placeholder-format.afk-status.sort-last", "Sort AFK players to the bottom of the player list?");
		c.addComment("placeholder-format.time.time-zone", "Time zones: https://www.mkyong.com/java/java-display-list-of-timezone-with-gmt/",
				"Or google it: \"what is my time zone\"");
		c.addComment("placeholder-format.time.use-system-zone", "Use system default time zone instead of searching for that?");
		c.addComment("placeholder-format.time.time-format", "Formats/examples: https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html",
				"Format of %server-time% placeholder.");
		c.addComment("placeholder-format.time.date-format", "Format of %date% placeholder.");
		c.addComment("placeholder-format.ping", "Ping color format for %ping% placeholder.");
		c.addComment("placeholder-format.ping.formats", "https://github.com/montlikadani/TabList/wiki/Ping-or-tps-formatting");
		c.addComment("placeholder-format.tps", "TPS color format for %tps% placeholder.");
		c.addComment("placeholder-format.tps.formats", "https://github.com/montlikadani/TabList/wiki/Ping-or-tps-formatting");
		c.addComment("placeholder-format.tps.size", "The amount of decimal to display after \".\" in %tps% placeholder",
				"The size should be higher than 0",
				"Example: 3 = 19.14");

		c.addComment("change-prefix-suffix-in-tablist", "Enable changing of prefix & suffix in player list?");
		c.addComment("change-prefix-suffix-in-tablist.refresh-interval", "Refresh interval in server ticks.",
				"Set to 0 to disable refreshing the groups automatically.");
		c.addComment("change-prefix-suffix-in-tablist.disabled-worlds", "Disable groups in these worlds.");
		c.addComment("change-prefix-suffix-in-tablist.disabled-worlds.use-as-whitelist", "Use the list as whitelist?");
		c.addComment("change-prefix-suffix-in-tablist.sync-plugins-groups-with-tablist", "Automatically add groups from another plugins to the tablist groups.yml on every reload?",
				"If a plugin does not support Vault, it will not be added.");
		c.addComment("change-prefix-suffix-in-tablist.hide-group-when-player-vanished", "Hide player's group in player list when the player is vanished?",
				"Requires Essentials, SuperVanish, PremiumVanish or CMI plugin");
		c.addComment("change-prefix-suffix-in-tablist.assign-global-group-to-normal", "Do you want to assign global group to normal groups?",
				"true - \"globalGroupPrefix + normalGroupPrefix\"", "false - \"normalGroupPrefix\"");
		c.addComment("change-prefix-suffix-in-tablist.prefer-primary-vault-group", "Prefer player's primary Vault group when assigning tablist group from groups.yml?",
				"true - player will be assigned their primary vault group where possible", "false - applies one of the group from the permission plugin");
		c.addComment("change-prefix-suffix-in-tablist.use-luckperms-weight-to-order-groups-to-first-place",
				"If true, groups will be sorted using LuckPerms weight if those are set.",
				"Groups with higher weight set, will appear first to the player.",
				"If the weights are not set or this option is disabled, the first group with higher priority will appear first.");
		c.addComment("change-prefix-suffix-in-tablist.followNameTagVisibility", "Follow the name tag visibility for players to show the name tag above player or not,",
				"depending if a scoreboard team with visibility 'hidden' is exist.",
				"true - Follows the name tag visibility and hides if there is a scoreboard team created with visibility 'hidden'",
				"false - Always shows the name tag above player");

		c.addComment("tablist-object-type", "Tablist objective types",
				"Shows your current health (with life indicator), your current ping or any NUMBER placeholder",
				"after the player's name (before the ping indicator).");
		c.addComment("tablist-object-type.type", "Types:",
				"none - disables tablist objects",
				"ping - player's ping",
				"health - player's health",
				"custom - one of the number-ending placeholder");
		c.addComment("tablist-object-type.refresh-interval", "How often should it refresh the values in seconds?",
				"Set to 0 to disable refreshing automatically");
		c.addComment("tablist-object-type.custom-value", "Custom placeholder - accepts only number-ending placeholders, like %level%");

		if (ServerVersion.current().isHigherOrEqual(ServerVersion.v1_20_4)) {
			c.addComment("tablist-object-type.number-format", "The format of this objective number what to display");
			c.addComment("tablist-object-type.number-format.type", "The format type of this objective to display",
					"Can be NONE (default), FIXED and STYLED");
			c.addComment("tablist-object-type.number-format.format", "The format of this objective, each format type is different,",
					" ",
					"none - shows as a yellow number (default)",
					"fixed - a unique text displayed instead of number without styling",
					"styled - changes the coloration of the number (use full color/formatting names 'green;bold;italic' or hexadecimal '#123456')",
					" ",
					"with 'styled' format you can specify only 1 color and each formatting names 1 time, separate with ';' character");
		}

		c.addComment("tablist-object-type.disabled-worlds", "In these worlds the objects will not be displayed");

		c.addComment("check-update", "Checks for plugin updates after server start");
		c.addComment("download-updates", "Download new updates to the updates folder and automatically apply when the server starts.",
				"This only works if the \"check-update\" option is enabled.");
		c.addComment("logconsole", "Can the plugin log debug messages to console? Sometimes its useful");

		tpsObservationValue = c.get("tps-performance-observation-value", -1.0);

		if (tpsObservationValue != -1.0 && (tpsObservationValue < 5.0 || tpsObservationValue > 18.0)) {
			tpsObservationValue = 16.0;
		}

		fakePlayers = c.get("fake-players.enabled", c.getBoolean("enable-fake-players"));
		countFakePlayersToOnlinePlayers = c.get("fake-players.count-fake-players-to-online-players", false);
		removeGrayColorFromTabInSpec = c.get("remove-gray-color-from-tab-in-spectator", false);
		ignoreVanishedPlayers = c.get("ignore-vanished-players-in-online-players", false);
		countVanishedStaff = c.get("count-vanished-staffs", true);
		hidePlayerFromTabAfk = c.get("hide-player-from-tab-when-afk", false);
		perWorldPlayerList = c.get("per-world-player-list.enabled", c.getBoolean("per-world-player-list"));

		ConfigurationSection section = c.getConfigurationSection("per-world-player-list.world-groups");

		if (section == null) {
			section = c.createSection("per-world-player-list.world-groups", new java.util.HashMap<String, List<String>>() {
				{
					put("exampleGroup2", Arrays.asList("exampleWorld2", "exampleAnotherWorld2"));
					put("example1", Arrays.asList("exampleWorld", "exampleAnotherWorld"));
				}
			});
		} else {
			c.set(section.getCurrentPath(), section);
		}

		if (perWorldPlayerList) {
			for (String key : section.getKeys(false)) {
				List<String> list = section.getStringList(key);

				if (!list.isEmpty()) {
					PER_WORLD_LIST_NAMES.add(list);
				}
			}
		}

		afkStatusEnabled = c.get("placeholder-format.afk-status.enable", false);
		afkStatusShowInRightLeftSide = c.get("placeholder-format.afk-status.show-in-right-or-left-side", true);
		afkStatusShowPlayerGroup = c.get("placeholder-format.afk-status.show-player-group", true);
		afkSortLast = c.get("placeholder-format.afk-status.sort-last", false);
		pingFormatEnabled = c.get("placeholder-format.ping.enable", true);
		tpsFormatEnabled = c.get("placeholder-format.tps.enable", true);
		prefixSuffixEnabled = c.get("change-prefix-suffix-in-tablist.enable", false);
		useDisabledWorldsAsWhiteList = c.get("change-prefix-suffix-in-tablist.disabled-worlds.use-as-whitelist", false);
		syncPluginsGroups = c.get("change-prefix-suffix-in-tablist.sync-plugins-groups-with-tablist", true);
		hideGroupInVanish = c.get("change-prefix-suffix-in-tablist.hide-group-when-player-vanished", false);
		assignGlobalGroup = c.get("change-prefix-suffix-in-tablist.assign-global-group-to-normal", false);
		preferPrimaryVaultGroup = c.get("change-prefix-suffix-in-tablist.prefer-primary-vault-group", false);
		useLPWeightToOrderGroupsFirst = c.get("change-prefix-suffix-in-tablist.use-luckperms-weight-to-order-groups-to-first-place", false);
		followNameTagVisibility = c.get("change-prefix-suffix-in-tablist.followNameTagVisibility", false);

		afkFormatYes = Global.replaceToUnicodeSymbol(c.get("placeholder-format.afk-status.format-yes", "&7 [AFK]&r "));
		afkFormatNo = Global.replaceToUnicodeSymbol(c.get("placeholder-format.afk-status.format-no", ""));

		if (c.get("placeholder-format.time.use-system-zone", false)) {
			timeZone = TimeZone.getTimeZone(java.time.ZoneId.systemDefault());
		} else {
			timeZone = TimeZone.getTimeZone(c.get("placeholder-format.time.time-zone", "GMT0"));
		}

		String old = "placeholder-format.time.time-format.format";
		String last = c.getString(old, null);

		if (last != null) {
			c.set(old, null); // Need to remove as this still exists so it will returns memorySection
			c.set("placeholder-format.time.time-format", last);
		}

		String tf = c.get("placeholder-format.time.time-format", "mm:HH");

		if (!tf.isEmpty()) {
			try {
				timeFormat = DateTimeFormatter.ofPattern(tf);
			} catch (IllegalArgumentException ignore) {
			}
		}

		if ((last = c.getString(old = "placeholder-format.time.date-format.format", null)) != null) {
			c.set(old, null);
			c.set("placeholder-format.time.date-format", last);
		}

		if (!(tf = c.get("placeholder-format.time.date-format", "dd/MM/yyyy")).isEmpty()) {
			try {
				dateFormat = DateTimeFormatter.ofPattern(tf);
			} catch (IllegalArgumentException ignore) {
			}
		}

		if ((customObjectSetting = c.getString("tablist-object-type.object-settings.custom.value", null)) == null) {
			c.addComment("tablist-object-type.custom-value", "The unique integer variable to be displayed.");
			customObjectSetting = c.get("tablist-object-type.custom-value", "%level%");
		}

		healthObjectRestricted = c.getStringList("tablist-object-type.object-settings.health.restricted-players"); // TODO deprecated
		objectsDisabledWorlds = c.get("tablist-object-type.disabled-worlds", Collections.singletonList("testingWorld"));

		if (!c.getBoolean("tablist-object-type.enable", true)) {
			c.set("tablist-object-type.type", "none");

			objectType = Objects.ObjectTypes.NONE;
		} else {
			try {
				objectType = Objects.ObjectTypes.valueOf(c.get("tablist-object-type.type", "ping").toUpperCase(Locale.ENGLISH));
			} catch (IllegalArgumentException e) {
				objectType = Objects.ObjectTypes.NONE;
			}

			if (objectType != Objects.ObjectTypes.HEALTH && ServerVersion.current().isHigherOrEqual(ServerVersion.v1_20_4)) {
				try {
					objectiveFormat = IPacketNM.ObjectiveFormat.valueOf(c.get("tablist-object-type.number-format.type",
							"none").toUpperCase(Locale.ENGLISH));
				} catch (IllegalArgumentException ex) {
					objectiveFormat = IPacketNM.ObjectiveFormat.NONE;
				}

				String format = c.get("tablist-object-type.number-format.format", "green;bold");

				if (objectiveFormat == IPacketNM.ObjectiveFormat.STYLED) {
					objectiveFormatText = format.split(";");
				} else if (objectiveFormat == IPacketNM.ObjectiveFormat.FIXED) {
					objectiveFormatText = ComponentParser.asComponent(format);
				}
			}
		}

		tpsColorFormats = c.get("placeholder-format.tps.formats", Arrays.asList("&a%tps% > 18.0", "&6%tps% == 16.0", "&c%tps% < 16.0"));
		pingColorFormats = c.get("placeholder-format.ping.formats", Arrays.asList("&a%ping% <= 200", "&6%ping% >= 200", "&c%ping% > 500"));

		tpsDigits = 10;

		int size = c.get("placeholder-format.tps.size", 2);

		if (size > 2) {
			size -= 2;

			for (int i = 0; i < size; i++) {
				tpsDigits *= 10;
			}
		}

		groupsDisabledWorlds = c.get("change-prefix-suffix-in-tablist.disabled-worlds.list", Collections.singletonList("myWorldWithUpper"));
		groupsRefreshInterval = c.get("change-prefix-suffix-in-tablist.refresh-interval", 30);

		if ((objectRefreshInterval = c.get("tablist-object-type.refresh-interval", 3)) < 1) {
			objectRefreshInterval = 0;
		} else {
			objectRefreshInterval *= 20;
		}

		// Just set if missing
		c.get("check-update", true);
		c.get("download-updates", false);

		logConsole = c.get("logconsole", true);

		// Here comes the options that removed
		c.set("hook", null);
		c.set("tablist-object-type.enable", null);
		c.set("change-prefix-suffix-in-tablist.hide-group-when-player-afk", null);
		c.set("placeholder-format.memory-bar", null);
		c.set("hide-players-from-tablist", null);
		if (healthObjectRestricted.isEmpty()) {
			c.set("tablist-object-type.object-settings.health.restricted-players", null);
		}

		c.save();
	}

	public static boolean isLogConsole() {
		return logConsole;
	}

	public static boolean isPerWorldPlayerList() {
		return perWorldPlayerList;
	}

	public static boolean isFakePlayers() {
		return fakePlayers;
	}

	public static boolean isCountFakePlayersToOnlinePlayers() {
		return countFakePlayersToOnlinePlayers;
	}

	public static boolean isRemoveGrayColorFromTabInSpec() {
		return removeGrayColorFromTabInSpec;
	}

	public static boolean isIgnoreVanishedPlayers() {
		return ignoreVanishedPlayers;
	}

	public static boolean isCountVanishedStaff() {
		return countVanishedStaff;
	}

	public static boolean isHidePlayerFromTabAfk() {
		return hidePlayerFromTabAfk;
	}

	public static boolean isAfkStatusEnabled() {
		return afkStatusEnabled;
	}

	public static boolean isAfkStatusShowInRightLeftSide() {
		return afkStatusShowInRightLeftSide;
	}

	public static boolean isAfkStatusShowPlayerGroup() {
		return afkStatusShowPlayerGroup;
	}

	public static boolean isAfkSortLast() {
		return afkSortLast;
	}

	public static String getAfkFormatYes() {
		return afkFormatYes;
	}

	public static String getAfkFormatNo() {
		return afkFormatNo;
	}

	public static TimeZone getTimeZone() {
		return timeZone;
	}

	public static DateTimeFormatter getTimeFormat() {
		return timeFormat;
	}

	public static DateTimeFormatter getDateFormat() {
		return dateFormat;
	}

	public static boolean isPingFormatEnabled() {
		return pingFormatEnabled;
	}

	public static boolean isTpsFormatEnabled() {
		return tpsFormatEnabled;
	}

	public static boolean isPrefixSuffixEnabled() {
		return prefixSuffixEnabled;
	}

	public static int getGroupsRefreshInterval() {
		return groupsRefreshInterval;
	}

	public static boolean isUseDisabledWorldsAsWhiteList() {
		return useDisabledWorldsAsWhiteList;
	}

	public static boolean isSyncPluginsGroups() {
		return syncPluginsGroups;
	}

	public static boolean isHideGroupInVanish() {
		return hideGroupInVanish;
	}

	public static boolean isPreferPrimaryVaultGroup() {
		return preferPrimaryVaultGroup;
	}

	public static Objects.ObjectTypes getObjectType() {
		return objectType;
	}

	public static int getObjectRefreshInterval() {
		return objectRefreshInterval;
	}

	public static String getCustomObjectSetting() {
		return customObjectSetting;
	}

	public static int getTpsDigits() {
		return tpsDigits;
	}

	public static List<String> getTpsColorFormats() {
		return tpsColorFormats;
	}

	public static List<String> getPingColorFormats() {
		return pingColorFormats;
	}

	public static List<String> getGroupsDisabledWorlds() {
		return groupsDisabledWorlds;
	}

	public static List<String> getHealthObjectRestricted() {
		return healthObjectRestricted;
	}

	public static List<String> getObjectsDisabledWorlds() {
		return objectsDisabledWorlds;
	}

	public static boolean isAssignGlobalGroup() {
		return assignGlobalGroup;
	}

	public static boolean isFollowNameTagVisibility() {
		return followNameTagVisibility;
	}

	public static double getTpsObservationValue() {
		return tpsObservationValue;
	}

	public static boolean isUseLPWeightToOrderGroupsFirst() {
		return useLPWeightToOrderGroupsFirst;
	}

	public static IPacketNM.ObjectiveFormat objectiveFormat() {
		return objectiveFormat;
	}

	public static Object getObjectiveFormatText() {
		return objectiveFormatText;
	}
}
