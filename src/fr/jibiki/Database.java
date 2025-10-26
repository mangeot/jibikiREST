package fr.jibiki;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.sql.PreparedStatement;

public class Database {

    protected static Connection myConnection = null;
    protected static final String DICTLIST_XMLSTRING_START = "<?xml version='1.0' encoding='UTF-8'?><d:dictionary-metadata-list "
            + "xmlns:d='http://www-clips.imag.fr/geta/services/dml'>";
    protected static final String DICTLIST_XMLSTRING_END = "</d:dictionary-metadata-list>";
    protected static final String DICTIONARY_FILES_TAG = "dictionary-metadata-files";
    protected static final String ENTRIES_HEAD_XMLSTRING = "<?xml version='1.0' encoding='UTF-8'?><d:entry-list xmlns:d='http://www-clips.imag.fr/geta/services/dml'>";
    protected static final String ENTRIES_TAIL_XMLSTRING = "\n</d:entry-list>";
    protected static HashMap VolumesDbNames = null;
    protected static HashMap selectEntryIdHashMap = new HashMap();
    protected static HashMap selectKeyHashMap = new HashMap();
    protected static HashMap selectHandleHashMap = new HashMap();
    protected static HashMap selectEntriesHashMap = new HashMap();
    protected static HashMap selectIndexesHashMap = new HashMap();
    protected static HashMap selectNextEntriesHashMap = new HashMap();
    protected static HashMap selectPreviousEntriesHashMap = new HashMap();

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

    protected static boolean initializeSelectTables() {
        if (myConnection != null) {
            for (Iterator keys = VolumesDbNames.keySet().iterator(); keys.hasNext();) {
                String volumekey = (String) keys.next();
                String[] dictlang = volumekey.split("\\|");
                String srclang = dictlang[1];
                String volumedbname = (String) VolumesDbNames.get(volumekey);
                try {
                    String selectQuery = "select xmlcode from " + volumedbname
                            + " where objectid in (select entryid from idx" + volumedbname
                            + " where key='cdm-contribution-id' and value= ? );";
                    PreparedStatement statement = myConnection.prepareStatement(selectQuery);
                    selectEntryIdHashMap.put(volumekey, statement);
                    selectQuery = "select xmlcode from " + volumedbname + " where objectid = ?;";
                    statement = myConnection.prepareStatement(selectQuery);
                    selectHandleHashMap.put(volumekey, statement);
                    selectQuery = "select lang, value from idx" + volumedbname + " where key= ? "
                            + " and entryid= ?;";
                    statement = myConnection.prepareStatement(selectQuery);
                    selectKeyHashMap.put(volumekey, statement);
                    selectQuery = "select xmlcode from " + volumedbname
                            + " where objectid in (select entryid from idx" + volumedbname + " where key= ? "
                            + " and value = any (?) limit NULLIF(?, -1) offset ?) order by multilingual_sort('"
                            + srclang + "',headword);";
                    statement = myConnection.prepareStatement(selectQuery);
                    selectEntriesHashMap.put(volumekey, statement);
                    selectQuery = "select value, entryid from idx" + volumedbname + " where key= ? "
                            + " and value = any (?) order by msort limit NULLIF(?, -1) offset ?;";
                    statement = myConnection.prepareStatement(selectQuery);
                    selectIndexesHashMap.put(volumekey, statement);
                    selectQuery = "select value, entryid from idx" + volumedbname
                            + " where key='cdm-headword' and msort < multilingual_sort('" + srclang
                            + "',?) order by msort desc limit 1;";
                    statement = myConnection.prepareStatement(selectQuery);
                    selectPreviousEntriesHashMap.put(volumekey, statement);
                    selectQuery = "select value, entryid from idx" + volumedbname
                            + " where key='cdm-headword' and msort > multilingual_sort('" + srclang
                            + "',?) order by msort limit 1;";
                    statement = myConnection.prepareStatement(selectQuery);
                    selectNextEntriesHashMap.put(volumekey, statement);

                } catch (Exception e) {
                    // handle any exceptions that occur
                    System.out.println("Exception is " + e.getMessage());
                }
            }
        } else {
            System.out.println("Not Connected...");
        }
        return true;
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

    public static String getEntryByHandle(String dict, String srclang, int handle) {
        String result = "";
        try {
            if (myConnection != null) {
                PreparedStatement statement = (PreparedStatement) selectHandleHashMap.get(dict + "|" + srclang);
                if (statement != null) {
                    statement.setInt(1, handle);

                    // execute the query and get the result set
                    ResultSet resultSet = statement.executeQuery();

                    // iterate through the result set and print the data
                    if (resultSet.next()) {
                        result = resultSet.getString("xmlcode");
                        System.out.println("Entry found: " + dict + " src: " + srclang + " handle: " + handle);
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

   public static HashMap getKeyByEntryId(String dict, String srclang, int entryid, String key) {
        HashMap result = new HashMap();
        try {
            if (myConnection != null) {
                PreparedStatement statement = (PreparedStatement) selectKeyHashMap.get(dict + "|" + srclang);
                if (statement != null) {
                    statement.setString(1, key);
                    statement.setInt(2, entryid);
                    System.out.println("debug:");
                    System.out.println(statement);
                        
                    // execute the query and get the result set
                    ResultSet resultSet = statement.executeQuery();

                    // iterate through the result set and print the data
                    while (resultSet.next()) {
                        String lang = resultSet.getString("lang");
                        String value = resultSet.getString("value");
                        Vector langVector = (Vector) result.get(lang);
                        if (langVector ==null) {
                            langVector = new Vector();
                            result.put(lang, langVector);
                        }
                        langVector.add(value);
                        System.out.println("Entry found: " + dict + " src: " + srclang + " entryid: " + entryid + " key:" + key + " lang:" + lang + " value:" + value);
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
        String result = ENTRIES_HEAD_XMLSTRING;
        String criteriaString = "<d:criteria d:name='" + mode + "' d:strategy='"
                + strategy + "' value='" + RestHttpServer.encodeXMLEntities(word) + "'>";

        try {
            if (myConnection != null) {
                PreparedStatement statement = null;
                if (mode.equals("handle")) {
                    return getEntryByHandle(dict, srclang, Integer.parseInt(word));
                } else if (mode.equals("previous") || mode.equals("next")) {
                    if (mode.equals("previous")) {
                        statement = (PreparedStatement) selectPreviousEntriesHashMap.get(dict + "|" + srclang);
                    } else {
                        statement = (PreparedStatement) selectNextEntriesHashMap.get(dict + "|" + srclang);
                    }
                    statement.setString(1, word);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        int entryid = resultSet.getInt("entryid");
                        System.out.println("entryid found: " + dict + " src: " + srclang + " entryid:" + entryid);
                        return getEntryByHandle(dict, srclang, entryid);
                    } else {
                        System.out.println("No entry...");
                    }
                } else {
                    if (key != null && key.equals("entries")) {
                        statement = (PreparedStatement) selectEntriesHashMap.get(dict + "|" + srclang);
                    } else {
                        statement = (PreparedStatement) selectIndexesHashMap.get(dict + "|" + srclang);
                    }
                    if (statement != null) {
                        String entryString = "\n<d:entry d:lang='" + srclang + "' d:dictionary='"
                                + dict + "'>";
                        String[] Words = word.split("\\|");

                        java.sql.Array sqlArray = myConnection.createArrayOf("VARCHAR", Words);
                        statement.setString(1, mode);
                        statement.setArray(2, sqlArray);
                        if (limit == null || limit.equals("")) {
                            limit = "-1";
                        }
                        statement.setInt(3, Integer.parseInt(limit));
                        int offsetInt = 0;
                        if (offset != null && !offset.equals("")) {
                            offsetInt = Integer.parseInt(offset);
                        }
                        statement.setInt(4, offsetInt);
                        System.out.println("debug:");
                        System.out.println(statement);
                        // execute the query and get the result set
                        ResultSet resultSet = statement.executeQuery();

                        // iterate through the result set and print the data
                        while (resultSet.next()) {
                            result += entryString;
                            if (key != null && key.equals("entries")) {
                                result += resultSet.getString("xmlcode") + "</d:entry>";
                            } else {
                                result += criteriaString;
                                result += resultSet.getString("value") + "</d:criteria>";
                                int entryid = resultSet.getInt("entryid");
                                if (key != null) {
                                    HashMap keyValues = getKeyByEntryId(dict, srclang, entryid, key);
                                    for (Iterator keys = keyValues.keySet().iterator(); keys.hasNext();) {
                                        String lang = (String) keys.next();
                                        Vector values = (Vector) keyValues.get(lang);
                                        for (Integer i = 0; i < values.size(); i++) {
                                            result += "<d:key value='"+key+"' d:lang='"+lang+"'>"+values.get(i)+"</d:key>";
                                        }
                                    }
                                }
                                result += "<d:handle>" + entryid + "</d:handle></d:entry>";
                            }
                            System.out.println("Entry found: " + dict + " src: " + srclang);
                        }
                    } else {
                        System.out.println("No entry...");
                    }
                }
            } else {
                System.out.println("Not Connected...");
            }
        } catch (Exception e) {
            // handle any exceptions that occur
            System.out.println("Exception is " + e.getMessage());
        }
        return result + ENTRIES_TAIL_XMLSTRING;
    }

    public static String trimXmlDeclaration(String XmlString) {
        return XmlString.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
    }

}
