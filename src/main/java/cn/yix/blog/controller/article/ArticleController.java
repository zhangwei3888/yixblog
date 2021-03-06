package cn.yix.blog.controller.article;

import cn.yix.blog.core.article.IArticleStorage;
import cn.yix.blog.utils.DateUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: yxdave
 * Date: 13-6-21
 * Time: 下午5:27
 */
@Controller
@RequestMapping("/article")
public class ArticleController {

    @Resource(name = "articleStorage")
    public IArticleStorage articleStorage;

    @RequestMapping("/query.htm")
    public String listArticles(@RequestParam(required = false, value = "keywords[]") String[] keywords, @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate, Model model) {
        fillPageParams(keywords, startDate, endDate, model);
        return "article/list";
    }

    private void fillPageParams(String[] keywords, String startDate, String endDate, Model model) {
        model.addAttribute("keywordstring", buildKeywordsString(keywords));
        model.addAttribute("startDate", startDate == null ? "" : startDate);
        model.addAttribute("endDate", endDate == null ? "" : endDate);
    }

    private String buildKeywordsString(String[] keywords) {
        StringBuilder builder = new StringBuilder();
        if (keywords != null) {
            for (String keyword : keywords) {
                builder.append(keyword).append(" ");
            }
        }
        return builder.toString().trim();
    }

    @RequestMapping(value = "/query.action", method = RequestMethod.POST)
    public
    @ResponseBody
    JSONObject listArticlesByJSON(@RequestParam(required = false, value = "keywords[]") String[] keywords, @RequestParam(required = false, defaultValue = "") String startDate,
                                  @RequestParam(required = false, defaultValue = "") String endDate, @RequestParam(required = false, defaultValue = "1") int page,
                                  @RequestParam(required = false, defaultValue = "15") int pageSize, @RequestParam(required = false, defaultValue = "addtime") String sortkey,
                                  @RequestParam(required = false, defaultValue = "0") int userId, @RequestParam(required = false) String tag) {
        if ("".equals(tag)) {
            tag = null;
        }
        return articleStorage.queryArticles(page, pageSize, DateUtils.parseDate(startDate, DateUtils.DATE_FORMAT), DateUtils.parseDate(endDate, DateUtils.DATE_FORMAT), userId, tag, keywords, sortkey);
    }

    @RequestMapping("/view/{articleId}.htm")
    public String showArticle(@PathVariable int articleId, Model model) {
        JSONObject res = articleStorage.queryArticle(articleId);
        model.addAttribute("res", res);
        return "article/content";
    }

    @RequestMapping("/tag_cloud.action")
    public
    @ResponseBody
    JSONObject getTopTags(@RequestParam int topnumber) {
        return articleStorage.queryTags(topnumber);
    }

    @RequestMapping("/tag/{tagName}.htm")
    public String queryArticlesByTag(@PathVariable String tagName, Model model) {
        model.addAttribute("tagname", tagName);
        return "article/tag_result";
    }

    @RequestMapping(value = "hot_users.action", method = RequestMethod.POST)
    public
    @ResponseBody
    JSONObject listHotUsers(@RequestParam int topnumber) {
        return articleStorage.queryArticleAuthors(topnumber);
    }
}
