/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author Dimitri Velovski
 */
public class PhraseReplacementData {
    String[] tokens;
    String rawText;
    LinkedHashMap<String, String> replacementMap = new LinkedHashMap<>();
    HashMap<String, String> replacementParameters = new HashMap<>();
    
    public PhraseReplacementData(String input) {
        rawText = input;
        tokens = input.split(" ");
    }
}
