package fr.jibiki;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.sql.PreparedStatement;

public class Database {

    protected static Connection myConnection = null;
    protected static final String DICTLIST_XMLSTRING_START = "<?xml version='1.0' encoding='UTF-8'?><d:dictionary-metadata-list "
            + "xmlns:d='http://www-clips.imag.fr/geta/services/dml'>";
    protected static final String DICTLIST_XMLSTRING_END = "</d:dictionary-metadata-list>";
    protected static final String DICTIONARY_FILES_TAG = "dictionary-metadata-files";
    protected static HashMap VolumesDbNames = null;
    protected static HashMap selectEntryIdHashMap = new HashMap();
    protected static HashMap selectEntriesHashMap = new HashMap();

    protected static PreparedStatement selectUsersStatement = null;
    protected static PreparedStatement selectDictionariesStatement = null;
    protected static PreparedStatement selectDictionaryStatement = null;
    protected static PreparedStatement selectVolumesStatement = null;
    protected static PreparedStatement selectVolumeStatement = null;

    public static String connect(String dburl, String dbuser, String dbpassword) {
        try {
            myConnection = DriverManager.getConnection(dburl, dbuser, dbpassword);
            if (myConnection != null) {
                System.out.println("Connected to database " + dburl + " with user " + dbuser);

                selectUsersStatement = myConnection.prepareStatement("SELECT * FROM users");
                selectDictionariesStatement = myConnection
                        .prepareStatement("SELECT name, xmlcode FROM dictionaries");
                selectDictionaryStatement = myConnection
                        .prepareStatement("SELECT xmlcode FROM dictionaries WHERE name= ?");
                selectVolumesStatement = myConnection
                        .prepareStatement("SELECT name, dictname, sourcelanguage, dbname FROM volumes");
                selectVolumeStatement = myConnection
                        .prepareStatement("SELECT xmlcode FROM volumes WHERE dictname= ? and sourcelanguage= ?");

                VolumesDbNames = getVolumesTable();
                initializeSelectTables();
                return ("Connected to database ");
            } else {
                return ("Not connected to database");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ("Not connected to database");
    }

    public static String getUsers() {
        String res = "";
        try {
            if (myConnection != null) {

                // execute the query and get the result set
                ResultSet resultSet = selectUsersStatement.executeQuery();
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
        } catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return res;
    }

    public static String getDictionaries() {
        String result = DICTLIST_XMLSTRING_START;
        try {
            if (myConnection != null) {
                // execute the query and get the result set
                ResultSet resultSet = selectDictionariesStatement.executeQuery();

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
        } catch (Exception e) {
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
                selectDictionaryStatement.setString(1, dict);

                // execute the query and get the result set
                ResultSet resultSet = selectDictionaryStatement.executeQuery();

                // iterate through the result set and print the data
                if (resultSet.next()) {
                    result = resultSet.getString("xmlcode");
                    System.out.println("Dict found: " + dict);
                }
            } else {
                System.out.println("Not Connected...");
            }
        } catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return result;
    }

    protected static HashMap getVolumesTable() {
        HashMap result = new HashMap();
        try {
            if (myConnection != null) {

                // execute the query and get the result set
                ResultSet resultSet = selectVolumesStatement.executeQuery();

                // iterate through the result set and print the data
                while (resultSet.next()) {
                    String dictname = resultSet.getString("dictname");
                    String sourcelanguage = resultSet.getString("sourcelanguage");
                    String dbname = resultSet.getString("dbname");
                    result.put(dictname + "|" + sourcelanguage, dbname);
                    System.out.println("Volume found: " + dictname + "|" + sourcelanguage);
                }
            } else {
                System.out.println("Not Connected...");
            }
        } catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return result;
    }

    protected static String getVolumeTableName(String dict, String lang) {
        return (String) VolumesDbNames.get(dict + "|" + lang);
    }

    protected static boolean initializeSelectTables() {
        for (Iterator keys = VolumesDbNames.keySet().iterator(); keys.hasNext();) {
            String volumekey = (String) keys.next();
            String volumedbname = (String) VolumesDbNames.get(volumekey);
            try {
                if (myConnection != null) {
                    String selectQuery = "select xmlcode from " + volumedbname
                            + " where objectid in (select entryid from idx" + volumedbname
                            + " where key='cdm-contribution-id' and value= ? );";
                    PreparedStatement statement = myConnection.prepareStatement(selectQuery);
                    selectEntryIdHashMap.put(volumekey, statement);
                    selectQuery = "select xmlcode from " + volumedbname
                            + " where objectid in (select entryid from idx" + volumedbname + " where key= ? "
                            + " and value= ? order by msort limit ? offset ?));";
                    statement = myConnection.prepareStatement(selectQuery);
 
                    selectEntriesHashMap.put(volumekey, statement);

                } else {
                    System.out.println("Not Connected...");
                }
            } catch (Exception e) {
                // handle any exceptions that occur
                System.out.println("Exception is " + e.getMessage());
            }
        }
        return true;
    }

    public static String getEntryId(String dict, String srclang, String entryId) {
        String result = "";
        try {
            if (myConnection != null) {
                PreparedStatement statement = (PreparedStatement) selectEntryIdHashMap.get(dict + "|" + srclang);
                if (statement != null) {
                    statement.setString(1, entryId);

                    // execute the query and get the result set
                    ResultSet resultSet = statement.executeQuery();

                    // iterate through the result set and print the data
                    if (resultSet.next()) {
                        result = resultSet.getString("xmlcode");
                        System.out.println("Volume found: " + dict + " src: " + srclang);
                    } else {
                        System.out.println("No result...");
                    }
                } else {
                    System.out.println("No volume...");
                }
            } else {
                System.out.println("Not Connected...");
            }
        } catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return result;
    }

    public static String getVolume(String dict, String srclang) {
        String result = "";
        try {
            if (myConnection != null) {
                selectVolumeStatement.setString(1, dict);
                selectVolumeStatement.setString(2, srclang);

                // execute the query and get the result set
                ResultSet resultSet = selectVolumeStatement.executeQuery();

                // iterate through the result set and print the data
                if (resultSet.next()) {
                    result = resultSet.getString("xmlcode");
                    System.out.println("Volume found: " + dict + " src: " + srclang);
                }
            } else {
                System.out.println("Not Connected...");
            }
        } catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return result;
    }

    public static String getEntries(String dict, String srclang, String mode, String word, String key, String strategy,
        String limit, String offset, String orderby) {
        String result = "";
        try {
            if (myConnection != null) {
                // SQL query to retrieve data from the 'book' table
                 PreparedStatement statement = (PreparedStatement) selectEntryIdHashMap.get(dict + "|" + srclang);
                if (statement != null) {
                    statement.setString(1, mode);
                    statement.setString(2, word);
                    if (limit == null || limit.equals("")) {
                        statement.setString(3, "ALL");
                    }
                    else {
                        statement.setInt(3, Integer.parseInt(limit));
                    }
                    int offsetInt = 0;
                    if (offset != null && !offset.equals("")) {
                        offsetInt = Integer.parseInt(offset);
                    }
                    statement.setInt(4, offsetInt);

                    // execute the query and get the result set
                    ResultSet resultSet = statement.executeQuery();

                    // iterate through the result set and print the data
                    if (resultSet.next()) {
                        result = resultSet.getString("xmlcode");
                        System.out.println("Entry found: " + dict + " src: " + srclang);
                    } else {
                        System.out.println("No result...");
                    }
                } else {
                    System.out.println("No entry...");
                }
            } else {
                System.out.println("Not Connected...");
            }
        } catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return result;
    }

    public static String trimXmlDeclaration(String XmlString) {
        return XmlString.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
    }

}
