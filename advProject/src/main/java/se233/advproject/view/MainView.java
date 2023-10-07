package se233.advproject.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import se233.advproject.Data;
import se233.advproject.Launcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainView implements Initializable {
    // selection box
    @FXML
    private ChoiceBox outputSelectionBox;
    enum outPutFileType {
        jpg, png
    }
    private outPutFileType outType = outPutFileType.png;
    // variables
    @FXML
    private ListView filesListView;
    @FXML
    private ImageView PreviewImageView;
    @FXML
    private Label FileStatusLabel;
    private File selectedDirectory;
    private Stage stage;
    private Scene scene;
    private Parent root;
    Data data = Data.getInstance();
    // init data
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initListView();
    }
    // handling dragboards
    @FXML
    public void handleDragOver(DragEvent event){
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
    }
    @FXML
    void handleDragDropped(DragEvent event) throws IOException {
        List<File> files = event.getDragboard().getFiles();
        // Use Stream to filter valid files
        List<File> validFiles = files.stream()
                .filter(file -> file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".zip"))
                .collect(Collectors.toList());
        // getting fail counts
        int originalSize = files.size();
        int validSize = validFiles.size();
        int failCount = originalSize - validSize;
        // handle zip files
        for (File f : validFiles) {
            if (f.getName().endsWith(".zip")) {
                boolean zipHandlingSuccess = handleZipFile(f);
                if (!zipHandlingSuccess) {
                    failCount++;
                }
            } else {
                filesListView.getItems().add(f.getName());
                data.addFiles(f);
            }
        }
        if (validSize > 0) data.setListView(filesListView);

        // Update label
        if (failCount == 0) {
            FileStatusLabel.setText("Files are all uploaded successfully !");
            FileStatusLabel.setTextFill(Color.GREEN);
        } else {
            FileStatusLabel.setText("!!! latest file upload contain " + failCount + " non png/jpg !!!");
            FileStatusLabel.setTextFill(Color.RED);
        }
    }
    // handle zip method
    private boolean handleZipFile(File zipFile) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    // Process each file entry within the zip file
                    String entryName = entry.getName();
                    // Check if the entry is a PNG or JPG file
                    if (entryName.endsWith(".png") || entryName.endsWith(".jpg")) {
                        System.out.println("Extracting and handling " + entryName + " from the zip file");

                        // Read the contents of the entry into a byte array
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        byte[] entryData = outputStream.toByteArray();
                        // Convert the byte array to a BufferedImage
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(entryData);
                        BufferedImage image = ImageIO.read(inputStream);
                        // save the image as a PNG file
                        File temp = new File(entryName);
                        ImageIO.write(image, "PNG", temp);
                        // Pass the written image to addFiles method
                        filesListView.getItems().add(entryName);
                        data.addFiles(temp);
                        temp.deleteOnExit();
                    } else {
                        System.out.println("Skipping " + entryName + " as it is not a PNG or JPG file");
                    }
                }
            }
            return true; // Handling was successful
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Handling failed
        }
    }
    //
    @FXML
    public void handleClickPreview(){
        try {
            if(filesListView.getItems().size() > 0) {
                int selectedIndex = filesListView.getSelectionModel().getSelectedIndex();
                selectedIndex = (selectedIndex == -1)? 0 : selectedIndex;
                System.out.println("listview selection at " + selectedIndex);
                data.setListViewSelecting(selectedIndex);
                Image img = new Image(new FileInputStream(data.getFiles().get(selectedIndex)));
                PreviewImageView.setImage(img);
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Invalid index selected: " + e.getMessage());
        } catch (Exception e){
            System.out.println(e);
        }
    }
    // method for buttons
    @FXML
    public void switchSceneToWatermark(ActionEvent event){
        try {
            root = FXMLLoader.load(Launcher.class.getResource("watermark-view.fxml"));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    @FXML
    public void switchSceneToResize(ActionEvent event) {
        try {
            root = FXMLLoader.load(Launcher.class.getResource("resize-view.fxml"));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e){
            System.out.println(e);
        }
    }

    // not in main view methods
    @FXML
    public void handleBackButton(ActionEvent event){
        try { // back to main
            root = FXMLLoader.load(Objects.requireNonNull(Launcher.class.getResource("main-view.fxml")));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /// method

    public String getOutputFileType(){
        if(outPutFileType.jpg == outType) return "jpg";
        return "png";
    }
    public void initListView(){
        try {
            if (data.getListView() != null) {
                filesListView.getItems().setAll(data.getListView().getItems());
                Image img = new Image(new FileInputStream(data.getFiles().get(data.getListViewSelecting())));
                PreviewImageView.setImage(img);
            }
        } catch (NullPointerException e) {
            System.out.println("there is an UI component not being initialized : " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
    }
    public void initChoiceBox(){
        // Populate the ChoiceBox
        outputSelectionBox.getItems().addAll(outPutFileType.values());
        outputSelectionBox.setValue(outType); // Default values
        // listener for change detection
        outputSelectionBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                outType = (outPutFileType) newVal;
            }
        });
    }

}
