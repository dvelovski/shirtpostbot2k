/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import java.awt.Color;

/**
 *
 * @author Admin
 */
public class ImageTemplateObject {
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_BOX = 3;
    
    public static final int OUTLINE_NONE = 1;
    public static final int OUTLINE_SET = 2;
    public static final int OUTLINE_RANDOM = 3;
    public static final int OUTLINE_RANDOM_SET = 4;
    public static final int OUTLINE_RANDOM_OTH = 5;
    
    public String textValue = "";
    public String imgTag = "";
    
    public Color preferredColour = Color.black; //preferred can be null
    public boolean useRandomColour = false;
    
    public Color preferredOutlineColour = Color.black; //preferred can be null
    public boolean useRandomOutlineColour = false;
    
    public int outlineMode = OUTLINE_NONE;
    
    //the following values are percentages of a template image
    public float locationX = 0.0f;
    public float locationY = 0.0f;
    public float width = 0.5f;
    public float height = 0.5f;
    
    public int templateObjectType = TYPE_TEXT;
    public boolean imageRandom = false; //if the image is not random we'll try to search, otherwise grab random from image folder.
    public boolean useToken = false;
    public String tokenName = "";
    
    public String describeSelf(){
        String val = "I am a ";
        switch (templateObjectType){
            case TYPE_TEXT:
                val += "text object ";
                break;
            case TYPE_IMAGE:
                val += "image object ";
                break;
            case TYPE_BOX:
                val += "box object ";
                break;
        }
        val += "located at X: " + locationX + ", Y: " + locationY;
        return val;
    }
}
