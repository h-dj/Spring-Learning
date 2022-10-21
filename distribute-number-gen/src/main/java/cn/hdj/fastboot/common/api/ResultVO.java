package cn.hdj.fastboot.common.api;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/4/15 14:28
 */
@Data
public class ResultVO implements  Serializable {

    private static final long serialVersionUID = 1348172152215944560L;

    /**
     * 200为正确
     */
    private Integer code;

    /**
     * 返回处理信息，成功或者失败
     */
    private String msg;

    /**
     * 成功返回true，失败返回false
     */
    private Boolean success;

    /**
     * 返回给前端的数据
     */
    private Object data;

    /**
     * 成功返回的json封装体
     */
    public static ResultVO successJson(String msg, Object value) {
        ResultVO results = new ResultVO();
        results.setCode(ResponseCodeEnum.SUCCESS.getCode());
        results.setMsg(msg);
        results.setSuccess(true);
        results.setData(value);
        return results;
    }

    /**
     * 成功返回的json封装体
     *
     * @param value 原始数据
     * @return json格式
     */
    public static ResultVO successJson(Object value) {
        ResultVO results = new ResultVO();
        results.setCode(ResponseCodeEnum.SUCCESS.getCode());
        results.setMsg(ResponseCodeEnum.SUCCESS.getMsg());
        results.setSuccess(true);
        results.setData(value);
        return results;
    }


    /**
     * 失败返回的json封装体
     *
     * @return json格式
     */
    public static ResultVO errorJson() {
        ResultVO results = new ResultVO();
        results.setCode(ResponseCodeEnum.UNKNOWN.getCode());
        results.setSuccess(false);
        results.setMsg(ResponseCodeEnum.UNKNOWN.getMsg());
        return results;
    }


    /**
     * 失败返回的json封装体
     *
     * @return json格式
     */
    public static ResultVO errorJson(String msg, Integer code) {
        ResultVO results = new ResultVO();
        results.setCode(code);
        results.setSuccess(false);
        results.setMsg(msg);
        return results;
    }

    public static ResultVO errorJson(String msg) {
        return errorJson(msg, ResponseCodeEnum.UNKNOWN.getCode());
    }

    public static ResultVO errorJson(ResponseCodeEnum r) {
        ResultVO results = new ResultVO();
        results.setCode(r.getCode());
        results.setSuccess(false);
        results.setMsg(r.getMsg());
        return results;
    }
}
