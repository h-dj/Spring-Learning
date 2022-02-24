package cn.hdj.message.message.domain.web;

import lombok.Data;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/2/24 15:47
 */
@Data
public class WebMsgResponse implements java.io.Serializable{

    private int statusCode;
    private String msg;
}
