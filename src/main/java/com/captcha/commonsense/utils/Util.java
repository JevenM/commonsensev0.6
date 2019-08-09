package com.captcha.commonsense.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Name: com.captcha.commonsense.utils/commonsense
 * @Auther: 毛文杰
 * @Date: 12/19/2018 14:21
 * @Description:
 */
public class Util {

    private static Logger logger = LoggerFactory.getLogger(Util.class);

    /**
     * @description: 保存文件操作
     * @param: [username, total, accurate, time, perTotalNum, perAccNum]
     * @return: void
     * @auther: 毛文杰
     * @date: 12/19/2018 2:14 PM
     */
    public static void save2txt(String outpath, String username, int total, int accurate, String time, String perTotalNum, String perAccNum){
        /*将成功通过的任务用时写入文件*/
        File fptime = new File(outpath);
        FileWriter fw = null;
        try {
            fw = new FileWriter(fptime, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        //写入文件中的内容
        //用户，服务器所有的点击次数，点击正确的次数，该用户用时，该用户点击次数，该用户点击正确的次数，时间便于分辨是否存入文档
        Date day=new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logger.info("存入时间time = {}", df.format(day));

        pw.println(username + " " + Integer.toString(total) + " " + Integer.toString(accurate) + " " + time + " " + perTotalNum + " " + perAccNum + " Time: " + df.format(day));
        pw.flush();

        try {
            fw.flush();
            pw.close();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @description: 获取正确的答案坐标
     * @param: [number: 选择要显示出来的图片编号]
     * @return: java.lang.String
     * @auther: 毛文杰
     * @date: 12/17/2018 4:53 PM
     */
    public static List<Integer> getAnswerPosition(String filePath, Integer number) {
        List posList = new ArrayList<Integer>();
        String position = generateQuestion(filePath, number, 2);
        //去掉两边的括号
        String pos = position.substring(1, position.length() - 1);
        logger.info("right position: = {}", pos);

        String[] poslist = pos.split(",");
        posList.add(Integer.parseInt(poslist[0]));
        posList.add(Integer.parseInt(poslist[1]));
        posList.add(Integer.parseInt(poslist[2]));
        posList.add(Integer.parseInt(poslist[3]));
        return posList;
    }

    /**
     * @description: 获得questions.txt中的问题语句
     * @param: [filepath, number, flag: 0代表图片名字 1代表问题 2代表正确答案位置]
     * @return: java.lang.String
     * @auther: 毛文杰
     * @date: 12/17/2018 4:51 PM
     */
    public static String generateQuestion(String filepath, int number, int flag) {
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
                //比如编号为5.jpg的图片，对应问题为5.jpg，但是在txt里面排第六行。
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
        if (flag == 1) {
            s = questionString.split(" ")[1];
        } else if (flag == 2) {
            s = questionString.split(" ")[2];
        }
        return s;
    }
}
