package se233.advproject.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResizeView extends MainView implements Initializable {

    private outPutFileType outType = outPutFileType.jpg;
    enum resizeMode {
        pct,
        w,
        h,
    }

    // handle scrolls
    @FXML
    private TextField pctBox;
    @FXML
    private TextField wTextField;
    @FXML
    private TextField hTextField;
    @FXML
    private Slider pctSlider;
    // toggle mode
    @FXML
    private ToggleButton percentageModeToggle,
            widthModeToggle, heightModeToggle;
    @FXML
    private Pane scaleSettingPane, widthSettingPane, heightSettingPane;
    // value
    private resizeMode selectedMode = resizeMode.pct;
    @Override
    public void initialize(URL url, ResourceBundle resources) {
        initListView();
        // slider to text
        pctBox.textProperty().bind(pctSlider.valueProperty().asString("%.2f"));
        // set toggle group
        ToggleGroup toggleGroup = new ToggleGroup();
        percentageModeToggle.setSelected(true); // initially on
        toggleGroup.selectedToggleProperty().addListener((obs, oldTog, newTog) -> {
            if (newTog == null) {
                oldTog.setSelected(true);
            }
        });
        //
        percentageModeToggle.setToggleGroup(toggleGroup);
        percentageModeToggle.setOnAction(event -> {
            selectedMode = resizeMode.pct;
            scaleSettingPane.setVisible(true);
            widthSettingPane.setVisible(false);
            heightSettingPane.setVisible(false);
        });
        //
        widthModeToggle.setToggleGroup(toggleGroup);
        widthModeToggle.setOnAction(event -> {
            selectedMode = resizeMode.w;
            scaleSettingPane.setVisible(false);
            widthSettingPane.setVisible(true);
            heightSettingPane.setVisible(false);
        });
        //
        heightModeToggle.setToggleGroup(toggleGroup);
        heightModeToggle.setOnAction(event -> {
            selectedMode = resizeMode.h;
            scaleSettingPane.setVisible(false);
            widthSettingPane.setVisible(false);
            heightSettingPane.setVisible(true);
        });
    }
    // resize and output on confirm
    @FXML
    public void handleConfirmation() throws IOException {
        if (data.getFiles() != null) {
            // open multithreading
            ExecutorService executorService = Executors.newFixedThreadPool(4);
            switch (selectedMode) {
                case pct -> {
                    for (int i = 0; i < data.getFiles().size(); i++) { // iterate all files
                        // files setup for handling
                        File file = data.getFiles().get(i);
                        String thmbPath = "target" + File.separator + "output" + File.separator + "resized_" + file.getName();
                        System.out.println("writing resized file to : " + thmbPath);
                        File thmbFile = new File(thmbPath);
                        // make image to compare size
                        Image img;
                        try {
                            img = new Image(new FileInputStream(file));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        // create thumbnail
                        Thumbnails.of(file).scale(pctSlider.getValue() * .01).toFile(thmbFile);
                    }
                }
                case w -> {
                    for (int i = 0; i < data.getFiles().size(); i++) { // iterate all files
                        // files setup for handling
                        File file = data.getFiles().get(i);
                        String thmbPath = "target" + File.separator + "output" + File.separator + "resized_" + file.getName();
                        System.out.println("writing resized file to : " + thmbPath);
                        File thmbFile = new File(thmbPath);
                        // make image to compare size
                        Image img;
                        try {
                            img = new Image(new FileInputStream(file));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        // calculation
                        double ratio = Double.parseDouble(wTextField.getText()) / img.getWidth();
                        int w_buffer = (int) (Double.parseDouble(wTextField.getText()));
                        int h_buffer = (int) (img.getHeight() * ratio);
                        // create thumbnail
                        Thumbnails.of(file).size(w_buffer, h_buffer).toFile(thmbFile);
                    }
                }
                case h -> {
                    for (int i = 0; i < data.getFiles().size(); i++) { // iterate all files
                        // files setup for handling
                        File file = data.getFiles().get(i);
                        String thmbPath = "target" + File.separator + "output" + File.separator + "resized_" + file.getName();
                        System.out.println("writing resized file to : " + thmbPath);
                        File thmbFile = new File(thmbPath);
                        // make image to compare size
                        Image img;
                        try {
                            img = new Image(new FileInputStream(file));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        // calculation
                        double ratio = Double.parseDouble(hTextField.getText()) / img.getHeight();
                        int w_buffer = (int) (img.getWidth() * ratio);
                        int h_buffer = (int) (Double.parseDouble(hTextField.getText()));
                        // create thumbnail
                        Thumbnails.of(file).size(w_buffer, h_buffer).toFile(thmbFile);
                    }
                }
            }
            // close multithreading
            executorService.shutdown();
        }
    }

}
