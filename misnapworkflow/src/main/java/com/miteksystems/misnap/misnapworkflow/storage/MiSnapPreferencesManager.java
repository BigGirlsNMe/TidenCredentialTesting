package com.miteksystems.misnap.misnapworkflow.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.miteksystems.misnap.params.MiSnapApiConstants;

/**
 * Created by asingal on 1/26/15.
 */
public class MiSnapPreferencesManager {

    public static final String PREF_FILE_NAME = "MiSnapWorkflowSettings";
    public static final String PREF_FIRST_TIME_USER_KEY = "PREF_FIRST_TIME_USER_KEY";
    public static final String PREF_FIRST_TIME_USER_MANUAL_KEY = "PREF_FIRST_TIME_USER_MANUAL_KEY";
    private static final String PREF_FIRST_TIME_VIDEO_USER_PASSPORT_KEY = "PREF_FIRST_TIME_VIDEO_USER_PASSPORT_KEY";
    private static final String PREF_FIRST_TIME_VIDEO_USER_IDCARDFRONT_KEY = "PREF_FIRST_TIME_VIDEO_USER_IDCARDFRONT_KEY";
    private static final String PREF_FIRST_TIME_VIDEO_USER_IDCARDBACK_KEY = "PREF_FIRST_TIME_VIDEO_USER_IDCARDBACK_KEY";
    private static final String PREF_FIRST_TIME_VIDEO_USER_DRIVERLICENSE_KEY = "PREF_FIRST_TIME_VIDEO_USER_DRIVERLICENSE_KEY";
    private static final String PREF_FIRST_TIME_MANUAL_USER_PASSPORT_KEY = "PREF_FIRST_TIME_MANUAL_USER_PASSPORT_KEY";
    private static final String PREF_FIRST_TIME_MANUAL_USER_IDCARDFRONT_KEY = "PREF_FIRST_TIME_MANUAL_USER_IDCARDFRONT_KEY";
    private static final String PREF_FIRST_TIME_MANUAL_USER_IDCARDBACK_KEY = "PREF_FIRST_TIME_MANUAL_USER_IDCARDBACK_KEY";
    private static final String PREF_FIRST_TIME_MANUAL_USER_DRIVERLICENSE_KEY = "PREF_FIRST_TIME_MANUAL_USER_DRIVERLICENSE_KEY";

    private static String getFirstTimeVideoUserKeyForDocType(String docType) {
        String key = PREF_FIRST_TIME_USER_KEY;
        if (docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_PASSPORT)) {
            key = PREF_FIRST_TIME_VIDEO_USER_PASSPORT_KEY;
        } else if (docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_ID_CARD_FRONT)) {
            key = PREF_FIRST_TIME_VIDEO_USER_IDCARDFRONT_KEY;
        } else if (docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_ID_CARD_BACK)) {
            key = PREF_FIRST_TIME_VIDEO_USER_IDCARDBACK_KEY;
        } else if (docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_DRIVER_LICENSE)) {
            key = PREF_FIRST_TIME_VIDEO_USER_DRIVERLICENSE_KEY;
        }
        return key;
    }

    private static String getFirstTimeManualUserKeyForDocType(String docType) {
        String key = PREF_FIRST_TIME_USER_MANUAL_KEY;
        if (docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_PASSPORT)) {
            key = PREF_FIRST_TIME_MANUAL_USER_PASSPORT_KEY;
        } else if (docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_ID_CARD_FRONT)) {
            key = PREF_FIRST_TIME_MANUAL_USER_IDCARDFRONT_KEY;
        } else if (docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_ID_CARD_BACK)) {
            key = PREF_FIRST_TIME_MANUAL_USER_IDCARDBACK_KEY;
        } else if (docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_DRIVER_LICENSE)) {
            key = PREF_FIRST_TIME_MANUAL_USER_DRIVERLICENSE_KEY;
        }
        return key;
    }

    /**
     *
     * @param lContext
     * @param docType
     * @return true if first time user, false if not
     */
    public static boolean isFirstTimeUser(Context lContext, String docType){
        if (lContext == null) {
            return false;
        }

        String key = getFirstTimeVideoUserKeyForDocType(docType);
        SharedPreferences sSharePrefs = lContext
                                        .getSharedPreferences(PREF_FILE_NAME, 0);
        if (sSharePrefs != null) {
            return sSharePrefs.getBoolean(key, true);
        }

        return true;
    }

    public static boolean isFirstTimeUserManual(Context lContext, String docType){
        if (lContext == null) {
            return false;
        }

        String key = getFirstTimeManualUserKeyForDocType(docType);
        SharedPreferences sSharePrefs = lContext
                                        .getSharedPreferences(PREF_FILE_NAME, 0);
        if (sSharePrefs != null) {
            return sSharePrefs.getBoolean(key, true);
        }

        return true;
    }

    /**
     *
     * @param lContext
     * @param isFirstTime
     * @param docType
     * @return true if successful, false if not
     */
    public static boolean setIsFirstTimeUser(Context lContext, boolean isFirstTime, String docType) {
        if (lContext == null) {
            return false;
        }

        String key = getFirstTimeVideoUserKeyForDocType(docType);
        SharedPreferences sSharePrefs = lContext
                                        .getSharedPreferences(PREF_FILE_NAME, 0);
        if (sSharePrefs != null) {
            Editor sSharedEditor = sSharePrefs.edit();
            if (sSharedEditor != null) {
                sSharedEditor.putBoolean(key, isFirstTime);
                return sSharedEditor.commit();
            }
        }

        return false;
    }

    public static boolean setIsFirstTimeUserManual(Context lContext, boolean isFirstTime, String docType) {
        if (lContext == null) {
            return false;
        }

        String key = getFirstTimeManualUserKeyForDocType(docType);
        SharedPreferences sSharePrefs = lContext
                                        .getSharedPreferences(PREF_FILE_NAME, 0);
        if (sSharePrefs != null) {
            Editor sSharedEditor = sSharePrefs.edit();
            if (sSharedEditor != null) {
                sSharedEditor.putBoolean(key, isFirstTime);
                return sSharedEditor.commit();
            }
        }

        return false;
    }

    public static boolean save(Context lContext, String lField, String lValue) {
        if (lContext == null || lField == null) {
            return false;
        }
        boolean bReturnValue=false;
        SharedPreferences sSharePrefs = lContext
                                        .getSharedPreferences(PREF_FILE_NAME, 0);
        if(sSharePrefs != null) {
            Editor sSharedEditor = sSharePrefs.edit();
            if(sSharedEditor != null) {
                sSharedEditor.putString(lField, lValue);
                bReturnValue = sSharedEditor.commit();
            }
        }
        return bReturnValue;
    }

    public static boolean save(Context lContext, String lField, boolean lValue) {
        if (lContext == null || lField == null) {
            return false;
        }
        boolean bReturnValue=false;
        SharedPreferences sSharePrefs = lContext
                .getSharedPreferences(PREF_FILE_NAME, 0);
        if(sSharePrefs != null) {
            Editor sSharedEditor = sSharePrefs.edit();
            if(sSharedEditor != null) {
                sSharedEditor.putBoolean(lField, lValue);
                bReturnValue = sSharedEditor.commit();
            }
        }
        return bReturnValue;
    }

    public static boolean save(Context lContext, String lField, int lValue) {
        if (lContext == null || lField == null) {
            return false;
        }
        boolean bReturnValue=false;
        SharedPreferences sSharePrefs = lContext
                .getSharedPreferences(PREF_FILE_NAME, 0);
        if(sSharePrefs != null) {
            Editor sSharedEditor = sSharePrefs.edit();
            if(sSharedEditor != null) {
                sSharedEditor.putInt(lField, lValue);
                bReturnValue = sSharedEditor.commit();
            }
        }
        return bReturnValue;
    }

    public static String getString(Context lContext, String lField){
        String bReturnValue=null;
        if (lContext == null) {
            return bReturnValue;
        }
        SharedPreferences sSharePrefs =
                lContext.getSharedPreferences(PREF_FILE_NAME, 0);
        if(sSharePrefs != null) {
            bReturnValue = sSharePrefs.getString(lField, bReturnValue);
        }

        return bReturnValue;
    }

    public static boolean getBoolean(Context lContext, String lField){
        boolean bReturnValue=false;
        if (lContext == null) {
            return bReturnValue;
        }
        SharedPreferences sSharePrefs =
                lContext.getSharedPreferences(PREF_FILE_NAME, 0);
        if(sSharePrefs != null) {
            bReturnValue = sSharePrefs.getBoolean(lField, bReturnValue);
        }

        return bReturnValue;
    }

    public static int getInt(Context lContext, String lField){
        int bReturnValue=-1;
        if (lContext == null) {
            return bReturnValue;
        }
        SharedPreferences sSharePrefs =
                lContext.getSharedPreferences(PREF_FILE_NAME, 0);
        if(sSharePrefs != null) {
            bReturnValue = sSharePrefs.getInt(lField, bReturnValue);
        }

        return bReturnValue;
    }
}
