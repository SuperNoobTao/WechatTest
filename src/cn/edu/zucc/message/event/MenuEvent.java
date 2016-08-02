package cn.edu.zucc.message.event;

import cn.edu.zucc.message.event.BaseEvent;

/**
 * 自定义菜单事件
 *
 * Created by vito on 2016/7/29.
 */
public class MenuEvent extends BaseEvent {
	// 事件KEY值，与自定义菜单接口中KEY值对应
	private String EventKey;

	public String getEventKey() {
		return EventKey;
	}

	public void setEventKey(String eventKey) {
		EventKey = eventKey;
	}
}
