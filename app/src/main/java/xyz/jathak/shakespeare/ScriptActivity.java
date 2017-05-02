package xyz.jathak.shakespeare;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

public class ScriptActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    public static Play play = new Play("");
    public static List<SceneID> ids = new ArrayList<SceneID>();
    public static String playName = "allswell";
    public static String[] playList;
    public static String[] playNames;
    private NfcAdapter mNfcAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean lightTheme = getSharedPreferences("app",0).getBoolean("lightTheme",true);
        String last = getSharedPreferences("app",0).getString("lastPlay","romeo");
        playName=last;
        if(lightTheme){
            setTheme(Play.getLightTheme(playName));
        }else{
            setTheme(Play.getDarkTheme(playName));
        }
        setContentView(R.layout.activity_script);
        playList = getResources().getStringArray(R.array.play_list);
        playNames = getResources().getStringArray(R.array.action_list);
        SpinnerAdapter mSpinnerAdapter = new BaseAdapter() {
            @Override
            public View getDropDownView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = LayoutInflater.from(ScriptActivity.this).inflate(R.layout.dropdown, null);
                }
                view.setBackgroundResource(Play.getPlayColorResource(playList[i]));
                ((TextView)view).setText(playNames[i]);
                return view;
            }

            @Override
            public int getCount() {
                return playNames.length;
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = LayoutInflater.from(ScriptActivity.this).inflate(R.layout.dropdown, null);
                }
                ((TextView)view).setText(playNames[i]);
                return view;
            }
        };
        ActionBar.OnNavigationListener mNavigationListener = new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                if(!playList[position].equals(playName)){
                    SharedPreferences p = getSharedPreferences("app",0);
                    SharedPreferences.Editor e = p.edit();
                    e.putString("lastPlay",playList[position]);
                    e.commit();
                    Intent i = new Intent(ScriptActivity.this,ScriptActivity.class);
                    startActivity(i);
                    finish();
                    return true;
                }
                return false;
            }
        };
        showLineNums = getSharedPreferences("app", 0).getBoolean("linenums", true);
        //getActionBar().setDisplayShowHomeEnabled(false);
        //getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(mSpinnerAdapter, mNavigationListener);
        getActionBar().setElevation(0);
        int index = 0;
        for(int i=0;i<playList.length;i++){
            if(playList[i].equals(last)){
                index=i;
                break;
            }
        }
        getActionBar().setSelectedNavigationItem(index);
        setup(last);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Register callback
        if(mNfcAdapter!=null)mNfcAdapter.setNdefPushMessageCallback(this, this);
        //setup(getSharedPreferences("app",0).getString("lastPlay",playList[0]));
    }

    @Override
    public void onResume(){
        super.onResume();
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        String message = new String(msg.getRecords()[0].getPayload());
        System.out.println("Received message: "+message);
        String[] parts = message.split(":");
        String play = parts[0];
        SharedPreferences p = getSharedPreferences("app",0);
        SharedPreferences.Editor e = p.edit();
        e.putString("lastPlay",play);
        e.commit();
        int sceneIndex = Integer.parseInt(parts[1]);
        int speechIndex = Integer.parseInt(parts[2]);
        Intent i = new Intent(this,getClass());
        i.putExtra("sceneIndex",sceneIndex);
        i.putExtra("speechIndex",speechIndex);
        startActivity(i);
        finish();
    }

    private void setup(final String name){
        SharedPreferences prefs = getSharedPreferences(name,0);
        play=new Play("");
        playName=name;
        play = PlayParser.parsePlay(this,playName);
        System.out.println("Setting up play " + playName);
        int color = Play.getPlayColorResource(playName);
        ids.clear();
        int cAct=1,cScene=1;
        for(Act a:play.acts){
            cScene=1;
            for(Scene s:a.scenes){
                ids.add(new SceneID(cAct,cScene));
                cScene++;
            }
            cAct++;
        }
        int sceneIndex = getIntent().getIntExtra("sceneIndex",-1);
        int speechIndex = getIntent().getIntExtra("speechIndex",-1);
        if(sceneIndex>=1&&speechIndex!=-1){
            SharedPreferences.Editor e = prefs.edit();
            SceneID id = ids.get(sceneIndex-1);
            e.putInt("a"+id.act+"s"+id.scene+"scroll",speechIndex);
            e.commit();
            System.out.println(sceneIndex+";"+speechIndex+";"+ids.get(sceneIndex-1).act+ids.get(sceneIndex-1).scene);
        }
        System.out.println(sceneIndex+";"+speechIndex+";outside");
        if(mSectionsPagerAdapter==null)mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        else mSectionsPagerAdapter.notifyDataSetChanged();

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);
        indicator.setCurrentItem(getIntent().getIntExtra("sceneIndex",prefs.getInt("lastPage",0)));
        indicator.setBackgroundResource(color);
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                SharedPreferences prefs = getSharedPreferences(playName,0);
                SharedPreferences.Editor e = prefs.edit();
                e.putInt("lastPage",i);
                e.commit();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.script, menu);
        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem item = menu.findItem(R.id.search);
        final SearchView searchView =(SearchView) item.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search " + play.name);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent i = new Intent(ScriptActivity.this,SearchResultsActivity.class);
                i.putExtra(SearchManager.QUERY, s);
                startActivityForResult(i, 10);
                item.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent i ){
        super.onActivityResult(requestCode, resultCode, i);
        System.out.println("Activity result: "+requestCode+";"+resultCode+"=="+RESULT_OK);
        if(requestCode==10&&resultCode==RESULT_OK){
            int sceneIndex = i.getIntExtra("sceneIndex",0);
            int speechIndex = i.getIntExtra("speechIndex",0);
            System.out.println("Received mark: "+sceneIndex+";"+speechIndex+"--"+mSectionsPagerAdapter.getCount());
            if(sceneIndex<mSectionsPagerAdapter.getCount()){
                mViewPager.setCurrentItem(sceneIndex);
                Fragment f = getFragmentManager().findFragmentByTag(getFragmentTag(mViewPager.getCurrentItem()));
                if(f instanceof SceneFragment){
                    System.out.println("Scrolling...");
                    ((SceneFragment)f).scrollTo(speechIndex);
                }
            }
        }
    }
    public static boolean showLineNums = true;

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        String text = playName+":"+mViewPager.getCurrentItem()+":"+getCurrentSpeech();
        NdefRecord[] records = {
                new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[0],
                        text.getBytes()),NdefRecord.createApplicationRecord("xyz.jathak.shakespeare")
        };
        NdefMessage msg = new NdefMessage(records);
        return msg;
    }

    public int getCurrentSpeech(){
        Fragment f = getFragmentManager().findFragmentByTag(getFragmentTag(mViewPager.getCurrentItem()));
        if(f instanceof SceneFragment){
            View v = f.getView().findViewById(R.id.list);
            if(v!=null&&v instanceof ListView){
                return ((ListView)v).getFirstVisiblePosition();
            }
        }
        return -1;
    }

    static class SceneID{int act, scene;SceneID(int act,int scene){this.act=act;this.scene=scene;}}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id== R.id.bookmarks){
            startActivityForResult(new Intent(this,BookmarksActivity.class),10);
        }
        if(id== R.id.themeToggle){
            SharedPreferences p = getSharedPreferences("app",0);
            SharedPreferences.Editor e = p.edit();
            e.putBoolean("lightTheme",!p.getBoolean("lightTheme",true));
            e.commit();
            Intent intent = new Intent(ScriptActivity.this, ScriptActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private String getFragmentTag(int pos){
        return "android:switcher:"+ R.id.pager+":"+pos;
    }
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0)return new CastFragment();
            SceneID s = ids.get(position-1);
            return SceneFragment.newInstance(s.act,s.scene);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0)return "Dramatis Personae";
            String text = "";
            int act = ids.get(position-1).act;
            int scene = ids.get(position-1).scene;
            int namedAct = act;
            int namedScene = scene;
            if(play.hasInduction){
                if(act==1)text=("Induction, Scene "+scene);
                else text=("Act "+(act-1)+", Scene "+scene);
                namedAct--;
            }else if(!play.acts.get(act-1).hasPrologue)text=("Act "+act+", Scene "+scene);
            else{
                if(scene==1)text=("Act "+act+", Prologue");
                else text=("Act "+act+", Scene "+(scene-1));
                namedScene--;
            }
            if(play.acts.get(act-1).hasEpilogue&&scene==play.acts.get(act-1).scenes.size()){
                text=("Act "+act+", Epilogue");
            }
            return text;
        }

        @Override
        public int getCount() {
            return ids.size()+1;
        }
    }

    public static class CastFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_personae, container, false);
            boolean lightTheme = rootView.getContext().getSharedPreferences("app",0).getBoolean("lightTheme", true);
            if(lightTheme){
                rootView.setBackgroundResource(R.color.lightBack);
            }else{
                rootView.setBackgroundResource(R.color.darkBack);
            }
            rootView.findViewById(R.id.section_label).setVisibility(View.GONE);
            TextView tv = (TextView)rootView.findViewById(R.id.textView);
            tv.setText(Html.fromHtml(play.personae));
            return rootView;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SceneFragment extends Fragment {
        private static final String ARG_ACT= "act";
        private static final String ARG_SCENE= "scene";
        private int act,scene,namedAct,namedScene;
        private SpeechAdapter adapter;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static SceneFragment newInstance(int act,int scene) {
            SceneFragment fragment = new SceneFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_ACT, act);
            args.putInt(ARG_SCENE, scene);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_script, container, false);
            boolean lightTheme = rootView.getContext().getSharedPreferences("app",0).getBoolean("lightTheme", true);
            if(lightTheme){
                rootView.setBackgroundResource(R.color.lightBack);
            }else{
                rootView.setBackgroundResource(R.color.darkBack);
            }
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            act = getArguments().getInt(ARG_ACT,1);
            scene = getArguments().getInt(ARG_SCENE,1);
            namedAct = act;
            namedScene = scene;
            if(play.hasInduction){
                if(act==1)textView.setText("Induction, Scene "+scene);
                else textView.setText("Act "+(act-1)+", Scene "+scene);
                namedAct--;
            }else if(!play.acts.get(act-1).hasPrologue)textView.setText("Act "+act+", Scene "+scene);
            else{
                if(scene==1)textView.setText("Act "+act+", Prologue");
                else textView.setText("Act "+act+", Scene "+(scene-1));
                namedScene--;
            }
            if(play.acts.get(act-1).hasEpilogue&&scene==play.acts.get(act-1).scenes.size()){
                textView.setText("Act "+act+", Epilogue");
            }
            textView.setVisibility(View.GONE);
            final ListView list = (ListView) rootView.findViewById(R.id.list);
            list.setFastScrollEnabled(true);
            list.setDividerHeight(0);
            adapter = new SpeechAdapter(getActivity(),play.acts.get(act-1).scenes.get(scene-1));
            list.setAdapter(adapter);
            final SharedPreferences prefs = getActivity().getSharedPreferences(playName,0);
            list.setSelection(prefs.getInt("a" + act + "s" + scene + "scroll", 0));
            list.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                    SharedPreferences.Editor e = prefs.edit();
                    e.putInt("a" + act + "s" + scene + "scroll", i);
                    e.commit();
                }
            });
            if ((getResources().getConfiguration().screenLayout &
                    Configuration.SCREENLAYOUT_SIZE_MASK) <
                    Configuration.SCREENLAYOUT_SIZE_LARGE) {
                list.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
            }
            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, final View view, final int i, long l) {
                    PopupMenu p = new PopupMenu(getActivity(),((ViewGroup)view).findViewById(R.id.speaker));
                    Menu m = p.getMenu();
                    Scene cScene = play.acts.get(act-1).scenes.get(scene-1);
                    final Speech cSpeech = cScene.speeches.get(i);
                    if(cSpeech.bookmarked)m.add("Remove Bookmark");
                    else m.add("Add Bookmark");
                    if(cSpeech.hasNotes)m.add("Edit Notes");
                    else m.add("Add Notes");
                    p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            if(menuItem.getTitle().equals("Add Bookmark")){
                                SharedPreferences.Editor e = view.getContext().getSharedPreferences(ScriptActivity.playName+"Marks",0).edit();
                                e.putBoolean(cSpeech.ident,true);
                                e.commit();
                                cSpeech.bookmarked=true;
                                ((SpeechAdapter)list.getAdapter()).notifyDataSetChanged();
                            }
                            if(menuItem.getTitle().equals("Remove Bookmark")){
                                SharedPreferences.Editor e = view.getContext().getSharedPreferences(ScriptActivity.playName+"Marks",0).edit();
                                e.putBoolean(cSpeech.ident,false);
                                e.commit();
                                cSpeech.bookmarked=false;
                                ((SpeechAdapter)list.getAdapter()).notifyDataSetChanged();
                            }
                            if(menuItem.getTitle().equals("Add Notes")||menuItem.getTitle().equals("Edit Notes")){
                                Intent intent = new Intent(view.getContext(),NoteActivity.class);
                                Scene cScene = play.acts.get(act-1).scenes.get(scene-1);
                                Speech cSpeech = cScene.speeches.get(i);
                                intent.putExtra("title",namedAct+"."+namedScene+"."+cSpeech.lines.get(0).linenum+"-"+cSpeech.lines.get(cSpeech.lines.size()-1).linenum);
                                NoteActivity.speech = cSpeech;
                                startActivityForResult(intent, 78);
                            }
                            return true;
                        }
                    });
                    if(cSpeech.speakers.size()>0)p.show();
                    return true;
                }
            });
            return rootView;
        }

        public void scrollTo(int position){
            if(getView()!=null&&getView().findViewById(R.id.list)!=null){
                System.out.println("scrollTo:"+position);
                ListView list = (ListView) getView().findViewById(R.id.list);
                list.setSelection(position);
            }
        }
        public void refresh(){
            if(adapter!=null)adapter.notifyDataSetChanged();
        }

        private class SpeechAdapter extends BaseAdapter {
            private Context ctx;
            private Scene scene;
            public SpeechAdapter(Context context,Scene scene) {
                ctx = context;
                this.scene = scene;
            }

            @Override
            public int getCount() {
                return scene.speeches.size();
            }

            @Override
            public Object getItem(int i) {
                return scene.speeches.get(i);
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int i, View convertView, ViewGroup parent) {
                View v = convertView;
                Speech speech = scene.speeches.get(i);
                //System.out.println(scene.speeches.size()+";"+speech.lines.size());
                if(v==null){
                    v=((LayoutInflater)ctx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.card, null);
                }
                TextView speaker = (TextView)v.findViewById(R.id.speaker);
                TextView text = (TextView)v.findViewById(R.id.text);
                TextView directions = (TextView)v.findViewById(R.id.directions);
                TextView linenum = (TextView)v.findViewById(R.id.linenum);
                View mark = v.findViewById(R.id.bookmarkIndicator);
                View note = v.findViewById(R.id.noteIndicator);
                if(speech.bookmarked){
                    mark.setVisibility(View.VISIBLE);
                }else mark.setVisibility(View.GONE);
                if(speech.hasNotes){
                    note.setVisibility(View.VISIBLE);
                }else note.setVisibility(View.GONE);
                text.setLineSpacing(0f,1.1f);
                linenum.setLineSpacing(0f,1.1f);
                View card = v.findViewById(R.id.card);
                boolean lightTheme = ctx.getSharedPreferences("app",0).getBoolean("lightTheme", true);
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
                    if(showLineNums) linenum.setVisibility(View.VISIBLE);
                    else linenum.setVisibility(View.GONE);
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
                        int width = textLength(l.text(),ctx);
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
                                    if(textLength(firstLine+" "+words[index],ctx)>maxwidth){
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
                            if(monoLength(speech.lines.get(j).text(),ctx)>maxwidth)linenumtext+="<br>";
                            if(monoLength(speech.lines.get(j).text(),ctx)>maxwidth*2)linenumtext+="<br>";
                            if(monoLength(speech.lines.get(j).text(),ctx)>maxwidth*3)linenumtext+="<br>";
                            if(monoLength(speech.lines.get(j).text(),ctx)>maxwidth*4)linenumtext+="<br>";
                            if(monoLength(speech.lines.get(j).text(),ctx)>maxwidth*5)linenumtext+="<br>";
                            if(monoLength(speech.lines.get(j).text(),ctx)>maxwidth*6)linenumtext+="<br>";
                        }
                    }
                    linenum.setText(Html.fromHtml(linenumtext));
                    lines=lines.substring(4);
                    text.setText(Html.fromHtml(lines));
                }

                return v;
            }
        }
    }

    public static int textLength(String text,Context ctx){
        float ts = ctx.getResources().getDimensionPixelSize(R.dimen.small_text_size);
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(5);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextSize(ts);
        mPaint.setTypeface(Typeface.create("Roboto Light",Typeface.NORMAL));
// ...
        float w = mPaint.measureText(text, 0, text.length());
        return (int)w;
    }
    public static int monoLength(String text,Context ctx){
        float ts = ctx.getResources().getDimensionPixelSize(R.dimen.small_text_size);
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(5);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextSize(ts);
        mPaint.setTypeface(Typeface.MONOSPACE);
// ...
        float w = mPaint.measureText(text, 0, text.length());
        return (int)w;
    }

}
