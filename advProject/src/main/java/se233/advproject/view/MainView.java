package se233.advproject.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import se233.advproject.Data;
import se233.advproject.Launcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class MainView implements Initializable {
    enum outPutFileType {
        jpg,
        png
    }
    private outPutFileType outType = outPutFileType.jpg;
    // variables
    @FXML
    private ListView filesListView;
    @FXML
    private ImageView PreviewImageView;
    @FXML
    private Label FileStatusLabel;
    private Stage stage;
    private Scene scene;
    private Parent root;
    //end var
    private HashMap<String, String> fileMap = new HashMap<>();
    Data data = Data.getInstance();
    /// inherited
    // List view
    // file dropping
    ////
    // init data
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initListView();
    }
    //
    // list view handlers
    @FXML
    public void handleDragOver(DragEvent event){
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
    }
    @FXML void handleDragDropped(DragEvent event) throws IOException {
        List<File> files = event.getDragboard().getFiles();
        // files handling
        boolean uploadSuccess = false;
        int failCount = 0;
        for (int i = 0; i < files.size(); i++) {
            File f = files.get(i);
            if (f.getName().contains("png") || (f.getName().contains("jpg"))) {
                System.out.println(f.getName() + " is png/jpg, uploading");
                filesListView.getItems().add(f.getName());
                data.addFiles(f);
                uploadSuccess = true;
            } else if (f.getName().contains("zip")) {
                System.out.println("handling" + f.getPath() + "...");
                /// handle zip
                boolean zipHandlingSuccess = handleZipFile(f);
                if (zipHandlingSuccess) {
                    uploadSuccess = true;
                } else {
                    failCount++;
                }
            } else {
                System.out.println(f.getName() + " is not png/jpg, excluding");
                failCount++;
            }
        }
        if (uploadSuccess) data.setListView(filesListView);
        // update label
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
    public void handleClickPreview(MouseEvent event){
        try {
            if(filesListView.getItems().size() > 0) {
                int selectedIndex = filesListView.getSelectionModel().getSelectedIndex();
                data.setListViewSelecting(selectedIndex);
                Image img = new Image(new FileInputStream(data.getFiles().get(selectedIndex)));
                PreviewImageView.setImage(img);
            }
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
            root = FXMLLoader.load(Launcher.class.getResource("main-view.fxml"));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e){
            System.out.println(e);
        }
    }
    @FXML
    public void handleOutputSelectionBox(){
        //
    }

    /// method
    public void initListView(){
        if (data.getListView() != null) {
            try {
                filesListView.getItems().setAll(data.getListView().getItems());
                Image img = null;
                img = new Image(new FileInputStream(data.getFiles().get(data.getListViewSelecting())));
                PreviewImageView.setImage(img);
            } catch (FileNotFoundException e) {
                System.out.println(e);
            }
        }
    }
}
