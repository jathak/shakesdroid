package xyz.jathak.shakespeare;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.timroes.android.listview.EnhancedListView;

/**
 * Created by jathak on 2/5/14.
 */
public class BookmarksFragment extends Fragment {
    private List<Speech> speeches = new ArrayList<Speech>();

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_marks,null);
        boolean lightTheme = v.getContext().getSharedPreferences("app",0).getBoolean("lightTheme", true);
        if(lightTheme){
            v.setBackgroundResource(R.color.lightBack);
        }else{
            v.setBackgroundResource(R.color.darkBack);
        }
        findSpeeches(ScriptActivity.play);
        setupList(v);
        return v;
    }
    private void findSpeeches(Play play){
        for(Act act:play.acts){
            for(Scene scene:act.scenes){
                for(Speech speech:scene.speeches){
                    if(speech.bookmarked){
                        speeches.add(speech);
                    }
                }
            }
        }
    }


    private void setupList(View v){
        EnhancedListView lv = (EnhancedListView)v.findViewById(R.id.list);
        lv.setSwipeDirection(EnhancedListView.SwipeDirection.END);
        lv.setDividerHeight(0);
        lv.setFastScrollEnabled(true);
        lv.setAdapter(new SpeechAdapter(getActivity(), speeches));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Speech speech = speeches.get(index);
                ScriptActivity.SceneID id = new ScriptActivity.SceneID(speech.actIndex,speech.sceneIndex);
                List<Scene> scenes = ScriptActivity.play.acts.get(id.act).scenes;
                Scene scene = scenes.get(id.scene);
                int speechIndex = 0;
                for (int i = 0; i < scene.speeches.size(); i++) {
                    if (speech.lines.get(0).linenum == scene.speeches.get(i).lines.get(0).linenum) {
                        speechIndex = i;
                        //System.out.println("Index: "+speechIndex);
                        break;
                    }
                }
                int sceneIndex = id.scene+1;
                for (int i = 0; i < id.act; i++) {
                    sceneIndex += ScriptActivity.play.acts.get(i).scenes.size();
                }
                Intent i = getActivity().getIntent();
                i.putExtra("speechIndex", speechIndex);
                i.putExtra("sceneIndex", sceneIndex);
                getActivity().setResult(Activity.RESULT_OK, i);
                getActivity().finish();
            }
        });
        lv.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(final EnhancedListView listview,final int i) {
                lastSpeech = speeches.remove(i);
                //Toast.makeText(getActivity(), "Removed from bookmarks", Toast.LENGTH_SHORT).show();
                ((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        speeches.add(i,lastSpeech);
                        ((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
                    }
                    @Override
                    public String getTitle(){
                        return "Bookmark Deleted";
                    }
                    @Override
                    public void discard() {
                        SharedPreferences prefs = getActivity().getSharedPreferences(ScriptActivity.playName+"Marks", 0);
                        SharedPreferences.Editor e = prefs.edit();
                        e.putBoolean(lastSpeech.ident,false);
                        e.commit();
                        lastSpeech.bookmarked=false;
                    }
                };
            }
        });
        lv.enableSwipeToDismiss();
    }
    private Speech lastSpeech = null;
    @Override
    public void onPause(){
        super.onPause();
        if(lastSpeech!=null){
            SharedPreferences prefs = getActivity().getSharedPreferences(ScriptActivity.playName+"Marks", 0);
            SharedPreferences.Editor e = prefs.edit();
            e.putBoolean(lastSpeech.ident,false);
            e.commit();
            lastSpeech.bookmarked=false;
        }
    }
    private class SpeechAdapter extends BaseAdapter {
        private Context ctx;
        private List<Speech> speeches;
        public SpeechAdapter(Context context,List<Speech> speeches) {
            ctx = context;
            this.speeches=speeches;
        }

        @Override
        public int getCount() {
            return speeches.size();
        }

        @Override
        public Object getItem(int i) {
            return speeches.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            View v = convertView;
            Speech speech = speeches.get(i);
            //System.out.println(scene.speeches.size()+";"+speech.lines.size());
            if(v==null){
                v=((LayoutInflater)ctx.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.old_card, null);
            }
            TextView speaker = (TextView)v.findViewById(R.id.speaker);
            TextView text = (TextView)v.findViewById(R.id.text);
            TextView directions = (TextView)v.findViewById(R.id.directions);
            TextView linenum = (TextView)v.findViewById(R.id.linenum);
            View card = v.findViewById(R.id.card);
            boolean lightTheme = ctx.getSharedPreferences("app",0).getBoolean("lightTheme", true);
            if(lightTheme){
                card.setBackgroundResource(R.drawable.card_background);
            }else{
                card.setBackgroundResource(R.drawable.card_dark);
            }
            List<Line> sl = speech.lines;
            linenum.setText(speech.ident);
            card.setVisibility(View.VISIBLE);
            directions.setVisibility(View.GONE);
            String sp = speech.speakers.get(0);
            for(int j=1;j<speech.speakers.size();j++){
                sp+="/"+speech.speakers.get(j);
            }
            String lines = "";
            for(Line l:speech.lines){
                lines+="<br>"+l.texti();
            }
            lines=lines.substring(4);
            speaker.setText(sp);
            text.setText(Html.fromHtml(lines));
            return v;
        }
    }
}
