package se233.advproject.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ResizeView extends MainView implements Initializable {
    //
    enum resizeMode {
        pct, w, h,
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
    // selection box
    @FXML
    private ChoiceBox outputSelectionBox;
    @Override
    public void initialize(URL url, ResourceBundle resources) {
        initListView();
        initChoiceBox();
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
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        File selectedDirectory = directoryChooser.showDialog(pctBox.getScene().getWindow());

        if (selectedDirectory != null) {
            String outputPath = selectedDirectory.getAbsolutePath();
            if (data.getFiles() != null) {
                int randomValue = (int) (Math.random() * 1000);
                AtomicInteger step = new AtomicInteger(randomValue);
                // setup multithreading
                int numThreads = 4;
                ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
                for (int i = 0; i < numThreads; i++) {
                    final int threadIndex = i;
                    Runnable task = null;
                    switch (selectedMode) {
                        case pct:
                            task = () -> {
                                for (int j = threadIndex; j < data.getFiles().size(); j += numThreads) {
                                    File file = data.getFiles().get(j);
                                    try {
                                        // file setup
                                        String thmbPath = outputPath + File.separator + "resized_" + getNoType(file) +
                                                step.getAndIncrement() + "." + getOutputFileType();
                                        System.out.println("writing resized file to : " + thmbPath);
                                        // write
                                        Thumbnails.of(file)
                                                .scale(pctSlider.getValue() * .01)
                                                .outputFormat(getOutputFileType())
                                                .toFile(new File(thmbPath));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            break;
                        case w:
                            task = () -> {
                                for (int j = threadIndex; j < data.getFiles().size(); j += numThreads) {
                                    File file = data.getFiles().get(j);
                                    try {
                                        // file setup
                                        String thmbPath = outputPath + File.separator + "resized_" + getNoType(file) +
                                                step.getAndIncrement() + "." + getOutputFileType();
                                        System.out.println("writing resized file to : " + thmbPath);
                                        // make image to compare size
                                        Image img;
                                        try {
                                            img = new Image(new FileInputStream(file));
                                        } catch (FileNotFoundException e) {
                                            throw new RuntimeException(e);
                                        }
                                        int ow = (int) img.getWidth();
                                        // calculation
                                        double ratio = Double.parseDouble(wTextField.getText()) / img.getWidth();
                                        int w_buffer = (int) (Double.parseDouble(wTextField.getText()));
                                        int h_buffer = (int) (img.getHeight() * ratio);
                                        // create thumbnail
                                        Thumbnails.of(file)
                                                .size(w_buffer, h_buffer)
                                                .outputFormat(getOutputFileType())
                                                .toFile(new File(thmbPath));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            break;
                        case h:
                            task = () -> {
                                for (int j = threadIndex; j < data.getFiles().size(); j += numThreads) {
                                    File file = data.getFiles().get(j);
                                    try {
                                        // file setup
                                        String thmbPath = outputPath + File.separator + "resized_" + getNoType(file) +
                                                step.getAndIncrement() + "." + getOutputFileType();
                                        System.out.println("writing resized file to : " + thmbPath);
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
                                        Thumbnails.of(file)
                                                .size(w_buffer, h_buffer)
                                                .outputFormat(getOutputFileType())
                                                .toFile(new File(thmbPath));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            break;
                    }
                    if (task != null) {
                        executorService.submit(task);
                    }
                }
                // done successfully
                executorService.shutdown();
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Done resize and exported");
                a.setContentText("files are all Successfully exported into : " + outputPath);
                a.show();
                // console
                System.out.println("Files are all Successfully exported into : " + outputPath);
            }
        }
    }
    private String getNoType(File file) {
        String fileName = file.getName();
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return fileName;  // return the original filename if there's no dot
        }
        return fileName.substring(0, lastDot);
    }
}
