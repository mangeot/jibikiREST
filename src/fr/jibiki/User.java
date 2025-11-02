package fr.jibiki;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {

    protected final static String PASSWORD_ENCODING = "US-ASCII";
    protected final static String PASSWORD_DIGEST = "SHA";
    protected final static int PASSWORD_DIGEST_LENGTH = 30;


    /*
 objectid         | numeric(19,0)          |           | not null | 
 objectversion    | integer                |           | not null | 
 name             | character varying(255) |           | not null | 
 login            | character varying(255) |           | not null | 
 password         | character varying(255) |           | not null | 
 email            | character varying(255) |           | not null | 
 lang             | character varying(3)   |           |          | 
 groups           | character varying(255) |           |          | 
 credits          | integer                |           | not null | 
 xmlcode          | text                   |           |          | 
 creationdate     | date                   |           |          | 
 modificationdate | date                   |           |          | 
     */
    public String name = "";
    public String login = "";
    public String password = "";
    public String email = "";
    public String lang = "";
    public String groups = "";
    public String xmlcode = "";

    public User(String theLogin) {
        this.login=theLogin;
    }

    public User(String theName, String theLogin, String thePassword, String theEmail, String theLang, String theGroups, String theXmlcode) {
        this.name=theName;
        this.login=theLogin;
        this.password=thePassword;
        this.email=theEmail;
        this.lang=theLang;
        this.groups=theGroups;
        this.xmlcode=theXmlcode;
    }

    /**	Uses the given digest algorithm to compute a 20 byte array of the
		*	user name and password.
		*/
	protected static String makeDigest(String user, String password) {
            String givenPasswordString = "";
			byte[] digestbytes = new byte[PASSWORD_DIGEST_LENGTH];
			if (user == null || user.equals("")) {
				System.out.println("Error in makeDigestString: user login empty");
			}
			try {
				MessageDigest messagedigest = MessageDigest.getInstance(PASSWORD_DIGEST);
				messagedigest.update(user.getBytes());
				messagedigest.update(password.getBytes());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				messagedigest.update(baos.toByteArray());
				digestbytes = messagedigest.digest();
                givenPasswordString = new String(digestbytes, PASSWORD_ENCODING);
                givenPasswordString = givenPasswordString.replaceAll("\\x00", "");
            } catch(NoSuchAlgorithmException nsae) {
                System.out.println("Error in makeDigest: NoSuchAlgorithmException " + PASSWORD_DIGEST);
                nsae.printStackTrace();
            }
            catch(UnsupportedEncodingException uee) {
                System.out.println("Error in makeDigest: UnsupportedEncoding " + PASSWORD_ENCODING);
                uee.printStackTrace();
            }
			return givenPasswordString;
		}
	
	public boolean HasCorrectPassword(String password) {
            boolean answer = false;
            String login = this.login;
            if (null != login && !login.equals("")) {
                String givenPasswordString = makeDigest(login,password);
                answer = this.password.equals(givenPasswordString);
            }
            return answer;
        } 
		
    public static User getUser(String login) {
        return (User) Database.UsersTable.get(login);
    } 
    
}
