package net.quepierts.endfieldpanorama.earlywindow;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class MinecraftProfile implements Resource {

    private static final Logger LOGGER = LoggerFactory.getLogger("EARLYDISPLAY");
    private static final Gson GSON = new Gson();

    private final   String              username;
    private final   ExecutorService     executor;
    private final   Future<?>           future;

    @Getter
    private         boolean             done;
    @Getter
    private         boolean             slim;
    @Getter
    private         byte[]              skin;

    public MinecraftProfile(String username) {
        this.username   = username;
        this.executor   = Executors.newSingleThreadExecutor();
        this.future     = this.executor.submit(() -> {
            try {
                this.request();
                this.done = true;
            } catch (Exception e) {
                LOGGER.error("Malformed URL", e);
            }
        });
    }

    private void request() throws MalformedURLException {
        if (this.done) {
            return;
        }

        var cache           = localCache(username);
        if (EarlyResourceLoader.hasFile(cache)) {
            try (var in = EarlyResourceLoader.loadFile(cache)) {
                int first = in.read();
                this.slim = first == 1;

                // read all bytes to skins
                this.skin = new byte[in.available()];
                in.read(skin);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }

        // get uuid
        var uuidJson        = httpGet("https://api.mojang.com/users/profiles/minecraft/" + username);

        String uuid;
        if (uuidJson == null) {
            return;
        }

        uuid = uuidJson.get("id").getAsString();

        // Get profile
        var profile     = httpGet("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);

        if (profile == null) {
            return;
        }

        var properties  = profile.getAsJsonArray("properties");
        for (var property : properties) {
            var object          = property.getAsJsonObject();
            var name            = object.get("name").getAsString();

            if (name.equals("textures")) {
                var value       = object.get("value");
                this.decodeSkin(value.getAsString());
                break;
            }
        }
    }

    private void decodeSkin(String base64) throws MalformedURLException {
        var decoded     = Base64.getDecoder().decode(base64);
        var json        = GSON.fromJson(new String(decoded), JsonObject.class);

        var texture     = json.getAsJsonObject("textures");
        var skin        = texture.getAsJsonObject("SKIN");
        var meta        = skin.getAsJsonObject("metadata");

        this.slim       = meta.has("model") && meta.get("model").getAsString().equals("slim");
        var skinUrlStr  = skin.get("url").getAsString();

        this.loadSkin(skinUrlStr);
    }

    private void loadSkin(String link) throws MalformedURLException {
        var url = URI.create(link).toURL();

        try (var in     = url.openStream();
             var out    = new ByteArrayOutputStream()
        ) {
            byte[] buffer = new byte[1024];
            int length;

            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }

            this.skin = out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // write cache
        if (this.skin == null || this.skin.length == 0) {
            return;
        }

        var cache   = localCache(username);
        var file    = EarlyResourceLoader.toFile(cache);
        file.getParentFile().mkdirs();
        try (var out = new FileOutputStream(file)) {
            int first = this.slim ? 1 : 0;
            out.write(first);
            out.write(this.skin);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonObject httpGet(String link) {
        try {
            var url = new URL(link);
            var connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                return GSON.fromJson(reader, JsonObject.class);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private static String localCache(String username) {
        return "skins/" + username + ".cache";
    }

    @Override
    public void free() {
        this.executor.shutdown();
    }
}
