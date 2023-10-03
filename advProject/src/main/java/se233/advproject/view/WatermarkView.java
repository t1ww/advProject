package se233.advproject.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.resizers.Resizers;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class WatermarkView extends MainView {
    /// init
    private static outPutFileType outType = outPutFileType.jpg;
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

    //
    @Override
    public void initialize(URL url, ResourceBundle resources) {
        initListView();
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
        String textData = (textContentTextfield.getText().isBlank()) ? "null" : textContentTextfield.getText();
        System.out.println(textData + " | is being used for watermarking");

        BufferedImage watermark = stringToImage(textData,
                new Font("Arial", Font.PLAIN, (int) sizeSlider.getValue()),
                colorPicker.getValue(), new Color(0,0,0,0),
                rotationSlider.getValue()
        );

        if (data.getFiles() != null) {
            File outputDir = new File("target" + File.separator + "output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            ExecutorService executorService = Executors.newFixedThreadPool(4);
            for (File file : data.getFiles()) { // iterate all files
                executorService.submit(() -> {
                    try {
                        if (!file.exists() || !file.canRead()) {
                            System.out.println("Cannot read file: " + file.getName());
                            return;
                        }
                        String thmbPath = outputDir + File.separator + "watermarked_" + file.getName();
                        System.out.println("writing watermarked file to : " + thmbPath);
                        javafx.scene.image.Image img = new Image(new FileInputStream(file));
                        double scaleValue = imgqSlider.getValue();
                        if (scaleValue <= 0) {
                            System.out.println("Invalid scale value: " + scaleValue);
                            return;
                        }
                        Thumbnails.of(file)
                                .scale(1)
                                .watermark(selectedPos, watermark, (float) opacitySlider.getValue()/100.f)
                                .toFile(thmbPath);
                        Thumbnails.of(thmbPath).scale(imgqSlider.getValue()).toFile(thmbPath);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            executorService.shutdown();
        }
    }

    private static BufferedImage stringToImage(String text, Font font,
                                               javafx.scene.paint.Color fxColor,
                                               Color bgColor, double rotationAngle) {
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

        // Check for negative or zero dimensions
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid dimensions for text image: width=" + width + ", height=" + height);
        }

        // set font
        g2d.setFont(font);
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(awtColor);
        g2d.drawString(text, 0, fm.getAscent());
        g2d.dispose();
        return img;
    }
}
