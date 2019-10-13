package cursemodpacksearch;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class CurseModpackSearch extends Application {

    private static final int PAGE_SIZE = 30;
    private static final int DEF_RESULT_COUNT = 30;

    private JSONArray mcModpackCategories;
    private Stage stage;
    private ObservableList<Modpack> list;
    private TableView<Modpack> table;
    private TextArea numberArea;
    private HashMap<Long, Boolean> categoryCheckboxStates = new HashMap<>();
    private Thread searchThread;
    private Button searchButton;

    @Override
    public void start(Stage stage) throws IOException, ParseException {
        this.stage = stage;
        fillMCCategories();
        Modpack.mapInit(mcModpackCategories);
        windowInit();
    }

    private void windowInit() {
        stage.setTitle("Curse Modpack Search");
        Group group = new Group();
        Scene scene = new Scene(group, 800, 980);
        scene.getStylesheets().add(CurseModpackSearch.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        VBox leftColumn = new VBox(10);
        leftColumn.getChildren().add(checkBoxInit());
        HBox numberAndSearch = new HBox(10);
        numberAndSearch.getChildren().add(numberAreaInit());
        numberAndSearch.getChildren().add(searchButtonInit());
        leftColumn.getChildren().add(numberAndSearch);
        leftColumn.setPadding(new Insets(10, 0, 0, 5));
        HBox hb = new HBox(50);
        hb.getChildren().add(leftColumn);
        hb.getChildren().add(tableInit());
        group.getChildren().add(hb);
        stage.show();
    }

    private TableView<Modpack> tableInit() {
        TableView table = new TableView(){public void requestFocus() {}};
        table.setPrefSize(500, 950);
        ObservableList<Modpack> list = FXCollections.observableArrayList();
        this.list = list;
        table.setItems(list);
        TableColumn<Modpack, Hyperlink> urlCol = new TableColumn<>("Name");
        urlCol.setCellValueFactory(new PropertyValueFactory<>("hyperlink"));
        TableColumn<Modpack, HBox> categCol = new TableColumn<>("Categories");
        categCol.setCellValueFactory(cdf -> new ReadOnlyObjectWrapper<>(cdf.getValue()
                .getImages()));
        urlCol.setCellFactory(new HyperlinkCell());
        table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY);
        urlCol.setMaxWidth(1f * Integer.MAX_VALUE * 50);
        categCol.setMaxWidth(1f * Integer.MAX_VALUE * 50);
        table.getColumns().addAll(urlCol, categCol);
        this.table = table;
        return table;
    }

    private TextArea numberAreaInit() {
        TextArea ta = new TextArea();
        ta.setPadding(new Insets(0));
        ta.setMaxSize(100, 10);
        ta.setText(String.valueOf(DEF_RESULT_COUNT));
        ta.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,4}")) {
                ta.setText(oldValue);
            }
        });
        numberArea = ta;
        return ta;
    }

    private Button searchButtonInit() {
        Button b = new Button("Search");
        b.setPrefWidth(70);
        b.setOnAction(event -> {
            if (searchThread == null || !searchThread.isAlive()) {
                b.setText("Stop");
                searchThread = new Thread(() -> {
                    try {
                        search();
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    Platform.runLater(() -> searchButton.setText("Search"));
                });
                searchThread.start();
            } else {
                searchThread.interrupt();
                b.setText("Search");
            }
        });
        searchButton = b;
        return b;
    }

    private void search() throws IOException, ParseException {
        int numberOfModpacks = Integer.parseInt(numberArea.getText());
        if (numberOfModpacks == 0) return;
        HashMap<Long, Boolean> states = (HashMap<Long, Boolean>) categoryCheckboxStates.clone();
        states.values().removeIf(Objects::isNull);
        List<Map.Entry<Long, Boolean>> trueEntries = states.entrySet().stream().filter(Map.Entry::getValue).collect(Collectors.toList());
        if (trueEntries.size() > 5) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Too many selected");
                alert.setHeaderText(null);
                alert.setContentText("You selected too many tags (max 5 turned on).");
                alert.showAndWait();
            });
            return;
        }
        Long firstId;
        if (trueEntries.size() == 0) {
            firstId = 0L;
        } else {
            firstId = trueEntries.get(0).getKey();
            states.remove(firstId);
        }
        list.clear();
        table.refresh();
        SearchLib.createReaderFor(firstId);
        while (list.size() < numberOfModpacks) {
            JSONObject jObj = SearchLib.getModpacksFromCategory();
            if (Thread.interrupted()) return;
            if (jObj == null) break;
            if (states.entrySet().stream()
                    .allMatch(entry ->
                            entry.getValue().equals(SearchLib.getCategoryIds(jObj).contains(entry.getKey())))) {
                String[] categories = (String[]) ((JSONArray) jObj.get("categories")).stream().map(obj -> ((JSONObject) obj).get("name")).toArray(String[]::new);
                try {
                    list.add(new Modpack((String) jObj.get("name"), (Long) jObj.get("id"), new URL((String) jObj.get("websiteUrl")), categories));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        if (list.size() == 0) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No modpacks found");
                alert.setHeaderText(null);
                alert.setContentText("No modpack was found. Refine your search categories.");
                alert.showAndWait();
            });
        }
    }

    private VBox checkBoxInit() {
        VBox checkBoxes = new VBox();
        for (Object o : mcModpackCategories) {
            JSONObject jObj = (JSONObject) o;
            HBox hb = new HBox();
            CheckBox cb1 = new CheckBox();
            CheckBox cb2 = new CheckBox();
            categoryCheckboxStates.put((Long) jObj.get("id"), null);
            cb2.getStyleClass().add("cross-box");
            cb2.setText((String) jObj.get("name"));
            cb1.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue.equals(false)) {
                    categoryCheckboxStates.put((Long) jObj.get("id"), true);
                    cb1.setSelected(true);
                    cb2.setSelected(false);
                } else {
                    cb1.setSelected(false);
                    categoryCheckboxStates.put((Long) jObj.get("id"), null);
                }
            });
            cb2.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue.equals(false)) {
                    categoryCheckboxStates.put((Long) jObj.get("id"), false);
                    cb2.setSelected(true);
                    cb1.setSelected(false);
                } else {
                    cb2.setSelected(false);
                    categoryCheckboxStates.put((Long) jObj.get("id"), null);
                }
            });
            hb.getChildren().add(cb1);
            hb.getChildren().add(cb2);
            checkBoxes.getChildren().add(hb);
        }
        return checkBoxes;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void fillMCCategories() throws IOException, ParseException {
        if (mcModpackCategories == null) {
            mcModpackCategories = JSONLib.getArrayWithKeys(new String[]{"id", "name", "avatarUrl"}, (JSONArray) JSONLib.readJSONFromURL("https://addons-ecs.forgesvc.net/api/v2/category/section/4471"));
        }
    }

}
