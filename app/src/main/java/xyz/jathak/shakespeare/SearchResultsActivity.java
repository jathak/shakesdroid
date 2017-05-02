package xyz.jathak.shakespeare;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jathak on 1/14/14.
 */
public class SearchResultsActivity extends Activity {
    private String lastQuery;
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
        setContentView(R.layout.fragment_marks);
        View rootView = findViewById(R.id.rootView);
        if(lightTheme){
            rootView.setBackgroundResource(R.color.lightBack);
        }else{
            rootView.setBackgroundResource(R.color.darkBack);
        }
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String query = intent.getStringExtra(SearchManager.QUERY);
        //getActionBar().setTitle("Search: \""+query+"\"");
        getActionBar().setDisplayShowTitleEnabled(false);
        //String query2=punctuate(query);
        lastQuery=query;
        findSpeeches(ScriptActivity.play, query.toLowerCase());
        setupList(query.toLowerCase());
    }
    private String punctuate(String query){
        String punct = "[\\.\\$\\?\\(\\),'\":\\-;]?";
        char[] chars = query.toLowerCase().toCharArray();
        String punctured = chars[0]+"";
        for(int i=1;i<chars.length;i++){
            punctured+=punct+chars[i];
        }
        System.out.println("punct: "+punctured);
        return punctured;
    }
    private List<Speech> speeches = new ArrayList<Speech>();
    private List<ScriptActivity.SceneID> ids = new ArrayList<ScriptActivity.SceneID>();
    private void findSpeeches(Play play,String query){
        for(int a=0;a<play.acts.size();a++){
            for(int s=0;s<play.acts.get(a).scenes.size();s++){
                for(int p=0;p<play.acts.get(a).scenes.get(s).speeches.size();p++){
                    Speech speech = play.acts.get(a).scenes.get(s).speeches.get(p);
                    for(int l=0;l<speech.lines.size();l++){
                        if(speech.lines.get(l).spoken.toLowerCase().contains(query)){
                            speeches.add(speech);
                            ids.add(new ScriptActivity.SceneID(a+1,s+1));
                            break;
                        }
                    }
                }
            }
        }
    }

    private void setupList(String query){
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setDividerHeight(0);
        lv.setFastScrollEnabled(true);
        lv.setAdapter(new SpeechAdapter(this,speeches,ids,query));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                ScriptActivity.SceneID id = ids.get(index);
                Speech speech = speeches.get(index);
                List<Scene> scenes = ScriptActivity.play.acts.get(id.act-1).scenes;
                Scene scene = scenes.get(id.scene-1);
                int speechIndex = 0;
                for(int i=0;i<scene.speeches.size();i++){
                    if(speech.lines.get(0).linenum==scene.speeches.get(i).lines.get(0).linenum){
                        speechIndex=i;
                        //System.out.println("Index: "+speechIndex);
                        break;
                    }
                }
                int sceneIndex = id.scene;
                for(int i=0;i<id.act-1;i++){
                    sceneIndex+=ScriptActivity.play.acts.get(i).scenes.size();
                }
                Intent i = getIntent();
                i.putExtra("speechIndex",speechIndex);
                i.putExtra("sceneIndex",sceneIndex);
                setResult(RESULT_OK,i);
                finish();
            }
        });
    }
    //private boolean searching = false;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem item = menu.findItem(R.id.search);
        final SearchView searchView =(SearchView) item.getActionView();
        item.expandActionView();
        searchView.clearFocus();
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                searchView.setQuery(getIntent().getStringExtra(SearchManager.QUERY),false);
                searchView.setQueryHint("Search " + ScriptActivity.play.name);
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                finish();
                //menuItem.expandActionView();
                //searchView.clearFocus();
                return false;
            }
        });
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQuery(getIntent().getStringExtra(SearchManager.QUERY),false);
        searchView.setQueryHint("Search " + ScriptActivity.play.name);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                speeches.clear();
                ids.clear();
                searchView.clearFocus();
                String query = s;
                findSpeeches(ScriptActivity.play,query.toLowerCase());
                setupList(query.toLowerCase());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

    private class SpeechAdapter extends BaseAdapter {
        private Context ctx;
        private List<Speech> speeches;
        private List<ScriptActivity.SceneID> ids;
        private String query;
        public SpeechAdapter(Context context,List<Speech> speeches,List<ScriptActivity.SceneID> ids,String query) {
            ctx = context;
            this.speeches=speeches;
            this.ids=ids;
            this.query=query;
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
            int act = ids.get(i).act;
            Act cAct = ScriptActivity.play.acts.get(act-1);
            int scene = ids.get(i).scene;
            if(cAct.hasPrologue)scene--;
            //String sceneStr = scene+"";
            //if(cAct.hasEpilogue&&i==cAct.scenes.size()-1)sceneStr="E";
            if(ScriptActivity.play.hasInduction)act--;
            List<Line> sl = speech.lines;
            if(sl.size()>1)linenum.setText(act+"."+scene+"." + sl.get(0).linenum+"-"+sl.get(sl.size()-1).linenum);
            else linenum.setText(act+"."+scene+"." + sl.get(0).linenum);
            card.setVisibility(View.VISIBLE);
            directions.setVisibility(View.GONE);
            String sp = speech.speakers.get(0);
            for(int j=1;j<speech.speakers.size();j++){
                sp+="/"+speech.speakers.get(j);
            }
            speaker.setText(sp);
            String lines = "";
            for(Line l:speech.lines){
                lines+="<br>"+l.texti();
            }
            lines=lines.substring(4);
            lines= lines.replaceAll("(?i)"+query,"<b><font face='sans-serif'>$0</font></b>");
            text.setText(Html.fromHtml(lines));
            return v;
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}