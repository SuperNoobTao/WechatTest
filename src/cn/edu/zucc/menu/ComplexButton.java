package cn.edu.zucc.menu;

/**
 * 复合类型的按钮
 *
 * Created by vito on 2016/7/29.
 */
public class ComplexButton extends Button {
	private Button[] sub_button;

	public Button[] getSub_button() {
		return sub_button;
	}

	public void setSub_button(Button[] sub_button) {
		this.sub_button = sub_button;
	}
}
