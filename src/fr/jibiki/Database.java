package fr.jibiki;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.sql.PreparedStatement;


public class Database {

    protected static Connection myConnection = null;
    protected static final String DICTLIST_XMLSTRING_START = "<?xml version='1.0' encoding='UTF-8'?><d:dictionary-metadata-list "
        + "xmlns:d='http://www-clips.imag.fr/geta/services/dml'>";
    protected static final String DICTLIST_XMLSTRING_END = "</d:dictionary-metadata-list>";
    protected static final String DICTIONARY_FILES_TAG = "dictionary-metadata-files";
    protected static HashMap VolumesDbNames = null;

    public static String connect(String dburl, String dbuser, String dbpassword) {
        try {
            myConnection = DriverManager.getConnection(dburl, dbuser, dbpassword);
            if (myConnection != null) {
                System.out.println("Connected to database " + dburl + " with user "+ dbuser);
                VolumesDbNames = getVolumesTable();
                return("Connected to database ");
            }
            else {
                return("Not connected to database");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } 
        return("Not connected to database");
    }

    public static String getUsers() {
        String res = "";
        try {
             if (myConnection != null) {
                // SQL query to retrieve data from the 'book' table
                String selectQuery = "SELECT * FROM users";
                Statement statement = myConnection.createStatement();

                // execute the query and get the result set
                ResultSet resultSet = statement.executeQuery(selectQuery);
                System.out.println("The Available Data\n");

                // iterate through the result set and print the data
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String login = resultSet.getString("login");

                    // print the retrieved data
                    res = res.concat("<br>" + "name: " + name + ", login: " + login);
                    System.out.println("name: " + name + ", login: " + login);
                }
            } else {
                System.out.println("Not Connected...");
            }
        }
        catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return res;
    }

       
    public static String getDictionaries() {
        String result = DICTLIST_XMLSTRING_START;
        try {
             if (myConnection != null) {
                // SQL query to retrieve data from the 'book' table
                String selectQuery = "SELECT name, xmlcode FROM dictionaries";
                PreparedStatement statement = myConnection.prepareStatement(selectQuery);
                //statement.setString(1,dict);

                // execute the query and get the result set
                ResultSet resultSet = statement.executeQuery();

                // iterate through the result set and print the data
                while (resultSet.next()) {
                    result += "<" + DICTIONARY_FILES_TAG + ">";
                    result += trimXmlDeclaration(resultSet.getString("xmlcode"));
                    result += "</" + DICTIONARY_FILES_TAG + ">";
                    String dict = resultSet.getString("name");
                    System.out.println("Dict found: " + dict);
                }
            } else {
                System.out.println("Not Connected...");
            }
        }
        catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        result += DICTLIST_XMLSTRING_END;
        return result;
    }

 
    public static String getDictionary(String dict) {
        String result = "";
        try {
             if (myConnection != null) {
                // SQL query to retrieve data from the 'book' table
                String selectQuery = "SELECT xmlcode FROM dictionaries WHERE name= ?";
                PreparedStatement statement = myConnection.prepareStatement(selectQuery);
                statement.setString(1,dict);

                // execute the query and get the result set
                ResultSet resultSet = statement.executeQuery();

                // iterate through the result set and print the data
                if (resultSet.next()) {
                    result = resultSet.getString("xmlcode");
                    System.out.println("Dict found: " + dict);
                }
            } else {
                System.out.println("Not Connected...");
            }
        }
        catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return result;
    }

    public static HashMap getVolumesTable() {
        HashMap result = new HashMap();
        try {
             if (myConnection != null) {
                // SQL query to retrieve data from the 'book' table
                String selectQuery = "SELECT name, dictname, sourcelanguage, dbname FROM volumes";
                PreparedStatement statement = myConnection.prepareStatement(selectQuery);

                // execute the query and get the result set
                ResultSet resultSet = statement.executeQuery();

                // iterate through the result set and print the data
                while (resultSet.next()) {
                    String dictname = resultSet.getString("dictname");
                    String sourcelanguage = resultSet.getString("sourcelanguage");
                    String dbname = resultSet.getString("dbname");
                    result.put(dictname + "|" + sourcelanguage,dbname);
                    System.out.println("Volume found: " + dictname + "|" + sourcelanguage);
                }
            } else {
                System.out.println("Not Connected...");
            }
        }
        catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return result;
    }

    public static String getVolumeTableName(String dict, String lang) {
        return (String) VolumesDbNames.get(dict+ "|" + lang);
    }

    public static String getEntryId(String dict, String srclang, String entryId) {
        String result = "";
        try {
             if (myConnection != null) {
                // SQL query to retrieve data from the 'book' table
                String volumedb = getVolumeTableName(dict, srclang);
                if (volumedb != null) {
                    String selectQuery = "select xmlcode from "+volumedb+" where objectid in (select entryid from idx"+volumedb+" where key='cdm-contribution-id' and value= ?);";
                    PreparedStatement statement = myConnection.prepareStatement(selectQuery);
                    statement.setString(1,entryId);

                    // execute the query and get the result set
                    ResultSet resultSet = statement.executeQuery();

                    // iterate through the result set and print the data
                    if (resultSet.next()) {
                        result = resultSet.getString("xmlcode");
                        System.out.println("Volume found: " + dict + " src: " + srclang);
                    }
                    else {
                        System.out.println("No result...");
                    }
                }
                else {
                        System.out.println("No volume...");
                }
               } else {
                System.out.println("Not Connected...");
            }
        }
        catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return result;
    }

   public static String getVolume(String dict, String srclang) {
        String result = "";
        try {
             if (myConnection != null) {
                // SQL query to retrieve data from the 'book' table
                String selectQuery = "SELECT xmlcode FROM volumes WHERE dictname= ? and sourcelanguage= ?";
                PreparedStatement statement = myConnection.prepareStatement(selectQuery);
                statement.setString(1,dict);
                statement.setString(2,srclang);

                // execute the query and get the result set
                ResultSet resultSet = statement.executeQuery();

                // iterate through the result set and print the data
                if (resultSet.next()) {
                    result = resultSet.getString("xmlcode");
                    System.out.println("Volume found: " + dict + " src: " + srclang);
                }
            } else {
                System.out.println("Not Connected...");
            }
        }
        catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return result;
    }


    public static String trimXmlDeclaration(String XmlString) {
        return XmlString.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
    }


}
