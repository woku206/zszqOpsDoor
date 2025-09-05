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

}
