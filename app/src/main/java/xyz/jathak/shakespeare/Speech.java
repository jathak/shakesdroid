package xyz.jathak.shakespeare;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jathak on 1/11/14.
 */
public class Speech {
    public List<Line> lines = new ArrayList<Line>();
    public List<String> speakers = new ArrayList<String>();
    public boolean bookmarked;
    public boolean hasNotes;
    public int actIndex;
    public int sceneIndex;
    public String note;
    public String ident;
}
