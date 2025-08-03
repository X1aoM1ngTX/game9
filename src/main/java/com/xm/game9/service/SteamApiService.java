package com.xm.game9.service;

import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.SecureRandom;

/**
 * Steam API服务
 */
@Service
@Slf4j
public class SteamApiService {

    private final RestTemplate restTemplate;

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
            String url = String.format("https://partner.steam-api.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=%d", appid);
            
            SteamApiResponse response = restTemplate.getForObject(url, SteamApiResponse.class);
            
            if (response != null && response.getResponse() != null) {
                return ResultUtils.success(response.getResponse().getPlayer_count());
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
    public static class SteamApiResponse {
        private ResponseData response;

        public ResponseData getResponse() {
            return response;
        }

        public void setResponse(ResponseData response) {
            this.response = response;
        }
    }

    /**
     * Steam API响应数据
     */
    public static class ResponseData {
        private int player_count;
        private String result;

        public int getPlayer_count() {
            return player_count;
        }

        public void setPlayer_count(int player_count) {
            this.player_count = player_count;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}

