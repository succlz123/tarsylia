import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by succlz123 on 2017/4/20.
 */
public class Main {

    private static final String xx = "PHPSESSID=cln3m1o6mnlp5ujbraqmcv21mrjs3fm9; duoshuo_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzaG9ydF9uYW1lIjoidGFyc3lsaWEiLCJ1c2VyX2tleSI6NDI5MiwibmFtZSI6Ilx1OThkZVx1ODg0Y1x1NjhjYiJ9.ghaI393-dYShx62q57Y2lxnOS7eU-2VZ2p8InkRvSaA; wordpress_logged_in_a138b3a49d53ab7d533339291067d074=%E9%A3%9E%E8%A1%8C%E6%A3%8B%7C1493894217%7CiygsIn9xK9YzIlHCiEA2pNoZSan0Eyul1QCA9uuq0Gw%7C37a1b2ded3039142163978d5f868ed45adc4341759f27ef842834367a1ed5a0e; wp-settings-time-4292=1492685147";

    public static void main(String args[]) {

        try {
            Element body = Jsoup.connect("http://tarsyliatales.com/category/comic").header("Cookie", xx).get();
            if (body == null) {
                return;
            }
            Elements elementsByClass = body.getElementsByClass("books-warp");
            ArrayList<HAHA> x = new ArrayList<>();
            HAHA haha = null;
            for (Element child : elementsByClass) {
                Elements children = child.children();
                for (int i = 0; i < children.size(); i++) {
                    Element element = children.get(i);
                    if (i % 2 == 0) {
                        haha = new HAHA();
                        haha.pic = element.child(0).attributes().get("src");
                        haha.name = element.child(1).text();
                    } else {
                        haha.content = element.children().get(1).getElementsByClass("intro-text").text();
                        Elements elements1 = element.children().get(1).getElementsByClass("post-list").get(0).children();
                        ArrayList<Div> divs = new ArrayList<>();
                        for (Element element1 : elements1) {
                            Div div = new Div();
                            div.name = element1.text();
                            div.url = element1.attr("href");
                            divs.add(div);
                        }
                        haha.divList = divs;
                        x.add(haha);
                    }
                }
            }

            Runtime rt = Runtime.getRuntime();

            for (HAHA haha1 : x) {
                String name = haha1.name;
                File file = new File(System.getProperty("user.dir") + "/tarsyliatales/" + name);
                if (!file.exists()) {
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    file.mkdir();
                }

                createFile(file.getAbsolutePath() + "/简介.txt", haha1.content);

                File coverFiel = new File(file.getAbsolutePath() + "/cover.png");
                if (!coverFiel.exists()) {
                    HttpURLConnection conect = conect(haha1.pic);
                    InputStream in = conect.getInputStream();
                    writeStream2File(new BufferedInputStream(in), coverFiel.getAbsolutePath());
                }

                List<Div> divList = haha1.divList;
                for (Div div : divList) {
                    File file1 = new File(file.getAbsolutePath() + "/" + div.name);
                    if (!file1.exists()) {
                        file1.mkdir();
                    }

                    URL url1 = new URL(div.url);
                    Element body1 = Jsoup.parse(url1, 100000).body();
                    System.out.println(div.name);
                    Elements imageGallery = body1.getElementById("imageGallery").children();
                    HashMap<Integer, String> pics = new HashMap<>();
                    for (int i = 0; i < imageGallery.size(); i++) {
                        Element element = imageGallery.get(i);
                        pics.put(i, element.attr("data-src"));
                        File file2 = new File(file1.getAbsolutePath() + "/" + i + ".jpg");
                        if (file2.exists()) {
                            continue;
                        }
                        System.out.print(i + ".jpg");
                        HttpURLConnection conect = conect(element.attr("data-src"));
                        InputStream in = conect.getInputStream();
                        writeStream2File(new BufferedInputStream(in), file2.getAbsolutePath());
                    }
                    div.pics = pics;
                }
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void createFile(String name, String text) {
        try {
            File file = new File(name);
            file.createNewFile();
            FileOutputStream out = null;
            if (file.exists()) {
                try {
                    out = new FileOutputStream(file);
                    out.write(text.getBytes(Charset.forName("UTF-8")));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HttpURLConnection conect(String src) throws IOException {
        URL url = new URL(src);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(100000);
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setInstanceFollowRedirects(true);
        urlConnection.connect();
        return urlConnection;
    }

    public static void writeStream2File(BufferedInputStream in, String filePath) throws IOException {
        int count = 0;
        byte[] bytes = new byte[10240];

        RandomAccessFile out = null;

        try {
            out = new RandomAccessFile(filePath, "rwd");
        } catch (FileNotFoundException e) {
        }
        try {
            while (true) {
                try {
                    count = in.read(bytes);
                } catch (IOException e) {
                    continue;
                }
                if (count <= 0) {
                    break;
                }
                //写入
                out.write(bytes, 0, count);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private static class HAHA {
        public String name;
        public String content;
        public String pic;
        public List<Div> divList;
    }

    private static class Div {
        public String name;
        public String url;
        public Map<Integer, String> pics;
    }
}
