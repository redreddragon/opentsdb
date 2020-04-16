package com.cloudiip.opentsdb.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author : longwenhe
 * @date : 2020/4/15 15:18
 * @description :
 */
public class OpenTSDB {
    private static final Logger LOG = LoggerFactory.getLogger(OpenTSDB.class);

    public static void main(String[] args) {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Content-Type", "application/json");
        String url = "http://172.16.22.155:4242/api/put?summary";
        final int len = 50;
        DataPoint[] dataPoints = new DataPoint[len];
        Random r = new Random();
        String metric = "iot.v1.metrics.value";
        Date date01 = new Date();
        Result finalResult = new Result(0, 0);
        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int j = 0; j < 10; j++) {
                DataPoint dataPoint = new DataPoint();
                dataPoint.setMetric(metric);
                Map<String, String> tagsMap = new HashMap<>();
                tagsMap.put("tenant", "cloudiip");
                tagsMap.put("template", "t00" + j);
                tagsMap.put("device", "d00" + j);
                tagsMap.put("property", "p00" + j);
                dataPoint.setTags(tagsMap);
                Date date = new Date();
                dataPoint.setTimestamp(date.getTime());
                dataPoint.setValue(r.nextFloat() * 1000);
                dataPoints[j] = dataPoint;
            }
            for (int j = 10; j < len; j++) {
                DataPoint dataPoint = new DataPoint();
                dataPoint.setMetric(metric);
                Map<String, String> tagsMap = new HashMap<>();
                tagsMap.put("tenant", "tenant_system");
                tagsMap.put("template", "t0" + j);
                tagsMap.put("device", "d0" + j);
                tagsMap.put("property", "p0" + j);
                dataPoint.setTags(tagsMap);
                Date date = new Date();
                dataPoint.setTimestamp(date.getTime());
                dataPoint.setValue(r.nextFloat() * 1000);
                dataPoints[j] = dataPoint;
            }
            String result = doPost(url, dataPoints, headersMap, finalResult, date01);
            JSONObject object = JSONObject.parseObject(result);
            Integer success = (Integer) object.get("success");
            Integer failed = (Integer) object.get("failed");
            finalResult.setSuccess(finalResult.getSuccess() + success);
            finalResult.setFailed(finalResult.getFailed() + failed);
            Date date02 = new Date();
            long time = (date02.getTime() - date01.getTime()) / 1000;
            System.out.println("Time used: " + time + "s");
            System.out.println(finalResult.toString());
        }

    }

    public static String doPost(String url, DataPoint[] dataPoint, Map<String, String> headers, Result finalResult, Date date01) {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;
        String result = "";
        try {
            HttpPost httpPost = new HttpPost(url);

            if (headers != null && headers.size() != 0) {
                for (String key : headers.keySet()) {
                    httpPost.addHeader(key, headers.get(key));
                }
            }
            if (dataPoint != null && dataPoint.length != 0) {
                if (headers.get("Content-Type") != null && headers.get("Content-Type").contains("json")) {
                    String jsonstr = JSONObject.toJSONString(dataPoint);
                    StringEntity s = new StringEntity(jsonstr);
                    s.setContentEncoding("UTF-8");
                    s.setContentType(headers.get("Content-Type"));//发送json数据需要设置contentType
                    httpPost.setEntity(s);
                } else {
                    List<NameValuePair> list = new LinkedList<>();
                    UrlEncodedFormEntity entityParam = new UrlEncodedFormEntity(list, "UTF-8");
                    httpPost.setEntity(entityParam);
                }
            }
            response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity);
        } catch (Exception e) {
            Date date02 = new Date();
            long time = (date02.getTime() - date01.getTime()) / 1000;
            System.out.println("Time used: " + time + "s");
            System.out.println(finalResult.toString());
            e.printStackTrace();
        }
        return result;
    }

    private static class Result {
        private Integer success;
        private Integer failed;

        @Override
        public String toString() {
            return "Result{" +
                    "success=" + success +
                    ", failed=" + failed +
                    '}';
        }

        public Result() {
        }

        public Result(Integer success, Integer failed) {
            this.success = success;
            this.failed = failed;
        }

        public Integer getSuccess() {
            return success;
        }

        public void setSuccess(Integer success) {
            this.success = success;
        }

        public Integer getFailed() {
            return failed;
        }

        public void setFailed(Integer failed) {
            this.failed = failed;
        }
    }
}
