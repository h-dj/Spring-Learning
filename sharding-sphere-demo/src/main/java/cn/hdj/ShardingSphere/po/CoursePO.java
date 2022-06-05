package cn.hdj.ShardingSphere.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/5/31 16:21
 */
@Data
@TableName(value = "COURSE")
public class CoursePO implements java.io.Serializable {



    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;
    @TableField(value = "CNO")
    private String cno;
    @TableField(value = "CNAME")
    private String cname;
    @TableField(value = "TNO")
    private String tno;
}
