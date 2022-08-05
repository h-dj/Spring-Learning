package cn.hdj.common.api;

import lombok.Data;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/8/4 17:10
 */
@Data
public class BasePage {

    private int pageSize;
    private int pageNum;


    public int getPageSize() {
        if (pageSize <= 0) {
            pageSize = 10;
        }
        return pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }
}
