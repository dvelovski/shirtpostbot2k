/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author dimitrivelovski
 */
public class DBPost {
    ArrayList<URL> imagesUsed = new ArrayList<>();
    ArrayList<String> rawTextOutput = new ArrayList<>();
    
    String usedTemplateName = "";
    String imageTemplate = "";
    String postID = "";
    String outputFilename = "";
    
    LocalDateTime genTime;
    
    public DBPost() {
        genTime = LocalDateTime.now();
    }
    public DBPost(GeneratedImageData source){
        this();
        
        outputFilename = source.writePath.getPath();
        imagesUsed = source.imageLinks;
        rawTextOutput = source.textLines;
        usedTemplateName = source.templateName;
        imageTemplate = source.iTemplate;
    }
    public void setPostID(String id){
        postID = id;
    }
}
