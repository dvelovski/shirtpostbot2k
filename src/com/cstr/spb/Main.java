/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import com.vdurmont.emoji.EmojiParser;
import facebook4j.AlbumUpdate;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Media;
import facebook4j.PagePhotoUpdate;
import facebook4j.Photo;
import facebook4j.Post;
import facebook4j.Reading;
import facebook4j.ResponseList;
import facebook4j.auth.AccessToken;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Admin
 */
public class Main {

    public static Facebook fb = new FacebookFactory().getInstance();
    public static ImageGenerator iGenerator = null;
    public static PhraseLibrary pLibrary = null;
    public static EditorWindowForm eWindow;
    public static Timer postTimer;
    public static PostDatabase postDBase = new PostDatabase();
    
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //this is to prevent issues with loading images from external resources
        System.setProperty("http.agent", "Chrome");
                
        eWindow = new EditorWindowForm();
        eWindow.setVisible(true);

        iGenerator = new ImageGenerator();
        iGenerator.loadImageTemplates();
        iGenerator.loadShirtTemplates();
        pLibrary = new PhraseLibrary();
        pLibrary.setup();
        
        eWindow.updateTemplates();
        
        login();
        
        setUpTimer();        
    }
    
    public static void setUpTimer(){
        if (postTimer != null){
            postTimer.cancel();            
        }
        postTimer = new Timer();
        postTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (Constants.SHOULD_POST){
                    GeneratedImageData bGen = iGenerator.generateImage(null, null);
                    if (bGen.writePath != null){
                        try {
                            //write image
                            
                            PagePhotoUpdate ppu = new PagePhotoUpdate(new Media(bGen.writePath));                            
                            String pic = fb.postPagePhoto(ppu);
                            
                            DBPost postInfo = new DBPost(bGen);
                            postInfo.setPostID(pic);
                            
                            postDBase.addPost(postInfo);
                            
                            consoleLog("Image successfully generated and posted - ID: " + pic + ", image: " + bGen.writePath);
                        } catch (FacebookException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }, 0, Constants.POST_DELAY_MINUTES * 1000 * 60);
        consoleLog("Configured to post once every " + (Constants.POST_DELAY_MINUTES) + " minutes.");
    }
    
    public static void login(){        
        fb.setOAuthAppId(Constants.APP_ID, Constants.APP_SECRET);
        fb.setOAuthAccessToken(new AccessToken(Constants.APP_TOKEN));
                        
        consoleLog("We have logged in.");        
        consoleLog("Page access has been established.");
    }
    
    public static void consoleLog(String s){
        String tm = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
        eWindow.addToLog(tm + " | " + s + System.lineSeparator());
    }
    
    public static void getPostLikes(){
        try {
            Calendar from = Calendar.getInstance();
            from.set(Calendar.MONTH, 1);
            //from.
            Calendar to = Calendar.getInstance();
            
            Reading reading = new Reading().summary().fields("id", "object_id", "reactions.summary(total_count)").since("-1 month");
            ResponseList<Post> feeds = fb.getPosts(reading);
            boolean notEmpty = true;
            
            ArrayList<ReturnedLikeContainer> returnedData = new ArrayList<>();
            
            System.out.println("collating...");
            while (notEmpty) {
                notEmpty = false;
                if (feeds == null){
                    break;
                }
                for (Post post : feeds) {
                    notEmpty = true;
                    ReturnedLikeContainer rlD = new ReturnedLikeContainer();
                    
                    Integer reactsCount = (post.getReactions().getSummary() == null ? 0 : post.getReactions().getSummary().getTotalCount());

                    rlD.postID = post.getObjectId();
                    rlD.reactionCount = reactsCount;
                    returnedData.add(rlD);
                }
                //System.out.println("still collating... " + returnedData.size());
                feeds = fb.fetchNext(feeds.getPaging());
            }
            System.out.println("Collated " + returnedData.size() + " records.");
                                    
            returnedData.sort(Comparator.comparingInt(ReturnedLikeContainer::getReactions).reversed());
            
            String albumTitle = "Best shirts of " + to.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.US) + " " + to.get(Calendar.YEAR);
            AlbumUpdate aUpd = new AlbumUpdate(albumTitle);            
            aUpd.setMessage(
                    EmojiParser.parseToUnicode("This is a list of the top 6 posts by the bot for the month just gone.\n"
                                            + "The shirt with the most corresponding reacts will be made into a t-shirt by Botmin!\n\n"
                                            + ":thumbsup: - Shirt 1\n"
                                            + ":heart: - Shirt 2\n"
                                            + ":laughing: - Shirt 3\n"
                                            + ":astonished: - Shirt 4\n"
                                            + ":cry: - Shirt 5\n"
                                            + ":angry: - Shirt 6\n"));
            String ab = fb.createAlbum(aUpd);              
            
            for (int i = 0; i < 6; i++){
                ReturnedLikeContainer dt = returnedData.get(i);
                
                Photo picData = fb.getPhoto(returnedData.get(i).postID, new Reading().fields("source"));
                try {
                    File imgPath = new File("top6/top6_" + i + " (" + returnedData.get(i).postID + ").png");
                    ImageIO.write(ImageIO.read(picData.getSource()), "png", imgPath);
                    fb.addAlbumPhoto(ab, new Media(imgPath), "Shirt #" + i + " - Reactions: " + dt.reactionCount);                    
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            System.out.println("Created top post test.");
        } catch (FacebookException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
