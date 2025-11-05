package org.jeecg.modules.home.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.jeecg.modules.home.entity.AiOpsDataResponse;
import org.jeecg.modules.home.entity.FirewallTicket;
import org.jeecg.modules.home.mapper.FirewallTicketMapper;
import org.jeecg.common.util.AssertUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.jeecg.modules.home.service.IWorkStageService;
import cn.hutool.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.api.vo.Result;

import org.springframework.stereotype.Service;

import java.util.ArrayList; 
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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

    @Autowired
    private FirewallTicketMapper firewallTicketMapper;

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
        // requestParamMap.put("pageNum", 1);
        // requestParamMap.put("pageSize", 10);
        // 设置定义的参数
        Map<String, Object> customParamMap = new HashMap<>();
        // 你自定义的参数
        customParamMap.put(key, value);
        requestParamMap.put("param", customParamMap);
 
        return requestParamMap;
    }

    /**
     * 将IP地址转换为长整型用于排序
     * 
     * @param ip IP地址字符串
     * @return IP地址的长整型表示
     */
    private static long ipToLong(String ip) {
        try {
            String[] parts = ip.trim().split("\\.");
            if (parts.length == 4) {
                return Long.parseLong(parts[0]) * 256L * 256L * 256L +
                       Long.parseLong(parts[1]) * 256L * 256L +
                       Long.parseLong(parts[2]) * 256L +
                       Long.parseLong(parts[3]);
            }
        } catch (Exception e) {
            // 如果解析失败，返回0（会排在前面）
        }
        return 0L;
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


    @Override
	public Result<?> qryCmdbSystem(){
        String url = String.format(dataBaseUrl, "getAllSystemInfo");
        // 构建请求Header信息
        Map<String, String> headerMap = buildWeakAuthHeader("data",dataApiKey);

        AiOpsDataResponse response = new AiOpsDataResponse();

        try {
            // 构建请求参数
            Map<String, Object> requestParamMap = new HashMap<>();
            // 发送请求，根据API的定义构建Get或者Post请求
            // HttpRequest httpRequest = HttpRequest.get(url);
            HttpRequest httpRequest = HttpRequest.post(url);
            httpRequest.headerMap(headerMap, true);
            httpRequest.body(JSON.toJSONString(requestParamMap));
            String result = httpRequest.execute().body();
            // 解析返回结果并使用
            response = JSON.parseObject(result, AiOpsDataResponse.class);

            
            if ("0000".equals(response.getCode())) {
                System.out.println("请求成功");
                List<String> systemNameList = new ArrayList<>();
                for (Map<String, Object> row : response.getData().getRows()) {
                    systemNameList.add(row.get("businessName").toString());
                }
                if (systemNameList.isEmpty()) {
                    return Result.error("qryCmdbSystem failed!!!",response);
                }
                return Result.ok(systemNameList);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Result.error("qryCmdbSystem failed!!!",response);
    }

    @Override
	public Result<?> getCmdbSystemIpList(String businessName){
        String url = String.format(dataBaseUrl, "getSystemIpList");
        // 构建请求Header信息
        Map<String, String> headerMap = buildWeakAuthHeader("data",dataApiKey);


        AiOpsDataResponse response = new AiOpsDataResponse();

        try {
            // 构建请求参数
            String key = "systemName";
            String  value = businessName;
            Map<String, Object> requestParamMap = buildRequestParam(key,value);
            // 发送请求，根据API的定义构建Get或者Post请求
            HttpRequest httpRequest = HttpRequest.post(url);
            httpRequest.headerMap(headerMap, true);
            httpRequest.body(JSON.toJSONString(requestParamMap));

            String result = httpRequest.execute().body();
            // 解析返回结果并使用
            response = JSON.parseObject(result, AiOpsDataResponse.class);

            if ("0000".equals(response.getCode())) {
                System.out.println("请求成功");
                List<String> systemIpList = new ArrayList<>();

                for (Map<String, Object> row : response.getData().getRows()) {
                    String ipListStr = row.get("ipList").toString();
                    // 解析JSON字符串数组
                    try {
                        String[] ipArray = ipListStr.replaceAll("\\[|\\]|\"", "").split(",");
                        for (String ip : ipArray) {
                            String trimmedIp = ip.trim();
                            if (!trimmedIp.isEmpty()) {
                                systemIpList.add(trimmedIp);
                            }
                        }
                    } catch (Exception e) {
                        // 如果解析失败，按原方式处理
                        String[] ipList = ipListStr.split(",");
                        for (String ip : ipList) {
                            String trimmedIp = ip.trim();
                            if (!trimmedIp.isEmpty()) {
                                systemIpList.add(trimmedIp);
                            }
                        }
                    }
                }
                
                // 对IP列表进行排序
                Collections.sort(systemIpList, new Comparator<String>() {
                    @Override
                    public int compare(String ip1, String ip2) {
                        long num1 = ipToLong(ip1);
                        long num2 = ipToLong(ip2);
                        return Long.compare(num1, num2);
                    }
                });
                
                return Result.ok(systemIpList);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Result.error("getCmdbSystemIpList failed!!!",response);
    }

    @Override
	public Result<?> getCmdbIpScoperList(){
        String url = String.format(dataBaseUrl, "getIpScoperList");
        // 构建请求Header信息
        Map<String, String> headerMap = buildWeakAuthHeader("data",dataApiKey);

        AiOpsDataResponse response = new AiOpsDataResponse();


        try {
            // 构建请求参数
            Map<String, Object> requestParamMap = new HashMap<>();
            // 发送请求，根据API的定义构建Get或者Post请求
            HttpRequest httpRequest = HttpRequest.post(url);
            httpRequest.headerMap(headerMap, true);
            httpRequest.body(JSON.toJSONString(requestParamMap));
            String result = httpRequest.execute().body();
            // 解析返回结果并使用
            response = JSON.parseObject(result, AiOpsDataResponse.class);

            if ("0000".equals(response.getCode())) {
                System.out.println("请求成功");
                List<String> ipScoperList = new ArrayList<>();
                for (Map<String, Object> row : response.getData().getRows()) {
                    ipScoperList.add(row.get("ipScoper").toString());
                }
                return Result.ok(ipScoperList);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Result.error("getCmdbIpScoperList failed!!!",response);
    }

    @Override
    public Result<?> saveTicket(JSONObject jsonObject) {
        try {
            FirewallTicket ticket = new FirewallTicket();
            
            // 设置工单类型（从前端接收，如果没有则默认为firewall）
            ticket.setTicketType(jsonObject.getString("ticket_type") != null ? jsonObject.getString("ticket_type") : "firewall");
            
            // 设置创建用户（从前端接收create_user字段）
            ticket.setCreateUser(jsonObject.getString("create_user"));
            
            // 设置状态（从前端接收，如果没有则默认为pending）
            ticket.setStatus(jsonObject.getString("status") != null ? jsonObject.getString("status") : "pending");
            
            // 获取基本信息(base_info) - 前端已经组装好，转换为JSON字符串
            Object baseInfoObj = jsonObject.get("base_info");
            if (baseInfoObj != null) {
                ticket.setBaseInfo(JSON.toJSONString(baseInfoObj));
            }
            
            // 获取工单信息(ticket_info) - 前端已经组装好，包含策略配置和生效时间，转换为JSON字符串
            Object ticketInfoObj = jsonObject.get("ticket_info");
            if (ticketInfoObj != null) {
                ticket.setTicketInfo(JSON.toJSONString(ticketInfoObj));
            }
            
            // 设置申请信息(apply_info)
            ticket.setApplyInfo(jsonObject.getString("apply_info"));
            
            // 设置创建时间
            ticket.setCreateTime(new Date());
            
            // 保存到数据库
            firewallTicketMapper.insert(ticket);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", ticket.getId());
            return Result.OK("保存成功", result);
        } catch (Exception e) {
            log.error("保存防火墙策略申请失败:", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> queryTicketList(String createUser) {
        try {
            QueryWrapper<FirewallTicket> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("create_user", createUser);
            queryWrapper.eq("ticket_type", "firewall");
            queryWrapper.orderByDesc("create_time");
            queryWrapper.last("LIMIT 10"); // 限制返回10条
            
            List<FirewallTicket> ticketList = firewallTicketMapper.selectList(queryWrapper);
            
            // 构建返回数据，只返回基础信息
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (FirewallTicket ticket : ticketList) {
                Map<String, Object> ticketMap = new HashMap<>();
                ticketMap.put("id", ticket.getId());
                ticketMap.put("ticketType", ticket.getTicketType());
                ticketMap.put("status", ticket.getStatus());
                ticketMap.put("createTime", ticket.getCreateTime());
                ticketMap.put("applyInfo", ticket.getApplyInfo());
                
                // 解析base_info获取基本信息
                if (ticket.getBaseInfo() != null && !ticket.getBaseInfo().isEmpty()) {
                    try {
                        JSONObject baseInfo = JSON.parseObject(ticket.getBaseInfo());
                        ticketMap.put("applicant", baseInfo.getString("applicant"));
                        ticketMap.put("urgencyLevel", baseInfo.getString("urgencyLevel"));
                        ticketMap.put("environmentType", baseInfo.getString("environmentType"));
                    } catch (Exception e) {
                        log.warn("解析base_info失败: " + e.getMessage());
                    }
                }
                
                resultList.add(ticketMap);
            }
            
            return Result.ok(resultList);
        } catch (Exception e) {
            log.error("查询工单列表失败:", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> queryTicketById(Integer id) {
        try {
            FirewallTicket ticket = firewallTicketMapper.selectById(id);
            if (ticket == null) {
                return Result.error("工单不存在");
            }
            
            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("id", ticket.getId());
            result.put("ticketType", ticket.getTicketType());
            result.put("createUser", ticket.getCreateUser());
            result.put("status", ticket.getStatus());
            result.put("createTime", ticket.getCreateTime());
            result.put("updateTime", ticket.getUpdateTime());
            result.put("baseInfo", ticket.getBaseInfo());
            result.put("ticketInfo", ticket.getTicketInfo());
            result.put("applyInfo", ticket.getApplyInfo());
            
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询工单详情失败:", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}   
