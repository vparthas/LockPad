package com.varunp.padlock.utils;

/**
 * Created by Varun on 1/14/2016.
 */
public class Globals
{
    //Filenames
    public static final String FILENAME_LOGIN_INFO = "login_info.json";
    public static final String FILENAME_RECOVERY_INFO = "recovery_info.json";
    public static final String FILENAME_FOLDER_LIST = "folder_reg.json";

    //folder names
    public static final String FOLDER_DATA = "data";
    public static final String FOLDER_TUTORIAL = "Tutorial";
    public static final String FOLDER_PARENT = "..";
    public static final String FILE_DELIM = "|";

    //file suffixes
    public static final String FILENAME_IMAGE = ".plimg";
    public static final String FILENAME_TEXT = ".pltxt";

    //bundle keys
    public static final String BUNDLE_KEY_PATH = "folder_path";

    //JSON Markers
    public static final String JSON_LOGIN_PASSWORD_OBJECT = "password";
    public static final String JSON_LOGIN_SALT_OBJECT = "salt";
    public static final String JSON_LOGIN_KEY_OBJECT = "key";

    public static final String JSON_RECOVERY_PASSWORD_OBJECT = "password_encrypted";
    public static final String JSON_RECOVERY_ANSWER_HASH_OBJECT = "recovery_answer_hash";
    public static final String JSON_RECOVERY_QUESTION_OBJECT = "recovery_question";
    public static final String JSON_RECOVERY_ANSWER_SALT_OBJECT = "recovery_answer_salt";

    //Nav Drawer Headers
    public static final String NAV_HEADER_FOLDERS = "Folders";
    public static final String NAV_HEADER_FILES = "Files";
    public static final String NAV_HEADER_NOTES = "Notes";
    public static final String NAV_HEADER_IMAGES = "Images";

    public static String TUTORIAL_TEXT_PLACEHOLDR = "TODO...";
}
