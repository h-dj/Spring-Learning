package cn.hdj.repeatsubmit.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 去重表
 * </p>
 *
 * @author huangjiajian
 * @since 2021-12-12
 */
@TableName("deduplicate")
public class DeduplicatePO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 唯一ID
     */
    private String uniqueId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String toString() {
        return "DeduplicatePO{" +
        "id=" + id +
        ", uniqueId=" + uniqueId +
        "}";
    }
}
