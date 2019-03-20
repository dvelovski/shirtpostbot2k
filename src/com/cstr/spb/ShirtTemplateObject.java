/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import static com.cstr.spb.Main.consoleLog;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author dimitrivelovski
 */
public class ShirtTemplateObject {
    public float locationX = 0.1f;
    public float locationY = 0.1f;
    public float width = 0.25f;
    public float height = 0.25f;
    
    public String shirtTemplateName = "";
    public String imageFileName = "";
    public BufferedImage cached = null;
    
    public void loadImage(){
        if (imageFileName.isEmpty()){
            return;
        }
        
        File imgPath = new File("resources/" + imageFileName);
        try {
            cached = ImageIO.read(imgPath);
        } catch (IOException ex) {
            Logger.getLogger(ShirtTemplateObject.class.getName()).log(Level.SEVERE, null, ex);
        }
        consoleLog("Image at " + imgPath.getAbsolutePath() + " requested was loaded successfully.");
         
    }

    public ShirtTemplateObject() {
        this("", "");
    }
    public ShirtTemplateObject(String nm, String fn){
        shirtTemplateName = nm;
        imageFileName = fn;
    }
    
    public float convertX(float absX){
        float f = (width - locationX) * absX;
        System.out.println("converted x " + f);
        System.out.println("Input was: element x " + absX + ", width: " + width + ",my x: " + locationX);
        return f;
    }
    public float convertY(float absY){
        return (height - locationY) * absY;
    }
}
