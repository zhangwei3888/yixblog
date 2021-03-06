/**
 * Created with IntelliJ IDEA.
 * User: yxdave
 * Date: 13-7-24
 * Time: 上午1:00
 * To change this template use File | Settings | File Templates.
 */
$(document).ready(function () {

    //build article manage page
    function listArticles(page) {
        var tag = $("#current_tag_input").val();
        $.ajax({
            url: "a/user/article/query.action",
            dataType: "json",
            data: {page: page, tag: tag},
            type: "post",
            success: function (data) {
                var box = $("#article_list").empty();
                buildSelfArticle(box, data);
            }
        });
    }

    listArticles(1);

    function buildSelfArticle(box, data) {
        appendArticleTitle(box);
        for (var index = 0; index < data.articles.length; index++) {
            appendArticleRows(box, data.articles[index]);
        }
        $("<dd></dd>").yixpager({
            pageInfo: data,
            callback: listArticles
        }).appendTo(box);
    }

    function appendArticleTitle(box) {
        var dt = $("<dt></dt>");
        var ul = $("<ul class='article_row'></ul>");
        $("<li class='article_title'>标题</li>").appendTo(ul);
        $("<li class='article_addtime'>添加时间</li>").appendTo(ul);
        $("<li class='article_edittime'>修改时间</li>").appendTo(ul);
        $("<li class='article_reply'>回复</li>").appendTo(ul);
        $("<li class='article_active'>操作</li>").appendTo(ul);
        ul.appendTo(dt);
        dt.appendTo(box);
    }

    function appendArticleRows(articleDLList, article) {
        var dd = $("<dd></dd>");
        var ul = $("<ul class='article_row'></ul>");
        var titleLi = $("<li class='article_title'></li>");
        $("<a target='_blank'></a>").attr("href", "a/article/view/" + article.id + ".htm").attr("title", article.title).html(article.title).appendTo(titleLi);
        titleLi.appendTo(ul);
        $("<li class='article_addtime time'></li>").html(article.addtimestring).appendTo(ul);
        $("<li class='article_edittime time'></li>").html(article.edittimestring).appendTo(ul);
        $("<li class='article_reply'></li>").html(article.replycount).appendTo(ul);
        var editbtn = $("<a href='#' class='edit_btn' title='编辑'></a>").click(function () {
            editArticle(article.id);
        });
        var deletebtn = $("<a href='#' class='delete_btn' title='删除'></a>").click(function () {
            deleteArticle(article.id);
        });
        $("<li class='article_active'></li>").append(deletebtn).append(editbtn).appendTo(ul);
        ul.appendTo(dd);
        dd.appendTo(articleDLList);
    }

    function editArticle(id) {
        alert("edit article :" + id);
    }

    function deleteArticle(id) {
        alert("delete article:" + id);
    }

    $("#menu").selectable({
        create: function () {
            $("#menu").children("li:eq(0)").addClass("ui-selected").click();
        },
        stop: function () {
            $(".ui-selected").not(":first").removeClass("ui-selected");
            var index = $(".ui-selected").prevAll().length;
            var itemWidth = $(".right_box .box_item:eq(0)").outerWidth(true);
            var scrollLeft = index * itemWidth;
            console.log("inner scroll left:" + scrollLeft);
            $(".right_box").dequeue().animate({scrollLeft: scrollLeft}, "slow");
        }
    });

    $("#user_info_tab").tabs({
        beforeLoad: function (event, ui) {
            ui.jqXHR.error(function () {
                ui.panel.html("request failed");
            });
        },
        show: {effect: "slide", duration: "fast"},
        hide: {effect: "slide", duration: "fast"}
    });

    $("#new_article_btn").button().click(function () {
        window.open("a/user/article/new.htm")
    });

    function updateImageList(page) {
        $.ajax({
            url: "a/user/images.action",
            dataType: "json",
            data:{page:page},
            type: "post",
            success: function (data) {
                if (data.success) {
                    var images = data.images;
                    var $imglist = $("#img_list");
                    $imglist.empty();
                    for (var i = 0, len = images.length; i < len; i++) {
                        buildImageItem(images[i]);
                    }
                    $imglist.yixpager({
                        pageInfo:data,
                        callback:updateImageList
                    });
                } else {
                    console.log(data.msg)
                }
            }
        });
    }

    updateImageList(1);

    function buildImageItem(image) {
        var imgBox = $("#img_list");
        var imgItem = $("<span></span>", {class: "img_item"}).appendTo(imgBox);
        $("<img>", {src: image.url}).appendTo(imgItem);
        $("<div></div>", {class: "delete"}).appendTo(imgItem).click(function () {
            $.ajax({
                url: "a/user/deleteImage.action",
                type: "post",
                data: {id: image.id},
                dataType: "json",
                success:function(data){
                    if(!data.success){
                        alert(data.msg);
                    }else{
                        updateImageList(1)
                    }
                }
            })
        })
    }

    var CommentList = function (url) {
        var box = $("<dl class='comment_list'></dl>");
        var panelCache;

        this.appendToPanel = function (panel) {
            panel.append(box);
            SyntaxHighlighter.highlight(box);
            panelCache = panel;
        };

        this.initUI = function (data) {
            showComments(data);
        };
        function appendTitle(comment_title) {
            var row = $("<ul class='comment_row'></ul>");
            $("<li class='article'></li>").html("日志").appendTo(row);
            $("<li class='time'></li>").html("发表时间").appendTo(row);
            $("<li class='author'></li>").html("作者").appendTo(row);
            row.appendTo(comment_title);
        }

        function appendDataRow(row_dd, item_data) {
            var row = $("<h3 class='comment_row'></h3>");
            var article_link = $("<a target='_blank'></a>").attr("href", "a/article/view/" + item_data.article.id + ".htm").html(item_data.article.title);
            $("<span class='article'></span>").html(article_link).appendTo(row);
            $("<span class='time'></span>").html(item_data.addtimestring).appendTo(row);
            $("<span class='author'></span>").append($("<a></a>", {href: "a/userinfo/" + item_data.author.id + ".htm", html: item_data.author.nick, target: "_blank"})).appendTo(row);
            row.appendTo(row_dd);
            $("<div></div>", {class: "comment_content", html: item_data.content}).appendTo(row_dd);
        }

        function showComments(data) {
            box.empty();
            var comment_title = $("<dt></dt>");
            comment_title.appendTo(box);
            appendTitle(comment_title);
            var comments = data.comments;
            var row_dd = $("<dd></dd>");
            for (var i = 0; i < comments.length; i++) {
                appendDataRow(row_dd, comments[i]);
            }
            row_dd.appendTo(box);
            row_dd.find("a").click(function () {
                window.open($(this).attr("href"));
            });
            $("<dd></dd>").yixpager({
                pageInfo: data,
                callback: changeCommentPage
            }).appendTo(box);
            if (panelCache != null) {
                SyntaxHighlighter.highlight(box);
            }
            row_dd.accordion({
                collapsible: true,
                icons: null,
                heightStyle: "content"
            });
        }

        function changeCommentPage(page) {
            $.ajax({
                url: url,
                type: "post",
                dataType: "json",
                data: {page: page},
                success: showComments
            });
        }
    };
    var currentCommentList;
    $("#comment_tab").tabs({
        beforeLoad: function (event, ui) {
            currentCommentList = new CommentList(ui.ajaxSettings.url);
            ui.ajaxSettings.type = "post";
            ui.ajaxSettings.dataType = "json";
            ui.ajaxSettings.success = function (data) {
                currentCommentList.initUI(data);
            };

        },
        load: function (event, ui) {
            currentCommentList.appendToPanel(ui.panel);
        }
    });

});