/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.ticket.utils;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.HttpHeaders;
import okio.BufferedSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author wujn
 * @version $Id HttpUtils.java, v 0.1 2018-01-16 15:19 wujn Exp $$
 */
public class HttpUtils {
    public static final String JSON_MIME = "application/json";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    private final static int CONNECTION_TIME_OUT = 60;
    private final static int READ_TIME_OUT = 60;
    private final static int WRITE_TIME_OUT = 0;
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static HttpUtils mInstance;
    private volatile OkHttpClient httpClient;

    private HttpUtils() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(64);
        dispatcher.setMaxRequestsPerHost(16);
        ConnectionPool connectionPool = new ConnectionPool(128, 5, TimeUnit.MINUTES);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.dispatcher(dispatcher);
        builder.connectionPool(connectionPool);
        builder.addNetworkInterceptor(chain -> {
            Request request = chain.request();
            Response response = chain.proceed(request);
            IpTag tag = (IpTag) request.tag();
            try {
                tag.ip = chain.connection().socket().getRemoteSocketAddress().toString();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                tag.ip = "";
            }
            return response;
        });
        builder.connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(READ_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS);
        builder.sslSocketFactory(createSSLSocketFactory(), new TrustAllManager());
        httpClient = builder.build();
    }

    public static HttpUtils getInstance() {
        HttpUtils inst = mInstance;
        if (inst == null) {
            synchronized (HttpUtils.class) {
                inst = mInstance;
                if (inst == null) {
                    inst = new HttpUtils();
                    mInstance = inst;
                }
            }
        }
        return inst;
    }

    private static RequestBody create(final MediaType contentType,
                                      final byte[] content, final int offset, final int size) {
        if (content == null) {
            throw new NullPointerException("content == null");
        }

        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return size;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(content, offset, size);
            }
        };
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory sSLSocketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return sSLSocketFactory;
    }

    private Response send(final Request.Builder requestBuilder) {
        IpTag tag = new IpTag();
        final long start = System.currentTimeMillis();
        Response res = null;
        try {
            Request request = requestBuilder.tag(tag).build();
            res = httpClient.newCall(request).execute();
            long duration = (System.currentTimeMillis() - start);
            LOGGER.info("request {} duration {} ms", request, duration);
            if (res.isSuccessful()) {
                return res;
            } else {
                LOGGER.error("服务端返回的状态码不是200,request={},response={}", request, res);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return res;
    }

    public String get(String url, Charset encoding) {
        try {
            Request.Builder requestBuilder = new Request.Builder().get().url(url);
            requestBuilder.addHeader("User-Agent", USER_AGENT);
            byte[] bytes = send(requestBuilder).body().bytes();
            if (bytes != null && bytes.length > 0) {
                return new String(bytes, encoding);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public String get(String url, Map<String, String> headers, Charset encoding) {
        try {
            Request.Builder requestBuilder = new Request.Builder().get().url(url);
            requestBuilder.addHeader("User-Agent", USER_AGENT);
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            byte[] bytes = send(requestBuilder).body().bytes();
            if (bytes != null && bytes.length > 0) {
                return new String(bytes, encoding);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public byte[] get(String url, Map<String, String> headers) {
        try {
            Request.Builder requestBuilder = new Request.Builder().get().url(url);
            requestBuilder.addHeader("User-Agent", USER_AGENT);
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            return send(requestBuilder).body().bytes();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public String get(String url) {
        try {
            Request.Builder requestBuilder = new Request.Builder().get().url(url);
            byte[] bytes = send(requestBuilder).body().bytes();
            if (bytes != null && bytes.length > 0) {
                return utf8String(bytes);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public List<String> getHeader(String url, String headerName) {
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .addHeader("User-Agent", USER_AGENT)
                    .get()
                    .url(url);
            return send(requestBuilder).headers(headerName);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public String post(String url, Map<String, String> headers, StringMap params) {
        try {
            final FormBody.Builder f = new FormBody.Builder();
            params.forEach((key, value) -> f.add(key, value.toString()));
            byte[] bytes = post(url, headers, f.build()).body().bytes();
            if (bytes != null && bytes.length > 0) {
                return utf8String(bytes);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public String post(String url, StringMap params) {
        try {
            final FormBody.Builder f = new FormBody.Builder();
            params.forEach((key, value) -> f.add(key, value.toString()));
            byte[] bytes = post(url, null, f.build()).body().bytes();
            if (bytes != null && bytes.length > 0) {
                return utf8String(bytes);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public String postByJSON(String url, String json) {
        try {
            Request.Builder requestBuilder = new Request.Builder().url(url).post(RequestBody.create(MediaType.parse(JSON_MIME), json));
            byte[] bytes = send(requestBuilder).body().bytes();
            if (bytes != null && bytes.length > 0) {
                return utf8String(bytes);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public static String utf8String(byte[] bytes) {
        return new String(bytes, Charset.forName("UTF-8"));
    }

    private Response post(String url, Map<String, String> headers, RequestBody body) {
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        requestBuilder.addHeader("User-Agent", USER_AGENT);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return send(requestBuilder);
    }

    private static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }

    private static class IpTag {
        public String ip = null;
    }
}
