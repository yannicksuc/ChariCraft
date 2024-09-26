package fr.lordfinn.charicraft;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class StreamersConfigLoader {

    public List<Streamer> loadStreamers(String filePath) {
        Gson gson = new Gson();
        Type streamerListType = new TypeToken<List<Streamer>>() {}.getType();

        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, streamerListType);
        } catch (IOException e) {
            ChariCraft.getInstance().getLogger().severe("Failed to load streamers configuration.");
            e.printStackTrace();
            return null;
        }
    }
}
