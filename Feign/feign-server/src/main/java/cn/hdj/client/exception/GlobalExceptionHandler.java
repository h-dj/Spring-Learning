package cn.hdj.client.exception;

import cn.hdj.client.domain.ApiResponse;
import cn.hdj.client.domain.ApiResponseHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author huangjiajian
 * @Date 2022/7/12 下午11:30
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理自定义异常
     *
     * @param ex 异常信息
     * @return 返回前端异常信息
     */
    @ExceptionHandler(ApiException.class)
    @ResponseBody
    public ApiResponse exception(ApiException ex) {
        log.error("错误详情：" + ex.getMessage(), ex);

        ApiResponse apiResponse = new ApiResponse(null);
        ApiResponseHeader header = apiResponse.getHeader();
        header.setCode(ex.getCode().toString());
        header.setDesc(ex.getMessage());
        return apiResponse;
    }
}
