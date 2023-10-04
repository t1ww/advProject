package se233.advproject.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class WatermarkView extends MainView {
    /// init
    @FXML
    private TextField textContentTextfield;
    // sliders
    @FXML
    private TextField rotationBox;
    @FXML
    private TextField sizeBox;
    @FXML
    private TextField opacityBox;
    @FXML
    private TextField imgqBox;
    @FXML
    private Slider rotationSlider;
    @FXML
    private Slider sizeSlider;
    @FXML
    private Slider opacitySlider;
    @FXML
    private Slider imgqSlider;
    @FXML
    private ColorPicker colorPicker;
    // positions
    @FXML
    private ToggleButton topleftPosPicker, topcentrePosPicker, toprightPosPicker;
    @FXML
    private ToggleButton middleleftPosPicker, middlecentrePosPicker, middlerightPosPicker;
    @FXML
    private ToggleButton bottomleftPosPicker, bottomcentrePosPicker, bottomrightPosPicker;
    private Positions selectedPos = Positions.CENTER;
    // selection box
    @FXML
    private ChoiceBox outputSelectionBox;
    @FXML
    private ChoiceBox fontSelectionBox;
    //
    @Override
    public void initialize(URL url, ResourceBundle resources) {
        initListView();
        initChoiceBox();
        // font box
        /// handle sliders
        rotationBox.textProperty().bind(rotationSlider.valueProperty().asString("%.2f"));
        sizeBox.textProperty().bind(sizeSlider.valueProperty().asString("%.2f"));
        opacityBox.textProperty().bind(opacitySlider.valueProperty().asString("%.2f"));
        imgqBox.textProperty().bind(imgqSlider.valueProperty().asString("%.2f"));
        // handle pos pickers
        // set toggle group
        ToggleGroup toggleGroup = new ToggleGroup();
        middlecentrePosPicker.setSelected(true); // initially on
        toggleGroup.selectedToggleProperty().addListener((obs, oldTog, newTog) -> {
            if (newTog == null) {
                oldTog.setSelected(true);
            }
        });
        topleftPosPicker.setToggleGroup(toggleGroup);
        topcentrePosPicker.setToggleGroup(toggleGroup);
        toprightPosPicker.setToggleGroup(toggleGroup);
        middleleftPosPicker.setToggleGroup(toggleGroup);
        middlecentrePosPicker.setToggleGroup(toggleGroup);
        middlerightPosPicker.setToggleGroup(toggleGroup);
        bottomleftPosPicker.setToggleGroup(toggleGroup);
        bottomcentrePosPicker.setToggleGroup(toggleGroup);
        bottomrightPosPicker.setToggleGroup(toggleGroup);
    }
    /// handle text customization

    /// handle position
    @FXML
    public void pickTopLeft(){
        selectedPos = Positions.TOP_LEFT;
    }
    @FXML
    public void pickTopCentre(){
        selectedPos = Positions.TOP_CENTER;
    }
    @FXML
    public void pickTopRight(){
        selectedPos = Positions.TOP_RIGHT;
    }
    @FXML
    public void pickMiddleLeft(){
        selectedPos = Positions.CENTER_LEFT;
    }
    @FXML
    public void pickMiddleCentre(){
        selectedPos = Positions.CENTER;
    }
    @FXML
    public void pickMiddleRight(){
        selectedPos = Positions.CENTER_RIGHT;
    }
    @FXML
    public void pickBottomLeft(){
        selectedPos = Positions.BOTTOM_LEFT;
    }
    @FXML
    public void pickBottomCentre(){
        selectedPos = Positions.BOTTOM_CENTER;
    }
    @FXML
    public void pickBottomRight(){ selectedPos = Positions.BOTTOM_RIGHT; }
    ///

    @FXML
    public void handleConfirmation() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        File selectedDirectory = directoryChooser.showDialog(sizeBox.getScene().getWindow());

        if (selectedDirectory != null) {
            String outputPath = selectedDirectory.getAbsolutePath();
            String textData = (textContentTextfield.getText().isBlank()) ? "null" : textContentTextfield.getText();
            System.out.println("text : " + textData + " ; is being used for watermarking");
            // make watermark image
            BufferedImage watermark = stringToImage(textData,
                    new Font("Arial", Font.PLAIN, (int) sizeSlider.getValue()),
                    colorPicker.getValue(), new Color(0, 0, 0, 0),
                    rotationSlider.getValue()
            );
            // data work
            if (data.getFiles() != null) {
                File outputDir = new File(outputPath);
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }
                int randomValue = (int) (Math.random() * 1000);
                AtomicInteger step = new AtomicInteger(randomValue);
                //working
                int numThreads = 4;
                ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
                for (int i = 0; i < numThreads; i++) {
                    final int threadIndex = i;
                    executorService.submit(() -> {
                        for (int j = threadIndex; j < data.getFiles().size(); j += numThreads) {
                            File file = data.getFiles().get(j);
                            try {
                                processFile(file, outputDir, watermark, step.getAndIncrement());
                            } catch (IOException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                // done successfully
                executorService.shutdown();
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Done watermarks and exported");
                a.setContentText("files are all Successfully exported into : " + outputPath);
                a.show();
            }
        }
    }
    private void processFile(File file, File outputPath,
BufferedImage watermark, int uniquifier) throws IOException, IllegalArgumentException {
        if (!file.exists() || !file.canRead()) {
            System.out.println("Cannot read file: " + file.getName());
            return;
        }
        String thmbPath = outputPath + File.separator + "watermarked_" + getNoType(file) + uniquifier + "." + getOutputFileType();
        File tempFile = new File(outputPath + File.separator + "temp_" + file.getName());
        System.out.println("writing watermarked file to : " + thmbPath);
        Thumbnails.of(file)
                .scale(1)
                .watermark(selectedPos, watermark, (float) opacitySlider.getValue()/100.f)
                .outputFormat(getOutputFileType())
                .toFile(tempFile); // Write watermark to the temp file

        // Now scale the tempFile and save to thmbPath
        Thumbnails.of(tempFile).scale(imgqSlider.getValue()*.01).toFile(thmbPath);

        // Once done, delete the temp file.
        Files.deleteIfExists(tempFile.toPath());
    }

    private static BufferedImage stringToImage(String text, Font font,
    javafx.scene.paint.Color fxColor,Color bgColor, double rotationAngle) {
        // Convert JavaFX Color to AWT Color
        Color awtColor = new Color((float) fxColor.getRed(),
                (float) fxColor.getGreen(),
                (float) fxColor.getBlue(),
                (float) fxColor.getOpacity());
        // Create a dummy image to get the text dimensions
        BufferedImage dummyImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dummyImg.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text);
        int height = fm.getHeight();
        g2d.dispose();
        // Create the actual image with the correct dimensions
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();

        // Apply rotation transformation here
        AffineTransform origTransform = g2d.getTransform();
        AffineTransform rotation = new AffineTransform();
        rotation.rotate(Math.toRadians(rotationAngle), width / 2.0, height / 2.0);  // Rotate around the center of the image
        g2d.setTransform(rotation);
        // set font
        g2d.setFont(font);
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(awtColor);
        g2d.drawString(text, 0, fm.getAscent());

        // Check for negative or zero dimensions
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid dimensions for text image: width=" + width + ", height=" + height);
        }

        g2d.dispose();
        return img;
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
