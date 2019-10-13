package cursemodpacksearch;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class JSONLib {

    private static final JSONParser parser = new JSONParser();

    static String readStringFromURL(String requestURL) throws IOException {
        InputStream is;
        is = new URL(requestURL).openStream();
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    static Object readJSONFromURL(String requestURL) throws IOException, ParseException {
        return parseJson(readStringFromURL(requestURL));
    }

    static JSONObject[] filterJSON(String jsonStr, Predicate<Object> predicate) throws ParseException {
        JSONArray jsonArray = (JSONArray) parser.parse(jsonStr);
        JSONObject[] result = ((ArrayList<JSONObject>) jsonArray.stream().filter(predicate).collect(Collectors.toList())).toArray(new JSONObject[0]);
        return result;
    }

    static Object parseJson(String jsonStr) throws ParseException {
        return parser.parse(jsonStr);
    }

    static JSONArray getArrayWithKeys(String[] keys, JSONArray arr) {
        JSONArray result = new JSONArray();
        JSONObject jObj;
        for (Object o : arr) {
            jObj = (JSONObject) o;
            JSONObject objToAdd = new JSONObject();
            for (String key : keys) {
                objToAdd.put(key, jObj.get(key));
            }
            result.add(objToAdd);
        }
        return result;
    }

}
