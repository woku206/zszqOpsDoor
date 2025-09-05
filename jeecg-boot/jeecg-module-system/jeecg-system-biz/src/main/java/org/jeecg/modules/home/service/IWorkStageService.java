package org.jeecg.modules.home.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import org.jeecg.modules.home.vo.*;
import org.apache.ibatis.annotations.Param;
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

}
