package org.jeecg.modules.home.controller;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.home.service.IWorkStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 首页查询前端控制器
 * </p>
 *
 * @Author zengyun
 * @since 2025-09-02
 */
@Slf4j
@RestController
@RequestMapping("/sys/home")
public class WorkStageController {
    /**
     * @Author 曾昀
     * @return
     */

    @Autowired
	private IWorkStageService workStageService;


    @GetMapping("/403")
    public Result<?> noauth()  {
        return Result.error("没有权限，请联系管理员分配权限！");
    }

    /**
     * 获取我的告警
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/myAlarmList", method = RequestMethod.GET)
    public Result<?> myAlarmList(
        @RequestParam(name="userName", required = true) String userName,
        HttpServletRequest req
    ){
        return workStageService.queryMyAlarmList(userName);
    }

    /**
     * 关闭告警
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/closeAlarm", method = RequestMethod.POST)
    public Result<?> closeAlarm(@RequestBody JSONObject jsonObject){
        return workStageService.closeAlarm(jsonObject.getLong("id"),jsonObject.getString("resolution"));
    }

    /**
     * 响应告警
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/dealAlarm", method = RequestMethod.POST)
    public Result<?> dealAlarm(@RequestBody JSONObject jsonObject
    ){
        return workStageService.dealAlarm(jsonObject.getLong("id"));
    }

    /**
     * 查询应用系统信息
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/getCmdbSystem", method = RequestMethod.GET)
    public Result<?> getCmdbSystem( HttpServletRequest req){
        return workStageService.qryCmdbSystem();
    }


    /**
     * 查询应用系统相关IP列表
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/getCmdbSystemIpList",  method = RequestMethod.POST)
    public Result<?> getCmdbSystemIpList(@RequestBody JSONObject jsonObject){
        return workStageService.getCmdbSystemIpList(jsonObject.getString("objectId"));
    }


    /**
     * 查询IP网段列表
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/getCmdbIpScoperList",  method = RequestMethod.GET)
    public Result<?> getCmdbIpScoperList(HttpServletRequest req){
        return workStageService.getCmdbIpScoperList();
    }

    /**
     * 保存防火墙策略申请草稿
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/saveTicket", method = RequestMethod.POST)
    public Result<?> saveTicket(@RequestBody JSONObject jsonObject){
        return workStageService.saveTicket(jsonObject);
    }

    /**
     * 查询工单列表
     * @param createUser 创建用户
     * @return
     */
    @RequestMapping(value = "/getTicketList", method = RequestMethod.GET)
    public Result<?> getTicketList(@RequestParam(name="createUser", required = true) String createUser){
        return workStageService.queryTicketList(createUser);
    }

    /**
     * 根据ID查询工单详情
     * @param id 工单ID
     * @return
     */
    @RequestMapping(value = "/getTicketById", method = RequestMethod.GET)
    public Result<?> getTicketById(@RequestParam(name="id", required = true) Integer id){
        return workStageService.queryTicketById(id);
    }

    /**
     * 生成工单附件
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/genTicket", method = RequestMethod.POST)
    public Result<?> genTicket(@RequestBody JSONObject jsonObject){
        return workStageService.genTicket(jsonObject);
    }

    /**
     * 提交工单（生成附件、上传ITIL、发起流程）
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/submitTicket", method = RequestMethod.POST)
    public Result<?> submitTicket(@RequestBody JSONObject jsonObject){
        return workStageService.submitTicket(jsonObject);
    }

    /**
     * 下载工单附件
     * @param id 工单ID
     * @return
     */
    @RequestMapping(value = "/downloadTicketAttachment", method = RequestMethod.GET)
    public Result<?> downloadTicketAttachment(@RequestParam(name="id", required = true) Integer id){
        return workStageService.downloadTicketAttachment(id);
    }

    /**
     * 下载文件
     * @param filePath 文件路径
     * @param response
     */
    @RequestMapping(value = "/downloadFile", method = RequestMethod.GET)
    public void downloadFile(@RequestParam(name="filePath", required = true) String filePath,
                           javax.servlet.http.HttpServletResponse response) throws Exception {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            response.setStatus(404);
            return;
        }
        
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + 
            java.net.URLEncoder.encode(file.getName(), "UTF-8") + "\"");
        
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
             java.io.OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }

    /**
     * 删除工单
     * @param id 工单ID
     * @return
     */
    @RequestMapping(value = "/deleteTicket", method = RequestMethod.DELETE)
    public Result<?> deleteTicket(@RequestParam(name="id", required = true) Integer id){
        return workStageService.deleteTicket(id);
    }
}
