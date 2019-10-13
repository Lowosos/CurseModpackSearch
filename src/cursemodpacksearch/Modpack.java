package cursemodpacksearch;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Modpack {

    private static HashMap<String, Image> imageMap = new HashMap<>();

    private Long id;
    private Hyperlink hyperlink;
    private URL url;
    private HashSet<String> categories;

    Modpack(String name, Long id, URL url, String[] categories) {
        this.id = id;
        this.url = url;
        hyperlink = new Hyperlink(name);
        hyperlink.setOnAction(t -> {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(this.url.toURI());
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
        hyperlink.setStyle( "-fx-alignment: CENTER;");
        this.categories = new HashSet<>(Arrays.asList(categories));
    }

    public Hyperlink getHyperlink() {
        return hyperlink;
    }

    public HBox getImages() {
        HBox container = new HBox();
        container.setSpacing(5);
        ObservableList<Node> children = container.getChildren();
        categories.stream().sorted().forEach(category -> children.add(new ImageView(imageMap.get(category))));
        container.setStyle("-fx-alignment: CENTER-LEFT;");
        return container;
    }

    static void mapInit(JSONArray categories) {
        for (Object o : categories) {
            JSONObject obj = (JSONObject) o;
            imageMap.put((String) obj.get("name"), new Image((String) obj.get("avatarUrl"), 20, 20, true, true, true));
        }
    }
}
