package com.captcha.commonsense;

import com.captcha.commonsense.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @Name: com.captcha.commonsense/commonsense
 * @Auther: 毛文杰
 * @Date: 12/25/2018 09:02
 * @Description: 旋转常识验证码的后台控制器
 */
@Controller
public class RotateCaptchaController {

    private Logger logger = LoggerFactory.getLogger(RotateCaptchaController.class);

    //img下文件的数
    private int count = 1000;

    private String ph = ClassUtils.getDefaultClassLoader().getResource("").getPath();
    private int month = Calendar.getInstance().get(Calendar.MONTH) + 1;

    private String outpath = "E:\\IdeaProject\\answers-" + month + "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "1.txt";

    private int total = 0;
    private int accurate = 0;

    private String questionPath = ph + "/static/question.txt";


    @GetMapping({"/", "inde", "next"})
    public String nextPage(Model model){
        Random random = new Random();
        Integer fileNum = random.nextInt(count);
        String filename = "Challenge" + fileNum;
        String webpath = "/static/images/img/" + filename;
        String path = ph + webpath;
        File file = new File(path);
        //问题内容
        String ques = findQuestion(questionPath, fileNum, 1);
        logger.info("问题 = {}", ques);
        List<String> list = new ArrayList<String>();
        //文件读取
        if (file.exists()) {
            //获取其中的每个图片
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                logger.error("文件夹是空的!");
            }
            else {
                for (File filep : files) {
                    if (filep.isDirectory()) {
                        logger.info("文件夹！= {}" + filep.getAbsolutePath());
                    }
                    else {
                        logger.info("文件夹名称！= {}, 文件名 = {}", filep.getParentFile().getName(), filep.getName());
                        list.add(filep.getName());
                    }
                }
            }
        } else {
            logger.error("文件夹不存在!");
        }
        //打乱list中的图片顺序
        Collections.shuffle(list);

        //添加model
        for (int i = 0; i < 9; i++) {
            model.addAttribute("p"+(i+1), webpath + "/" + list.get(i));
        }
        model.addAttribute("question", ques);
        model.addAttribute("num_", fileNum);

        //找到后面的初始化旋转参数，示例：
        //question.txt文件内容
        //0.	what is a two wheeled method of transportation	0.1439171	bicycle	['0bicycle3.jpg//270', '0bicycle52.jpg//90', '0bicycle9.jpg//180', 'badminton24.jpg//90', 'badminton40.jpg//90', 'gun15.jpg//270', 'gun4.jpg//90', 'gun8.jpg//180', '化妆桌22.jpg//90']
        String roteDegree = findQuestion(questionPath, fileNum, 4);
        //去掉两边的括号[]
        String subRot = roteDegree.substring(1, roteDegree.length() - 1);
        logger.info("initial rotate: = {}", subRot);
        //分割出每个字符串
        String[] file_rotelist = subRot.split(", ");

        //去掉两边的单引号，加入fl中
        List<String> fl = new ArrayList();
        for (int i = 0; i < file_rotelist.length; i++) {
            fl.add(file_rotelist[i].substring(1, file_rotelist[i].length() - 1));
        }
        //用于存放传去前端的旋转角度
        String[] roteArr = new String[9];
        //存放答案图片所在的index序号
        List ansList = new ArrayList();
        //根据question.txt中的答案找到对应的图片索引序号
        for (String picname : list){
            //图片文件名字中第一个字符为0的是答案
            if (picname.charAt(0) == '0'){
                logger.info("序号 = {}", list.indexOf(picname));
                ansList.add(list.indexOf(picname));
                //遍历对比找到fl中的答案，将list中的序号index赋给数组相应位置，并给数组赋值，值为该图片的旋转角度，目的在于旋转角度也要和文件序号保持对应
                for (String ss : fl){
                    if (ss.contains(picname)){
                        roteArr[list.indexOf(picname)] = ss.split("//")[1];
                    }
                }
            }
        }
        //添加model
        model.addAttribute("ansindex", ansList);
        model.addAttribute("rotatelist", roteArr);
        return "inde";
    }


    /**
     * @description: 通过随机生成的数字查找对应的问题
     * @param: [filepath, number, flag]
     * @return: java.lang.String
     * @auther: 毛文杰
     * @date: 12/25/2018 3:41 PM
     */
    public String findQuestion(String filepath, int number, int flag) {
        String questionString = "";
        try {
            File file = new File(filepath);
            if (file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), "GBK");//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int i = 0;
                //这里要求TXT每行的编号和那行对应的图片的编号严格相对应
                //比如编号为Challenge5的文件夹，对应问题在txt里面排第六行
                while ((lineTxt = bufferedReader.readLine()) != null && i <= number) {
                    questionString = lineTxt;
                    i++;
                }
                read.close();
            } else {
                logger.error("找不到指定的文件");
            }
        } catch (Exception e) {
            logger.error("读取文件内容出错");
            e.printStackTrace();
        }
        String s = "";

        switch (flag){
            case 1:
                //获得问题
                s = questionString.split("\t")[1];
                break;
            case 2:
                //获得用时
                s = questionString.split("\t")[2];
                break;
            case 3:
                //获得ans name
                s = questionString.split("\t")[3];
                break;
            case 4:
                //获得初始化旋转角度
                s = questionString.split("\t")[4];
                break;
        }

//        if (flag == 1) {
//            //获得问题
//            s = questionString.split("\t")[1];
//        } else if (flag == 2) {
//            //获得用时
//            s = questionString.split("\t")[2];
//        } else if (flag == 3){
//            //获得ans name
//            s = questionString.split("\t")[3];
//        } else if (flag == 4){
//            //获得初始旋转角度
//            s = questionString.split("\t")[3];
//        }
        return s;
    }

    /**
     * @description: 接受前端数据信息
     * @param: [num_, username, time, perNum, succflag]
     * @return: java.lang.String
     * @auther: 毛文杰
     * @date: 12/27/2018 10:18 AM
     */
    @PostMapping("postData")
    @ResponseBody
    public String acceptData(String num_, String username, String time, String perNum, String succflag){
        Map<String, Object> map = new HashMap<String, Object>();

        //服务器总次数加以一
        addNum(1);

        //解析用户cookie信息
        String person[] = perNum.split(",");
        String perTotalNum = person[0];
        String perAcc = person[1];
        String perAccNum = perAcc.split("=")[1];

        if (Integer.parseInt(succflag) == 1){
            addNum(2);
        }
        Util.save2txt(outpath, username, total, accurate, time, perTotalNum, perAccNum);
        return "s";
    }

    /**
     * @description: 递增防止数据被覆盖
     * @return: void
     * @auther: 毛文杰
     * @date: 12/27/2018 10:22 AM
     */
    public synchronized void addNum(int flag){
        if(flag == 1){
            total++;
        }
        else{
            accurate++;
        }
    }
}

