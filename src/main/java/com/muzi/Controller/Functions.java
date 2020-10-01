package com.muzi.Controller;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Functions {
    static String surl = "1uw8r4k8tnowEx2EuSKhyiw";//分享链接/s/后的一部分
    static String pwd = "0f75";//提取码
    static String BUDSS="";//记得填哦
    static String STOKEN="";

    static String dlink;

    public static void main(String[] args) {
        try {
            verifyPwd(surl, pwd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //解析分享链接
    public static String verifyPwd(String surl, String pwd) throws IOException {
        String surl_1 = surl.substring(1);

        String url = "https://pan.baidu.com/share/verify?channel=chunlei&clienttype=0&web=1&app_id=250528&surl=" + surl_1;
        System.out.println(url);
        Document document = Jsoup
                .connect(url)
                .ignoreContentType(true)
                .header("User-Agent", "netdisk")
                .header("Referer", "https://pan.baidu.com/disk/home")
                .data("pwd", pwd)
                .post();
        Element body = document.body();
        JSONObject jsonStr = JSONObject.parseObject(body.text());
        System.out.println(jsonStr);
        int errno_id = Integer.parseInt(jsonStr.getString("errno"));
        String randsk = jsonStr.getString("randsk");
        System.out.println("提取文件状态:" + errno_id);
        System.out.println("当前randsk:" + randsk);
        if (errno_id != 0) {
            return "0";
        }
        getSign(surl, randsk);
        return randsk;
    }

    public static String getSign(String surl, String randsk) throws IOException {
        if (randsk == null) {
            return "0";
        }
        String url = "https://pan.baidu.com/s/" + surl;
        Document document2 = Jsoup
                .connect(url)
                //.ignoreContentType(true)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.514.1919.810 Safari/537.36")
                .cookie("BDCLND", randsk)
                .get();
        //System.out.println(document2);
        String Strs = String.valueOf(document2);

        String pattern = "yunData.setData\\((.+)\\);";
        Pattern r = Pattern.compile(pattern); //正则表达式

        Matcher m = r.matcher(Strs);
        if (m.find()) {
            System.out.println("找到文件信息: " + m.group(0));
        }
        String jsonStr = m.group(0).substring(15);
        String jsonStrs = jsonStr.substring(jsonStr.indexOf("(") + 1, jsonStr.indexOf(")"));
        JSONObject jsonStrss = JSONObject.parseObject(jsonStrs);

        System.out.println(jsonStrss);

        String sign = jsonStrss.getString("sign");
        String timestamp = jsonStrss.getString("timestamp");
        String shareid = jsonStrss.getString("shareid");
        String uk = jsonStrss.getString("uk");

        JSONObject A1 = jsonStrss.getJSONObject("file_list");
        List<Object> A2 = A1.getJSONArray("list");
        String A2s = String.valueOf(A2);
        String fs = A2s.substring(A2s.indexOf("[") + 1, A2s.indexOf("]"));
        JSONObject A3 = JSONObject.parseObject(fs);
        String fs_id = A3.getString("fs_id");

        System.out.println(A1);
        System.out.println(A2);
        System.out.println("A2s=" + A2s);
        System.out.println(A3);
        System.out.println(fs_id);

        System.out.println("当前获取sign：" + sign);
        System.out.println("当前获取timestamp：" + timestamp);
        System.out.println("当前获取shareid：" + shareid);
        System.out.println("当前获取uk：" + uk);
        System.out.println("当前获取fs_id：" + fs_id);

        getLink(fs_id, timestamp, sign, randsk, shareid, uk);
        return "1";
    }

    public static String getLink(String fs_id, String timestamp, String sign, String randsk, String share_id, String uk) throws IOException {
        int app_id = 250528;
        String url = "https://pan.baidu.com/api/sharedownload?app_id=" + app_id + "&channel=chunlei&clienttype=12&sign=" + sign + "&timestamp=" + timestamp + "&web=1";
        System.out.println("怼这个:" + url);
        String randsks = null;
        try {
            randsks = URLDecoder.decode(randsk, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String randskss = "{" + "\"sekey\"" + ":" + "\"" + randsks + "\"" + "}";
        System.out.println(randsks);
        System.out.println(randskss);
        String fid_list = "[" + fs_id + "]";
        System.out.println(fid_list);
        Document document3 = Jsoup
                .connect(url)
                .ignoreContentType(true)
                .data("encrypt", "0")
                .data("extra", randskss)
                .data("fid_list", fid_list)
                .data("primaryid", share_id)
                .data("uk", uk)
                .data("product", "share")
                .data("type", "nolimit")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.514.1919.810 Safari/537.36")
                .cookie("BDUSS", BUDSS)
                .cookie("STOKEN", STOKEN)
                .cookie("BDCLND", randsk)
                .header("Referer", "https://pan.baidu.com/disk/home")
                .post();
        Element body = document3.body();
        JSONObject jsonStr = JSONObject.parseObject(body.text());
        System.out.println("得到的json:" + jsonStr);
        List<Object> A2 = jsonStr.getJSONArray("list");
        String A2s = String.valueOf(A2);
        String fs = A2s.substring(A2s.indexOf("[") + 1, A2s.lastIndexOf( ']' ));
        System.out.println(fs);
        JSONObject A3 = JSONObject.parseObject(fs);
        dlink = A3.getString("dlink");
        System.out.println("得到的pcs地址:"+dlink);
        getReal();
        return "1";
    }

    public static String getReal() throws IOException {
        String url = dlink;
        Connection.Response response = Jsoup
                .connect(url)
                .ignoreContentType(true)
                .followRedirects(true)
                .header("User-Agent", "LogStatistic")
                .cookie("BDUSS", BUDSS)
                .execute();
        System.out.println("得到真实地址："+response.url());
        return "1";
    }
}
