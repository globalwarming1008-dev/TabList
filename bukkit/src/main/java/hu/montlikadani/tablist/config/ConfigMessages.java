package hu.montlikadani.tablist.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.tablist.utils.Util;

public final class ConfigMessages {

	private final File file;

	public ConfigMessages(File folder) {
		file = new File(folder, "messages.yml");
	}

	@SuppressWarnings("unchecked")
	public void createAndLoad() {
		boolean fileWasExisted = file.exists();

		if (!fileWasExisted) {
			try {
				if (!file.createNewFile()) {
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		FileConfiguration messagesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
		boolean saveRequired = false;

		if (fileWasExisted && messagesConfig.get("unknownsub-command") != null) {
			messagesConfig.set("unknownsub-command", null);
			saveRequired = true;
		}

		for (MessageKeys key : MessageKeys.values()) {
			if (key.type == String.class) {
				String str;

				if (!fileWasExisted || (str = messagesConfig.getString(key.path, null)) == null) {
					messagesConfig.set(key.path, str = (String) key.defaultValue);
					saveRequired = true;
				}

				key.value = Util.applyTextFormat(str);
			} else if (key.type == List.class) {
				if (!fileWasExisted) {
					key.value = key.defaultValue;
					messagesConfig.set(key.path, key.value);
					saveRequired = true;
					continue;
				}

				List<String> list;

				try {
					list = (List<String>) messagesConfig.getList(key.path, null);
				} catch (ClassCastException ex) {
					list = null;
				}

				if (list == null) {
					key.value = key.defaultValue;
					messagesConfig.set(key.path, key.value);
					saveRequired = true;
				} else {
					key.value = list;
				}
			}
		}

		if (saveRequired) {
			try {
				messagesConfig.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String get(MessageKeys key, Object... variables) {
		String text = (String) key.value;

		if (!text.isEmpty()) {
			for (int i = 0; i < variables.length; i += 2) {
				final int a = i;
				text = Global.replace(text, String.valueOf(variables[i]), () -> String.valueOf(variables[a + 1]));
			}
		}

		return text;
	}

	public static List<String> getList(MessageKeys key, Object... variables) {
		if (variables.length == 0) {
			return (List<String>) key.value;
		}

		List<String> list = new java.util.ArrayList<>((List<String>) key.value);

		list.replaceAll(one -> {
			if (one.isEmpty()) {
				return one;
			}

			for (int y = 0; y < variables.length; y += 2) {
				final int a = y;
				one = Global.replace(one, String.valueOf(variables[y]), () -> String.valueOf(variables[a + 1]));
			}

			return one;
		});

		return list;
	}

	public enum MessageKeys {

		RELOAD_CONFIG("&aReloaded!"),
		NO_PERMISSION("&cYou don't have permission for that!&7 (%perm%)"),
		NO_CONSOLE("&cThis command '&7/%command%&c' can only be in-game."),

		SET_GROUP_META_COULD_NOT_BE_EMPTY("set-group", "&cThe text could not be empty!"),
		SET_GROUP_META_SET("set-group", "&aYou have set meta for&e %team%&a, with this:&r %meta%"),
		SET_GROUP_PRIORITY_MUST_BE_NUMBER("set-group", "&cThe priority must be a number!"),
		SET_GROUP_NOT_FOUND("set-group", "&cThis team&7 %team%&c not found."),
		SET_GROUP_REMOVED("set-group", "&cTeam with name&7 %team%&c removed."),

		TOGGLE_CONSOLE_USAGE("toggle", "&cUse: &7/%command% toggle <player/all>"),
		TOGGLE_ENABLED("toggle", "&aTab is turned on!"),
		TOGGLE_DISABLED("toggle", "&cTab is turned off!"),
		TOGGLE_PLAYER_NOT_FOUND("toggle", "&cThis player&7 %player%&c not found!"),

		FAKE_PLAYER_ADDED("fake-player", "&aFake player&7 %name%&a added"),
		FAKE_PLAYER_REMOVED("fake-player", "&cFake player&7 %name%&c removed"),
		FAKE_PLAYER_ALREADY_ADDED("fake-player", "&cFake player&7 %name%&c already added."),
		FAKE_PLAYER_NOT_EXISTS("fake-player", "&cFake player with this name not exists."),
		FAKE_PLAYER_NO_FAKE_PLAYER("fake-player", "&cThere is no fake player created."),
		FAKE_PLAYER_DISABLED("fake-player", "&cFake player option is disabled in config."),
		FAKE_PLAYER_PING_CAN_NOT_BE_LESS("fake-player", "&cThe ping amount should be a number and greater than&e %amount%"),
		FAKE_PLAYER_LIST("fake-player", "&cFake players&7 (&e%amount%&7):", "&r%fake-players%");

		public final Class<?> type;

		private String path;
		private Object value;

        private final Object defaultValue;

		MessageKeys(String value) {
			this(null, value);
		}

		MessageKeys(String mainSection, String value) {
			path(mainSection);
			this.value = defaultValue = value;
			type = String.class;
		}

		MessageKeys(String mainSection, String... defaultValue) {
			path(mainSection);
			value = this.defaultValue = Arrays.asList(defaultValue);
			type = List.class;
		}

		private void path(String mainSection) {
			if (mainSection != null) {
				path = mainSection + '.' + name().substring(mainSection.length());
			} else {
				path = name();
			}

			path = hu.montlikadani.tablist.Global.replaceFrom(path, 0, "_", "", 1)
					.replace('_', '-').toLowerCase(java.util.Locale.ENGLISH);
		}
	}
}
