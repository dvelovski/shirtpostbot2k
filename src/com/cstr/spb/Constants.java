/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.parser.JSONParser;
import org.json.parser.ParseException;
import org.json.simple.JSONObject;

/**
 *
 * @author Admin
 */
public class Constants {
    public static String APP_ID = "";
    public static String APP_SECRET = "";
    public static String APP_TOKEN = "";
    
    public static String PAGE_ACCESS_TOKEN = "";
    
    public static Random rand = new SecureRandom();
    
    public static final Color BUFFER_CLEAR = new Color(135, 206, 235);
    
    public static boolean SHOULD_POST = false;
    public static int POST_DELAY_MINUTES = 1;
    
    static{
        JSONParser jParse = new JSONParser();
        try {
            JSONObject creds = (JSONObject) jParse.parse(new FileReader(new File("resources/credentials.json")));
            APP_ID = (String) creds.get("appID");
            APP_SECRET = (String) creds.get("appSecret");
            APP_TOKEN = (String) creds.get("appToken");
        } catch (IOException ex) {
            Logger.getLogger(Constants.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Constants.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
