/**
* Author: maowenjie
* Time: 2018-12-18
* */

/**
 * 重要提示：
 * 点击图片标识位置，无论点击任何位置，都会标识出一个对勾（由于无法定位其中的小图片区域），
 * 一次只能标识一个，不能再次点击，直至点击提交之后，若提示选择失败，才可以再次选中图片。
 * 若一次选择之后点击提交就成功，则也不能再次点击，直接跳转到下一页。
 * 点击之后，下方即显示出坐标
 * */

//表示时间段
var time;
//计时开始时间
var time_ori;
//这个flag用来辨别图上此刻是否有icon标记存在，gou：对勾
var gouflag;
//将来传到后台的坐标值
var objX, objY;


/**
 * 页面加载即开始计时，有不合理之处，即在用户第一次进入，
 * 输入用户名的时候，这段时间也算入了识别时间
 * 【上述问题已解决】
 */
$(function () {
    //记录开始时间
    var date = new Date();
    time_ori = date.getTime();

    //$("#capImg").attr("onclick", "Show(this)");
    //$("#capImg").attr("onclick", "c()");

    //禁用提交按钮，直到用户标记小对勾之后
    $("#subBtn").attr("disabled", true);
    //设置flag为0，表示此时没有小对勾出现
    gouflag = 0;
});

/**
 * 下一页，设置过渡加载动画
 * 由于现在电脑的性能很高，即使20人同时使用此系统，
 * 也不会出现加载很慢的情况，所以我设计的缓冲加载一般并不会有人看到(￣▽￣)~*
 */
function next() {
    var index = layer.load(3, {
        shade: [0.1, '#fff'] //0.1透明度的白色背景
    });
    setTimeout(function () {
        layer.closeAll('loading');
    }, 2000);
    window.location.href = "index";
}

/**
 * 获取此时鼠标相对于外框的位置，
 * 这保证了图片左上角的坐标为(0, 0)，
 * 图片右下角的坐标为(500, 350), 从而便于定位点击位置与答案位置作对比
 */
function getPos() {
    //对象x位置
    var objTop = getOffsetTop(document.getElementById("capImg"));
    //对象y位置
    var objLeft = getOffsetLeft(document.getElementById("capImg"));
    //鼠标x位置
    var mouseX = event.clientX + document.body.scrollLeft;
    //鼠标y位置
    var mouseY = event.clientY + document.body.scrollTop;
    //计算点击的相对位置
    objX = mouseX - objLeft;
    objY = mouseY - objTop;
}

/**
 * 点击图片插入图标
 * @param idstr 图片标签的id
 * @param url 插入的图片的路径
 */
function insertWithin(idstr, url) {
    //首先判断是否已有小对勾
    if (gouflag !== 0) {
        layer.msg("单选-不能再次点击其他位置，请点击下方提交按钮，重新再次点击“对勾”取消选择，然后重新点击图片");
    } else {

        //通过html页面的元素的id获取大div对象
        var node = document.getElementById("captchaBox");
        //创建一个新的div
        var NewDiv = document.createElement("div");
        //对div设置绝对定位，方便后来设置left，top
        NewDiv.style.position = "absolute";
        NewDiv.id = "iconDiv";
        //设置onclick属性
        NewDiv.onclick = cancelIcon;
        //获得这时的鼠标位置
        getPos();
        //弹出框便于测试
        // alert(objX + ", " + objY);

        //设置位置
        NewDiv.style.left = objX + 'px';
        NewDiv.style.top = objY + 'px';
        NewDiv.style.zIndex = '999';

        //创建div内里面的img标签对象
        var Newimg = document.createElement("img");
        //对图片设置路径和img的id
        Newimg.src = url;
        Newimg.id = idstr;
        //追加一个新的子结点
        NewDiv.appendChild(Newimg);
        //追加一个新的结点
        node.appendChild(NewDiv);
        //是否已有对号，是
        gouflag = 1;
        //改为default
        document.getElementById("capImg").style.cursor = "default";
        //显示坐标
        document.getElementById('xxx').innerText = "此次点击X坐标：" + objX;
        document.getElementById('yyy').innerText = "此次点击Y坐标：" + objY;
        //将提交按钮解禁
        $("#subBtn").attr("disabled", false);
    }
    //禁用下一张按钮
    $("#nextBtn").attr("disabled", true);
}

/**
 * 再次点击icon会取消选择
 * 当你选择完一个图片，发现自己手残选错了，
 * 或者找到了更好的答案，这时你可以悬崖勒马，再次点击小对勾取消选择，然后获得再次选择的机会
 */
function cancelIcon() {
    //删除包含img小对勾的小div
    $("#iconDiv").remove();
    //取消之后，自然flag变为0啦
    gouflag = 0;
    //提交按钮就不能用啦，毕竟你都还没选择
    $("#subBtn").attr("disabled", true);
    //但是下一张图片按钮就可以用啦
    $("#nextBtn").attr("disabled", false);
    //改变悬浮鼠标标识为pointer
    document.getElementById("capImg").style.cursor = "pointer";
}


/**
 * 提交答案，涉及操作稍多，详情看注释
 */
function submitAnswer() {
    //在此记录计时结束时间，用户点击提交的时候即是识别终止的时间
    var date = new Date();
    var time_finl = date.getTime();
    time = time_finl - time_ori;

    //禁止多次点击提交按钮
    $("#subBtn").attr("disabled", true);
    //解除下一张按钮的禁用状态
    $("#nextBtn").attr("disabled", false);
    // 通过cookie获取当前用户测试验证码的次数
    checkCookie();

    //获取用户名
    user = getCookie('username');

    //解析cookie，如果为空表明用户还没有填写，但理论上不可能为空，
    //因为用户不填写username就不能点击提交按钮
    if (user == "" || user == null) {
        //理论上这个不会执行
        layer.alert("请输入用户名！")
        checkCookie();
    } else {
        //直接从这执行，获得用户个人cookie中的资料
        var personalStr = getCookie2('num');
        var data = "posx=" + objX + "&posy=" + objY + "&num=" + $("#num").val() + "&username=" + user + "&time=" + time + "&perNum=" + personalStr;

        //ajax提交数据到后台controller处理
        $.ajax({
            type: "POST",
            url: 'postPosition',
            data: data,
            async: false,
            cache: false,
            success: function (data) {
                //此语句非常关键，判断后台返回结果正误
                if (data.flag === 'success') {
                    //成功，用户没有权限再次点击图片
                    $("#capImg").removeAttr("onclick");
                    //将鼠标指示样式也改为默认箭头
                    document.getElementById("capImg").style.cursor = "default";
                    //再次获得此时用户名
                    var username = getCookie('username');

                    //如果不为空(这是肯定的)
                    if (username != null && username !== "") {
                        //获取cookies中用户总点击次数
                        var temp = parseInt(getCookie2('num'));
                        //将此时的用户点击正确的次数加一
                        var temp2 = parseInt(getCookie2('succnum')) + 1;
                        //更新用户cookie，过期时间我一直设为了1天
                        setCookie('username', username, temp, temp2, 1);
                        //在图片下方提示用户信息
                        $("#totalcount").text('User: ' + username + "; CookiesTotalCount: " + getCookie2('num'));
                    }

                    //悬浮框提醒
                    layer.msg(data.msg, {icon: 1, time: 2000, offset: '550px'});
                    //提示成功与失败信息
                    $("#succp").text("congratulations, " + user + "! Picture selection is correct");
                    //显示结果，统计信息和成功率等等
                    $("#result").text("Total Time: " + time / 1000 + "s; Success Rate: " + data.SucRate + "; thisTotal: " + data.total + "; acc: " + data.acc);
                    //两秒后跳转，这个两秒要和后台设置的提醒语句一致
                    setTimeout(function () {
                        window.location.href = "index";
                    }, 2000);
                }
                else {
                    //如果失败，将图片鼠标指示改为手指pointer
                    document.getElementById("capImg").style.cursor = "pointer";
                    //弹出消息提示框
                    layer.msg(data.msg, {icon: 2, time: 1000, offset: '550px'});

                    //销毁新建icon div中的img，使得用户可以重新点击选择，这种js方法不如使用jquery方法简单
                    // var box = document.getElementById("iconImg");
                    // box.parentNode.removeChild(box);

                    //直接删除外面的div框，而不是删除里面的img
                    $("#iconDiv").remove();
                    //将flag设置为0，代表可以再次点击选择啦
                    gouflag = 0;
                    //提示成功与失败语句
                    $("#succp").text("Sorry, " + user + "! Picture selection is error");
                    //提示结果语句，表示出成功率和用时等信息
                    $("#result").text("Total Time: " + time / 1000 + "s; Success Rate: " + data.SucRate + "; thisTotal: " + data.total + "; acc: " + data.acc);
                }
            }
        });
    }
}


/**
 * 获取obj对象距离浏览器可视窗口上边缘的长度
 * @param obj
 * @returns {*|number}
 */
function getOffsetTop(obj) {
    //offsetTop表示元素的上边框至offsetParent元素的上内边框之间像素的距离
    var tmp = obj.offsetTop;
    //offsetParent表示元素的上一级元素
    var val = obj.offsetParent;
    while (val != null) {
        //累计距离上一个元素内边框的距离，由于此代码只有两层，循环两次即可
        tmp += val.offsetTop;
        //找到父级元素，并一层层往外找
        val = val.offsetParent;
    }
    return tmp;
}

/**
 * 获取obj对象距离浏览器可视窗口左边缘的长度
 * @param obj
 * @returns {*|number}
 */
function getOffsetLeft(obj) {
    //offsetLeft表示元素的左外边框至offsetParent元素左内边框之间的像素距离，具体注释同上
    var tmp = obj.offsetLeft;
    var val = obj.offsetParent;
    while (val != null) {
        tmp += val.offsetLeft;
        val = val.offsetParent;
    }
    return tmp;
}

//--------------------------分割线-----------------------------------


/**
 * 下面这些函数都是为了设置cookie，从cookie中获取验证次数，
 * 摘自luosainan师姐 - DndAndRotationCaptcha 的代码
 * */

//逗号分隔
function getCookie(c_name) {
    if (document.cookie.length > 0) {
        var c_start = document.cookie.indexOf(c_name + "=")
        if (c_start != -1) {
            c_start = c_start + c_name.length + 1
            var c_end = document.cookie.indexOf(",", c_start)
            if (c_end == -1)
                c_end = document.cookie.length
            //解码
            return unescape(document.cookie.substring(c_start, c_end))
        }
    }
    return ""
}

//分号分割
function getCookie2(c_name) {
    if (document.cookie.length > 0) {
        var c_start = document.cookie.indexOf(c_name + "=")
        if (c_start != -1) {
            c_start = c_start + c_name.length + 1;
            var c_end = document.cookie.indexOf(";", c_start)
            if (c_end == -1)
                c_end = document.cookie.length;
            return unescape(document.cookie.substring(c_start, c_end))
        }
    }
    return ""
}

/**
 * 设置浏览器cookie
 * @param c_name 用户名
 * @param value 用户填入的用户名的值
 * @param num 该用户总点击次数
 * @param succnum 该用户点击成功次数
 * @param expiredays cookie过期时间
 */
function setCookie(c_name, value, num, succnum, expiredays) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + expiredays);
    //escape编码加密
    document.cookie = c_name + "=" + escape(value) + ",num=" + escape(num) + ",succnum=" + succnum
        + ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString())
}

/**
 * 检查cookie中是否已存有用户信息，没有则新建cookie，填入信息
 */
function checkCookie() {
    var temp;
    var totalcount = $("#totalcount");
    var username = getCookie('username');
    if (username != null && username != "") {
        temp = parseInt(getCookie2('num')) + 1;
        temp2 = parseInt(getCookie2('succnum'));
        setCookie('username', username, temp, temp2, 1);

    } else {
        username = prompt('Please enter your name:', "");
        while (username == null || username === "") {
            username = prompt('Please enter your name:', "")
        }
        //用户第一次书写username的时间不算做识别验证码的时间
        var date = new Date();
        time_ori = date.getTime();
        setCookie('username', username, 1, 0, 1)
    }
    totalcount.text('User: ' + username + "; CookiesTotalCount: " + getCookie2('num'));
}
//----------------------------------分割线----------------------------------

/* 以下废弃不用 */
// function mousePosition(ev) {
//     if (ev.pageX || ev.pageY) {
//         return {x: ev.pageX, y: ev.pageY};
//     }
//     return {
//         x: ev.clientX + document.body.scrollLeft - document.body.clientLeft,
//         y: ev.clientY + document.body.scrollTop - document.body.clientTop
//     };
// }
//
// function mouseMove(ev) {
//     ev = ev || window.event;
//     var mousePos = mousePosition(ev);
//     document.getElementById('xxx').value = mousePos.x;
//     document.getElementById('yyy').value = mousePos.y;
// }
//
// document.onmousemove = mouseMove;

// function Show(el) {
//     var x = parseInt(document.getElementById('xxx').value) - el.offsetLeft;
//     var y = parseInt(document.getElementById('yyy').value) - el.offsetTop - 125;
//     x1 = "X:" + x;
//     y1 = "Y:" + y;
//     alert(x1+","+y1);
//     checkCookie();// 通过cookie获取当前用户测试验证码的次数
//     user = getCookie('username');
//     var data = "posx=" + x + "&posy=" + y + "&num=" + $("#num").val();
//     if (user == "" || user == null) {
//         layer.alert("请输入用户名！")
//         checkCookie();
//     } else {
//         var date = new Date()
//         var time_finl = date.getTime();
//         time = time_finl - time_ori;
//         $.ajax({
//             type: "POST",
//             url: 'postPosition',
//             data: data,
//             async: false,
//             cache: false,
//             success: function (data) {
//                 if (data.msg === 'success') {
//                     $("#capImg").removeAttr("onclick");
//                     document.getElementById("capImg").style.cursor = "default";
//
//                     var username = getCookie('username');
//                     if (username != null && username != "") {
//                         var temp = parseInt(getCookie2('num'));
//                         var temp2 = parseInt(getCookie2('succnum')) + 1;
//                         setCookie('username', username, temp, temp2, 1);
//                         $("#totalcount").text('User: ' + username + "; KookiesTotalCount: " + getCookie2('num'));
//                     }
//
//                     layer.msg(data.msg, {icon: 1, time: 3000, offset: '470px'});
//                     $("#succp").text("congratulations, " + user + "! Picture selection is correct");
//                     $("#result").text("Total Time: " + time / 1000 + "s; Success Rate: " + data.SucRate + "; thisTotal: " + data.total + "; acc: " + data.acc);
//                     //setTimeout(function(){ window.location.href = "index";}, 3000);
//                 }
//                 else {
//                     layer.msg(data.msg, {icon: 2, time: 1000, offset: '470px'});
//                     $("#succp").text("Sorry, " + user + "! Picture selection is error");
//                     $("#result").text("Total Time: " + time / 1000 + "s; Success Rate: " + data.SucRate + "; thisTotal: " + data.total + "; acc: " + data.acc);
//                 }
//             }
//         });
//     }
// }
/*废弃不用*/

//实时查看距离页面边缘距离
// function m(){
//     document.getElementById('xxx').value = event.clientX;
//     document.getElementById('yyy').value = event.clientY;
// }


// /**
//  * 获取鼠标相对位置并且传给后台
//  * 暂时不用
//  */
// function c(){
//     getPos();
//     //var clickObjPosition = objX + "," + objY;
//     //alert(clickObjPosition);
//     document.getElementById('xxx').innerText = "此次点击X坐标：" + objX;
//     document.getElementById('yyy').innerText = "此次点击X坐标：" + objY;
//
//     // 通过cookie获取当前用户测试验证码的次数
//     checkCookie();
//     user = getCookie('username');
//
//     if (user == "" || user == null) {
//         //理论上这个不会执行
//         layer.alert("请输入用户名！")
//         checkCookie();
//     } else {
//         //直接从这执行
//         var personalStr = getCookie2('num');
//         var date = new Date()
//         var time_finl = date.getTime();
//         time = time_finl - time_ori;
//         var data = "posx=" + objX + "&posy=" + objY + "&num=" + $("#num").val() + "&username=" + user + "&time=" + time + "&perNum=" + personalStr;
//         $.ajax({
//             type: "POST",
//             url: 'postPosition',
//             data: data,
//             async: false,
//             cache: false,
//             success: function (data) {
//                 if (data.flag === 'success') {
//                     $("#capImg").removeAttr("onclick");
//                     document.getElementById("capImg").style.cursor = "default";
//
//                     var username = getCookie('username');
//
//                     if (username != null && username != "") {
//                         var temp = parseInt(getCookie2('num'));
//                         var temp2 = parseInt(getCookie2('succnum')) + 1;
//                         setCookie('username', username, temp, temp2, 1);
//                         $("#totalcount").text('User: ' + username + "; KookiesTotalCount: " + getCookie2('num'));
//                     }
//
//                     layer.msg(data.msg, {icon: 1, time: 3000, offset: '550px'});
//                     $("#succp").text("congratulations, " + user + "! Picture selection is correct");
//                     $("#result").text("Total Time: " + time / 1000 + "s; Success Rate: " + data.SucRate + "; thisTotal: " + data.total + "; acc: " + data.acc);
//                     //两秒后跳转
//                     setTimeout(function(){ window.location.href = "index";}, 5000);
//                 }
//                 else {
//                     layer.msg(data.msg, {icon: 2, time: 1000, offset: '550px'});
//                     $("#succp").text("Sorry, " + user + "! Picture selection is error");
//                     $("#result").text("Total Time: " + time / 1000 + "s; Success Rate: " + data.SucRate + "; thisTotal: " + data.total + "; acc: " + data.acc);
//                 }
//             }
//         });
//     }
// }