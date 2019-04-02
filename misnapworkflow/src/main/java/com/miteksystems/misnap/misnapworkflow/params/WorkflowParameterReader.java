package com.miteksystems.misnap.misnapworkflow.params;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by awood on 10/13/16.
 */

public class WorkflowParameterReader {
    private Intent intent;
    private JSONObject allParams;
    private JSONObject changedParams;
    private boolean hasAnyParams;
    private String param;
    private int minValue;
    private int maxValue;
    private int defaultValue;
    private boolean isHex;

    public WorkflowParameterReader(Intent intent) {
        this.intent = intent;
        changedParams = new JSONObject();
        extractExtrasFromIntent();
    }

    public int getGlareTracking() {
        param = WorkflowApi.MISNAP_WORKFLOW_TRACK_GLARE;
        minValue = WorkflowApi.TRACK_GLARE_MIN;
        maxValue = WorkflowApi.TRACK_GLARE_MAX;
        defaultValue = WorkflowApi.TRACK_GLARE_DEFAULT;
        return getCroppedParameterValue();
    }

    /*
     * Gets the parameter's value to use in the workflow.
     * If nothing is set, it uses the default value.
     * If a value is too high, it crops it to the maximum value.
     * If a value is too low, it crops it to the minimum value.
     * If a value is invalid, it crops it to the minimum value and adds it to the list of changed parameters.
     */
    private int getCroppedParameterValue() {
        int value = defaultValue;
        if (parameterWasPassedIn()) {
            try {
                value = getParameterValue();
                value = cropToRange(value);
            } catch (JSONException e) {
                e.printStackTrace();
                value = minValue;
                addToChangedParams(value);
            }
        }
        return value;
    }

    private boolean parameterWasPassedIn() {
        if (hasAnyParams) {
            return allParams.has(param);
        }
        return false;
    }

    /*
     * Reads a parameter's value from the Intent extras.
     * If it is a non-integer value, it throws a JSONException.
     */
    private int getParameterValue() throws JSONException {
        return allParams.getInt(param);
    }

    private int cropToRange(int value) {
        if (value < minValue) {
            return minValue;
        } else if (value > maxValue) {
            return maxValue;
        } else {
            return value;
        }
    }

    private void addToChangedParams(int value) {
        try {
            changedParams.put(param, String.valueOf(value));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Side-effect: Sets hasAnyParams to false if the Intent is null, or if the Intent has no
     * workflow settings in the extras.
     */
    private void extractExtrasFromIntent() {
        hasAnyParams = false;
        if (intent != null) {
            String extras = intent.getStringExtra(WorkflowApi.WORKFLOW_SETTINGS);
            if (extras != null) {
                try {
                    allParams = new JSONObject(extras);
                    hasAnyParams = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
