/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.parser.JSONParser;
import org.json.parser.ParseException;

/**
 *
 * @author Admin
 */
public class ImageGenerator {
    public ArrayList<ImageTemplate> templates = new ArrayList<>();
    public ArrayList<ShirtTemplateObject> shirtTemplates = new ArrayList<>();
    
    public ArrayList<Font> fontPool = new ArrayList<>();
    
    public float ratioMax = 3.5f;
    
    public ImageGenerator(){
        fontPool.add(new Font("Papyrus", Font.PLAIN, 12));
        fontPool.add(new Font("Arial", Font.PLAIN, 12));
        fontPool.add(new Font("Comic Sans MS", Font.PLAIN, 12));
        fontPool.add(new Font("Impact", Font.PLAIN, 12));                
        fontPool.add(new Font("Courier", Font.PLAIN, 12));
        fontPool.add(new Font("Brush Script MT", Font.ITALIC, 12));
        fontPool.add(new Font("Vivaldi", Font.ITALIC, 12));
        fontPool.add(new Font("Dubai", Font.PLAIN, 12));
        fontPool.add(new Font("Fixedsys", Font.PLAIN, 12));
        fontPool.add(new Font("Bradley Hand ITC", Font.PLAIN, 12));
        fontPool.add(new Font("Copperplate Gothic", Font.PLAIN, 12));
        fontPool.add(new Font("Snap ITC", Font.PLAIN, 12));
        fontPool.add(new Font("Stencil", Font.PLAIN, 12));
        fontPool.add(new Font("Parchment", Font.PLAIN, 12));
        fontPool.add(new Font("Gigi", Font.PLAIN, 12));
        
       // shirtTemplates.add(new ShirtTemplateObject("Shirt 1", "shirt 1.jpg"));
       // shirtTemplates.add(new ShirtTemplateObject("Shirt 2", "shirt 2.jpg"));        
    }
    
    public ImageTemplate getRandomTemplate(){
        return templates.get(Constants.rand.nextInt(templates.size()));
    }
    public ShirtTemplateObject getRandomShirt(){
        return shirtTemplates.get(Constants.rand.nextInt(shirtTemplates.size()));
    }
    
    public GeneratedImageData generateImage(ImageTemplate tmp, PostTemplate pTemp){
        //firstly we choose a random image
        //then we choose a random template
        //and hope to god the things match with lines or it'll end up weird.
        GeneratedImageData gd = new GeneratedImageData();
        
        if (tmp == null){
            tmp = getRandomTemplate();
        }
        
        ArrayList<ImageTemplateObject> templateObjects = new ArrayList<>(tmp.objects);
        
        PostTemplate ptmp = (pTemp == null ? Main.pLibrary.getRandomTemplate() : pTemp);
        ShirtTemplateObject imgTemplate = getRandomShirt();
        
        GeneratedPostData gPostData = Main.pLibrary.quickEvalTemplate(ptmp);
        PhraseReplacementData[] st = gPostData.generatedData;
        LinkedHashMap<String, String> rData = gPostData.replacementTokenData;        
        
        boolean cont = true;
        if (templateObjects.size() > ptmp.lines.length){
            //System.out.println("We have a conflict - image has more lines than text template, do not continue.");            
            //cont = false;
        }else if (ptmp.lines.length > templateObjects.size()){
            System.out.println("We have a conflict - image has less lines than text template");
        }
        
        BufferedImage bImg = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
        
        gd.templateName = ptmp.templateName;
        gd.iTemplate = imgTemplate.shirtTemplateName;
        
        for (PhraseReplacementData darta : st){
            gd.textLines.add(darta.rawText);
        }
        
        if (cont){
            //time to create a buffered image and get its details.
            BufferedImage loadImage = imgTemplate.cached;
            if (loadImage == null){
                imgTemplate.loadImage();
                loadImage = imgTemplate.cached;
            }
            
            Graphics2D renderGraphics = bImg.createGraphics();
            renderGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            renderGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            renderGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            renderGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            renderGraphics.drawImage(loadImage, 0, 0, 1024, 1024, null);
                        
            //search for boxes and identify text elements
            int textElementCount = 0;
            
            List<ImageTemplateObject> boxes = new ArrayList<>();
            for (int id = templateObjects.size() - 1; id >= 0; id--){
                ImageTemplateObject it = templateObjects.get(id);
                if (it.templateObjectType == ImageTemplateObject.TYPE_BOX){
                    boxes.add(templateObjects.remove(id));                    
                }
                if (it.templateObjectType == ImageTemplateObject.TYPE_TEXT){
                    textElementCount ++;
                }
            }
            templateObjects.addAll(0, boxes);
            
            int textIndex = 0;
            
            ArrayList<String> lineOverrideList = new ArrayList<>();
            boolean overridingWithList = false;
            String overrideText = "";
            
            LP: for (int i = 0; i < templateObjects.size(); i++){
                renderGraphics.setFont(fontPool.get(Constants.rand.nextInt(fontPool.size())));
                
                ImageTemplateObject c = templateObjects.get(i);
                //System.out.println(i + " - " + c.describeSelf());
                
                /**
                 * 
                 *  x is a value of 0 - 1
                    to translate to the object:
                     x = (obj.width - obj.x) * x

                    0.4 = (1.0 - 0.2) * 0.5
                 */
                float posX = (imgTemplate.width  * c.locationX) * bImg.getWidth() + (imgTemplate.locationX * bImg.getWidth());
                float posY = (imgTemplate.height * c.locationY) * bImg.getHeight() + (imgTemplate.locationY * bImg.getHeight());
                //System.out.println(posX + ", " +  posY);
                //System.out.println(c.locationX + ", " + c.locationY + ", WIDTH " + c.width + ", HEIGHT " + c.height);
                float obW = (c.width) * (bImg.getWidth() * imgTemplate.width);
                float obH = (c.height) * (bImg.getHeight() * imgTemplate.height);
                //System.out.println("OBJECT CALCULATED SIZE: " + obW + " x " + obH);
                
                AffineTransform current = renderGraphics.getTransform();
                
                switch (c.templateObjectType){
                    case ImageTemplateObject.TYPE_TEXT:
                        //we will get a string width at our current font
                        //if it's not right, we scale
                        if (!overridingWithList){                            
                            if (textIndex >= st.length){
                                break;
                            }
                        }else{
                            if (overrideText.isEmpty()){
                                break;
                            }
                        }
                        String textToUse = (overridingWithList ? overrideText : st[textIndex].rawText);
                        
                        Rectangle2D currBounds = renderGraphics.getFontMetrics().getStringBounds(textToUse, renderGraphics);   
                        setGraphicsColour(c, renderGraphics);          
                        float scX = (float) (obW / currBounds.getWidth());
                        float scY = (float) (obH / currBounds.getHeight());

                        //System.out.println("Font scale X: " + scX);
                        //System.out.println("Font scale y: " + scY);
                        int bigLineMode = 2;

                        boolean normalRender = false;
                        boolean resizedY = false;

                        if (bigLineMode == 1){
                            while (scY / scX > ratioMax){
                                scY /= 1.5;
                                resizedY = true;
                            }
                            normalRender = true;
                        }else{
                            //try to split at word ends
                            //System.out.println("mode 2 got line: " + st[textIndex].rawText);
                            //System.out.println("mode 2 got line: " + textToUse);
                            String[] textElements = textToUse.split(" ");
                            ArrayList<String> finalLines = new ArrayList<>();
                            if (scY / scX > ratioMax){
                                //we should split into
                                //get a local scale - add a word until it's too big, then go one back if we can
                                String cLine = "";
                                int wIndex = 0;
                                Rectangle2D testBounds = null;

                                while (wIndex < textElements.length){
                                    //System.out.println("processing: " + textElements[wIndex]);
                                    //System.out.println("cLine: "+ cLine);
                                    String nowLine = cLine;
                                    cLine += textElements[wIndex] + " ";

                                    testBounds = renderGraphics.getFontMetrics().getStringBounds(cLine, renderGraphics);
                                    float localScX = (float) (obW / testBounds.getWidth());
                                    float localScY = (float) (obH / testBounds.getHeight());
                                    //System.out.println(localScX + ", " + localScY + " - result: " + (localScY / localScX));
                                    if (localScY / localScX > ratioMax){
                                        finalLines.add(nowLine.trim());
                                        cLine = textElements[wIndex] + " ";                                                            
                                    }
                                    wIndex++;
                                    
                                    
                                }
                                finalLines.add(cLine.trim());
                                if (textIndex >= st.length - 1){
                                    //we hit the end of text lines, don't do anything, i'm just thinking about cases
                                    if (textIndex < textElementCount){
                                        lineOverrideList.clear();
                                        //System.out.println(textElementCount / st.length);
                                        //System.out.println("remaining text elements " + (textElementCount - textIndex));
                                        //split it up
                                        int sz = (int) Math.ceil(finalLines.size() / (textElementCount - textIndex));
                                        lineOverrideList.addAll(finalLines.subList(sz, finalLines.size()));
                                        finalLines.removeAll(lineOverrideList);
                                        
                                        
                                        //System.out.println(lineOverrideList);
                                        //System.out.println(finalLines);
                                        
                                        overridingWithList = true;
                                        overrideText = String.join(" ", lineOverrideList);
                                        
                                        //System.out.println("What's left? " + overrideText);
                                    }
                                }else{
                                   // System.out.println("text index ain't at end");
                                }
                                //System.out.println("goin with it " + cLine);
                                /*System.out.print("Final output: ");
                                for (String oL : finalLines){
                                    System.out.print("\"" + oL + "\", ");
                                }
                                System.out.println();*/

                                int lnY = 0;
                                Font currFnt = renderGraphics.getFont();

                                for (int ln = 0; ln < finalLines.size(); ln++){
                                    String inLine = finalLines.get(ln);
                                    Rectangle2D bnds = renderGraphics.getFontMetrics(currFnt).getStringBounds(inLine, renderGraphics);
                                    float localScX = (float) (obW / bnds.getWidth());
                                    float localScY = (float) ((obH / finalLines.size()) / bnds.getHeight());

                                    //System.out.println(localScX + ", " + localScY + " - result: " + (localScY / localScX));
                                    boolean xShrunk = false;
                                    int tries = 0;
                                    
                                    while (localScY / localScX < 1 && tries < 3){
                                        localScX /= 2;
                                        xShrunk = true;
                                        //System.out.println("Resizing... " + ln);
                                        tries++;
                                    }

                                    Font fnt = currFnt.deriveFont(AffineTransform.getScaleInstance(localScX, localScY));
                                    renderGraphics.setFont(fnt);

                                    float x4 = (imgTemplate.locationX * bImg.getWidth()) + 
                                               ((c.width * imgTemplate.width * bImg.getWidth()) / 2) +
                                               (c.locationX * imgTemplate.width * bImg.getWidth());
                                    x4 -= renderGraphics.getFontMetrics(fnt).stringWidth(inLine) / 2;
                                    
                                    GlyphVector gVec = fnt.createGlyphVector(renderGraphics.getFontRenderContext(), inLine);
                                    Shape sH = AffineTransform.getTranslateInstance(
                                            (xShrunk ? x4 : (int) (posX + ((bImg.getWidth() * imgTemplate.width) * c.locationX))),
                                            posY + renderGraphics.getFontMetrics().getAscent() + lnY).
                                            createTransformedShape(gVec.getOutline());

                                    fillAndOutlineShape(c, renderGraphics, sH);
                                    //renderGraphics.fillRect((int) x4, (int) posY + lnY, 8, 8);
                                    lnY += renderGraphics.getFontMetrics().getAscent() + renderGraphics.getFontMetrics().getLeading() + renderGraphics.getFontMetrics().getDescent();
                                }

                            }else{
                                normalRender = true;
                            }
                        }
                        if (normalRender){
                            Font fnt = renderGraphics.getFont().deriveFont(AffineTransform.getScaleInstance(scX, scY));
                            renderGraphics.setFont(fnt);
                            
                            GlyphVector gV = fnt.createGlyphVector(renderGraphics.getFontRenderContext(), textToUse);
                            Shape sH = AffineTransform.getTranslateInstance((posX + ((bImg.getWidth() * imgTemplate.width) * c.locationX)), 
                                    posY + renderGraphics.getFontMetrics().getAscent() + (resizedY ? renderGraphics.getFontMetrics().getHeight() / 2 : 0)).
                                    createTransformedShape(gV.getOutline());
                            fillAndOutlineShape(c, renderGraphics, sH);
                            
                            if (overridingWithList){
                                overrideText = "";
                            }
                        }

                        textIndex++;
                        break;
                    case ImageTemplateObject.TYPE_IMAGE:
                        //we get a random image based on criteria - for the moment, download a random one
                        BufferedImage rnd = null;
                        String toUse = "";
                        try {
                            if (c.useToken){
                                //System.out.println("The element has requested use of a text token.");
                                if (c.tokenName.isEmpty() && !rData.isEmpty()){
                                    Object[] values = rData.values().toArray();
                                    toUse = (String) values[Constants.rand.nextInt(values.length)];
                                }else{
                                    toUse = rData.getOrDefault(c.tokenName, "meme");
                                }
                            }
                            if (toUse.isEmpty()){
                                toUse = Main.pLibrary.randomStringFromList(Main.pLibrary.nounList);
                            }

                            //System.out.println("We are using the following image token: " + toUse);
                                                        
                            URL searchURL;
                            searchURL = new URL("https://www.googleapis.com/customsearch/v1?"
                                    + "key=AIzaSyCXSxFyT8MP0e68SkEWiZZOjIVP4RPSIq0&"
                                    + "cx=011405303231145693790:uo9udq9hchm&"
                                    + "searchType=image&"
                                    + "q=" + toUse.replace(" ", "+") + "&"
                                    + "start=" + Constants.rand.nextInt(100) + "&"
                                    + "size=medium&"
                                    + "num=1");

                            URLConnection connection = searchURL.openConnection();

                            String line;
                            StringBuilder builder = new StringBuilder();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()), 1);
                            while((line = reader.readLine()) != null) {
                                builder.append(line);
                            }

                            JSONObject json = null;
                            try {
                                json = (JSONObject) new JSONParser().parse(builder.toString());
                            } catch (ParseException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                            JSONArray itms = (JSONArray) json.get("items");
                            JSONObject img1 = (JSONObject) itms.get(0);
                            String imgLink = (String) img1.get("link");
                            URL imgURL = new URL(imgLink);
                            rnd = ImageIO.read(imgURL);
                            
                            gd.imageLinks.add(imgURL);
                            
                        } catch (IOException ex) {
                            Logger.getLogger(ImageGenerator.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("Revert to flickr?");
                            
                            URL imageURL;
                            try {
                                imageURL = new URL("https://loremflickr.com/320/240/" + toUse);                                
                                rnd = ImageIO.read(imageURL);
                                gd.imageLinks.add(imageURL);
                            } catch (MalformedURLException ex1) {
                                Logger.getLogger(ImageGenerator.class.getName()).log(Level.SEVERE, null, ex1);
                            } catch (IOException ex1) {
                                Logger.getLogger(ImageGenerator.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                            
                        }
                        Rectangle2D i2 = new Rectangle2D.Float((posX + ((bImg.getWidth() * imgTemplate.width) * c.locationX)), posY, obW, obH);
                        renderGraphics.drawImage(rnd, (int) (posX + ((bImg.getWidth() * imgTemplate.width) * c.locationX)), (int) posY, (int) obW, (int) obH, null);                         
                        fillAndOutlineShape(c, renderGraphics, i2);
                        break;
                    case ImageTemplateObject.TYPE_BOX:
                        Rectangle2D r2 = new Rectangle2D.Float((posX + ((bImg.getWidth() * imgTemplate.width) * c.locationX)), posY, obW, obH);
                        fillAndOutlineShape(c, renderGraphics, r2);
                        break;
                }
            }
            
            try {
                gd.writePath = new File("output/" + System.currentTimeMillis() + ".png");
                gd.result = bImg;
                ImageIO.write(bImg, "png", gd.writePath);
            } catch (IOException ex) {
                Logger.getLogger(ImageGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return gd;
    }
    
    public void fillAndOutlineShape(ImageTemplateObject c, Graphics2D renderGraphics, Shape s){       
        if (c.templateObjectType != ImageTemplateObject.TYPE_IMAGE){
            setGraphicsColour(c, renderGraphics);
            renderGraphics.fill(s);
        }
        switch (c.outlineMode){
            case ImageTemplateObject.OUTLINE_NONE:
                break;
            case ImageTemplateObject.OUTLINE_RANDOM_SET:
                if (Constants.rand.nextInt(1000) % 2 == 0){
                    break;
                }
            case ImageTemplateObject.OUTLINE_SET:
                renderGraphics.setColor(c.preferredOutlineColour);
                renderGraphics.draw(s);
                break;
            case ImageTemplateObject.OUTLINE_RANDOM_OTH:
                if (Constants.rand.nextInt(1000) % 2 != 0){
                    break;
                }
            case ImageTemplateObject.OUTLINE_RANDOM:
                renderGraphics.setColor(randomColour());
                renderGraphics.draw(s);
                break;
        }
    }
    
    public void setGraphicsColour(ImageTemplateObject c, Graphics2D renderGraphics){
        if (c.useRandomColour){
            Color clr = randomColour();
            renderGraphics.setColor(clr);
        }else{
            renderGraphics.setColor(c.preferredColour);
        }
    }
    public Color randomColour(){
        return new Color(Constants.rand.nextInt(256), Constants.rand.nextInt(256), Constants.rand.nextInt(256));
    }
    
    public void saveImageTemplates(){
        JSONArray jArray = new JSONArray();
        for (ImageTemplate t : templates){
            JSONObject iT = new JSONObject();
            iT.put("templateName", t.templateName);
            JSONArray ob = new JSONArray();
            for (ImageTemplateObject iTO : t.objects){
                JSONObject tt = new JSONObject();
                tt.put("x", iTO.locationX);
                tt.put("y", iTO.locationY);
                tt.put("width", iTO.width);
                tt.put("height", iTO.height);
                tt.put("type", iTO.templateObjectType);
                tt.put("textValue", iTO.textValue);
                tt.put("tokenName", iTO.tokenName);
                tt.put("imageRandom", iTO.imageRandom);
                tt.put("useToken", iTO.useToken);
                tt.put("imageTag", iTO.imgTag);
                tt.put("colourRandom", iTO.useRandomColour);
                tt.put("preferredColour", iTO.preferredColour.getRGB());
                tt.put("outlineColour", iTO.preferredOutlineColour.getRGB());
                tt.put("outlineMode", Math.max(1, iTO.outlineMode));
                ob.add(tt);
            }
            iT.put("objects", ob);
            jArray.add(iT);
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("resources/libraries/image_templates.dat")));
            bw.write(jArray.toJSONString());
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
            Main.consoleLog("Saving of image template library failed: " + ex.getMessage());
        }
        Main.consoleLog("Image template library saved successfully.");
    }
    
    public void saveShirtTemplates(){
        JSONArray jShirts = new JSONArray();
        for (ShirtTemplateObject st : shirtTemplates){
            JSONObject shirt = new JSONObject();
            shirt.put("name", st.shirtTemplateName);
            shirt.put("imageName", st.imageFileName);
            shirt.put("x", st.locationX);
            shirt.put("y", st.locationY);
            shirt.put("width", st.width);
            shirt.put("height", st.height);
            jShirts.add(shirt);            
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("resources/libraries/shirt_templates.dat")));
            bw.write(jShirts.toJSONString());
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
            Main.consoleLog("Saving of shirt template library failed: " + ex.getMessage());
        }
        Main.consoleLog("Shirt template library saved successfully.");
    }
    
    public void loadImageTemplates(){
        JSONParser jP = new JSONParser();
        JSONArray jArray;
        try {
            jArray = (JSONArray) jP.parse(new FileReader(new File("resources/libraries/image_templates.dat")));
            
            for (Object jO : jArray){
                JSONObject jTemp = (JSONObject) jO;
                ImageTemplate iTemp = new ImageTemplate();
                iTemp.templateName = (String) jTemp.get("templateName");
                JSONArray jObjects = (JSONArray) jTemp.get("objects");
                for (Object ii : jObjects){
                    JSONObject iObj = (JSONObject) ii;
                    ImageTemplateObject iTO = new ImageTemplateObject();
                    iTO.locationX = ((Number) iObj.get("x")).floatValue();
                    iTO.locationY = ((Number) iObj.get("y")).floatValue();
                    iTO.width = ((Number) iObj.get("width")).floatValue();
                    iTO.height = ((Number) iObj.get("height")).floatValue();
                    iTO.templateObjectType = ((Number) iObj.get("type")).intValue();
                    iTO.textValue = (String) iObj.get("textValue");
                    iTO.tokenName = (String) iObj.get("tokenName");
                    iTO.imageRandom = (Boolean) iObj.get("imageRandom");
                    iTO.useRandomColour = (Boolean) iObj.get("colourRandom");
                    iTO.useToken = (Boolean) iObj.getOrDefault("useToken", false);
                    iTO.imgTag = (String) iObj.get("imageTag");
                    iTO.preferredColour = new Color(((Number) iObj.getOrDefault("preferredColour", 0)).intValue());
                    iTO.preferredOutlineColour = new Color(((Number) iObj.getOrDefault("outlineColour", 0)).intValue());
                    iTO.outlineMode = ((Number) iObj.getOrDefault("outlineMode", 0)).intValue();
                    iTemp.objects.add(iTO);
                }
                templates.add(iTemp);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadShirtTemplates(){
        JSONParser jP = new JSONParser();
        JSONArray jArray;
        try {
            jArray = (JSONArray) jP.parse(new FileReader(new File("resources/libraries/shirt_templates.dat")));
            
            for (Object jO : jArray){
                JSONObject jTemp = (JSONObject) jO;
                ShirtTemplateObject st = new ShirtTemplateObject();
                st.shirtTemplateName = (String) jTemp.get("name");
                st.imageFileName = (String) jTemp.get("imageName");
                st.locationX = ((Number) jTemp.get("x")).floatValue();
                st.locationY = ((Number) jTemp.get("y")).floatValue();
                st.width = ((Number) jTemp.get("width")).floatValue();
                st.height = ((Number) jTemp.get("height")).floatValue();
                shirtTemplates.add(st);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
