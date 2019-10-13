package cursemodpacksearch;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class HyperlinkCell implements Callback<TableColumn<Modpack, Hyperlink>, TableCell<Modpack, Hyperlink>> {

    @Override
    public TableCell<Modpack, Hyperlink> call(TableColumn<Modpack, Hyperlink> arg) {
        return new TableCell<Modpack, Hyperlink>() {
            @Override
            protected void updateItem(Hyperlink item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(item);
            }
        };

    }
}