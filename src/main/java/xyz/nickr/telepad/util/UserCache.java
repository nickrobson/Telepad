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

    public String getUsername(long id) {
        return idsToUsernames.get(id);
    }

    public Long getUserId(String username) {
        return usernamesToIds.get(username);
    }

    public void store(User user) {
        store(user.getUsername(), user.getId());
    }

    public void store(String username, long userId) {
        usernamesToIds.put(username, userId);
        idsToUsernames.put(userId, username);
    }

}
