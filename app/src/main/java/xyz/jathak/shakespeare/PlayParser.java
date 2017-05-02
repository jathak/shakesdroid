package xyz.jathak.shakespeare;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by jathak on 1/11/14.
 */
public class PlayParser {
    public static Play parsePlay(Context ctx, String playName){
        XmlResourceParser xrp = null;
        Play play = null;
        for(int i=0;i<ScriptActivity.playList.length;i++){
            if(ScriptActivity.playList[i].equals(playName))play=new Play(ScriptActivity.playNames[i]);
        }
        if(play==null)return null;
        Act currentAct = null;
        Scene currentScene = null;
        int linenum = 0;
        Speech currentSpeech = null;
        Line currentLine = null;
        String currentType = "";
        String lastType = "";
        boolean inSpeech = false;
        boolean inLine = false;
        boolean inPGroup = false;
        try {
            xrp = ctx.getAssets().openXmlResourceParser("res/xml/"+playName+".xml");
            int eventType = xrp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT){
                if(eventType == XmlPullParser.START_DOCUMENT) {

                } else if(eventType == XmlPullParser.START_TAG) {
                    currentType=xrp.getName().trim();
                    //addLog("start: "+xrp.getName());
                    if(currentType.equals("ACT")||currentType.equals("INDUCT")){
                        currentAct = new Act();
                    }
                    if(currentType.equals("SCENE")||currentType.equals("PROLOGUE")||currentType.equals("EPILOGUE")){
                        currentScene = new Scene();
                        linenum=0;
                    }
                    if(currentType.equals("SPEECH")){
                        currentSpeech = new Speech();
                    }
                    if(currentType.equals("LINE")){
                        currentLine = new Line();
                        currentLine.type = Line.TYPE_SPOKEN;
                    }
                    if(currentType.equals("STAGEDIR")){
                        inSpeech = lastType.equals("SPEECH");
                        inLine = lastType.equals("LINE");
                        if(!inLine){
                            currentLine = new Line();
                            currentLine.type = Line.TYPE_DIRECTION;
                        }else currentLine.type = Line.TYPE_COMBO;
                    }
                    if(currentType.equals("PGROUP"))inPGroup=true;
                    lastType=xrp.getName();
                } else if(eventType == XmlPullParser.END_TAG) {
                    currentType=xrp.getName().trim();
                    //addLog("end: "+xrp.getName());
                    if(lastType.equals("SPEECH"))lastType="";
                    if(lastType.equals("LINE"))lastType="SPEECH";
                    if(lastType.equals("STAGEDIR")&&inLine)lastType="LINE";
                    if(lastType.equals("STAGEDIR")&&inSpeech)lastType="SPEECH";
                    if(currentType.equals("PGROUP")){
                        inPGroup=false;
                        play.personae+="\n";
                    }
                    if(currentType.equals("INDUCT")){
                        play.acts.add(currentAct);
                        play.hasInduction=true;
                    }
                    if(currentType.equals("ACT")){
                        play.acts.add(currentAct);
                    }
                    if(currentType.equals("SCENE")){
                        currentAct.scenes.add(currentScene);
                    }else if(currentType.equals("PROLOGUE")){
                        currentAct.scenes.add(currentScene);
                        currentAct.hasPrologue=true;
                    }else if(currentType.equals("EPILOGUE")){
                        currentAct.scenes.add(currentScene);
                        currentAct.hasEpilogue=true;
                    }
                    if(currentType.equals("SPEECH")){
                        currentSpeech.actIndex = play.acts.size();
                        currentSpeech.sceneIndex = currentAct.scenes.size();
                        currentScene.speeches.add(currentSpeech);
                    }
                    if(currentType.equals("LINE")){
                        currentSpeech.lines.add(currentLine);
                    }
                    if(currentType.equals("STAGEDIR")){
                        if(inSpeech){
                            currentSpeech.lines.add(currentLine);
                            currentType="SPEECH";
                        }
                        else if(!inLine){
                            Speech s = new Speech();
                            s.lines.add(currentLine);
                            s.actIndex = play.acts.size();
                            s.sceneIndex = currentAct.scenes.size();
                            currentScene.speeches.add(s);
                        }else currentType="LINE";
                    }else currentType="";
                } else if(eventType == XmlPullParser.TEXT) {
                    //addLog(":     "+xrp.getText());
                    String text = xrp.getText().trim();
                    text = text.replaceAll("\\&c","etc");
                    //if(text.contains("little more than kin, and less")){
                      //  System.out.println(currentType+";"+lastType+";"+text);
                    //}
                    text = text.replaceAll("\n"," ");

                    if(currentType.equals("PERSONA")){
                        String[] parts = text.split(",");
                        /*if(parts.length==1)play.personae+="<font face='sans-serif-condensed'>"+text+"</font><br>";
                        if(parts.length==2)play.personae+="<font face='sans-serif-condensed'>"+parts[0]+"</font>,"+parts[1]+"<br>";
                        if(parts.length>=3)*/play.personae+=text+"<br>";
                        if(!inPGroup)play.personae+="<br>";
                    }
                    if(currentType.equals("GRPDESCR")){
                        play.personae+=text+"<br><br>";
                    }
                    if(currentType.equals("SPEAKER")){
                       currentSpeech.speakers.add(text);
                    }
                    if(currentType.equals("LINE")){
                        currentLine.spoken = text;
                        linenum++;
                        currentLine.linenum=linenum;
                    }
                    if(currentType.equals("STAGEDIR")){
                        currentLine.direction = text;
                    }
                }
                eventType = xrp.next();
            }
            play.personae = play.personae.replaceAll("@#","<font face='sans-serif-condensed'>");
            play.personae = play.personae.replaceAll("#@","</font>");
            SharedPreferences notes = ctx.getSharedPreferences(ScriptActivity.playName+"Notes", 0);
            SharedPreferences marks = ctx.getSharedPreferences(ScriptActivity.playName+"Marks", 0);
            for(Act act:play.acts){
                for(Scene scene:act.scenes){
                    for(Speech speech:scene.speeches){
                        if(speech.speakers.size()>0){
                            String ident = getIdent(speech,play);
                            speech.ident = ident;
                            String note = notes.getString(ident,null);
                            if(note!=null&&note!=""){
                                speech.hasNotes=true;
                                speech.note=note;
                            }else speech.hasNotes=false;
                            speech.bookmarked=marks.getBoolean(ident,false);
                        }
                    }
                }
            }
            //System.out.println(log);
            return play;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }
    static String log = "";
    private static void addLog(String s){
        //log+="\n"+s;
    }
    public static String getIdent(Speech speech, Play play){
        Act a = play.acts.get(speech.actIndex);
        Scene s = a.scenes.get(speech.sceneIndex);
        int act = speech.actIndex;
        int scene = speech.sceneIndex;
        if(!play.hasInduction)act++;
        if(!a.hasPrologue)scene++;
        int startLine = speech.lines.get(0).linenum;
        int endLine = speech.lines.get(speech.lines.size()-1).linenum;
        return act+"."+scene+"."+startLine+"-"+endLine;
    }
}
