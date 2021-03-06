package cn.edu.zucc.controller;

import cn.edu.zucc.pojo.SNSUserInfo;
import cn.edu.zucc.pojo.WeixinMedia;
import cn.edu.zucc.pojo.WeixinMediaEver;
import cn.edu.zucc.pojo.WeixinOauth2Token;
import cn.edu.zucc.service.CoreService;
import cn.edu.zucc.util.AdvancedUtil;
import cn.edu.zucc.util.CommonUtil;
import cn.edu.zucc.util.SignUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;



/**
 * Created by vito on 2016/7/26.
 */
@Controller
@RequestMapping("/welcome")
public class HomeController {



    @RequestMapping(value="/api",method = RequestMethod.GET)
    @ResponseBody
    public void api( HttpServletResponse response, HttpServletRequest request) throws IOException {

        // 微信加密签名
        String signature = request.getParameter("signature");
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");
        // 随机字符串
        String echostr = request.getParameter("echostr");
        PrintWriter out = response.getWriter();
        // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
        if (SignUtil.checkSignature(signature, timestamp, nonce)) {
            out.print(echostr);
        }
        out.close();
        out = null;

    }

    @RequestMapping(value="/api",method = RequestMethod.POST)
    @ResponseBody
    public void  getWeiXinMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 将请求、响应的编码均设置为UTF-8（防止中文乱码）
        request.setCharacterEncoding("UTF-8");  //微信服务器POST消息时用的是UTF-8编码，在接收时也要用同样的编码，否则中文会乱码；
        response.setCharacterEncoding("UTF-8"); //在响应消息（回复消息给用户）时，也将编码方式设置为UTF-8，原理同上；
        //初始化配置文件

        // 调用核心业务类接收消息、处理消息
        String respMessage = CoreService.processRequest(request);

        // 响应消息
        PrintWriter out = response.getWriter();
        out.print(respMessage);
        out.close();
    }

    @RequestMapping(value="/OAuth",method = RequestMethod.GET)
    public String OAuth( HttpServletResponse response, HttpServletRequest request) throws IOException, ServletException {
        System.out.println("进入OAuth");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");

        // 用户同意授权后，能获取到code
        String code = request.getParameter("code");
        System.out.println(code);

        // 用户同意授权
        if (!"authdeny".equals(code)) {
            // 获取网页授权access_token
            WeixinOauth2Token weixinOauth2Token = AdvancedUtil.getOauth2AccessToken("wxa2e22be671c6774b", "2833fb4fa09b18f4218661131b95c0f2", code);
            // 网页授权接口访问凭证(json取得acccessToken)
            String accessToken = weixinOauth2Token.getAccessToken();
            // 用户标识(json里取得openId)
            String openId      = weixinOauth2Token.getOpenId();
            // 获取用户信息
            SNSUserInfo snsUserInfo = AdvancedUtil.getSNSUserInfo(accessToken, openId);

            // 设置要传递的参数
            request.setAttribute("snsUserInfo", snsUserInfo);
        }
        // 跳转到index.jsp
      return "index";
    }


    @RequestMapping(value="/Upload",method = RequestMethod.POST)
    public String Upload(HttpServletRequest request,
                         HttpServletResponse response, ModelMap model) throws IOException, ServletException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        // 得到上传的文件
        MultipartFile mFile = multipartRequest.getFile("file");
        System.out.println(mFile.getName());
        System.out.println(mFile.getOriginalFilename());
        System.out.println(mFile.getContentType());
        String accessToken = CommonUtil.getToken("wxa2e22be671c6774b", "2833fb4fa09b18f4218661131b95c0f2");
        WeixinMediaEver weixinMedia = AdvancedUtil.uploadMediaEver(accessToken,"image",mFile);
        System.out.println(weixinMedia.getMediaId());
        System.out.println(weixinMedia.getUrl());
        return "index";
    }

}
