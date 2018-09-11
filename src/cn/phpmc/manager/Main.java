/*
 * 版权所有(C)Niconico Craft 保留所有权利
 * 您不得在未经作者许可的情况下，擅自发布本软件的任何部分或全部内容
 * 否则将会追究二次发布者的法律责任
 */
package cn.phpmc.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.acraft.NicoHttp.Object.Http.Handler;
import org.acraft.NicoHttp.Event.Exchange;
import org.acraft.NicoHttp.Object.Http.Server;

/**
 *
 * @author jiang
 */
public class Main {

    private static int Port;
    private static String Token;
    public static Map<String, String> terminal = new HashMap<String, String>();
    public static Map<String, Process> processlist = new HashMap<String, Process>();
    public static Map<String, OutputStream> oslist = new HashMap<String, OutputStream>();
    public static Map<String, InputStream> islist = new HashMap<String, InputStream>();
    public static Exchange e;

    public static void main(String[] args) {

        Port = 21567;
        Token = "123456789";
        if (args.length > 0) {
            try {
                Port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                log("Error: Port must be a number.");
                System.exit(1);
            }
        }
        if (args.length > 1) {
            Token = args[1];
        }
        log("PHPMC 7 Minecraft Server Manager");
        log("CopyRight (C) 2012-2018 ZeroDream.NET All Right Reserved.");
        log("Server is running on port: " + Port);
        FTPServer ftp = new FTPServer();
        ftp.setPort(2121);
        ftp.start();
        File data = new File("data/");
        if(!data.exists()) {
            data.mkdirs();
        }
        try {
            Server.create(Port).setHandler(new Handler() {
                @Override
                public void onRequest(Exchange event) throws IOException {
                    event.setHeader("Content-Type", "text/html;charset=UTF-8");
                    event.setHeader("X-Powered-By", "PHPMC/7.0");
                    event.setHeader("Server", "PHPMC7");
                    event.setHeader("Access-Control-Allow-Origin", "*");
                    event.sendHeader(200, 0);
                    e = event;
                    String name;
                    String token;
                    switch ($_GET("action")) {
                        case "create":
                            name = $_GET("name");
                            token = $_GET("token");
                            String cmds = "cmd /c @echo off&chcp 65001>nul&cd data/" + name + "/&cmd";
                            if (!token.equals(md5(Token))) {
                                event.write("Token Error: " + token + "/" + md5(Token) + "/" + Token);
                                return;
                            }
                            if (name.equals("")) {
                                event.write("Container name undefined");
                                return;
                            }
                            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                                cmds = "bash";
                                log("Server OS: Linux");
                            } else {
                                log("Server OS: Windows");
                            }
                            File dir = new File("data/" + name + "/");
                            if(!dir.exists()) {
                                dir.mkdirs();
                            }
                            try {
                                Process process = Runtime.getRuntime().exec(cmds);
                                InputStream is = process.getInputStream();
                                final OutputStream os = process.getOutputStream();
                                processlist.put(name, process);
                                islist.put(name, is);
                                oslist.put(name, os);
                                Thread t1 = new Thread(new ReaderConsole(is, "UTF-8", name));
                                t1.start();
                                if(System.getProperty("os.name").toLowerCase().contains("linux")) {
                                    cmds = "cd data/" + name + "/\n";
                                    oslist.get(name).write(cmds.getBytes(Charset.forName("UTF-8")));
                                    oslist.get(name).flush();
                                }
                                event.write("Container started!");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                event.write("Container start failed, Error: " + ex.getLocalizedMessage());
                            }
                            break;
                        case "setuser":
                            String ftp_user = $_GET("user");
                            String ftp_pass = URLDecoder.decode($_GET("pass"), "UTF-8");
                            String ftp_home =URLDecoder.decode( $_GET("home"), "UTF-8");
                            token = $_GET("token");
                            if (!token.equals(md5(Token))) {
                                event.write("Token Error");
                                return;
                            }
                            if (ftp_user.equals("") || ftp_pass.equals("") || ftp_home.equals("")) {
                                event.write("Username, Password or Home directory undefined");
                                return;
                            }
                            File home = new File(ftp_home);
                            if(!home.exists()) {
                                home.mkdirs();
                            }
                            ftp.setUser(ftp_user, ftp_pass.toCharArray(), ftp_home);
                            event.write("Successful");
                            break;
                        case "getlogs":
                            name = $_GET("name");
                            token = $_GET("token");
                            if (name.equals("")) {
                                event.write("Container name undefined");
                                return;
                            }
                            if (!terminal.containsKey(name)) {
                                event.write("Container not found");
                                return;
                            }
                            if (!token.equals(md5(Token + name))) {
                                event.write("Token Error");
                                return;
                            }
                            try {
                                String logs = terminal.get(name);
                                event.write(logs);
                            } catch (Exception ex) {
                                event.write("Error: " + ex.getLocalizedMessage());
                            }
                            break;
                        case "command":
                            name = $_GET("name");
                            String cmd = $_GET("cmd");
                            token = $_GET("token");
                            if (!token.equals(md5(Token))) {
                                event.write("Token Error");
                                return;
                            }
                            if (name.equals("")) {
                                event.write("Container name undefined");
                                return;
                            }
                            if (cmd.equals("")) {
                                event.write("Command undefined");
                                return;
                            }
                            cmd = URLDecoder.decode(cmd, "UTF-8");
                            log("Container: " + name + " execute command: " + cmd);
                            try {
                                cmd = cmd + "\n";
                                oslist.get(name).write(cmd.getBytes(Charset.forName("UTF-8")));
                                oslist.get(name).flush();
                                event.write("Successful");
                            } catch (Exception ex) {
                                event.write("Error: " + ex.getLocalizedMessage());
                            }
                            break;
                        case "close":
                            name = $_GET("name");
                            token = $_GET("token");
                            if (!token.equals(md5(Token))) {
                                event.write("Token Error");
                                return;
                            }
                            if (name.equals("")) {
                                event.write("Container name undefined");
                                return;
                            }
                            try {
                                event.write("Successful");
                                event.getExchange().close();
                                terminal.remove(name);
                                processlist.get(name).destroyForcibly();
                                processlist.remove(name);
                                islist.get(name).close();
                                islist.remove(name);
                                oslist.get(name).close();
                                oslist.remove(name);
                            } catch (Exception ex) {
                                //event.write("Error: " + ex.getLocalizedMessage());
                            }
                            break;
                        case "exist":
                            name = $_GET("name");
                            token = $_GET("token");
                            if (!token.equals(md5(Token))) {
                                event.write("Token Error");
                                return;
                            }
                            if (name.equals("")) {
                                event.write("Container name undefined");
                                return;
                            }
                            try {
                                event.write(terminal.containsKey(name));
                            } catch (Exception ex) {
                                event.write("Error: " + ex.getLocalizedMessage());
                            }
                            break;
                        case "file-exist":
                            name = URLDecoder.decode($_GET("name"), "UTF-8");
                            token = $_GET("token");
                            if (!token.equals(md5(Token))) {
                                event.write("Token Error");
                                return;
                            }
                            if (name.equals("")) {
                                event.write("File name undefined");
                                return;
                            }
                            try {
                                File file = new File(name);
                                log("File: " + name + " exist: " + file.exists());
                                event.write(file.exists());
                            } catch (Exception ex) {
                                event.write("Error: " + ex.getLocalizedMessage());
                            }
                            break;
                        default:
                            event.write("<!DOCTYPE html><html><head><title>PHPMC 7.0</title></head>"
                                    + "<body><center><h1>PHPMC 7</h1><hr><p>Minecraft Server Manager</p></center></body></html>");
                            break;
                    }
                }
            });
        } catch (NullPointerException ex) {
            log("Error: Failed bind port!");
            log("Exception: " + ex.getLocalizedMessage());
            System.exit(1);
        }
    }

    public static String $_GET(String name) {
        String value;
        try {
            value = e.getArgs().getOrDefault(name, "");
        } catch (Exception ex) {
            value = "";
        }
        return value;
    }

    public static void log(String str) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + sdf.format(d) + " INFO] " + str);
    }

    public static void log(int str) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + sdf.format(d) + " INFO] " + str);
    }

    public static void log(Exception str) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + sdf.format(d) + " INFO] " + str);
    }

    public static void log(Object str) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + sdf.format(d) + " INFO] " + str);
    }

    public final static String md5(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            log(e);
            return null;
        }
    }
}
