package edu.ntou.haohao.ntoudromflow;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by Jeffrey on 2015/12/14.
 */

public class dromflow {
    ArrayList<HashMap<String, Object>> info = new ArrayList<HashMap<String, Object>>();
    String url;

    public ArrayList<HashMap<String, Object>> getData() throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
        String str = df.format(Calendar.getInstance().getTime());
        url = "http://dormflow.ntou.edu.tw/" + str + "/%b1J%aa%d9sortbyip.html";

        Document xmlDoc;
        try {

            xmlDoc = Jsoup.connect(url).timeout(10000).get();
        }
        catch (IOException e) {
            throw e;
        }

        Elements ip = xmlDoc.select("th[class=ip]");
        Elements sum = xmlDoc.select("td[class=fsum]");
        Elements in = xmlDoc.select("td[class=fin]");
        Elements out = xmlDoc.select("td[class=fout]");

        HashMap<String, Object> map;
        for (int i = 1; i < ip.size(); i++) {
            map = new HashMap<>();
            map.put("IP", ip.get(i).text());
            map.put("SUM", sum.get(i - 1).text());
            map.put("IN", in.get(i - 1).text());
            map.put("OUT", out.get(i - 1).text());
            info.add(map);
        }

        return info;
    }
}