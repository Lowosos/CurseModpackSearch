package cursemodpacksearch;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class SearchLib {
    static InputStream is;
    static BufferedReader reader;

    static void createReaderFor(Long id) throws IOException {
        is = new URL("https://addons-ecs.forgesvc.net/api/v2/addon/search?categoryId=" + id + "&gameId=432&index=0&sectionId=4471&sort=1").openStream();
        reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8.toString()));
    }

    static JSONObject getModpacksFromCategory() throws ParseException, IOException {
        reader.skip(1);
        int count = 0;
        int intch;
        StringBuilder result = new StringBuilder();
        while ((intch = reader.read()) != -1) {
            char ch = (char) intch;
            result.append(ch);
            if (ch == '{') count++;
            else if (ch == '}') count--;
            if (count == 0 && result.length() > 1) {
                break;
            }
        }
        if (result.length() == 0) return null;
        return (JSONObject) JSONLib.parseJson(result.toString());
    }

    static ArrayList<Long> getCategoryIds(JSONObject modpack) {
        ArrayList<Long> result = new ArrayList<>();
        for (Object o : (JSONArray) modpack.get("categories")) {
            result.add((Long) ((JSONObject) o).get("categoryId"));
        }
        return result;
    }
}
