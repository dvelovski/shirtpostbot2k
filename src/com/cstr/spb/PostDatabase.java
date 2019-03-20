/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 *
 * @author Admin
 */
public class PostDatabase {
    public HashMap<LocalDate, ArrayList<DBPost>> postData = new HashMap<>(); //collection of daily post collections
    
    public ArrayList<DBPost> getPostsForMonth(int year, int month){
        ArrayList<DBPost> results = new ArrayList<>();
        return results;
    }
    public void addPost(DBPost dPost){
        Calendar cl = Calendar.getInstance();
        LocalDate dt = LocalDate.now();
        
        ArrayList<DBPost> postList = postData.getOrDefault(dt, new ArrayList<>());
        postList.add(dPost);
        postData.put(dt, postList);
        
        DefaultTableModel mdl = (DefaultTableModel) Main.eWindow.getPostTable().getModel();
        mdl.addRow(new Object[]{dPost.genTime.toString(), dPost.postID, 
            dPost.usedTemplateName, dPost.imageTemplate, 
            dPost.imagesUsed, dPost.rawTextOutput});
    }
    public void saveLibrary(){
        JSONObject jArr = new JSONObject();
        for (Map.Entry<LocalDate, ArrayList<DBPost>> obj : postData.entrySet()) {
            Object key = obj.getKey();
            ArrayList<DBPost> val = obj.getValue();
            
            JSONArray posts = new JSONArray();
            for (DBPost pst : val){
                JSONObject postInfo = new JSONObject();
                postInfo.put("postID", pst.postID);
                postInfo.put("template", pst.usedTemplateName);
                postInfo.put("iTemplate", pst.imageTemplate);
                postInfo.put("generatedTime", pst.genTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                
                JSONArray links = new JSONArray();
                for (URL lnk : pst.imagesUsed){
                    links.add(lnk.toString().replace(':', '>'));
                }
                postInfo.put("urlList", links);
                
                JSONArray text = new JSONArray();
                for (String str : pst.rawTextOutput){
                    text.add(str);
                }
                postInfo.put("rawText", text);
                
                posts.add(postInfo);
            }
            jArr.put(key, posts);
        }
        System.out.println(jArr.toJSONString());
    }
}
