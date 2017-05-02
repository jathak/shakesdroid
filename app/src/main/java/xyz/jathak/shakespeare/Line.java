package xyz.jathak.shakespeare;

/**
 * Created by jathak on 1/11/14.
 */
public class Line {
    public int type = 0;
    public static final int TYPE_SPOKEN = 0;
    public static final int TYPE_DIRECTION = 1;
    public static final int TYPE_COMBO = 2;
    public String spoken = "";
    public String direction = "";
    public int linenum;
    public boolean wraps = false;
    public String text(){
        if(type==TYPE_SPOKEN)  return spoken;
        if(type==TYPE_DIRECTION) return direction;
        if(type==TYPE_COMBO) return "["+direction+"] "+spoken;
        return null;
    }
    public String texti(){
        if(type==TYPE_SPOKEN)  return spoken;
        if(type==TYPE_DIRECTION) return "<font face='monospace'>["+direction+"]</font>";
        if(type==TYPE_COMBO) return "<font face='monospace'>["+direction+"]</font> "+spoken;
        return null;
    }
}
