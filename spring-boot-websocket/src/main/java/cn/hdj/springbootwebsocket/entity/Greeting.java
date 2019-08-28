package cn.hdj.springbootwebsocket.entity;

/**
 * @author hdj
 * @Description:
 * @date 8/28/19
 */
public class Greeting {

    private String content;

    public Greeting() {
    }

    public Greeting(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
