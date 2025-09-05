// package org.jeecg.modules.home.entity;

// import java.util.List;
// import java.util.Map;

// public class AiOpsAlertResponse {
//    /**
//     * 请求返回码, ’0000‘为成功，其它均为失败
//     */
//    private String retCode;

//    /**
//     * 异常信息，当返回码非'0000'时才有
//     */
//    private String retMsg;

//    /**
//     * 返回总数  
//     */
//     private Integer totalCount;

//    /**
//     * 本次请求返回的数据
//     */
//    private T retData;

//    // getter and setter


//     public String getRetCode() {
//         return retCode;
//     }

//     public void setRetCode(String retCode) {
//         this.retCode = retCode;
//     }

//     public String getRetMsg() {
//         return retMsg;
//     }

//     public void setRetMsg(String retMsg) {
//         this.retMsg = retMsg;
//     }

//     public Integer getTotalCount() {
//         return totalCount;
//     }

//     public void setTotalCount(Integer totalCount) {
//         this.totalCount = totalCount;
//     }

//     public ResponseData getData() {
//         return data;
//     }

//     public void setData(ResponseData data) {
//         this.data = data;
//     }

//    public static class ResponseData {
//        /**
//         * 数据总条数
//         */
//        private Long totalNum;
//        /**
//         * 每页数据量
//         */
//        private Integer pageSize;
//        /**
//         * 当前页
//         */
//        private Integer pageNum;
//        /**
//         * 数据行
//         */
//        private List<Map<String, Object>> rows;

//        // getter and setter

//         public Long getTotalNum() {
//             return totalNum;
//         }

//         public void setTotalNum(Long totalNum) {
//             this.totalNum = totalNum;
//         }

//         public Integer getPageSize() {
//             return pageSize;
//         }

//         public void setPageSize(Integer pageSize) {
//             this.pageSize = pageSize;
//         }

//         public Integer getPageNum() {
//             return pageNum;
//         }

//         public void setPageNum(Integer pageNum) {
//             this.pageNum = pageNum;
//         }
//         public List<Map<String, Object>> getRows() {
//             return rows;
//         }

//         public void setRows(List<Map<String, Object>> rows) {
//             this.rows = rows;
//         }
//    }
// }