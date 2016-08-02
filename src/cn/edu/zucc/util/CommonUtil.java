package cn.edu.zucc.util;

import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import cn.edu.zucc.pojo.TemplateData;
import cn.edu.zucc.pojo.Token;
import cn.edu.zucc.pojo.WxTemplate;
import javafx.scene.input.TouchEvent;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.RedisAPI;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 通用工具类
 *
 * Created by vito on 2016/7/29.
 */
public class CommonUtil {
	private static Logger log = LoggerFactory.getLogger(CommonUtil.class);

	// 凭证获取（GET）
	public final static String token_url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";

	/**
	 * 发送https请求
	 *
	 * @param requestUrl 请求地址
	 * @param requestMethod 请求方式（GET、POST）
	 * @param outputStr 提交的数据
	 * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
	 */
	public static JSONObject httpsRequest(String requestUrl, String requestMethod, String outputStr) {
		JSONObject jsonObject = null;
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();

			URL url = new URL(requestUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setSSLSocketFactory(ssf);

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod(requestMethod);

			// 当outputStr不为null时向输出流写数据
			if (null != outputStr) {
				OutputStream outputStream = conn.getOutputStream();
				// 注意编码格式
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}

			// 从输入流读取返回内容
			InputStream inputStream = conn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			StringBuffer buffer = new StringBuffer();
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}

			// 释放资源
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			inputStream = null;
			conn.disconnect();
			jsonObject = JSONObject.fromObject(buffer.toString());
		} catch (ConnectException ce) {
			log.error("连接超时：{}", ce);
		} catch (Exception e) {
			log.error("https请求异常：{}", e);
		}
		return jsonObject;
	}

	/**
	 * 获取接口访问凭证
	 *
	 * @param appid 凭证
	 * @param appsecret 密钥
	 * @return
	 */
	public static String getToken(String appid, String appsecret) {
		JedisPool pool = RedisAPI.getPool();
		Jedis jedis = null;
			jedis = pool.getResource();

			String key = StringUtil.getRediskey_accesstoken(appid);

			String token = jedis.get(key);
			if (token == null) {
				String accesstoken = null;
				String requestUrl = token_url.replace("APPID", appid).replace("APPSECRET", appsecret);
				// 发起GET请求获取凭证
				JSONObject jsonObject = httpsRequest(requestUrl, "GET", null);
				if (jsonObject != null) {
					try {
						accesstoken=jsonObject.getString("access_token");
						int expire=jsonObject.getInt("expires_in");
						jedis.setex(key,expire-60,accesstoken);
					} catch (JSONException e) {
						token = null;
						// 获取token失败
						log.error("获取token失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));
					}
				}

				return accesstoken;
			}

		return token;


	}

	/**
	 * URL编码（utf-8）
	 *
	 * @param source
	 * @return
	 */
	public static String urlEncodeUTF8(String source) {
		String result = source;
		try {
			result = java.net.URLEncoder.encode(source, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 根据内容类型判断文件扩展名
	 *
	 * @param contentType 内容类型
	 * @return
	 */
	public static String getFileExt(String contentType) {
		String fileExt = "";
		if ("image/jpeg".equals(contentType))
			fileExt = ".jpg";
		else if ("audio/mpeg".equals(contentType))
			fileExt = ".mp3";
		else if ("audio/amr".equals(contentType))
			fileExt = ".amr";
		else if ("video/mp4".equals(contentType))
			fileExt = ".mp4";
		else if ("video/mpeg4".equals(contentType))
			fileExt = ".mp4";
		return fileExt;
	}






		/**
		 * 发送模板消息
		 * appId 公众账号的唯一标识
		 * appSecret 公众账号的密钥
		 * openId 用户标识
		 */
		public  static void send_template_message(String appId, String appSecret, String openId) {
			String access_token = getToken(appId, appSecret);

			String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + access_token;
			WxTemplate temp = new WxTemplate();
			temp.setUrl("http://weixin.qq.com/download");
			temp.setTouser(openId);
			temp.setTopcolor("#000000");
//        temp.setTemplate_id("ngqIpbwh8bUfcSsECmogfXcV14J0tQlEpBO27izEYtY");
			temp.setTemplate_id("K8d6YFoB_6Q2rg4mVkjZ26C-m-ZIBANtBO8EriSJuZc");
			Map<String, TemplateData> m = new HashMap<String, TemplateData>();
			TemplateData firstData = new TemplateData();
			firstData.setColor("#000000");
			firstData.setValue("恭喜购物成功");
			m.put("firstData", firstData);
			TemplateData product = new TemplateData();
			product.setColor("#000000");
			product.setValue("韩版西服");
			m.put("product", product);
			TemplateData price = new TemplateData();
			price.setColor("#000000");
			price.setValue("149元");
			m.put("price", price);
			TemplateData time = new TemplateData();
			time.setColor("#000000");
			time.setValue("2016-08-02 13:43:05");
			TemplateData remark = new TemplateData();
			remark.setColor("#000000");
			remark.setValue("感谢您的光临，我们将尽快发货");
			m.put("remark", remark);
			temp.setData(m);
			String jsonString = JSONObject.fromObject(temp).toString();
			System.out.println(jsonString);
			JSONObject jsonObject = httpsRequest(url, "POST", jsonString);
			System.out.println(jsonObject);
			int result = 0;
			if (null != jsonObject) {
				if (0 != jsonObject.getInt("errcode")) {
					result = jsonObject.getInt("errcode");
					log.error("错误 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));
				}
			}
			log.info("模板消息发送结果：" + result);
		}

	public static void main(String args[]) {
		String accessToken = CommonUtil.getToken("wxa2e22be671c6774b", "2833fb4fa09b18f4218661131b95c0f2");
		CommonUtil.send_template_message("wxa2e22be671c6774b","2833fb4fa09b18f4218661131b95c0f2","ouWiZv7MRTGGEE27Dbqwm1Bz5Zkc");






	}

}