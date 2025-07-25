package hu.montlikadani.tablist.utils.reflection;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.NamespacedKey;

import com.google.gson.JsonObject;

import hu.montlikadani.tablist.packets.PacketNM;
import hu.montlikadani.tablist.tablist.TabText;

public final class JsonComponent {

	public static final com.google.gson.Gson GSON = new com.google.gson.GsonBuilder().disableHtmlEscaping().create();

	private final ArrayList<JsonObject> jsonList = new ArrayList<>(10);
	private java.util.Map<String, String> fonts;

	private Object emptyJson;

	Object parseProperty(String text, List<TabText.JsonElementData> existingJson) {
		if (text.isEmpty()) {
			if (emptyJson == null) {
				emptyJson = PacketNM.NMS_PACKET.fromJson(GSON.toJson(""));
			}

			return emptyJson;
		}

		jsonList.clear();
		jsonList.trimToSize();

		text = text.replace('\u00a7', '&').replace("&#", "#").replace("&x", "#");

		int length = text.length(), index = 0;
		MColor lastColor = null;

		JsonObject obj = new JsonObject();
		StringBuilder builder = new StringBuilder(length);

		for (int i = 0; i < length; i++) {

			// Finds hex colours that may be coming from essentials (&x&f) and removes "&" character to match the correct hex colour
			int count = i + 13; // = #§a§a§e§2§a§5
			int j = i + 1;

			for (; j < count && j < length; j += 2) {
				if (text.charAt(j) != '&') {
					break;
				}
			}

			if (j == count) {
				text = Global.replaceFrom(text, i, "&", "", 6);
				length = text.length();
			}

			char charAt = text.charAt(i);

			if (charAt == '[' && existingJson != null && index < existingJson.size() && text.charAt(i + 1) == '"'
					&& text.charAt(i + 2) == '"' && text.charAt(i + 3) == ',' && text.charAt(i + 4) == '{') {
				if (builder.length() != 0) {
					obj.addProperty("text", builder.toString());
					jsonList.add(obj);
					builder = new StringBuilder();
				}

				obj = new JsonObject();
				obj.addProperty("text", "");

				TabText.JsonElementDataNew data = (TabText.JsonElementDataNew) existingJson.get(index);
				obj.add("extra", data.element);

				jsonList.add(obj);

				// GSON is escaping unicode characters \u258b to the actual char instead of leaving it as-is
				// This means that the formatting will break and also the last 4 or more json text is displayed
				// as the length of the json was changed
				// we need to avoid using unicode characters or a temporary solution
				// https://stackoverflow.com/questions/43091804/
				i += data.jsonLength - 1;
				obj = new JsonObject();

				index++;
				continue;
			}

			if (charAt == '&') {
				int next = i + 1;

				if (next < length) {
					lastColor = MColor.byCode(text.charAt(next));
				}

				if (lastColor != null) {
					if (builder.length() != 0) {
						obj.addProperty("text", builder.toString());
						jsonList.add(obj);

						obj = new JsonObject();
						builder = new StringBuilder();
					}

					// We don't need these formatting as the default colour is white
					if (lastColor != MColor.WHITE && lastColor != MColor.RESET) {
						if (lastColor.formatter) {
							obj.addProperty(lastColor.propertyName, true);
						} else {
							obj.addProperty("color", lastColor.propertyName);
						}
					}

					i = next;
				} else {
					builder.append(charAt);
				}
			} else if (charAt == '#') {
				int end = i + 7;

				if (end >= length || !validateHex(text, i + 1, end)) {
					builder.append(charAt);
				} else {
					if (builder.length() != 0) {
						obj.addProperty("text", builder.toString());
						jsonList.add(obj);
						builder = new StringBuilder();
					}

					obj = new JsonObject();
					obj.addProperty("color", text.substring(i, end));
					i += 6; // Increase loop to skip the next 6 hex digit
				}
			} else if (charAt == '{') {
				int closeIndex;
				int fromIndex = i + 10;

				if (text.regionMatches(true, i, "{gradient=", 0, 10)
						&& (closeIndex = text.indexOf('}', fromIndex)) != -1) {
					Color[] colors = new Color[2];
					int co = 0;

					for (String one : text.substring(fromIndex, closeIndex).split(":", 2)) {
						if (one.isEmpty() || one.charAt(0) != '#' || !validateHex(one, 1, one.length())) {
							closeIndex = -1;
							break;
						}

						colors[co] = Color.decode(one);
						co++;
					}

					if (co == 2) {
						int g = closeIndex + 1;
						int endIndex = text.indexOf("{/gradient}", g);

						if (endIndex == -1) {
							continue;
						}

						Color startColor = colors[0];
						Color endColor = colors[1];

						if (builder.length() != 0) {
							obj.addProperty("text", builder.toString());
							jsonList.add(obj);
							builder = new StringBuilder();
						}

						obj = new JsonObject();

						for (; g < endIndex; g++) {
							obj.addProperty("text", text.charAt(g));

							double perc = (double) g / (double) endIndex;

							// Don't know what is this but works
							// https://www.spigotmc.org/threads/470496/
							int red = (int) (startColor.getRed() + perc * (endColor.getRed() - startColor.getRed()));
							int green = (int) (startColor.getGreen() + perc * (endColor.getGreen() - startColor.getGreen()));
							int blue = (int) (startColor.getBlue() + perc * (endColor.getBlue() - startColor.getBlue()));

							// https://stackoverflow.com/questions/4801366
							obj.addProperty("color", String.format("#%06x",
									((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff)));

							if (lastColor != null && lastColor.formatter) {
								obj.addProperty(lastColor.propertyName, true);
							}

							jsonList.add(obj);
							obj = new JsonObject();
						}

						lastColor = null;
						i = endIndex + 10;
						continue;
					}
				}

				fromIndex = i + 6;

				String font = "";

				if (text.regionMatches(true, i, "{font=", 0, 6)
						&& (closeIndex = text.indexOf('}', fromIndex)) != -1) {
					if (fonts == null) {
						fonts = new java.util.HashMap<>(1);
					}

					String res = fonts.computeIfAbsent(text.substring(fromIndex, closeIndex), key -> {
						try {
							return NamespacedKey.minecraft(key).toString();
						} catch (IllegalArgumentException ignore) {
						}

						return null;
					});

					if (res != null) {
						font = res;
					}
				} else if (text.regionMatches(true, i, "{/font", 0, 6)
						&& (closeIndex = text.indexOf('}', fromIndex)) != -1) {
					font = fonts.computeIfAbsent("default", s -> NamespacedKey.minecraft(s).toString());
				} else {
					builder.append(charAt);
					continue;
				}

				if (builder.length() != 0) {
					obj.addProperty("text", builder.toString());
					jsonList.add(obj);
					builder = new StringBuilder();
				}

				obj = new JsonObject();
				obj.addProperty("font", font);
				i = closeIndex;
			} else {
				builder.append(charAt);
			}
		}

		obj.addProperty("text", builder.toString());
		jsonList.add(obj);

		return PacketNM.NMS_PACKET.fromJson("[\"\"," + Global.replaceFrom(GSON.toJson(jsonList, List.class), 0, "[", "", 1));
	}

	private boolean validateHex(String text, int start, int end) {
		for (int b = start; b < end; b++) {
			if (!Character.isLetterOrDigit(text.charAt(b))) {
				return false;
			}
		}

		return true;
	}
}
