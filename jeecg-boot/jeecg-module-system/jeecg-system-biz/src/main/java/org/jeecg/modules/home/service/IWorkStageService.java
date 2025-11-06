package org.jeecg.modules.home.service;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;




/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
public interface IWorkStageService{

	/**
	 * 查询我的告警列表
	 * 
	 * @param req
	 * @param queryWrapper
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	Result<?> queryMyAlarmList(String userName);
	

	/**
	 * 关闭告警
	 * 
	 * @param id
	 * @param resolution
	 * @return
	 */
	Result<?> closeAlarm(Long id, String resolution);


	/**
	 * 处理告警
	 * 
	 * @param id
	 * @param resolution
	 * @return
	 */
	Result<?> dealAlarm(Long id);



	/**
	 * 查询应用系统信息
	 * 
	 * @param req
	 * @param queryWrapper
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	Result<?> qryCmdbSystem();

	/**
	 * 查询应用系统相关IP列表
	 * 
	 * @param businessName
	 * @return
	 */
	Result<?> getCmdbSystemIpList(String objectId);


	/**
	 * 查询IP网段列表
	 * @param req
	 * @return
	 */
	Result<?> getCmdbIpScoperList();

	/**
	 * 保存防火墙策略申请草稿
	 * @param jsonObject
	 * @return
	 */
	Result<?> saveTicket(JSONObject jsonObject);

	/**
	 * 查询工单列表
	 * @param createUser 创建用户
	 * @return
	 */
	Result<?> queryTicketList(String createUser);

	/**
	 * 根据ID查询工单详情
	 * @param id 工单ID
	 * @return
	 */
	Result<?> queryTicketById(Integer id);

}
