/*
 * 版权所有(C)Niconico Craft 保留所有权利
 * 您不得在未经作者许可的情况下，擅自发布本软件的任何部分或全部内容
 * 否则将会追究二次发布者的法律责任
 */
package cn.phpmc.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author jiang
 */
public class FileReader {

    public static String read(String fileName) {
        String encoding = "GB2312";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            // Main.log(e.getMessage());
        } catch (IOException e) {
            Main.log(e.getMessage());
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            Main.log("The OS does not support " + encoding);
            Main.log(e.getMessage());
            return null;
        }
    }
}
