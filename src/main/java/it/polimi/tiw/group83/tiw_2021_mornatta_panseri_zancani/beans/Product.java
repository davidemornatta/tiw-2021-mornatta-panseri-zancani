package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class Product {
    private int code;
    private String name;
    private String category;
    private String description;
    private BufferedImage image;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(this.image, "png", output);
        String imageAsBase64 = Base64.getEncoder().encodeToString(output.toByteArray());
        return imageAsBase64;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
