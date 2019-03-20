/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import java.awt.Color;
import java.util.ArrayList;

/**
 *
 * @author Admin
 */
public class ImageTemplate {
    public String templateName = "";    
    
    public ArrayList<ImageTemplateObject> objects = new ArrayList<>();
    
    public void addTemplateObject(ImageTemplateObject iTO){
        objects.add(iTO);
    }        
    
    public ImageTemplate cloneSelf(){
        ImageTemplate iT = new ImageTemplate();
        for (ImageTemplateObject iO : objects){
            ImageTemplateObject iNew = new ImageTemplateObject();
            iT.addTemplateObject(iNew);
            
            iNew.height = iO.height;
            iNew.width = iO.width;
            iNew.imageRandom = iO.imageRandom;
            iNew.imgTag = iO.imgTag;
            iNew.locationX = iO.locationX;
            iNew.locationY = iO.locationY;
            iNew.preferredColour = new Color(iO.preferredColour.getRGB());
            iNew.templateObjectType = iO.templateObjectType;
            iNew.textValue = iO.textValue;
            iNew.tokenName = iO.tokenName;
            iNew.useRandomColour = iO.useRandomColour;
            iNew.useToken = iO.useToken;                        
        }
        
        return iT;        
    }
}
