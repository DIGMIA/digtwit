package digtwit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;
import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Status;



/**
 *
 * @author jurajbednar
 */
public class Main {

    private static final String defaultPropertyFile = "digtwit.properties";

    public void writeLastId(String idFile, BigInteger lastId) throws IOException {
        BufferedWriter fw = new BufferedWriter(new FileWriter(idFile));
        fw.write(lastId.toString());
        fw.newLine();
        fw.close();
    }

    public void doWork() {
        try {
        FileInputStream f = new FileInputStream(defaultPropertyFile);
        Properties p = new Properties();
        p.load(f);
        f.close();

        final String oauthKey = p.getProperty("oauthkey", "b1JfjQRcdh5IKgl20l7g");
        final String oauthSecret = p.getProperty("oauthsecret", "pa7uxiwrvsNtzf0lPbMGLF3OtLF68JfSSFTGg6BlNJk");
        final String accessToken = p.getProperty("accesstoken");
        final String accessTokenSecret = p.getProperty("accesstokensecret");
        final String username = p.getProperty("username");
        final String stringToLookFor = p.getProperty("string", "#digmia");
        final String idFile = p.getProperty("idfile", ".digtwit.id");

        if ((username == null) || (username.length() == 0)) {
            System.err.println("We need username parameter in the configuration file. Please configure your digtwit.properties.");
            System.exit(2);
        }

        if ((accessToken == null) ||
                (accessTokenSecret == null) ||
                (accessToken.length() == 0) ||
                (accessTokenSecret.length() == 0)) {
            System.err.println("First, we need to authorize on twitter (accesstoken and accesstokensecret are not");
            System.err.println("present in the configuration file).\n");
            OAuthSignpostClient client = new OAuthSignpostClient(oauthKey, oauthSecret, "oob");
            Twitter jtwit = new Twitter(username, client);
            System.err.println("Please go to URL: ");
            System.err.println(client.authorizeUrl().toASCIIString());
            System.err.print("When you are finished, please enter the verification PIN from Twitter here: ");
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);
            client.setAuthorizationCode(in.readLine());
            String[] accessTokens = client.getAccessToken();
            if (accessTokens == null) {
                System.err.println("Authentication failed. Please try again.");
                System.exit(3);
            } else {
                System.err.println("Authentication successful. Please write the following to your configuration file:");
                System.out.println("accesstoken=" + accessTokens[0]);
                System.out.println("accesstokensecret=" + accessTokens[1]);
                System.err.println("Exiting.");
                System.exit(1);
            }
        }

        OAuthSignpostClient client = new OAuthSignpostClient(oauthKey, oauthSecret, accessToken, accessTokenSecret);
        Twitter twitter = new Twitter(username, client);
        twitter.setMaxResults(100);
        twitter.setSource("digtwit");

        BigInteger lastId = null;
        try {
            BufferedReader idReader = new BufferedReader(new FileReader(idFile));
            lastId = new BigInteger(idReader.readLine());
        } catch (Exception e) {
            System.err.println("Error reading lastId: " + e);
        }

        if (lastId != null)
         twitter.setSinceId(lastId);

        List<Number> friends = twitter.users().getFriendIDs();

        for (Status status : twitter.getHomeTimeline()) {
            if (status.getId().compareTo(lastId) > 0) {
                lastId = status.getId();
                // if this goes wrong, nothing else works and that is okay,
                // otherwise we would get repeated retweets if can't save last
                // tweet id
                writeLastId(idFile, lastId);
            }
            if (status.getUser().toString().equals(username))
                continue;
            boolean isFriend = false;
            final long statusUserId = status.getUser().getId();
            for (Number friendId : friends)
                if (statusUserId == friendId.longValue())
                    isFriend = true;
            if (!isFriend)
               continue;
        
            if ( (status.getText().contains(stringToLookFor)) && (! status.getText().startsWith("RT "))) {
                System.out.println("Retweeting: " + status.getText());
                twitter.retweet(status);
            }
        }




        } catch (IOException e) {
            System.err.println("Exception caught: " + e);
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.doWork();
    }

}
