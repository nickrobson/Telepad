package xyz.nickr.telepad.util;

import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import pro.zackpollard.telegrambot.api.user.User;
import xyz.nickr.telepad.TelepadBot;

/**
 * Caches usernames to user IDs.
 *
 * @author Nick Robson
 */
public class UserCache {

    public static final String USER_CACHE_FILENAME = "tgusers.cache";

    private final Map<String, Long> usernamesToIds;
    private final Map<Long, String> idsToUsernames;

    public UserCache() {
        this.usernamesToIds = new ConcurrentHashMap<>();
        this.idsToUsernames = new ConcurrentHashMap<>();

        File file = new File(USER_CACHE_FILENAME);

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject object = TelepadBot.GSON.fromJson(reader, JsonObject.class);
                object.entrySet().forEach(e -> store(e.getKey(), e.getValue().getAsLong()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        new Thread(new PeriodicSaveThread(true)).start();

        Runtime.getRuntime().addShutdownHook(new Thread(new PeriodicSaveThread(false)));
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

    @AllArgsConstructor
    private class PeriodicSaveThread implements Runnable {

        private final boolean periodic;

        @Override
        public void run() {
            do {
                Map<String, Long> usernamesToIds = new ConcurrentHashMap<>(UserCache.this.usernamesToIds);
                try (FileWriter writer = new FileWriter(USER_CACHE_FILENAME)) {
                    JsonObject object = new JsonObject();
                    usernamesToIds.forEach(object::addProperty);
                    TelepadBot.GSON.toJson(object, writer);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (periodic) {
                    try {
                        Thread.sleep(10 * 60 * 1000); // 10 minutes
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (periodic);
        }
    }
}
