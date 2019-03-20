/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

/**
 *
 * @author Admin
 */
public class PostTemplate {
    public String templateName = "";
    public PostTemplateLine[] lines = new PostTemplateLine[1];
    
    public PostTemplate() {
        this(1, new String[]{""});
    }
    
    public PostTemplate(int lns, String[] values){
        lines = new PostTemplateLine[lns];
        for (int i = 0; i < lns; i++){
            lines[i] = new PostTemplateLine();
            lines[i].lineText = values[i];
        }
    }
    
    public PostTemplate setNumLines(int lns){
        lines = new PostTemplateLine[lns];
        for (int i = 0; i < lns; i++){
            lines[i] = new PostTemplateLine();
        }
        return this;
    }
    public PostTemplate setLineValues(String[] vals){        
        for (int i = 0; i < vals.length; i++){
            lines[i].lineText = vals[i];
        }
        return this;
    }
}
