package org.jeecg.modules.home.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.api.vo.Result;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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
/**
 * 工作台服务实现类
 * @author zengyun
 * @since 2025-09-02
 */
public class WorkStageServiceImpl implements IWorkStageService {

    @Value("${aiops.dataBaseUrl}")
    private  String dataBaseUrl;

    @Value("${aiops.alertBaseUrl}")
    private  String alertBaseUrl;

    @Value("${aiops.dataApiKey}")
    private  String dataApiKey;

    @Value("${aiops.alertApiKey}")
    private  String alertApiKey;

    @Value("${jeecg.path.upload}")
    private String uploadPath;

    @Value("${itil.baseUrl}")
    private String itilBaseUrl;

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
        String url = String.format(alertBaseUrl, "alert/page");
        // 构建请求Header信息
        Map<String, String> headerMap = buildWeakAuthHeader("alert",alertApiKey);


        try {
            // 计算当天0点和当前时间的时间戳（毫秒）
            long nowMillis = System.currentTimeMillis();
            long startOfDayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

            String value = userName;

            // 构建请求参数
            String request = "{\"page\":0,\"from\":0,\"size\":100,\"filter\":{\"status\":[\"dispose\",\"processing\"],\"dataSpace\":1,"
                    + "\"extendList\":[{\"key\":\"extend2\",\"value\":\"" + value + "\"}],"
                    + "\"isClusterWarn\":null,\"hasTicket\":null,\"hasIncident\":null,"
                    + "\"arriveTime\":[" + startOfDayMillis + "," + nowMillis + "],"
                    + "\"updateTimeLe\":null,\"isRecovery\":null,\"isNotify\":null,"
                    + "\"isSilence\":null,\"isMaintain\":null,\"search\":\"\"},"
                    + "\"sort\":[{\"key\":\"arriveTime\",\"order\":\"desc\"}]}";
            // 发送请求，根据API的定义构建Get或者Post请求
            HttpRequest httpRequest = HttpRequest.post(url);
            httpRequest.headerMap(headerMap, true);
            httpRequest.body(request);
            String result = httpRequest.execute().body();   
            // 解析返回结果并使用   
            if (result.contains("entity")) {
                JSONArray response = JSON.parseObject(result).getJSONArray("entity");
                if (response.size() > 0) {  
                    JSONArray mappedList = new JSONArray();
                    for (Object obj : response) {
                        JSONObject jsonObject = (JSONObject) obj;
                        JSONObject mappedObject = new JSONObject();
                        
                        // 字段映射
                        if (jsonObject.containsKey("id")) {
                            mappedObject.put("id", jsonObject.get("id"));
                    }
                    if (jsonObject.containsKey("severity")) {
                        mappedObject.put("alarm_level", jsonObject.get("severity"));
                    }
                    if (jsonObject.containsKey("status")) {
                        mappedObject.put("alarm_status", jsonObject.get("status"));
                    }
                    if (jsonObject.containsKey("content")) {
                        mappedObject.put("content", jsonObject.get("content"));
                    }
                    if (jsonObject.containsKey("host")) {
                        mappedObject.put("host", jsonObject.get("host"));
                    }
                    if (jsonObject.containsKey("beginTime")) {
                        Object beginTime = jsonObject.get("beginTime");
                        // 如果是时间戳，转换为Date对象
                        if (beginTime instanceof Long) {
                            mappedObject.put("alarm_time", new Date((Long) beginTime));
                        } else {
                            mappedObject.put("alarm_time", beginTime);
                        }
                    }
                    
                    mappedList.add(mappedObject);
                }
                return Result.ok(mappedList);
                } else {
                    
                    return Result.ok("");
                }
            } else {
                return Result.error("queryMyAlarmList failed!!!",result);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(e.getMessage());
        }
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
        String url = String.format(dataBaseUrl, "getSystemInfoForMenhu");
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
                List<Map<String, Object>> systemNameList = new ArrayList<>();
                for (Map<String, Object> row : response.getData().getRows()) {
                    Map<String, Object> systemNameMap = new HashMap<>();
                    systemNameMap.put("businessName", row.get("businessName").toString());
                    systemNameMap.put("objectId", row.get("objectId").toString());
                    systemNameList.add(systemNameMap);  
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
	public Result<?> getCmdbSystemIpList(String objectId){
        String url = String.format(dataBaseUrl, "getSystemIpList");
        // 构建请求Header信息
        Map<String, String> headerMap = buildWeakAuthHeader("data",dataApiKey);


        AiOpsDataResponse response = new AiOpsDataResponse();

        try {
            // 构建请求参数
            String key = "objectId";
            String  value = objectId;
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
            // 如果包含id，则走更新逻辑
            Integer id = jsonObject.getInteger("id");
            if (id != null) {
                ticket.setId(id);
            }
            
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
            
            Map<String, Object> result = new HashMap<>();
            // 插入或更新
            if (ticket.getId() != null) {
                // 更新
                ticket.setUpdateTime(new Date());
                firewallTicketMapper.updateById(ticket);
                result.put("id", ticket.getId());
                return Result.OK("更新成功", result);
            } else {
                // 新增
                ticket.setCreateTime(new Date());
                firewallTicketMapper.insert(ticket);
            }
            
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

    @Override
    public Result<?> genTicket(JSONObject jsonObject) {
        try {
            // 这里接收前端生成的 Excel 文件数据，保存到服务器
            // 前端会调用此接口并传递文件内容
            String fileName = jsonObject.getString("fileName");
            String fileContent = jsonObject.getString("fileContent"); // Base64编码的文件内容
            
            if (fileName == null || fileContent == null) {
                return Result.error("文件名或文件内容不能为空");
            }

            // 创建保存目录
            String saveDir = uploadPath + File.separator + "firewall_tickets";
            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件路径
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = sdf.format(new Date());
            String saveFileName = timestamp + "_" + fileName;
            String filePath = saveDir + File.separator + saveFileName;

            // 将Base64内容解码并保存
            byte[] fileBytes = java.util.Base64.getDecoder().decode(fileContent);
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(fileBytes);
            fos.close();

            Map<String, Object> result = new HashMap<>();
            result.put("filePath", filePath);
            result.put("fileName", saveFileName);
            return Result.OK("文件生成成功", result);
        } catch (Exception e) {
            log.error("生成工单附件失败:", e);
            return Result.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件到 ITIL 平台
     */
    private String uploadFileToItil(File file) {
        try {
            String uploadUrl = itilBaseUrl + "/ops/dataviewdev/attach/rest/upload";
            
            // 使用 Hutool 上传文件
            String response = HttpUtil.createPost(uploadUrl)
                    .form("indexId", "")
                    .form("processInstanceId", "")
                    .form("initialFileName", "")
                    .form("handleClass", "")
                    .form("file", file)
                    .execute()
                    .body();

            JSONObject responseJson = JSON.parseObject(response);
            if (responseJson.getInteger("errorCode") != null && responseJson.getInteger("errorCode") == 0) {
                JSONObject responseBody = responseJson.getJSONObject("responseBody");
                if (responseBody != null) {
                    return responseBody.getString("uuid");
                }
            }
            log.error("上传文件到ITIL失败: " + response);
            return null;
        } catch (Exception e) {
            log.error("上传文件到ITIL异常:", e);
            return null;
        }
    }

    /**
     * 调用 ITIL 流程发起接口
     */
    private String startItilProcess(String applyUser, String title, String typeOne, String typeTwo, 
                                     String priority, String solutiontime, String detail, 
                                     String handler, String reqAttach, String status, String source) {
        try {
            String processUrl = itilBaseUrl + "/ops/api/process/start";
            
            Map<String, Object> params = new HashMap<>();
            params.put("applyUser", applyUser);
            params.put("title", title);
            params.put("typeOne", typeOne);
            params.put("typeTwo", typeTwo);
            params.put("priority", priority);
            params.put("solutiontime", solutiontime);
            params.put("detail", detail);
            if (handler != null && !handler.isEmpty()) {
                params.put("handler", handler);
            }
            if (reqAttach != null && !reqAttach.isEmpty()) {
                params.put("reqAttach", reqAttach);
            }
            params.put("status", status);
            params.put("source", source);
            params.put("processName", "服务请求");


            String response = HttpUtil.createPost(processUrl)
                    .body(JSON.toJSONString(params))
                    .contentType("application/json")
                    .execute()
                    .body();
            
            log.info("ITIL流程发起响应: " + response);

            JSONObject responseJson = JSON.parseObject(response);
            if (responseJson.getInteger("errorCode") != null && responseJson.getInteger("errorCode") == 0) {
                JSONObject responseBody = responseJson.getJSONObject("responseBody");
                if (responseBody != null) {
                    JSONObject responseBodyJson = responseBody.getJSONObject("result");
                    log.info("ITIL流程发起响应: " + responseBodyJson);
                    return responseBodyJson.toString();
                }
            }   
            log.error("ITIL流程发起失败: " + response);
            // 这里需要根据实际ITIL接口返回格式判断
            return response;
        } catch (Exception e) {
            log.error("调用ITIL流程发起接口异常:", e);
            return e.getMessage().toString();
        }
    }

    @Override
    public Result<?> downloadTicketAttachment(Integer id) {
        try {
            FirewallTicket ticket = firewallTicketMapper.selectById(id);
            if (ticket == null) {
                return Result.error("工单不存在");
            }
            
            if (ticket.getAttachmentPath() == null || ticket.getAttachmentPath().isEmpty()) {
                return Result.error("工单附件不存在");
            }

            File file = new File(ticket.getAttachmentPath());
            if (!file.exists()) {
                return Result.error("附件文件不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("filePath", ticket.getAttachmentPath());
            result.put("fileName", file.getName());
            return Result.ok(result);
        } catch (Exception e) {
            log.error("获取附件信息失败:", e);
            return Result.error("获取附件信息失败: " + e.getMessage());
        }
    }

    /**
     * IP/网段格式校验（用于手工输入地址 externalAddress）
     *
     * @param ip 输入内容，多个条目用逗号分隔
     * @return null 表示校验通过；非 null 表示失败原因（可直接返回给前端）
     */
    private String validateIpFormat(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return "地址不能为空";
        }

        String ipRegex = "^(\\d{1,3}\\.){3}\\d{1,3}(/\\d{1,2})?$";
        String[] ips = ip.split(",");
        for (String raw : ips) {
            String singleIp = raw.trim();
            if (singleIp.isEmpty()) {
                continue;
            }
            if (!singleIp.matches(ipRegex)) {
                return "地址格式不正确（" + singleIp + "），请输入IPv4或CIDR网段，例如：192.168.1.1 或 10.55.0.0/16";
            }

            // 支持两类输入：
            // 1) 主机IP：a.b.c.d
            // 2) 网段：a.b.c.d/prefix，并校验网段地址与掩码是否匹配（host bits 必须为 0）
            String ipWithoutMask = singleIp;
            Integer prefixLen = null;
            boolean hasMask = singleIp.contains("/");
            if (hasMask) {
                String[] ipAndMask = singleIp.split("/");
                ipWithoutMask = ipAndMask[0];
                String maskStr = ipAndMask.length > 1 ? ipAndMask[1] : "";
                try {
                    prefixLen = Integer.parseInt(maskStr);
                } catch (NumberFormatException e) {
                    return "网段掩码格式不正确（" + singleIp + "），掩码必须为0-32的整数";
                }
                if (prefixLen < 0 || prefixLen > 32) {
                    return "网段掩码范围不正确（" + singleIp + "），掩码必须在0-32之间";
                }
            }

            String[] parts = ipWithoutMask.split("\\.");
            if (parts.length != 4) {
                return "地址格式不正确（" + singleIp + "）";
            }

            long ipLong = 0;
            int[] octets = new int[4];
            for (int idx = 0; idx < parts.length; idx++) {
                String part = parts[idx];
                try {
                    int num = Integer.parseInt(part);
                    if (num < 0 || num > 255) {
                        return "IP段取值范围错误（" + singleIp + "），每段必须在0-255之间";
                    }
                    octets[idx] = num;
                    ipLong = (ipLong << 8) | (num & 0xFF);
                } catch (NumberFormatException e) {
                    log.error("IP格式校验失败，无法解析IP部分: " + part, e);
                    return "IP段格式不正确（" + singleIp + "）";
                }
            }

            // “看起来像网段”的情况必须带掩码：x.x.x.0 或 x.x.0.0 等
            boolean looksLikeNetwork = (octets[3] == 0) || (octets[2] == 0 && octets[3] == 0);
            if (!hasMask && looksLikeNetwork) {
                return "网段必须显式携带掩码（" + singleIp + "），例如：" + ipWithoutMask + "/24";
            }

            // 如果带掩码，则必须是该掩码下的网段起始地址（host bits 为 0）
            if (prefixLen != null) {
                long hostMask = (prefixLen == 32) ? 0L : ((1L << (32 - prefixLen)) - 1L);
                if ((ipLong & hostMask) != 0) {
                    return "网段地址与掩码不匹配（" + singleIp + "）";
                }
            }
        }
        return null;
    }

    /**
     * 端口格式校验
     */
    private boolean validatePort(String port) {
        if (port == null || port.trim().isEmpty()) {
            return false;
        }
        String[] ports = port.split(",");
        for (String p : ports) {
            p = p.trim();
            if (p.isEmpty()) {
                continue;
            }
            try {
                if (!p.equals("1-65535")){
                    int num = Integer.parseInt(p);
                    if (num < 1 || num > 65535) {
                        return false;
                    }
                }
                }catch (NumberFormatException e) {
                    return false;
                }
        }
        return true;
    }

    /**
     * CMDB网段校验
     */
    private boolean isInCmdbRange(String ip) {
        String[] cmdbRanges = {"10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"};
        for (String range : cmdbRanges) {
            String[] rangeParts = range.split("/");
            String rangeIp = rangeParts[0];
            String[] ipParts = ip.split("\\.");
            String[] rangeIpParts = rangeIp.split("\\.");
            if (ipParts.length >= 2 && rangeIpParts.length >= 2) {
                if (ipParts[0].equals(rangeIpParts[0]) && ipParts[1].equals(rangeIpParts[1])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测策略唯一性冲突
     * 规则：源地址、目的地址、端口三者需保证唯一（统一规则，不再区分地址类型）
     */
    private String detectConflicts(JSONObject ticketInfo) {
        try {
            JSONObject ticketInfoObj = ticketInfo;
            if (ticketInfoObj == null) {
                return null;
            }
            
            Object policiesObj = ticketInfoObj.get("policies");
            if (policiesObj == null) {
                return null;
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> policies;
            if (policiesObj instanceof List) {
                policies = (List<Map<String, Object>>) policiesObj;
            } else if (policiesObj instanceof String) {
                policies = (List<Map<String, Object>>) JSON.parseObject((String) policiesObj, List.class);
            } else {
                return null;
            }
            
            Map<String, List<Integer>> comboMap = new HashMap<>();
            
            for (int i = 0; i < policies.size(); i++) {
                Map<String, Object> policy = policies.get(i);
                // 将前端传入的“多IP/多端口”按逗号拆分成多行组合后再判重
                List<String> sourceAddresses = parseToList(policy.get("sourceAddress"));
                List<String> destAddresses = parseToList(policy.get("destAddress"));
                List<String> ports = splitByComma(policy.get("port"));

                // 生成所有组合：src * dest * port
                for (String src : sourceAddresses) {
                    for (String dst : destAddresses) {
                        for (String p : ports) {
                            String comboKey = src + "||" + dst + "||" + p;
                            if (!comboMap.containsKey(comboKey)) {
                                comboMap.put(comboKey, new ArrayList<>());
                            }
                            // 记录冲突所在“策略行号”(从1开始)
                            comboMap.get(comboKey).add(i + 1);
                        }
                    }
                }
            }
            
            for (Map.Entry<String, List<Integer>> entry : comboMap.entrySet()) {
                if (entry.getValue().size() > 1) {
                    // 去重并排序冲突行号
                    List<Integer> rows = new ArrayList<>(new java.util.LinkedHashSet<>(entry.getValue()));
                    java.util.Collections.sort(rows);
                    return "源地址、目的地址、端口三者需保证唯一，存在重复组合。冲突行：" + rows.toString();
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("检测冲突失败:", e);
            return "检测冲突失败: " + e.getMessage();
        }
    }

    /**
     * 将地址字段统一解析为列表：
     * - List: 逐项trim并过滤空值
     * - String: 按逗号分隔并trim，过滤空值
     */
    private List<String> parseToList(Object address) {
        List<String> result = new ArrayList<>();
        if (address == null) {
            return result;
        }
        if (address instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) address;
            for (Object o : list) {
                if (o == null) continue;
                String s = o.toString().trim();
                if (!s.isEmpty()) {
                    result.add(s);
                }
            }
        } else {
            String s = address.toString();
            if (s != null) {
                String[] parts = s.split(",");
                for (String part : parts) {
                    String t = part.trim();
                    if (!t.isEmpty()) {
                        result.add(t);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 端口字段按逗号分隔为列表；若为空则返回空列表
     */
    private List<String> splitByComma(Object value) {
        List<String> result = new ArrayList<>();
        if (value == null) {
            return result;
        }
        String s = value.toString();
        if (s == null) {
            return result;
        }
        for (String part : s.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                result.add(t);
            }
        }
        return result;
    }
    
    /**
     * 格式化地址用于冲突检测（统一处理数组和字符串）
     */
    private String formatAddressForConflict(Object address) {
        if (address == null) {
            return "";
        }
        if (address instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> addressList = (List<String>) address;
            return String.join(",", addressList);
        } else {
            return address.toString();
        }
    }

    @Override
    public Result<?> submitTicket(JSONObject jsonObject) {
        try {
            // ========== 格式校验和唯一性校验 ==========
            
            // 1. 校验基本信息
            JSONObject baseInfo = jsonObject.getJSONObject("base_info");
            if (baseInfo == null) {
                return Result.error("基本信息不能为空");
            }
            if (baseInfo.getString("urgencyLevel") == null || baseInfo.getString("urgencyLevel").isEmpty()) {
                return Result.error("紧急程度不能为空");
            }
            
            // 2. 校验工单信息
            JSONObject ticketInfo = jsonObject.getJSONObject("ticket_info");
            if (ticketInfo == null) {
                return Result.error("工单信息不能为空");
            }
            
            // 3. 校验申请说明
            String applyInfo = jsonObject.getString("apply_info");
            if (applyInfo == null || applyInfo.trim().isEmpty()) {
                return Result.error("申请说明不能为空");
            }
            
            // 4. 校验策略配置
            Object policiesObj = ticketInfo.get("policies");
            if (policiesObj == null) {
                return Result.error("策略配置不能为空");
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> policies;
            if (policiesObj instanceof List) {
                policies = (List<Map<String, Object>>) policiesObj;
            } else if (policiesObj instanceof String) {
                policies = (List<Map<String, Object>>) JSON.parseObject((String) policiesObj, List.class);
            } else {
                return Result.error("策略配置格式错误");
            }
            
            if (policies.isEmpty()) {
                return Result.error("至少需要一条策略配置");
            }
            
            // 5. 校验每条策略的必填字段和格式
            for (int i = 0; i < policies.size(); i++) {
                Map<String, Object> policy = policies.get(i);
                int policyIndex = i + 1;
                
                if (policy.get("sourceType") == null || policy.get("sourceType").toString().isEmpty()) {
                    return Result.error("第" + policyIndex + "行源地址类型不能为空");
                }
                if (policy.get("sourceSystem") == null || policy.get("sourceSystem").toString().isEmpty()) {
                    return Result.error("第" + policyIndex + "行源地址应用系统不能为空");
                }
                Object sourceAddress = policy.get("sourceAddress");
                if (sourceAddress == null || 
                    (sourceAddress instanceof String && ((String) sourceAddress).isEmpty()) ||
                    (sourceAddress instanceof List && ((List<?>) sourceAddress).isEmpty())) {
                    return Result.error("第" + policyIndex + "行源地址不能为空");
                }
                if (policy.get("destType") == null || policy.get("destType").toString().isEmpty()) {
                    return Result.error("第" + policyIndex + "行目的地址类型不能为空");
                }
                if (policy.get("destSystem") == null || policy.get("destSystem").toString().isEmpty()) {
                    return Result.error("第" + policyIndex + "行目的地址应用系统不能为空");
                }
                Object destAddress = policy.get("destAddress");
                if (destAddress == null || 
                    (destAddress instanceof String && ((String) destAddress).isEmpty()) ||
                    (destAddress instanceof List && ((List<?>) destAddress).isEmpty())) {
                    return Result.error("第" + policyIndex + "行目的地址不能为空");
                }
                if (policy.get("port") == null || policy.get("port").toString().isEmpty()) {
                    return Result.error("第" + policyIndex + "行端口不能为空");
                }
                if (policy.get("protocol") == null || policy.get("protocol").toString().isEmpty()) {
                    return Result.error("第" + policyIndex + "行协议不能为空");
                }
                
                // 校验端口格式
                String port = policy.get("port").toString();
                if (!validatePort(port)) {
                    return Result.error("第" + policyIndex + "行端口格式不正确，端口号必须在1-65535范围内");
                }
                
                // 校验外部地址IP格式和网段
                String sourceType = policy.get("sourceType").toString();
                if ("externalAddress".equals(sourceType) && sourceAddress instanceof String) {
                    String sourceIp = (String) sourceAddress;
                    String sourceErr = validateIpFormat(sourceIp);
                    if (sourceErr != null) {
                        return Result.error("第" + policyIndex + "行源地址校验失败：" + sourceErr);
                    }
                    String[] sourceIps = sourceIp.split(",");
                    for (String ip : sourceIps) {
                        ip = ip.trim();
                        if (!ip.isEmpty() && isInCmdbRange(ip)) {
                            return Result.error("第" + policyIndex + "行源地址 " + ip + " 不能在CMDB网段内");
                        }
                    }
                }
                
                String destType = policy.get("destType").toString();
                if ("externalAddress".equals(destType) && destAddress instanceof String) {
                    String destIp = (String) destAddress;
                    String destErr = validateIpFormat(destIp);
                    if (destErr != null) {
                        return Result.error("第" + policyIndex + "行目的地址校验失败：" + destErr);
                    }
                    String[] destIps = destIp.split(",");
                    for (String ip : destIps) {
                        ip = ip.trim();
                        if (!ip.isEmpty() && isInCmdbRange(ip)) {
                            return Result.error("第" + policyIndex + "行目的地址 " + ip + " 不能在CMDB网段内");
                        }
                    }
                }

                // 源/目的网段输入只能是24位掩码的网段（ipScoperAddress 场景）
                if ("ipScoperAddress".equals(sourceType)) {
                    List<String> srcNets = parseToList(sourceAddress);
                    for (String net : srcNets) {
                        if (net.contains("/")) {
                            String[] arr = net.split("/");
                            if (arr.length < 2 || !"24".equals(arr[1])) {
                                return Result.error("第" + policyIndex + "行源网段必须为24位掩码网段，例如：10.1.1.0/24");
                            }
                        }
                    }
                }
                if ("ipScoperAddress".equals(destType)) {
                    List<String> dstNets = parseToList(destAddress);
                    for (String net : dstNets) {
                        if (net.contains("/")) {
                            String[] arr = net.split("/");
                            if (arr.length < 2 || !"24".equals(arr[1])) {
                                return Result.error("第" + policyIndex + "行目的网段必须为24位掩码网段，例如：10.1.1.0/24");
                            }
                        }
                    }
                }

                // 源地址、目的地址相同的防火墙策略拦截（逐组合检查）
                List<String> srcList = parseToList(sourceAddress);
                List<String> dstList = parseToList(destAddress);
                for (String src : srcList) {
                    for (String dst : dstList) {
                        if (src.equals(dst)) {
                            return Result.error("第" + policyIndex + "行存在源地址与目的地址相同的策略（" + src + "），请调整");
                        }
                    }
                }
            }
            
            // 6. 校验生效时间
            String effectiveType = ticketInfo.getString("effectiveType");
            if ("scheduled".equals(effectiveType)) {
                if (ticketInfo.get("effectiveDate") == null || ticketInfo.getString("effectiveDate").isEmpty()) {
                    return Result.error("生效时间不能为空");
                }
            }
            
            // 7. 唯一性校验
            String conflictError = detectConflicts(ticketInfo);
            if (conflictError != null) {
                return Result.error(conflictError);
            }
            
            // ========== 生成并保存 Excel 文件 ==========
            String fileName = jsonObject.getString("fileName");
            String fileContent = jsonObject.getString("fileContent"); // Base64编码
            
            if (fileName == null || fileContent == null) {
                return Result.error("文件名或文件内容不能为空");
            }

            // 创建保存目录
            String saveDir = uploadPath + File.separator + "firewall_tickets";
            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件路径
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = sdf.format(new Date());
            String saveFileName = timestamp + "_" + fileName;
            String filePath = saveDir + File.separator + saveFileName;

            // 将Base64内容解码并保存
            byte[] fileBytes = java.util.Base64.getDecoder().decode(fileContent);
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(fileBytes);
            fos.close();

            File file = new File(filePath);

            // 2. 上传文件到 ITIL 平台
            String uuid = uploadFileToItil(file);
            if (uuid == null) {
                return Result.error("上传文件到ITIL平台失败");
            }

            // 3. 调用 ITIL 流程发起接口
            String applyUser = jsonObject.getString("applyUser");
            String title = jsonObject.getString("title");
            String typeOne = jsonObject.getString("typeOne");
            String typeTwo = jsonObject.getString("typeTwo");
            String priority = jsonObject.getString("priority");
            String solutiontime = jsonObject.getString("solutiontime");
            String detail = jsonObject.getString("apply_info");
            String handler = jsonObject.getString("handler");
            String status = jsonObject.getString("status"); // submit
            String source = jsonObject.getString("source");

            String response =  startItilProcess(applyUser, title, typeOne, typeTwo, 
                    priority, solutiontime, detail, handler, uuid, status, source);
            String code = JSON.parseObject(response).getString("code");
            String processInstanceId = JSON.parseObject(response).getString("processInstanceId");
            String processKey = JSON.parseObject(response).getString("processKey");
            
            
            if (code.isEmpty()) {
                log.warn("ITIL流程发起可能失败，但文件已上传");
            }

            FirewallTicket ticket = new FirewallTicket();

            // 4. 更新工单记录（如果有ID）
            Integer ticketId = jsonObject.getInteger("ticketId");
            if (ticketId != null) {
                ticket = firewallTicketMapper.selectById(ticketId);
                if (ticket != null) {
                    ticket.setAttachmentPath(filePath);
                    ticket.setItilAttachmentUuid(uuid);
                    ticket.setItilId(code);
                    ticket.setProcessInstanceId(processInstanceId);
                    ticket.setProcessKey(processKey);
                    ticket.setStatus("processing");
                    ticket.setApplyInfo(jsonObject.getString("apply_info"));
                    ticket.setBaseInfo(JSON.toJSONString(jsonObject.get("base_info")));
                    ticket.setTicketInfo(JSON.toJSONString(jsonObject.get("ticket_info")));
                    ticket.setUpdateTime(new Date());
                    firewallTicketMapper.updateById(ticket);
                }
            }   else {
                ticket.setCreateUser(applyUser);
                ticket.setTicketType("firewall");
                ticket.setApplyInfo(jsonObject.getString("apply_info"));
                ticket.setBaseInfo(JSON.toJSONString(jsonObject.get("base_info")));
                ticket.setTicketInfo(JSON.toJSONString(jsonObject.get("ticket_info")));
                ticket.setStatus("processing"); 
                ticket.setCreateTime(new Date());
                ticket.setUpdateTime(new Date());
                ticket.setAttachmentPath(filePath);
                ticket.setItilAttachmentUuid(uuid);
                ticket.setItilId(code);
                ticket.setProcessInstanceId(processInstanceId);
                ticket.setProcessKey(processKey);
                firewallTicketMapper.insert(ticket);   
                ticketId = ticket.getId();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", ticketId);
            result.put("processInstanceId", processInstanceId);
            result.put("processKey", processKey);
            return Result.OK("工单提交成功", result);
        } catch (NumberFormatException e) {
            log.error("提交工单失败，数字格式错误:", e);
            return Result.error("数据格式错误: " + e.getMessage() + "，请检查IP地址或端口格式是否正确");
        } catch (com.alibaba.fastjson.JSONException e) {
            log.error("提交工单失败，JSON解析错误:", e);
            return Result.error("数据解析错误: " + e.getMessage() + "，请检查提交的数据格式");
        } catch (IOException e) {
            log.error("提交工单失败，文件操作错误:", e);
            return Result.error("文件操作失败: " + e.getMessage() + "，请检查文件路径和权限");
        } catch (Exception e) {
            log.error("提交工单失败:", e);
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName();
            }
            return Result.error("提交失败: " + errorMsg + "，请联系管理员或稍后重试");
        }
    }

    @Override
    public Result<?> deleteTicket(Integer id) {
        try {
            FirewallTicket ticket = firewallTicketMapper.selectById(id);
            if (ticket == null) {
                return Result.error("工单不存在");
            }
            
            // 检查状态，只有pending状态的工单可以删除
            if (!"pending".equals(ticket.getStatus())) {
                return Result.error("只能删除待处理状态的工单");
            }
            
            // 删除附件文件（如果存在）
            if (ticket.getAttachmentPath() != null && !ticket.getAttachmentPath().isEmpty()) {
                File attachmentFile = new File(ticket.getAttachmentPath());
                if (attachmentFile.exists()) {
                    attachmentFile.delete();
                }
            }
            
            // 删除数据库记录
            firewallTicketMapper.deleteById(id);
            
            return Result.OK("工单已删除");
        } catch (Exception e) {
            log.error("删除工单失败:", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}   
