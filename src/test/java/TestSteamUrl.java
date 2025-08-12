public class TestSteamUrl {
    public static void main(String[] args) {
        // 测试Steam URL解析
        String url1 = "https://store.steampowered.com/app/361420/ASTRONEER/";
        
        System.out.println("测试URL: " + url1);
        System.out.println("解析结果: " + extractAppId(url1));
    }
    
    public static String extractAppId(String steamUrl) {
        if (steamUrl == null || steamUrl.trim().isEmpty()) {
            return null;
        }
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "https?://(?:store\\.steampowered|steamcommunity)\\.com/app/(\\d+)/?.*"
        );
        
        java.util.regex.Matcher matcher = pattern.matcher(steamUrl.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        
        return null;
    }
}