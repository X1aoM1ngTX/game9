package com.xm.game9.service;

import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.exception.BusinessException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Steam API服务
 */
@Service
@Slf4j
public class SteamApiService {

    private final RestTemplate restTemplate;

    // 从配置文件中读取Steam API Key
    @Value("${steam.api.key:}")
    private String steamApiKey;

    public SteamApiService() {
        this.restTemplate = createUnsafeRestTemplate();
    }

    /**
     * 创建忽略SSL证书验证的RestTemplate
     */
    private RestTemplate createUnsafeRestTemplate() {
        try {
            // 创建信任所有证书的SSL上下文
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            }, new SecureRandom());

            // 设置默认的SSL上下文
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            // 创建RestTemplate
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(5000);

            return new RestTemplate(factory);
        } catch (Exception e) {
            log.error("创建RestTemplate失败", e);
            return new RestTemplate();
        }
    }

    /**
     * 获取游戏当前在线玩家数量
     *
     * @param appid Steam应用ID (uint32)
     * @return 在线玩家数量
     */
    public BaseResponse<Integer> getNumberOfCurrentPlayers(Integer appid) {
        if (appid == null || appid <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Steam应用ID参数错误");
        }

        try {
            // 修改URL，添加API Key参数
            String url;
            if (steamApiKey != null && !steamApiKey.trim().isEmpty()) {
                url = String.format("https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=%d&key=%s",
                        appid, steamApiKey);
            } else {
                // 如果没有API Key，则不添加key参数（某些情况下可能仍然工作）
                url = String.format("https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=%d",
                        appid);
                log.warn("未配置Steam API Key，可能无法正常获取数据");
            }

            log.info("调用Steam API: {}", url);

            SteamApiResponse response = restTemplate.getForObject(url, SteamApiResponse.class);

            if (response != null && response.getResponse() != null) {
                // 检查API调用是否成功
                if ("1".equals(response.getResponse().getResult()) || response.getResponse().getResult() == null) {
                    return ResultUtils.success(response.getResponse().getPlayer_count());
                } else {
                    log.warn("Steam API调用失败，appid: {}, result: {}", appid, response.getResponse().getResult());
                    return ResultUtils.success(0);
                }
            } else {
                log.warn("Steam API返回数据格式异常，appid: {}", appid);
                return ResultUtils.success(0);
            }

        } catch (Exception e) {
            log.error("调用Steam API失败，appid: {}, 错误: {}", appid, e.getMessage(), e);
            return ResultUtils.success(0);
        }
    }

    /**
     * Steam API响应内部类
     */
    @Setter
    @Getter
    public static class SteamApiResponse {
        private ResponseData response;

    }

    /**
     * Steam API响应数据
     */
    @Setter
    @Getter
    public static class ResponseData {
        private int player_count;
        private String result; // 1表示成功，2表示失败

    }
}