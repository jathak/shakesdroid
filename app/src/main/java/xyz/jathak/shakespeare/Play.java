package xyz.jathak.shakespeare;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jathak on 1/11/14.
 */
public class Play {
    public String name;
    public List<Act> acts = new ArrayList<Act>();
    public Play(String name){
        this.name = name;
    }
    public boolean hasInduction = false;
    public String personae = "";

    public static int getPlayColorResource(String name) {
        switch (name) {
            case "allswell": return R.color.allswell;
            case "asyoulike": return R.color.asyoulike;
            case "antony": return R.color.antony;
            case "errors": return R.color.errors;
            case "corio": return R.color.corio;
            case "cymb": return R.color.cymb;
            case "hamlet": return R.color.hamlet;
            case "hen_iv_1":
            case "hen_iv_2": return R.color.hen_iv;
            case "hen_v": return R.color.hen_v;
            case "hen_vi_1":
            case "hen_vi_2":
            case "hen_vi_3": return R.color.hen_vi;
            case "hen_viii": return R.color.hen_viii;
            case "caesar": return R.color.caesar;
            case "john": return R.color.john;
            case "lear": return R.color.lear;
            case "laborlost": return R.color.laborlost;
            case "macbeth": return R.color.macbeth;
            case "measure": return R.color.measure;
            case "venice": return R.color.venice;
            case "merrywives": return R.color.merrywives;
            case "midsummer": return R.color.midsummer;
            case "muchado": return R.color.muchado;
            case "othello": return R.color.othello;
            case "pericles": return R.color.pericles;
            case "rich_ii": return R.color.rich_ii;
            case "rich_iii": return R.color.rich_iii;
            case "romeo": return R.color.romeo;
            case "taming": return R.color.taming;
            case "tempest": return R.color.tempest;
            case "timon": return R.color.timon;
            case "titus": return R.color.titus;
            case "troilus": return R.color.troilus;
            case "twelfth": return R.color.twelfth;
            case "two_gent": return R.color.two_gent;
            case "winter": return R.color.winter;
        }
        return -1;
    }

    public static int getLightTheme(String name) {
        switch (name) {
            case "allswell": return R.style.allswell_Light;
            case "asyoulike": return R.style.asyoulike_Light;
            case "antony": return R.style.antony_Light;
            case "errors": return R.style.errors_Light;
            case "corio": return R.style.corio_Light;
            case "cymb": return R.style.cymb_Light;
            case "hamlet": return R.style.hamlet_Light;
            case "hen_iv_1":
            case "hen_iv_2": return R.style.hen_iv_Light;
            case "hen_v": return R.style.hen_v_Light;
            case "hen_vi_1":
            case "hen_vi_2":
            case "hen_vi_3": return R.style.hen_vi_Light;
            case "hen_viii": return R.style.hen_viii_Light;
            case "caesar": return R.style.caesar_Light;
            case "john": return R.style.john_Light;
            case "lear": return R.style.lear_Light;
            case "laborlost": return R.style.laborlost_Light;
            case "macbeth": return R.style.macbeth_Light;
            case "measure": return R.style.measure_Light;
            case "venice": return R.style.venice_Light;
            case "merrywives": return R.style.merrywives_Light;
            case "midsummer": return R.style.midsummer_Light;
            case "muchado": return R.style.muchado_Light;
            case "othello": return R.style.othello_Light;
            case "pericles": return R.style.pericles_Light;
            case "rich_ii": return R.style.rich_ii_Light;
            case "rich_iii": return R.style.rich_iii_Light;
            case "romeo": return R.style.romeo_Light;
            case "taming": return R.style.taming_Light;
            case "tempest": return R.style.tempest_Light;
            case "timon": return R.style.timon_Light;
            case "titus": return R.style.titus_Light;
            case "troilus": return R.style.troilus_Light;
            case "twelfth": return R.style.twelfth_Light;
            case "two_gent": return R.style.two_gent_Light;
            case "winter": return R.style.winter_Light;
        }
        return -1;
    }

    public static int getDarkTheme(String name) {
        switch (name) {
            case "allswell": return R.style.allswell_Dark;
            case "asyoulike": return R.style.asyoulike_Dark;
            case "antony": return R.style.antony_Dark;
            case "errors": return R.style.errors_Dark;
            case "corio": return R.style.corio_Dark;
            case "cymb": return R.style.cymb_Dark;
            case "hamlet": return R.style.hamlet_Dark;
            case "hen_iv_1":
            case "hen_iv_2": return R.style.hen_iv_Dark;
            case "hen_v": return R.style.hen_v_Dark;
            case "hen_vi_1":
            case "hen_vi_2":
            case "hen_vi_3": return R.style.hen_vi_Dark;
            case "hen_viii": return R.style.hen_viii_Dark;
            case "caesar": return R.style.caesar_Dark;
            case "john": return R.style.john_Dark;
            case "lear": return R.style.lear_Dark;
            case "laborlost": return R.style.laborlost_Dark;
            case "macbeth": return R.style.macbeth_Dark;
            case "measure": return R.style.measure_Dark;
            case "venice": return R.style.venice_Dark;
            case "merrywives": return R.style.merrywives_Dark;
            case "midsummer": return R.style.midsummer_Dark;
            case "muchado": return R.style.muchado_Dark;
            case "othello": return R.style.othello_Dark;
            case "pericles": return R.style.pericles_Dark;
            case "rich_ii": return R.style.rich_ii_Dark;
            case "rich_iii": return R.style.rich_iii_Dark;
            case "romeo": return R.style.romeo_Dark;
            case "taming": return R.style.taming_Dark;
            case "tempest": return R.style.tempest_Dark;
            case "timon": return R.style.timon_Dark;
            case "titus": return R.style.titus_Dark;
            case "troilus": return R.style.troilus_Dark;
            case "twelfth": return R.style.twelfth_Dark;
            case "two_gent": return R.style.two_gent_Dark;
            case "winter": return R.style.winter_Dark;
        }
        return -1;
    }
}
