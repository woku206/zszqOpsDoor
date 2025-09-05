package org.jeecg.modules.home.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import org.jeecg.modules.home.entity.AiOpsDataResponse;
import org.jeecg.common.util.AssertUtils;

import org.jeecg.modules.home.service.IWorkStageService;
import cn.hutool.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.api.vo.Result;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
public class WorkStageServiceImpl implements IWorkStageService {

    @Value("${aiops.dataBaseUrl}")
    private  String dataBaseUrl;

    @Value("${aiops.alertBaseUrl}")
    private  String alertBaseUrl;

    @Value("${aiops.dataApiKey}")
    private  String dataApiKey;

    @Value("${aiops.alertApiKey}")
    private  String alertApiKey;

     /**
    * 构建弱认证的请求头
    *
    * @return
    */
   private static Map<String, String> buildWeakAuthHeader(String type,String apiKey) {
    TreeMap<String, String> headerMap = new TreeMap<>();
    if (type.equals("data")) {
        headerMap.put("apiKey", apiKey);
    } else if (type.equals("alert")) {
        headerMap.put("refinerAccessToken", apiKey);
        headerMap.put("Content-Type", "application/json");
    }
    return headerMap;
   }

   /**
    * 构建请求参数
    *
    * @return
    */
    private static Map<String, Object> buildRequestParam(String key,String value) {
        Map<String, Object> requestParamMap = new HashMap<>();
        // 设置分页参数
        requestParamMap.put("pageNum", 1);
        requestParamMap.put("pageSize", 10);
        // 设置定义的参数
        Map<String, Object> customParamMap = new HashMap<>();
        // 你自定义的参数
        customParamMap.put(key, value);
        requestParamMap.put("param", customParamMap);
 
        return requestParamMap;
    }

    
    @Override
	public Result<?> queryMyAlarmList(String userName){
        String url = String.format(dataBaseUrl, "getAllRelateAlarm");
        // 构建请求Header信息
        Map<String, String> headerMap = buildWeakAuthHeader("data",dataApiKey);

        String key = "userName";

        String value = userName;


        AiOpsDataResponse response = new AiOpsDataResponse();

        try {
                // 构建请求参数
            Map<String, Object> requestParamMap = buildRequestParam(key,value);
            // 发送请求，根据API的定义构建Get或者Post请求
    //        HttpRequest httpRequest = HttpRequest.get(url);
            HttpRequest httpRequest = HttpRequest.post(url);
            httpRequest.headerMap(headerMap, true);
            httpRequest.body(JSON.toJSONString(requestParamMap));
            String result = httpRequest.execute().body();
            // 解析返回结果并使用
            response = JSON.parseObject(result, AiOpsDataResponse.class);
            if ("0000".equals(response.getCode())) {
                System.out.println("请求成功");
                return Result.ok(response);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Result.error("queryMyAlarmList failed!!!",response);
    }


    @Override
	public Result<?> closeAlarm(Long id, String resolution){
        AssertUtils.assertNotEmpty("id必须填写", id);

        String endfix_url = String.format("alert/%s/close",id.toString());

        String url = String.format(alertBaseUrl,endfix_url);
        // 构建请求Header信息
        Map<String, String> headerMap = buildWeakAuthHeader("alert",alertApiKey);

        JSONObject response =  new JSONObject();
        
        try {
                // 构建请求参数
            Map<String, Object> data = new HashMap<>();
            data.put("alertId", id);
            data.put("resolution", resolution);
                // 发送请求，根据API的定义构建Get或者Post请求
    //        HttpRequest httpRequest = HttpRequest.get(url);
            HttpRequest httpRequest = HttpRequest.post(url);
            httpRequest.headerMap(headerMap, true);
            httpRequest.body(JSON.toJSONString(data));
            String result = httpRequest.execute().body();
            response = JSON.parseObject(result);

            // 解析返回结果并使用
            if ("0000".equals(response.getString("retCode"))) {
                System.out.println("请求成功");
                return Result.ok(response);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Result.error("closeAlarm filed!!!",response);
    }



    @Override
	public Result<?> dealAlarm(Long id){
        AssertUtils.assertNotEmpty("id必须填写", id);


        String url = String.format(alertBaseUrl,"alert/batchClaim");
        // 构建请求Header信息
        Map<String, String> headerMap = buildWeakAuthHeader("alert",alertApiKey);

        JSONObject response =  new JSONObject();

        try {
                // 构建请求参数
            Map<String, Object> data = new HashMap<>();
            data.put("selected",new ArrayList<>(Arrays.asList(id)));
            data.put("checkall", false);
            data.put("unselected",new ArrayList<>());

                // 发送请求，根据API的定义构建Get或者Post请求
    //        HttpRequest httpRequest = HttpRequest.get(url);
            HttpRequest httpRequest = HttpRequest.post(url);
            httpRequest.headerMap(headerMap, true);
            httpRequest.body(JSON.toJSONString(data));
            String result = httpRequest.execute().body();
            response = JSON.parseObject(result);

            // 解析返回结果并使用
            if ("0000".equals(response.getString("retCode"))) {
                System.out.println("请求成功");
                return Result.ok(response);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(e.getMessage());
        }
        return Result.error("dealAlarm filed!!!",response);
    }
}
