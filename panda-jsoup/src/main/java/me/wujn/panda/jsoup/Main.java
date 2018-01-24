/**
 * Zentech-Inc
 * Copyright (C) 2017 All Rights Reserved.
 */
package me.wujn.panda.jsoup;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wujn
 * @version $Id Main.java, v 0.1 2017-12-15 17:23 wujn Exp $$
 */
public class Main {

    private static Map<String, String> HEAD_IMG_CACHE = new HashMap<String, String>();

    public static void main(String[] args) throws IOException {
        save();
    }

    private static void save() {
        List<String> urls = getListUrl("http://finance.eastmoney.com/yaowen.html");
        if (urls != null && urls.size() > 0) {
            for (String url : urls) {
                try {
                    Document document = Jsoup.connect(url).timeout(10000).get();
                    Elements contentEl = document.select("#ContentBody");
                    String description = contentEl.get(0).select(".b-review").text();
                    String author = document.select(".time-source .author").text();
                    String source = document.select(".time-source .source").text();
                    String time = document.select(".time-source .time").text();
                    String orginContent = contentEl.get(0).html();
                    String content = StringUtils.substring(orginContent, orginContent.indexOf("<!--文章主体-->"), orginContent.indexOf("<!--原文标题-->"));

                    Article article = new Article();
                    article.setArticleId(parseArticleId(url));
                    article.setDescription(description);
                    article.setAuthor(parseAuthor(author));
                    article.setSource(parseSource(source));
                    article.setPubTime(parseTime(time));
                    article.setHeadImgUrl(HEAD_IMG_CACHE.get(url));
                    article.setTitle(parseTitle(document.title()));
                    article.setContent(parseContent(content));
                    FileUtils.writeStringToFile(new File("d://ar/" + article.getArticleId()), JSON.toJSONString(article), "UTF-8");
                    HEAD_IMG_CACHE.clear();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * NEWS_ID+CATE_ID
     *
     * @param url
     * @return
     */
    private static String parseArticleId(String url) {
        String id = StringUtils.substring(url, url.lastIndexOf("/") + 1, url.lastIndexOf(".html"));
        String[] arr = StringUtils.split(id, ",");
        return arr[1] + arr[0];
    }

    private static String parseAuthor(String originContent) {
        if (StringUtils.isBlank(originContent)) {
            return "匿名";
        }
        return StringUtils.replace(originContent, "作者：", "");
    }

    private static String parseSource(String originContent) {
        if (StringUtils.isBlank(originContent)) {
            return "未知";
        }
        return StringUtils.replace(originContent, "来源：", "");
    }

    private static String parseTime(String originContent) {
        if (StringUtils.isBlank(originContent)) {
            return DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        }
        return originContent;
    }

    private static String parseTitle(String originContent) {
        if (StringUtils.isBlank(originContent)) {
            return "未知";
        }
        return StringUtils.replace(originContent, " _东方财富网", "");
    }

    private static String parseContent(String originContent) {
        if (StringUtils.isBlank(originContent)) {
            return "";
        }
        String withOutA = StringUtils.replaceAll(originContent, "<a\\s*[^>]*>(.*?)</a>", "$1");
        String content = StringUtils.replaceAll(withOutA, "<iframe\\s*[^>]*>(.*?)</iframe>", "");
        String x = StringUtils.replaceAll(content, "style=\\\"(.*?)\\\"", "");
        String xx = StringUtils.replaceAll(x, "<img\\s*[^>]*/>", "");
        String xxx = StringUtils.replaceAll(xx, "id=\\\"(.*?)\\\"", "");
        String xxxx = StringUtils.replaceAll(xxx, "class=\\\"(.*?)\\\"", "");
        String xxxxx = StringUtils.replaceAll(xxxx, "<center\\s*[^>]*>(.*?)</center>", "");
        String xxxxxx = StringUtils.replaceAll(xxxxx, "<strong\\s*[^>]*>(.*?)</strong>", "");
        return StringUtils.replaceAll(xxxxxx, "\\n", "");
    }

    private static List<String> getListUrl(String listPageUrl) {
        try {
            List<String> urls = new ArrayList<String>();
            Document document = Jsoup.connect(listPageUrl).timeout(10000).get();
            if (document != null) {
                String urlSelector = "#artitileList1 ul li[id^=newsTr]";
                Elements urlElements = document.select(urlSelector);
                for (Element li : urlElements) {
                    Elements aList = li.select("p.title>a");
                    String link = aList.get(0).attr("href");
                    String title = aList.get(0).text();
                    Elements headImg = li.select("img");
                    String headImgSrc = headImg.get(0).attr("src");
                    HEAD_IMG_CACHE.put(link, headImgSrc);
                    urls.add(link);
                }
                return urls;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
