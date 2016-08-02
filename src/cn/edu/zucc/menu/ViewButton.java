package cn.edu.zucc.menu;

/**
 * view类型的按钮
 *
 * Created by vito on 2016/7/29.
 */
public class ViewButton extends Button {
	private String type;
	private String url;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
