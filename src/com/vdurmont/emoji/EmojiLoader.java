package com.vdurmont.emoji;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.parser.JSONParser;
import org.json.parser.ParseException;

/**
 * Loads the emojis from a JSON database.
 *
 * @author Vincent DURMONT [vdurmont@gmail.com]
 */
public class EmojiLoader {

    /**
     * No need for a constructor, all the methods are static.
     */
    private EmojiLoader() {
    }

    /**
     * Loads a JSONArray of emojis from an InputStream, parses it and returns
     * the associated list of {@link com.vdurmont.emoji.Emoji}s
     *
     * @param stream the stream of the JSONArray
     *
     * @return the list of {@link com.vdurmont.emoji.Emoji}s
     * @throws IOException if an error occurs while reading the stream or
     * parsing the JSONArray
     */
    public static List<Emoji> loadEmojis(File stream) throws IOException, ParseException {
        JSONParser jp = new JSONParser();

        JSONArray emojisJSON = (JSONArray) jp.parse(new FileReader(stream));
        List<Emoji> emojis = new ArrayList<Emoji>(emojisJSON.size());
        for (int i = 0; i < emojisJSON.size(); i++) {
            Emoji emoji = buildEmojiFromJSON((JSONObject) emojisJSON.get(i));
            if (emoji != null) {
                emojis.add(emoji);
            }
        }
        return emojis;
    }

    private static String inputStreamToString(
            InputStream stream
    ) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = new InputStreamReader(stream, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        String read;
        while ((read = br.readLine()) != null) {
            sb.append(read);
        }
        br.close();
        return sb.toString();
    }

    protected static Emoji buildEmojiFromJSON(
            JSONObject json
    ) throws UnsupportedEncodingException {
        if (!json.containsKey("emoji")) {
            return null;
        }

        byte[] bytes = json.get("emoji").toString().getBytes("UTF-8");
        String description = null;
        if (json.containsKey("description")) {
            description = (String) json.get("description");
        }
        boolean supportsFitzpatrick = false;
        if (json.containsKey("supports_fitzpatrick")) {
            supportsFitzpatrick = (Boolean) json.get("supports_fitzpatrick");
        }
        List<String> aliases = jsonArrayToStringList((JSONArray) json.get("aliases"));
        List<String> tags = jsonArrayToStringList((JSONArray) json.get("tags"));
        return new Emoji(description, supportsFitzpatrick, aliases, tags, bytes);
    }

    private static List<String> jsonArrayToStringList(JSONArray array) {
        List<String> strings = new ArrayList<String>(array.size());
        for (int i = 0; i < array.size(); i++) {
            strings.add((String) array.get(i));
        }
        return strings;
    }
}
