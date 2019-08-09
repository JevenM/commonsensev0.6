package com.captcha.commonsense;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;
import com.captcha.commonsense.utils.Util;


/**
 * @Name: com.captcha.commonsense/commonsense
 * @Auther: 毛文杰
 * @Date: 12/17/2018 12:07
 * @Description:
 */
@Controller
public class CaptchaBGController {

    private String ph = ClassUtils.getDefaultClassLoader().getResource("").getPath();

    private static Logger logger = LoggerFactory.getLogger(CaptchaBGController.class);

    private int total = 0;
    private int accurate = 0;

    private String filePath = ph + "/static/question.txt";

    //由于这个文件是保存在生成的target中的，所以暂时不再使用，改为使用下面的outpath，持久化文件，防止数据丢失
    private String rootpath = ph + "/static/answers.txt";

    private String outpath = "E:\\IdeaProject\\answers-" + Calendar.getInstance().get(Calendar.MONTH+1) + "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + ".txt";

    private int questionNumber;

//    /**
//     * @description: 网站维护页面，暂时
//     * @param: []
//     * @return: java.lang.String
//     * @auther: 毛文杰
//     * @date: 12/21/2018 2:18 PM
//     */
//    @GetMapping("/")
//    public String hello(){
//        return "pagenf";
//    }

//    @RequestMapping(value = {"/", "index"})
//    public String changeBG(Model model) {
//        Random random = new Random();
//        questionNumber = random.nextInt(1035);
//        logger.info("问题序号：= {}",  questionNumber);
//        String captchaPath = "/static/images/img/" + questionNumber + ".jpg";
//        logger.info("图片位置：= {}", captchaPath);
//
//        //questionNumber就是问题/图片的编号
//        model.addAttribute("question", Util.generateQuestion(filePath, questionNumber, 1));
//        model.addAttribute("showCaptchaImg", captchaPath);
//        model.addAttribute("questionNum", questionNumber);
//        return "index";
//    }


    /**
     * @description: 获取前端用户传来的信息进行处理
     * @param: [posx：点击的x轴坐标, posy：点击的y轴坐标, num：前端传来的图片编号(名字), username: 用户名, time：一次点击用时, perNum：前端cookie的个人信息，需要分割解析]
     * @return: org.springframework.http.ResponseEntity<java.util.Map<java.lang.String,java.lang.Object>>
     * @auther: 毛文杰
     * @date: 12/19/2018 2:16 PM
     */
    @PostMapping("postPosition")
    public ResponseEntity<Map<String, Object>> getPosition(String posx, String posy, String num, String username, String time, String perNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        logger.info("posx: = {}; posy: = {}", posx, posy);
        int posX = Integer.parseInt(posx), posY = Integer.parseInt(posy);
        Integer leftTopx = 0, rightBottomx = 0, leftTopy = 0, rightBottomy = 0;

        //从图片名获取位置，并且解析存进有序列表
        List<Integer> list = Util.getAnswerPosition(filePath, Integer.parseInt(num));
        leftTopx = list.get(0);
        leftTopy = list.get(1);
        rightBottomx = list.get(2);
        rightBottomy = list.get(3);

        //服务器总次数加以一
        total++;

        //解析用户cookie信息
        String person[] = perNum.split(",");
        String perTotalNum = person[0];
        String perAcc = person[1];
        String perAccNum = perAcc.split("=")[1];

        //判断正误
        if (posX >= leftTopx && posX <= rightBottomx && posY >= leftTopy && posY <= rightBottomy) {
            accurate++;
            map.put("msg", "Success, Jump to next page after 2s");
            map.put("flag","success");
            map.put("SucRate", accurate * 1.0 / total);
            map.put("total", total);
            map.put("acc", accurate);

            //保存进文件
            Util.save2txt(outpath, username, total, accurate, time, perTotalNum, perAccNum);

            return new ResponseEntity<>(map, HttpStatus.OK);
        } else {
            map.put("flag","failed");
            map.put("msg", "Failed, Please re-select");
            map.put("total", total);
            map.put("acc", accurate);
            map.put("SucRate", accurate * 1.0 / total);
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }
}
