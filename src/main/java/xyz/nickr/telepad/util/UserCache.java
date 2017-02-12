package xyz.nickr.telepad.util;

import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import pro.zackpollard.telegrambot.api.user.User;
import xyz.nickr.telepad.TelepadBot;

/**
 * Caches usernames to user IDs.
 *
 * @author Nick Robson
 */
public class UserCache {

    private final Map<String, Long> usernamesToIds;
    private final Map<Long, String> idsToUsernames;

    public UserCache() {
        this.usernamesToIds = new HashMap<>();
        this.idsToUsernames = new HashMap<>();

        try (FileReader reader = new FileReader("tgusers.cache")) {
            JsonObject object = TelepadBot.GSON.fromJson(reader, JsonObject.class);
            object.entrySet().forEach(e -> store(e.getKey(), e.getValue().getAsLong()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try (FileWriter writer = new FileWriter("tgusers.cache")) {
                JsonObject object = new JsonObject();
                this.usernamesToIds.forEach(object::addProperty);
                TelepadBot.GSON.toJson(object, writer);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }));
    }

    /**
     * Gets a cached username from a user ID.
     *
     * @param id The user ID
     *
     * @return The username, or null if not cached.
     */
    public String getUsername(long id) {
        return idsToUsernames.get(id);
    }

    /**
     * Gets a cached user ID from a username.
     *
     * @param username The username
     *
     * @return The user ID, or null if not cached.
     */
    public Long getUserId(String username) {
        if (!username.startsWith("@"))
            username = "@" + username;
        return usernamesToIds.get(username);
    }

    /**
     * Stores a User into the cache.
     *
     * @param user The user.
     */
    public void store(User user) {
        store(user.getUsername(), user.getId());
    }

    /**
     * Stores a username and ID into the cache.
     *
     * @param username The username
     * @param userId The user ID
     */
    public void store(String username, long userId) {
        if (!username.startsWith("@"))
            username = "@" + username;
        usernamesToIds.put(username, userId);
        idsToUsernames.put(userId, username);
    }

}
