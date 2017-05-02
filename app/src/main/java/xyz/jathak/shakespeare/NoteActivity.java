package xyz.jathak.shakespeare;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by jathak on 1/14/14.
 */
public class NoteActivity extends Activity {
    private String lastQuery;
    public static Speech speech;
    private EditText edit;
    private String title;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean lightTheme = getSharedPreferences("app",0).getBoolean("lightTheme",true);
        String play = getSharedPreferences("app",0).getString("lastPlay","romeo");
        if(lightTheme){
            setTheme(Play.getLightTheme(play));
        }else{
            setTheme(Play.getDarkTheme(play));
        }
        setContentView(R.layout.activity_note);
        View rootView = findViewById(R.id.rootView);
        if(lightTheme){
            rootView.setBackgroundResource(R.color.lightBack);
        }else{
            rootView.setBackgroundResource(R.color.darkBack);
        }
        title = getIntent().getStringExtra("title");
        getActionBar().setTitle("Notes on " + title);
        setupSpeech();
        edit = (EditText)findViewById(R.id.editText);
        SharedPreferences prefs = getSharedPreferences(ScriptActivity.playName+"Notes",0);
        if(prefs.getString(title,null)!=null)edit.setText(prefs.getString(title,""));
        edit.setTypeface(Typeface.createFromAsset(getAssets(),"slab.ttf"));
        edit.setSelection(edit.getText().length());
    }

    @Override
    public void onPause(){
        super.onPause();
        SharedPreferences.Editor e = getSharedPreferences(ScriptActivity.playName+"Notes",0).edit();
        if(edit.getText().toString().trim().equals("")){
            e.putString(title,null);
            speech.hasNotes=false;
            speech.note=null;
        }else {
            speech.note = edit.getText().toString().trim();
            speech.hasNotes = true;
            e.putString(title,speech.note);

        }
        e.commit();
    }


    @Override
    public void onResume(){
        super.onResume();
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    public void setupSpeech(){
        TextView speaker = (TextView)findViewById(R.id.speaker);
        TextView text = (TextView)findViewById(R.id.text);
        TextView directions = (TextView)findViewById(R.id.directions);
        TextView linenum = (TextView)findViewById(R.id.linenum);
        text.setLineSpacing(0f,1.1f);
        linenum.setLineSpacing(0f,1.1f);
        View card = findViewById(R.id.card);
        boolean lightTheme = getSharedPreferences("app",0).getBoolean("lightTheme", true);
        if(lightTheme){
            card.setBackgroundResource(R.drawable.card_background);
        }else{
            card.setBackgroundResource(R.drawable.card_dark);
        }
        speaker.bringToFront();
        text.bringToFront();
        if(speech.speakers.size()==0){
            card.setVisibility(View.GONE);
            speaker.setVisibility(View.GONE);
            text.setVisibility(View.GONE);
            linenum.setVisibility(View.GONE);
            directions.setVisibility(View.VISIBLE);
            directions.setText(speech.lines.get(0).direction);
        }else{
            linenum.setVisibility(View.VISIBLE);
            card.setVisibility(View.VISIBLE);
            speaker.setVisibility(View.VISIBLE);
            text.setVisibility(View.VISIBLE);
            directions.setVisibility(View.GONE);
            String sp = speech.speakers.get(0);
            for(int j=1;j<speech.speakers.size();j++){
                sp+="/"+speech.speakers.get(j);
            }
            speaker.setText(sp);
            String lines = "";
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int maxwidth = dm.widthPixels-(int)Math.ceil(46 * dm.density);
            for(Line l:speech.lines){
                int width = ScriptActivity.textLength(l.text(),this);
                if(width>maxwidth&&l.type==Line.TYPE_SPOKEN){
                    String[] words = l.text().split(" ");
                    if(words.length>1){
                        String firstLine = words[0];
                        int index = 1;
                        while(true){
                            if(index>=words.length){
                                lines+="<br>"+firstLine;
                                l.wraps=false;
                            }
                            if(ScriptActivity.textLength(firstLine+" "+words[index],this)>maxwidth){
                                lines+="<br>"+firstLine+"<br>\t\t";
                                for(int j=index;j<words.length;j++){
                                    lines+=" "+words[j];
                                }
                                l.wraps=true;
                                break;
                            }else{
                                firstLine+=" "+words[index];
                                l.wraps=false;
                            }
                            index++;
                        }
                    }else lines+="<br>"+l.texti();
                }else lines+="<br>"+l.texti();
            }

            String linenumtext = "";
            for(int j=0;j<speech.lines.size();j++){
                int num = speech.lines.get(j).linenum;
                if(num!=0&&num%5==0){
                    linenumtext+="<font face='monospace'>"+num+"</font>";
                }
                if(speech.lines.get(j).wraps)linenumtext+="<br>";
                if(j!=speech.lines.size()-1)linenumtext+="<br>";
                if(speech.lines.get(j).type==Line.TYPE_DIRECTION){
                    if(ScriptActivity.monoLength(speech.lines.get(j).text(),this)>maxwidth)linenumtext+="<br>";
                    if(ScriptActivity.monoLength(speech.lines.get(j).text(),this)>maxwidth*2)linenumtext+="<br>";
                    if(ScriptActivity.monoLength(speech.lines.get(j).text(),this)>maxwidth*3)linenumtext+="<br>";
                    if(ScriptActivity.monoLength(speech.lines.get(j).text(),this)>maxwidth*4)linenumtext+="<br>";
                    if(ScriptActivity.monoLength(speech.lines.get(j).text(),this)>maxwidth*5)linenumtext+="<br>";
                    if(ScriptActivity.monoLength(speech.lines.get(j).text(),this)>maxwidth*6)linenumtext+="<br>";
                }
            }
            linenum.setText(Html.fromHtml(linenumtext));
            lines=lines.substring(4);
            text.setText(Html.fromHtml(lines));
        }
    }
}