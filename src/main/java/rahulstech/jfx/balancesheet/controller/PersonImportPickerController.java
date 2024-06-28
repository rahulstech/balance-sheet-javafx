package rahulstech.jfx.balancesheet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import rahulstech.jfx.balancesheet.json.model.Person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings("ALL")
public class PersonImportPickerController extends Controller {

    private static final Person NONE = new Person();
    static {
        NONE.set_id(null);
        NONE.setPerson_name("None");
        NONE.setDue("0");
    }

    @FXML
    private Button selectAllButton;

    @FXML
    private ListView<Person> listView;

    private final List<Person> people;

    public PersonImportPickerController(List<Person> people) {
        if (null == people) {
            this.people = Collections.singletonList(NONE);
        }
        else {
            this.people = new ArrayList<>();
            this.people.add(NONE);
            this.people.addAll(people);
        }
    }

    @Override
    protected void onInitialize(ResourceBundle resources) {
        initializeAccountList();
        configureSelectAllButton();
    }

    private void initializeAccountList() {
        listView.getItems().addAll(people);
        listView.setCellFactory(param -> new ListCell<Person>() {
            @Override
            protected void updateItem(Person item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getPerson_name());
                }
            }
        });

        listView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
    }

    private void configureSelectAllButton() {
        selectAllButton.setOnAction(e->{
            if (listView.getSelectionModel().getSelectedItems().size() == people.size()) {
                listView.getSelectionModel().clearSelection();
            }
            else {
                listView.getSelectionModel().selectAll();
            }
        });
    }

    public List<Person> getAllSelectedPeople() {
        return new ArrayList<>(listView.getSelectionModel().getSelectedItems());
    }
}
