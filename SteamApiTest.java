import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SteamApiTest {
    public static void main(String[] args) {
        try {
            Integer appid = 730;
            String urlString = "https://partner.steam-api.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=" + appid;
            
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                System.out.println("Response: " + response.toString());
            } else {
                System.out.println("GET request failed");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}