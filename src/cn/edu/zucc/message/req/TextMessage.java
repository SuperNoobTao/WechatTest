package cn.edu.zucc.message.req;

/**
 * Created by vito on 2016/7/25.
 */
public class TextMessage extends BaseMessage {
    // 消息内容
    private String Content;

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }
}
