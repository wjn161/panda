/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.ticket.controller;

import me.wujn.panda.ticket.utils.BaseController;
import me.wujn.panda.ticket.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujn
 * @version $Id TicketController.java, v 0.1 2018-01-16 15:17 wujn Exp $$
 */
@Controller
@RequestMapping("/ticket")
public class TicketController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    final String GET_COOKIE_URL = "https://kyfw.12306.cn/otn/login/init";
    final String GET_VALICODE_URL = "https://kyfw.12306.cn/passport/captcha/captcha-image?login_site=E&module=login&rand=sjrand&0.236089536729706";

    private static final Map<String, String> COOKIE_CACHE = new ConcurrentHashMap<>();

    @GetMapping("/index")
    public String index(Model model) throws Exception {
        String cookie = getInitCookie();
        if (cookie == null) {
            return "/error/503";
        }
        return "/ticket/index";
    }

    @GetMapping("/valicode")
    public void valicode(HttpServletResponse response) throws IOException {
        String cookie = getInitCookie();
        if (cookie == null) {
            return;
        }
        response.setContentType("image/jpeg");
        Map<String, String> cookieMap = new HashMap<>();
        cookieMap.put("Cookie", cookie);
        byte[] content = HttpUtils.getInstance().get(GET_VALICODE_URL, cookieMap);
        OutputStream stream = response.getOutputStream();
        stream.write(content);
        stream.flush();
        stream.close();
    }

    private synchronized String getInitCookie() {
        String cachedCookie = COOKIE_CACHE.get("INIT_COOKIE");
        if (cachedCookie != null) {
            return cachedCookie;
        }
        List<String> cookies = HttpUtils.getInstance().getHeader(GET_COOKIE_URL, "Set-Cookie");
        StringBuilder stringBuilder = new StringBuilder();
        if (cookies != null && cookies.size() > 0) {
            for (String item : cookies) {
                String value = item.substring(0, item.indexOf(";") + 1);
                stringBuilder.append(value);
            }
            String cookieStr = stringBuilder.toString();
            COOKIE_CACHE.put("INIT_COOKIE", cookieStr);
            return cookieStr;
        }
        return null;
    }
}
