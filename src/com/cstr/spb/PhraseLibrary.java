/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import static com.cstr.spb.Main.consoleLog;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.parser.JSONParser;
import org.json.parser.ParseException;

/**
 *
 * @author Admin
 */
public class PhraseLibrary {
    public ArrayList<String> phraseTemplates = new ArrayList<>();
    
    public ArrayList<PostTemplate> postTemplates = new ArrayList<>();
    
    public ArrayList<String> nounList = new ArrayList<>();
    public ArrayList<String> pluralNounList = new ArrayList<>();
    public ArrayList<String> adjectiveList = new ArrayList<>();
    public ArrayList<String> familyList = new ArrayList<>();
    public ArrayList<String> professionList = new ArrayList<>();
    public ArrayList<String> placeList = new ArrayList<>();
    public ArrayList<String> countryList = new ArrayList<>();
    public ArrayList<String> nationalityNounsList = new ArrayList<>();
    public ArrayList<String> nationalityAdjectivesList = new ArrayList<>();
    public ArrayList<String> foodList = new ArrayList<>();
    public ArrayList<String> monthList = new ArrayList<>();
    public ArrayList<String> dayList = new ArrayList<>();
    public ArrayList<String> traitList = new ArrayList<>();
    public ArrayList<String> businessList = new ArrayList<>();
    public ArrayList<String> genreList = new ArrayList<>();
    public ArrayList<String> animalList = new ArrayList<>();
    public ArrayList<String> bandList = new ArrayList<>();
    public ArrayList<String> drinkList = new ArrayList<>();
    public ArrayList<String> verbList = new ArrayList<>();
    public ArrayList<String> gerundList = new ArrayList<>();
    public ArrayList<String> famousList = new ArrayList<>();
    public ArrayList<String> subcultureList = new ArrayList<>();
    public ArrayList<String> movieList = new ArrayList<>();
    public ArrayList<String> clothingList = new ArrayList<>();
    public ArrayList<String> religionList = new ArrayList<>();
    public ArrayList<String> siteList = new ArrayList<>();
    public ArrayList<String> hairList = new ArrayList<>();
    
    public HashMap<String, PhraseWordConsumer> consumers = new HashMap<>();
    
    Pattern findVar = Pattern.compile("(\\$([0-9])+)");
    Pattern replaceParams = Pattern.compile("\\[([^]]+)\\]");
    /* THE FOLLOWING REPLACEMENT SCHEMES:
        |NOUN|
            |NOUN| can be any
            |SNOUN| is a noun not ending in S (crudely enforcing singularity)
            |PLNOUN| is a plural noun
        |ADJECTIVE|
        |1.100|
        |x.y| (random number between x and y)
        |option1,option2,option3| (options separated by a comma)
        |MONTH|
        |DAY|        
    
        we use regex to capture these groups and process them when a post is generated.
    */
    
    public void setup(){
        parseList(nounList, "noun list", "NOUN LIST");
        parseList(adjectiveList, "adjectives", "ADJECTIVE LIST");
        parseList(pluralNounList, "plurals", "PLURAL NOUN LIST");
        parseList(familyList, "family", "FAMILY MEMBER LIST");
        parseList(foodList, "food", "FOOD LIST");
        parseList(professionList, "professions", "JOB LIST");
        parseList(placeList, "places", "PLACE LIST");
        parseList(countryList, "countries", "COUNTRY LIST");
        parseList(nationalityNounsList, "nationality_nouns", "NATIONALITY NOUN LIST");
        parseList(nationalityAdjectivesList, "nationality_adjectives", "NATIONALITY ADJECTIVE LIST");
        parseList(traitList, "traits", "TRAIT LIST");
        parseList(businessList, "businesses", "BUSINESS LIST");
        parseList(genreList, "genres", "GENRE LIST");
        parseList(animalList, "animals", "ANIMAL LIST");
        parseList(bandList, "bands", "BAND LIST");
        parseList(drinkList, "drinks", "DRINK / BEVERAGE LIST");
        parseList(verbList, "verbs", "VERB LIST");
        parseList(gerundList, "gerunds", "GERUND LIST");
        parseList(subcultureList, "subcultures", "SUBCULTURE LIST");
        parseList(famousList, "famous_people", "FAMOUS PERSONS LIST");
        parseList(movieList, "movies", "MOVIE LIST");
        parseList(clothingList, "clothes", "CLOTHING LIST");
        parseList(religionList, "religions", "RELIGION LIST");
        parseList(siteList, "websites", "WEBSITE LIST");
        parseList(hairList, "hairstyles", "HAIR STYLE LIST");
        
        monthList.addAll(Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"));
        dayList.addAll(Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));                        
        
        parsePhraseLibrary();
        
        PhraseWordConsumer professionConsumer = new PhraseWordConsumer(new String[]{"profession"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("PROFESSION")){
                        input.tokens[i] = randomStringFromList(professionList);
                        
                        if (input.tokens[i].startsWith("A") || input.tokens[i].startsWith("E") || input.tokens[i].startsWith("I") ||
                                input.tokens[i].startsWith("O") || input.tokens[i].startsWith("U")){
                            if (i > 0) {
                                input.tokens[i - 1] = "an";
                            }
                        }
                        
                        input.replacementMap.put("profession", input.tokens[i]);
                    }
                }
            }
        }.register();
        
        PhraseWordConsumer familyConsumer = new PhraseWordConsumer(new String[]{"family"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("FAMILY")){
                        input.tokens[i] = randomStringFromList(familyList);
                        input.replacementMap.put("family", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer subculture = new PhraseWordConsumer(new String[]{"subculture"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("SUBCULTURE")){
                        input.tokens[i] = randomStringFromList(subcultureList);
                        input.replacementMap.put("subculture", input.tokens[i]);
                    }
                }
            }
        }.register();
        
        PhraseWordConsumer nounConsumer = new PhraseWordConsumer(new String[]{"noun"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("NOUN")){
                        String mode = input.replacementParameters.getOrDefault("mode", "");
                        switch (mode){
                            case "plural":
                                input.tokens[i] = randomStringFromList(pluralNounList);
                                break;
                            case "random":
                                input.tokens[i] = randomStringFromList(Constants.rand.nextInt(200) > 100 ? nounList : pluralNounList);
                                break;
                            default:
                                input.tokens[i] = randomStringFromList(nounList);
                        }
                        input.replacementMap.put("noun", input.tokens[i]);
                    }
                }
            }
        }.register();
        
        PhraseWordConsumer foodConsumer = new PhraseWordConsumer(new String[]{"food"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("FOOD")){
                        input.tokens[i] = randomStringFromList(foodList);
                        input.replacementMap.put("food", input.tokens[i]);
                    }
                }
            }
        }.register();
        
        PhraseWordConsumer monthConsumer = new PhraseWordConsumer(new String[]{"month"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("MONTH")){
                        input.tokens[i] = randomStringFromList(monthList);
                        input.replacementMap.put("month", input.tokens[i]);
                    }
                }
            }
        }.register();
        
        PhraseWordConsumer dayConsumer = new PhraseWordConsumer(new String[]{"day"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("DAY")){
                        input.tokens[i] = randomStringFromList(dayList);
                        input.replacementMap.put("day", input.tokens[i]);
                    }
                }
            }
        }.register();
        
        PhraseWordConsumer placeConsumer = new PhraseWordConsumer(new String[]{"place"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("PLACE")){
                        input.tokens[i] = randomStringFromList(placeList);
                        input.replacementMap.put("place", input.tokens[i]);
                    }
                }
            }
        }.register();
        
        PhraseWordConsumer countryConsumer = new PhraseWordConsumer(new String[]{"country"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("COUNTRY")){
                        input.tokens[i] = randomStringFromList(countryList);
                        input.replacementMap.put("country", input.tokens[i]);
                    }
                }
            }
        }.register();
        
        PhraseWordConsumer traitConsumer = new PhraseWordConsumer(new String[]{"trait"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("TRAIT")){
                        input.tokens[i] = randomStringFromList(traitList);
                        input.replacementMap.put("trait", input.tokens[i]);
                    }
                }
            }
        }.register();
        
        PhraseWordConsumer genreConsumer = new PhraseWordConsumer(new String[]{"genre"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("GENRE")){
                        input.tokens[i] = randomStringFromList(genreList);
                        input.replacementMap.put("genre", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer businessConsumer = new PhraseWordConsumer(new String[]{"business"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("BUSINESS")){
                        input.tokens[i] = randomStringFromList(businessList);
                        input.replacementMap.put("business", input.tokens[i]);
                    }
                }
            }
        }.register();
        
        PhraseWordConsumer animalConsumer = new PhraseWordConsumer(new String[]{"animal"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("ANIMAL")){
                        input.tokens[i] = randomStringFromList(animalList);
                        input.replacementMap.put("animal", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer drinkConsumer = new PhraseWordConsumer(new String[]{"drink"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("DRINK")){
                        input.tokens[i] = randomStringFromList(drinkList);
                        input.replacementMap.put("drink", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer bandConsumer = new PhraseWordConsumer(new String[]{"band"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("BAND")){
                        input.tokens[i] = randomStringFromList(bandList);
                        input.replacementMap.put("band", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer vGerundConsumer = new PhraseWordConsumer(new String[]{"verb", "gerund"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("VERB")){
                        input.tokens[i] = randomStringFromList(verbList);
                        input.replacementMap.put("verb", input.tokens[i]);
                    }else if (input.tokens[i].equalsIgnoreCase("GERUND")){
                        input.tokens[i] = randomStringFromList(gerundList);
                        input.replacementMap.put("gerund", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer famousConsumer = new PhraseWordConsumer(new String[]{"famous"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("FAMOUS")){
                        input.tokens[i] = randomStringFromList(famousList);
                        input.replacementMap.put("famous", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer numberConsumer = new PhraseWordConsumer(new String[]{"number"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("NUMBER")){
                        //get parameters here
                        int min = Integer.parseInt(input.replacementParameters.getOrDefault("min", "0"));
                        int max = Integer.parseInt(input.replacementParameters.getOrDefault("max", "0"));
                        input.tokens[i] = Integer.toString(Constants.rand.nextInt(max) - min);
                        input.replacementMap.put("NUMBER", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer nationalityConsumer = new PhraseWordConsumer(new String[]{"nationality"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("NATIONALITY")){
                        //check mode = if mode is noun (default) or adjective
                        input.tokens[i] = (input.replacementParameters.getOrDefault("mode", "noun").equals("noun") ? randomStringFromList(nationalityNounsList) : randomStringFromList(nationalityAdjectivesList));
                        input.replacementMap.put("nationality", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer movieConsumer = new PhraseWordConsumer(new String[]{"movie"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("MOVIE")){
                        input.tokens[i] = randomStringFromList(movieList);
                        input.replacementMap.put("movie", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer adjectiveConsumer = new PhraseWordConsumer(new String[]{"adjective"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("ADJECTIVE")){
                        input.tokens[i] = randomStringFromList(adjectiveList);
                        input.replacementMap.put("adjective", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer checkConsumer = new PhraseWordConsumer(new String[]{"checkbox"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("CHECKBOX")){
                        input.tokens[i] = Character.toString(Constants.rand.nextInt(100) > 50 ? '\u2610' : '\u2611');
                    }
                }
            }
        }.register();
        PhraseWordConsumer randomConsumer = new PhraseWordConsumer(new String[]{"random"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("RANDOM")){
                        Object[] ideas = input.replacementParameters.values().toArray();
                        String id = (String) ideas[Constants.rand.nextInt(ideas.length)];                        
                        PhraseWordConsumer eat = consumers.get(id.toLowerCase());
                        if (eat != null && eat != this){
                            input.tokens[i] = id.toUpperCase();
                            eat.executeReplacement(input);
                        }
                        if (input.tokens[i].equals("RANDOM")){
                            input.tokens[i] = randomStringFromList(nounList);
                        }
                    }
                }
            }
        }.register();
        PhraseWordConsumer clothingConsumer = new PhraseWordConsumer(new String[]{"clothing"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("CLOTHING")){
                        input.tokens[i] = randomStringFromList(clothingList);
                        input.replacementMap.put("clothing", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer religionConsumer = new PhraseWordConsumer(new String[]{"religion"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("RELIGION")){
                        input.tokens[i] = randomStringFromList(religionList);
                        input.replacementMap.put("religion", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer websiteConsumer = new PhraseWordConsumer(new String[]{"website"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("WEBSITE")){
                        input.tokens[i] = randomStringFromList(siteList);
                        input.replacementMap.put("website", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer hairstyleConsumer = new PhraseWordConsumer(new String[]{"hairstyle"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("HAIRSTYLE")){
                        input.tokens[i] = randomStringFromList(hairList);
                        input.replacementMap.put("hairstyle", input.tokens[i]);
                    }
                }
            }
        }.register();
        PhraseWordConsumer wikiConsumer = new PhraseWordConsumer(new String[]{"wiki"}){
            @Override            
            public void executeReplacement(PhraseReplacementData input) {
                for (int i = 0; i < input.tokens.length; i++){
                    if (input.tokens[i].equalsIgnoreCase("WIKI")){
                        try {
                            URLConnection uConn = new URL("https://en.wikipedia.org/w/api.php?"
                                    + "format=json&action=query&prop=extracts&plaintext=1&generator=random&grnnamespace=0&exintro=1").
                                    openConnection();
                            String line;
                            StringBuilder builder = new StringBuilder();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(uConn.getInputStream()), 1);
                            while((line = reader.readLine()) != null) {
                                builder.append(line);
                            }
                            reader.close();
                            
                            JSONObject jWiki = (JSONObject) new JSONParser().parse(builder.toString());
                            JSONObject qu = (JSONObject) jWiki.get("query");
                            JSONObject pg = (JSONObject) qu.get("pages");
                            JSONObject ex = (JSONObject) pg.values().toArray()[0];
                            
                            Pattern sentencePat = Pattern.compile("^.*?[\\.!\\?](?:\\s|$)");
                            String toUse = (String) ex.get("extract");
                            toUse = toUse.replaceAll("[\\s]+", " ");
                            Matcher mt = sentencePat.matcher(toUse);
                            if (mt.find()){
                                toUse = mt.group();
                            }
                            toUse = toUse.replaceAll("\\<[^>]*>", "");
                            
                            input.tokens[i] = toUse;
                            input.replacementMap.put("wiki", input.tokens[i]);
                                                        
                        } catch (MalformedURLException ex) {
                            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParseException ex) {
                            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }.register();
    }
    
    public String randomStringFromList(ArrayList<String> source){
        return source.get(Constants.rand.nextInt(source.size()));
    }
    
    public PostTemplate getRandomTemplate(){
        return postTemplates.get(Constants.rand.nextInt(postTemplates.size()));
    }
    public PostTemplate getNamedTemplate(String st){
        for (PostTemplate pt : postTemplates){
            if (pt.templateName.equalsIgnoreCase(st)){
                return pt;
            }
        }
        return getRandomTemplate();
    }
    
    public void parseList(ArrayList<String> target, String fileTarget, String desc){
        try {
            //parse the text files
            BufferedReader bReader = new BufferedReader(new FileReader(new File("resources/" + fileTarget + ".txt")));
            String ln = "";
            
            while ((ln = bReader.readLine()) != null){
                target.add(ln);
            }
            
            bReader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        consoleLog("Parsed " + desc + " list of " + target.size());
    }
    
    public GeneratedPostData quickEvalTemplate(PostTemplate p){
        GeneratedPostData gpData = new GeneratedPostData();
        
        PhraseReplacementData[] out = new PhraseReplacementData[p.lines.length];
        gpData.generatedData = out;
        
        for (int i = 0; i < p.lines.length; i++){
            String pLineTemplate = p.lines[i].lineText;
            if (pLineTemplate.equalsIgnoreCase("RLINE")){
                //get a random template
                int counter = 0;
                while (pLineTemplate.equalsIgnoreCase("RLINE")){                    
                    PostTemplate ptem = getRandomTemplate();
                    pLineTemplate = ptem.lines[Constants.rand.nextInt(ptem.lines.length)].lineText;
                    counter++;
                    if (counter > 5){
                        pLineTemplate = "lmoa";
                    }
                }
            }
            
            
            String replTemplate = pLineTemplate;
            HashMap<String, String> vals = new HashMap<>();
            Matcher parm = replaceParams.matcher(replTemplate);
            ArrayList<String> toRemove = new ArrayList<>();
            
            while(parm.find()){
                //correct formation is key:value, so we split and add if it contains
                String grp = parm.group(1);
                if (grp.contains(":")){
                    String[] kv = grp.split(":");
                    vals.put(kv[0], kv[1]);
                }
                toRemove.add(parm.group());
            }
            
            //System.out.println("Parameters for replacement: " + vals);
            
            for (String str : toRemove){
                replTemplate = replTemplate.replace(str, "");
            }
            
            PhraseReplacementData prd = new PhraseReplacementData(replTemplate);
            prd.replacementParameters = vals;
            consumers.values().stream().forEach((pwc) -> {
                //fetch param group                
                pwc.executeReplacement(prd);
            });
                        
            out[i] = prd;
            out[i].rawText = "";
            for (String s : prd.tokens){
                out[i].rawText += s + " ";
            }
            out[i].rawText = out[i].rawText.trim();            
        }
        
        LinkedHashMap <String, String> rData = new LinkedHashMap<>();
        gpData.replacementTokenData = rData;
        for (PhraseReplacementData rdt : out){
            rData.putAll(rdt.replacementMap);
        }        
        Object[] dataList = rData.values().toArray();
        
        for (int pl = 0; pl < out.length; pl++){            
            PhraseReplacementData outRaw = out[pl];
            Matcher matches = findVar.matcher(outRaw.rawText);
            while (matches.find()){
                String checker = matches.group();
                //get the number
                int replacementNum = Integer.parseInt(checker.substring(1)) - 1;
                if (replacementNum < dataList.length){
                    outRaw.rawText = outRaw.rawText.replace(checker, (CharSequence) dataList[replacementNum]);
                }else{
                    Main.consoleLog("We will ignore this token: " + checker);
                }
            }
            if (p.lines[pl].allTextUpper){
                out[pl].rawText = out[pl].rawText.toUpperCase();
            }
        }
                
        return gpData;
    }
    
    void registerWordConsumer(PhraseWordConsumer pwc){        
        for (String s : pwc.acceptedWords){
            consumers.put(s, pwc);
        }
        
    }
    public void savePhraseLibrary(){
        JSONArray jArray = new JSONArray();
        for (PostTemplate pt : postTemplates){
            JSONObject pB = new JSONObject();
            pB.put("templateName", pt.templateName);
            JSONArray pElements = new JSONArray();
            for (PostTemplateLine pl : pt.lines){
                if (!pl.lineText.isEmpty()){
                    JSONObject jLine = new JSONObject();
                    jLine.put("text", pl.lineText);
                    jLine.put("allUpper", pl.allTextUpper);
                    pElements.add(jLine);
                }
            }
            pB.put("lines", pElements);
            jArray.add(pB);
        }
        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("resources/libraries/post_templates.dat")));
            bw.write(jArray.toJSONString());
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(PhraseLibrary.class.getName()).log(Level.SEVERE, null, ex);
            Main.consoleLog("Saving of post template library failed: " + ex.getMessage());
        }
        Main.consoleLog("Post template library saved successfully.");
    }
    public void parsePhraseLibrary(){
        JSONParser jP = new JSONParser();
        JSONArray jArray;
        try {
            jArray = (JSONArray) jP.parse(new FileReader(new File("resources/libraries/post_templates.dat")));
            
            for (Object jO : jArray){
                JSONObject jsO = (JSONObject) jO;
                PostTemplate pT = new PostTemplate();
                pT.templateName = (String) jsO.get("templateName");
                JSONArray pE = (JSONArray) jsO.get("lines");
                pT.lines = new PostTemplateLine[pE.size()];
                for (int i = 0; i < pE.size(); i++){
                    pT.lines[i] = new PostTemplateLine();
                    JSONObject lineE = (JSONObject) pE.get(i);
                    pT.lines[i].allTextUpper = (boolean) lineE.getOrDefault("allUpper", false);
                    pT.lines[i].lineText = (String) lineE.getOrDefault("text", "");
                }
                postTemplates.add(pT);
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

abstract class PhraseWordConsumer{
    String[] acceptedWords = {};    

    public PhraseWordConsumer(String[] words) {
        acceptedWords = words;
    }
    public abstract void executeReplacement(PhraseReplacementData prd);
    public PhraseWordConsumer register(){
        Main.pLibrary.registerWordConsumer(this);
        return this;
    }
}
