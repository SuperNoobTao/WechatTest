package cn.edu.zucc.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import cn.edu.zucc.message.resp.Article;
import cn.edu.zucc.message.resp.Music;
import cn.edu.zucc.pojo.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;


/**
 * Created by vito on 2016/7/28.
 */
    public class AdvancedUtil {
    private static Logger log = LoggerFactory.getLogger(AdvancedUtil.class);

    /**
     * 组装文本客服消息
     *
     * @param openId 消息发送对象
     * @param content 文本消息内容
     * @return
     */
    public static String makeTextCustomMessage(String openId, String content) {
        // 对消息内容中的双引号进行转义
        content = content.replace("\"", "\\\"");
        String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"text\",\"text\":{\"content\":\"%s\"}}";
        return String.format(jsonMsg, openId, content);
    }

    /**
     * 组装图片客服消息
     *
     * @param openId 消息发送对象
     * @param mediaId 媒体文件id
     * @return
     */
    public static String makeImageCustomMessage(String openId, String mediaId) {
        String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"image\",\"image\":{\"media_id\":\"%s\"}}";
        return String.format(jsonMsg, openId, mediaId);
    }

    /**
     * 组装语音客服消息
     *
     * @param openId 消息发送对象
     * @param mediaId 媒体文件id
     * @return
     */
    public static String makeVoiceCustomMessage(String openId, String mediaId) {
        String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"voice\",\"voice\":{\"media_id\":\"%s\"}}";
        return String.format(jsonMsg, openId, mediaId);
    }

    /**
     * 组装视频客服消息
     *
     * @param openId 消息发送对象
     * @param mediaId 媒体文件id
     * @param thumbMediaId 视频消息缩略图的媒体id
     * @return
     */
    public static String makeVideoCustomMessage(String openId, String mediaId, String thumbMediaId) {
        String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"video\",\"video\":{\"media_id\":\"%s\",\"thumb_media_id\":\"%s\"}}";
        return String.format(jsonMsg, openId, mediaId, thumbMediaId);
    }

    /**
     * 组装音乐客服消息
     *
     * @param openId 消息发送对象
     * @param music 音乐对象
     * @return
     */
    public static String makeMusicCustomMessage(String openId, Music music) {
        String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"music\",\"music\":%s}";
        jsonMsg = String.format(jsonMsg, openId, JSONObject.fromObject(music).toString());
        // 将jsonMsg中的thumbmediaid替换为thumb_media_id
        jsonMsg = jsonMsg.replace("thumbmediaid", "thumb_media_id");
        return jsonMsg;
    }

    /**
     * 组装图文客服消息
     *
     * @param openId 消息发送对象
     * @param articleList 图文消息列表
     * @return
     */
    public static String makeNewsCustomMessage(String openId, List<Article> articleList) {
        String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"news\",\"news\":{\"articles\":%s}}";
        jsonMsg = String.format(jsonMsg, openId, JSONArray.fromObject(articleList).toString().replaceAll("\"", "\\\""));
        // 将jsonMsg中的picUrl替换为picurl
        jsonMsg = jsonMsg.replace("picUrl", "picurl");
        return jsonMsg;
    }

    /**
     * 发送客服消息
     *
     * @param accessToken 接口访问凭证
     * @param jsonMsg json格式的客服消息（包括touser、msgtype和消息内容）
     * @return true | false
     */
    public static boolean sendCustomMessage(String accessToken, String jsonMsg) {
        log.info("消息内容：{}", jsonMsg);
        boolean result = false;
        // 拼接请求地址
        String requestUrl = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=ACCESS_TOKEN";
        requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken);
        // 发送客服消息
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "POST", jsonMsg);

        if (null != jsonObject) {
            int errorCode = jsonObject.getInt("errcode");
            String errorMsg = jsonObject.getString("errmsg");
            if (0 == errorCode) {
                result = true;
                log.info("客服消息发送成功 errcode:{} errmsg:{}", errorCode, errorMsg);
            } else {
                log.error("客服消息发送失败 errcode:{} errmsg:{}", errorCode, errorMsg);
            }
        }

        return result;
    }

    /**
     * 获取网页授权凭证
     *
     * @param appId 公众账号的唯一标识
     * @param appSecret 公众账号的密钥
     * @param code
     * @return WeixinAouth2Token
     */
    public static WeixinOauth2Token getOauth2AccessToken(String appId, String appSecret, String code) {
        WeixinOauth2Token wat = null;
        // 拼接请求地址
        String requestUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
        requestUrl = requestUrl.replace("APPID", appId);
        requestUrl = requestUrl.replace("SECRET", appSecret);
        requestUrl = requestUrl.replace("CODE", code);
        // 获取网页授权凭证
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);
        if (null != jsonObject) {
            try {
                wat = new WeixinOauth2Token();
                wat.setAccessToken(jsonObject.getString("access_token"));
                wat.setExpiresIn(jsonObject.getInt("expires_in"));
                wat.setRefreshToken(jsonObject.getString("refresh_token"));
                wat.setOpenId(jsonObject.getString("openid"));
                wat.setScope(jsonObject.getString("scope"));
            } catch (Exception e) {
                wat = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                log.error("获取网页授权凭证失败 errcode:{} errmsg:{}", errorCode, errorMsg);
            }
        }
        return wat;
    }

    /**
     * 刷新网页授权凭证
     *
     * @param appId 公众账号的唯一标识
     * @param refreshToken
     * @return WeixinAouth2Token
     */
    public static WeixinOauth2Token refreshOauth2AccessToken(String appId, String refreshToken) {
        WeixinOauth2Token wat = null;
        // 拼接请求地址
        String requestUrl = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=APPID&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";
        requestUrl = requestUrl.replace("APPID", appId);
        requestUrl = requestUrl.replace("REFRESH_TOKEN", refreshToken);
        // 刷新网页授权凭证
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);
        if (null != jsonObject) {
            try {
                wat = new WeixinOauth2Token();
                wat.setAccessToken(jsonObject.getString("access_token"));
                wat.setExpiresIn(jsonObject.getInt("expires_in"));
                wat.setRefreshToken(jsonObject.getString("refresh_token"));
                wat.setOpenId(jsonObject.getString("openid"));
                wat.setScope(jsonObject.getString("scope"));
            } catch (Exception e) {
                wat = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                log.error("刷新网页授权凭证失败 errcode:{} errmsg:{}", errorCode, errorMsg);
            }
        }
        return wat;
    }

    /**
     * 通过网页授权获取用户信息
     *
     * @param accessToken 网页授权接口调用凭证
     * @param openId 用户标识
     * @return SNSUserInfo
     */
    @SuppressWarnings( { "deprecation", "unchecked" })
    public static SNSUserInfo getSNSUserInfo(String accessToken, String openId) {
        SNSUserInfo snsUserInfo = null;
        // 拼接请求地址
        String requestUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";
        requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace("OPENID", openId);
        // 通过网页授权获取用户信息
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);

        if (null != jsonObject) {
            try {
                snsUserInfo = new SNSUserInfo();
                // 用户的标识
                snsUserInfo.setOpenId(jsonObject.getString("openid"));
                // 昵称
                snsUserInfo.setNickname(jsonObject.getString("nickname"));
                // 性别（1是男性，2是女性，0是未知）
                snsUserInfo.setSex(jsonObject.getInt("sex"));
                // 用户所在国家
                snsUserInfo.setCountry(jsonObject.getString("country"));
                // 用户所在省份
                snsUserInfo.setProvince(jsonObject.getString("province"));
                // 用户所在城市
                snsUserInfo.setCity(jsonObject.getString("city"));
                // 用户头像
                snsUserInfo.setHeadImgUrl(jsonObject.getString("headimgurl"));
                // 用户特权信息
                snsUserInfo.setPrivilegeList(JSONArray.toList(jsonObject.getJSONArray("privilege"), List.class));
            } catch (Exception e) {
                snsUserInfo = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                log.error("获取用户信息失败 errcode:{} errmsg:{}", errorCode, errorMsg);
            }
        }
        return snsUserInfo;
    }


    /**
     * 创建临时带参二维码
     *
     * @param accessToken 接口访问凭证
     * @param expireSeconds 二维码有效时间，单位为秒，最大不超过1800
     * @param sceneId 场景ID
     * @return WeixinQRCode
     */
    public static WeixinQRCode createTemporaryQRCode(String accessToken, int expireSeconds, int sceneId) {
        WeixinQRCode weixinQRCode = null;
        // 拼接请求地址
        String requestUrl = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=ACCESS_TOKEN";
        requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken);
        // 需要提交的json数据
        String jsonMsg = "{\"expire_seconds\": %d, \"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": %d}}}";
        // 创建临时带参二维码
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "POST", String.format(jsonMsg, expireSeconds, sceneId));

        if (null != jsonObject) {
            try {
                weixinQRCode = new WeixinQRCode();
                weixinQRCode.setTicket(jsonObject.getString("ticket"));
                weixinQRCode.setExpireSeconds(jsonObject.getInt("expire_seconds"));
                log.info("创建临时带参二维码成功 ticket:{} expire_seconds:{}", weixinQRCode.getTicket(), weixinQRCode.getExpireSeconds());
            } catch (Exception e) {
                weixinQRCode = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                log.error("创建临时带参二维码失败 errcode:{} errmsg:{}", errorCode, errorMsg);
            }
        }
        return weixinQRCode;
    }

    /**
     * 创建永久带参二维码
     *
     * @param accessToken 接口访问凭证
     * @param sceneId 场景ID
     * @return ticket
     */
    public static String createPermanentQRCode(String accessToken, int sceneId) {
        String ticket = null;
        // 拼接请求地址
        String requestUrl = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=ACCESS_TOKEN";
        requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken);
        // 需要提交的json数据
        String jsonMsg = "{\"action_name\": \"QR_LIMIT_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": %d}}}";
        // 创建永久带参二维码
        JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "POST", String.format(jsonMsg, sceneId));

        if (null != jsonObject) {
            try {
                ticket = jsonObject.getString("ticket");
                log.info("创建永久带参二维码成功 ticket:{}", ticket);
            } catch (Exception e) {
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                log.error("创建永久带参二维码失败 errcode:{} errmsg:{}", errorCode, errorMsg);
            }
        }
        return ticket;
    }

    /**
     * 根据ticket换取二维码
     *
     * @param ticket 二维码ticket
     * @param savePath 保存路径
     */
    public static String getQRCode(String ticket, String savePath) {
        String filePath = null;
        // 拼接请求地址
        String requestUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET";
        requestUrl = requestUrl.replace("TICKET", CommonUtil.urlEncodeUTF8(ticket));
        try {
            URL url = new URL(requestUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setRequestMethod("GET");

            if (!savePath.endsWith("/")) {
                savePath += "/";
            }
            // 将ticket作为文件名
            filePath = savePath + ticket + ".jpg";

            // 将微信服务器返回的输入流写入文件
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            FileOutputStream fos = new FileOutputStream(new File(filePath));
            byte[] buf = new byte[8096];
            int size = 0;
            while ((size = bis.read(buf)) != -1)
                fos.write(buf, 0, size);
            fos.close();
            bis.close();

            conn.disconnect();
            log.info("根据ticket换取二维码成功，filePath=" + filePath);
        } catch (Exception e) {
            filePath = null;
            log.error("根据ticket换取二维码失败：{}", e);
        }
        return filePath;
    }


    /**
     * 永久上传媒体文件
     *
     * @param accessToken 接口访问凭证
     * @param type 媒体文件类型（image、voice、video和thumb）
     * @param file 媒体文件的url
     */
    public static WeixinMediaEver uploadMediaEver(String accessToken, String type, MultipartFile file) {
        System.out.println("accessToken="+accessToken);
        System.out.println("image="+type);

        WeixinMediaEver weixinMedia = null;
        // 拼装请求地址
        String uploadMediaUrl = "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=ACCESS_TOKEN&type=TYPE";
        uploadMediaUrl = uploadMediaUrl.replace("ACCESS_TOKEN", accessToken).replace("TYPE", type);

        // 定义数据分隔符
        String boundary = "------------7da2e536604c8";
        try {
            URL uploadUrl = new URL(uploadMediaUrl);
            HttpURLConnection uploadConn = (HttpURLConnection) uploadUrl.openConnection();
            uploadConn.setDoOutput(true);
            uploadConn.setDoInput(true);
            uploadConn.setRequestMethod("POST");
            // 设置请求头Content-Type
            uploadConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            // 获取媒体文件上传的输出流（往微信服务器写数据）
            OutputStream outputStream = uploadConn.getOutputStream();



            // 从请求头中获取内容类型
            String contentType = file.getContentType();
            // 根据内容类型判断文件扩展名
            String fileExt = CommonUtil.getFileExt(contentType);
            // 请求体开始
            outputStream.write(("--" + boundary + "\r\n").getBytes());
            outputStream.write(String.format("Content-Disposition: form-data; name=\"media\"; filename=\"file1%s\"\r\n", fileExt).getBytes());
            outputStream.write(String.format("Content-Type: %s\r\n\r\n", contentType).getBytes());

            // 获取媒体文件的输入流（读取文件）
            BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
            byte[] buf = new byte[8096];
            int size = 0;
            while ((size = bis.read(buf)) != -1) {
                // 将媒体文件写到输出流（往微信服务器写数据）
                outputStream.write(buf, 0, size);
            }
            // 请求体结束
            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
            outputStream.close();
            bis.close();


            // 获取媒体文件上传的输入流（从微信服务器读数据）
            InputStream inputStream = uploadConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer buffer = new StringBuffer();
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            uploadConn.disconnect();

            // 使用JSON-lib解析返回结果
            JSONObject jsonObject = JSONObject.fromObject(buffer.toString());
            weixinMedia = new WeixinMediaEver();
            weixinMedia.setMediaId(jsonObject.getString("media_id"));
            weixinMedia.setUrl(jsonObject.getString("url"));
        } catch (Exception e) {
            weixinMedia = null;
            log.error("上传媒体文件失败：{}", e);
        }
        return weixinMedia;
    }




    /**
     * 上传媒体文件
     *
     * @param accessToken 接口访问凭证
     * @param type 媒体文件类型（image、voice、video和thumb）
     * @param file 媒体文件的url
     */
    public static WeixinMedia uploadMedia1(String accessToken, String type, MultipartFile file) {
        System.out.println("accessToken="+accessToken);
        System.out.println("image="+type);

        WeixinMedia weixinMedia = null;
        // 拼装请求地址
        String uploadMediaUrl = "http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";
        uploadMediaUrl = uploadMediaUrl.replace("ACCESS_TOKEN", accessToken).replace("TYPE", type);

        // 定义数据分隔符
        String boundary = "------------7da2e536604c8";
        try {
            URL uploadUrl = new URL(uploadMediaUrl);
            HttpURLConnection uploadConn = (HttpURLConnection) uploadUrl.openConnection();
            uploadConn.setDoOutput(true);
            uploadConn.setDoInput(true);
            uploadConn.setRequestMethod("POST");
            // 设置请求头Content-Type
            uploadConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            // 获取媒体文件上传的输出流（往微信服务器写数据）
            OutputStream outputStream = uploadConn.getOutputStream();



            // 从请求头中获取内容类型
            String contentType = file.getContentType();
            // 根据内容类型判断文件扩展名
            String fileExt = CommonUtil.getFileExt(contentType);
            // 请求体开始
            outputStream.write(("--" + boundary + "\r\n").getBytes());
            outputStream.write(String.format("Content-Disposition: form-data; name=\"media\"; filename=\"file1%s\"\r\n", fileExt).getBytes());
            outputStream.write(String.format("Content-Type: %s\r\n\r\n", contentType).getBytes());

            // 获取媒体文件的输入流（读取文件）
            BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
            byte[] buf = new byte[8096];
            int size = 0;
            while ((size = bis.read(buf)) != -1) {
                // 将媒体文件写到输出流（往微信服务器写数据）
                outputStream.write(buf, 0, size);
            }
            // 请求体结束
            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
            outputStream.close();
            bis.close();


            // 获取媒体文件上传的输入流（从微信服务器读数据）
            InputStream inputStream = uploadConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer buffer = new StringBuffer();
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            uploadConn.disconnect();

            // 使用JSON-lib解析返回结果
            JSONObject jsonObject = JSONObject.fromObject(buffer.toString());
            weixinMedia = new WeixinMedia();
            weixinMedia.setType(jsonObject.getString("type"));
            // type等于thumb时的返回结果和其它类型不一样
            if ("thumb".equals(type))
                weixinMedia.setMediaId(jsonObject.getString("thumb_media_id"));
            else
                weixinMedia.setMediaId(jsonObject.getString("media_id"));
            weixinMedia.setCreatedAt(jsonObject.getInt("created_at"));
        } catch (Exception e) {
            weixinMedia = null;
            log.error("上传媒体文件失败：{}", e);
        }
        return weixinMedia;
    }

    /**
     * 上传媒体文件
     *
     * @param accessToken 接口访问凭证
     * @param type 媒体文件类型（image、voice、video和thumb）
     * @param mediaFileUrl 媒体文件的url
     */
    public static WeixinMedia uploadMedia(String accessToken, String type, String mediaFileUrl) {
        System.out.println("accessToken="+accessToken);
        System.out.println("image="+type);
        System.out.println("mediaFileUrl="+mediaFileUrl);
        WeixinMedia weixinMedia = null;
        // 拼装请求地址
        String uploadMediaUrl = "http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";
        uploadMediaUrl = uploadMediaUrl.replace("ACCESS_TOKEN", accessToken).replace("TYPE", type);

        // 定义数据分隔符
        String boundary = "------------7da2e536604c8";
        try {
            URL uploadUrl = new URL(uploadMediaUrl);
            HttpURLConnection uploadConn = (HttpURLConnection) uploadUrl.openConnection();
            uploadConn.setDoOutput(true);
            uploadConn.setDoInput(true);
            uploadConn.setRequestMethod("POST");
            // 设置请求头Content-Type
            uploadConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            // 获取媒体文件上传的输出流（往微信服务器写数据）
            OutputStream outputStream = uploadConn.getOutputStream();

            URL mediaUrl = new URL(mediaFileUrl);
            HttpURLConnection meidaConn = (HttpURLConnection) mediaUrl.openConnection();
            meidaConn.setDoOutput(true);
            meidaConn.setRequestMethod("GET");

            // 从请求头中获取内容类型
            String contentType = meidaConn.getHeaderField("Content-Type");
            // 根据内容类型判断文件扩展名
            String fileExt = CommonUtil.getFileExt(contentType);
            // 请求体开始
            outputStream.write(("--" + boundary + "\r\n").getBytes());
            outputStream.write(String.format("Content-Disposition: form-data; name=\"media\"; filename=\"file1%s\"\r\n", fileExt).getBytes());
            outputStream.write(String.format("Content-Type: %s\r\n\r\n", contentType).getBytes());

            // 获取媒体文件的输入流（读取文件）
            BufferedInputStream bis = new BufferedInputStream(meidaConn.getInputStream());
            byte[] buf = new byte[8096];
            int size = 0;
            while ((size = bis.read(buf)) != -1) {
                // 将媒体文件写到输出流（往微信服务器写数据）
                outputStream.write(buf, 0, size);
            }
            // 请求体结束
            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
            outputStream.close();
            bis.close();
            meidaConn.disconnect();

            // 获取媒体文件上传的输入流（从微信服务器读数据）
            InputStream inputStream = uploadConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer buffer = new StringBuffer();
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            uploadConn.disconnect();

            // 使用JSON-lib解析返回结果
            JSONObject jsonObject = JSONObject.fromObject(buffer.toString());
            weixinMedia = new WeixinMedia();
            weixinMedia.setType(jsonObject.getString("type"));
            // type等于thumb时的返回结果和其它类型不一样
            if ("thumb".equals(type))
                weixinMedia.setMediaId(jsonObject.getString("thumb_media_id"));
            else
                weixinMedia.setMediaId(jsonObject.getString("media_id"));
            weixinMedia.setCreatedAt(jsonObject.getInt("created_at"));
        } catch (Exception e) {
            weixinMedia = null;
            log.error("上传媒体文件失败：{}", e);
        }
        return weixinMedia;
    }



    /**
     * 下载媒体文件
     *
     * @param accessToken 接口访问凭证
     * @param mediaId 媒体文件标识
     * @param savePath 文件在服务器上的存储路径
     * @return
     */
    public static String getMedia(String accessToken, String mediaId, String savePath) {
        String filePath = null;
        // 拼接请求地址
        String requestUrl = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=ACCESS_TOKEN&media_id=MEDIA_ID";
        requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace("MEDIA_ID", mediaId);
        System.out.println(requestUrl);
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setRequestMethod("GET");

            if (!savePath.endsWith("/")) {
                savePath += "/";
            }
            // 根据内容类型获取扩展名
            String fileExt = CommonUtil.getFileExt(conn.getHeaderField("Content-Type"));
            // 将mediaId作为文件名
            filePath = savePath + mediaId + fileExt;

            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            FileOutputStream fos = new FileOutputStream(new File(filePath));
            byte[] buf = new byte[8096];
            int size = 0;
            while ((size = bis.read(buf)) != -1)
                fos.write(buf, 0, size);
            fos.close();
            bis.close();

            conn.disconnect();
            log.info("下载媒体文件成功，filePath=" + filePath);
        } catch (Exception e) {
            filePath = null;
            log.error("下载媒体文件失败：{}", e);
        }
        return filePath;
    }





    public static void main(String args[]) {
        // 获取接口访问凭证
        String accessToken = CommonUtil.getToken("wxa2e22be671c6774b", "2833fb4fa09b18f4218661131b95c0f2");

///**
// * 创建临时二维码
// */
//        WeixinQRCode weixinQRCode = createTemporaryQRCode(accessToken, 900, 111111);
//        // 临时二维码的ticket
//        System.out.println(weixinQRCode.getTicket());
//        // 临时二维码的有效时间
//        System.out.println(weixinQRCode.getExpireSeconds());

///**
// *创建永久二维码
// **/
//        String ticket = createPermanentQRCode(accessToken, 617);
//        System.out.println(ticket);


/**
 *换取二维码
 *
 */
        String ticket = "gQEv8ToAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL3ZFUzcyUlhsWWg0YklxdERfR2g5AAIE0fueVwMEAAAAAA==";
        String savePath = "D:/download";
        getQRCode(ticket,savePath);


//        /**
//         * 根据ticket换取二维码
//         */
//        String ticket = "gQEg7zoAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2lIVVJ3VmJsTzFsQ0ZuQ0Y1bG5WAAIEW35+UgMEAAAAAA==";
//        String savePath = "G:/download";
//        // 根据ticket换取二维码
//        getQRCode(ticket, savePath);


//        /**
//         * 上传多媒体文件
//         */
//        WeixinMedia weixinMedia = uploadMedia(accessToken, "image", "http://img05.tooopen.com/images/20150202/sy_80219211654.jpg");
//        System.out.println(weixinMedia.getMediaId());
//        System.out.println(weixinMedia.getType());
//        System.out.println(weixinMedia.getCreatedAt());

//        /**
//         * 下载多媒体文件
//         */
//        getMedia(accessToken, "ru-F4OLTdzjRF3yo6zgItOy1E4RPSiHFyUW_yU5OvoS3SXQ-PTwYzV3m9OHVecMF", "D:/download");
    }

}