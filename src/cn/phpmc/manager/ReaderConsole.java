package cn.phpmc.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

class ReaderConsole
        implements Runnable {

    private InputStream is;
    private String ConsoleEncode;
    private String Terminal;
    
    ReaderConsole(InputStream is, String encode, String Terminal) {
        this.is = is;
        this.ConsoleEncode = encode;
        this.Terminal = Terminal;
    }

    @Override
    public void run() {
        Main.terminal.put(Terminal, "");
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(this.is, ConsoleEncode);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader br = new BufferedReader(isr);
        try {
            String c;
            while ((c = br.readLine()) != null) {
                String logs = Main.terminal.get(Terminal);
                logs += c + "\n";
                Main.terminal.replace(Terminal, logs);
                System.out.println(c);
            }
        } catch (IOException e) {
            Main.log("Error: " + e.getMessage());
        }
    }
}
