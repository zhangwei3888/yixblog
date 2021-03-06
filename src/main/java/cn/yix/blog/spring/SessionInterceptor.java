package cn.yix.blog.spring;

import cn.yix.blog.core.admin.IAdminAccountStorage;
import cn.yix.blog.core.user.IUserAccountStorage;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: yixian
 * Date: 13-6-7
 * Time: 上午11:21
 */
public class SessionInterceptor extends HandlerInterceptorAdapter {
    private String[] adminBlackList;
    private String[] userBlackList;
    private boolean open;
    private Logger logger = Logger.getLogger(getClass());
    @Resource(name = "adminAccountStorage")
    private IAdminAccountStorage adminAccountStorage;
    @Resource(name = "userAccountStorage")
    private IUserAccountStorage userAccountStorage;

    public void setAdminBlackList(String[] adminBlackList) {
        this.adminBlackList = adminBlackList;
    }

    public void setUserBlackList(String[] userBlackList) {
        this.userBlackList = userBlackList;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!open) {
            return true;
        }
        HttpSession session = request.getSession();
        JSONObject admin = (JSONObject) session.getAttribute("admin");
        JSONObject user = (JSONObject) session.getAttribute("user");
        String uri = getRequestURI(request);
        if (admin == null) {
            for (String adminBlackpattern : adminBlackList) {
                if (checkUriMatch(uri, adminBlackpattern)) {
                    if (uri.endsWith(".action")) {
                        PrintWriter writer = response.getWriter();
                        writer.write(generateNotLoginJSON("您必须登录管理员账号后才能访问此页面").toJSONString());
                        writer.flush();
                        writer.close();
                    } else {
                        response.sendRedirect("/static/pages/AdminNotLogin.html");
                    }
                    return false;
                }
            }
        }
        if (user == null) {
            for (String userBlackpattern : userBlackList) {
                if (checkUriMatch(uri, userBlackpattern)) {
                    if (uri.endsWith(".action")) {
                        PrintWriter writer = response.getWriter();
                        writer.write(generateNotLoginJSON("您必须登录").toJSONString());
                        writer.flush();
                        writer.close();
                    } else {
                        response.sendRedirect("/static/pages/UserNotLogin.html");
                    }
                    return false;
                }
            }
        }
        boolean handleResult = super.preHandle(request, response, handler);
        if (admin != null) {
            updateSessionAdmin(session, admin.getIntValue("id"));
        }
        if (user != null) {
            updateSessionUser(session, user.getIntValue("id"));
        }
        return handleResult;
    }

    private void updateSessionAdmin(HttpSession session, int adminId) {
        JSONObject updatedAdmin = adminAccountStorage.queryAdminById(adminId);
        session.setAttribute("admin", updatedAdmin);
    }

    private void updateSessionUser(HttpSession session, int userId) {
        JSONObject updatedUser = userAccountStorage.queryUser(userId).getJSONObject("user");
        logger.debug("session update user info:" + updatedUser.toJSONString());
        session.setAttribute("user", updatedUser);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.debug("post handle called,requestURI:" + request.getRequestURI() + ",url:" + request.getRequestURL().toString());
        if (request.getRequestURI().endsWith(".htm") || request.getRequestURI().endsWith("/")) {
            logger.debug("called");
            String path = request.getContextPath();
            path = path.length() > 0 ? path + "/" : path;
            modelAndView.getModel().put("basePath", path);
            JSONObject sessionAdmin = (JSONObject) request.getSession().getAttribute("admin");
            if (sessionAdmin != null) {
                modelAndView.getModel().put("admin", sessionAdmin);
            }
            JSONObject sessionUser = (JSONObject) request.getSession().getAttribute("user");
            if (sessionUser != null) {
                modelAndView.getModel().put("user", sessionUser);
            }
//            logger.debug(((JSONObject)(modelAndView.getModel().get("data"))).toJSONString());
        }
        super.postHandle(request, response, handler, modelAndView);
    }

    private String getRequestURI(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctxPath = request.getContextPath();
        return uri.replace(ctxPath, "");
    }

    private boolean checkUriMatch(String uri, String pattern) {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, uri);
    }

    private JSONObject generateNotLoginJSON(String msg) {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("msg", msg);
        logger.info("not login response:" + json.toJSONString());
        return json;
    }
}
