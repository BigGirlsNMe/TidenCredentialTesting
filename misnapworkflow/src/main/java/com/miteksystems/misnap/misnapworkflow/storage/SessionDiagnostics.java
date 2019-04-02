package com.miteksystems.misnap.misnapworkflow.storage;

import com.miteksystems.misnap.events.OnFrameProcessedEvent;
import com.miteksystems.misnap.params.MiSnapApiConstants;
import com.miteksystems.misnap.params.ParamsHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by jlynch on 8/26/16.
 */
public class SessionDiagnostics {
    public static final String  FAILURE_TYPE = "FAILURE_TYPE";
    public static final String FAILURE_PERCENT = "FAILURE_PERCENT";
    private static final int MINIMUM_ERROR_THRESHOLD = 26;
    private static final int REJECT_MODULATOR = 2;

    private String docType;
    private HashMap<Integer, Integer> failures;
    private ArrayList<Integer> fieldList;
    private int lowConfidenceBrightnessFailCount = 0;
    private int angleBrightnessFailCount = 0;
    private int frameCount = 0;

    public SessionDiagnostics(String docType) {
        this.docType = docType;
        failures = new HashMap<>();
        fieldList = new ArrayList<>();
        fieldList.add(OnFrameProcessedEvent.FRAME_MIN_BRIGHTNESS_CHECK);
        fieldList.add(OnFrameProcessedEvent.FRAME_MAX_BRIGHTNESS_CHECK);
        fieldList.add(OnFrameProcessedEvent.FRAME_BUSY_BACKGROUND_CHECK);
        fieldList.add(OnFrameProcessedEvent.FRAME_LOW_CONTRAST_CHECK);
        fieldList.add(OnFrameProcessedEvent.FRAME_SHARPNESS_CHECK);
        fieldList.add(OnFrameProcessedEvent.FRAME_HORIZONTAL_MINFILL_CHECK);
        fieldList.add(OnFrameProcessedEvent.FRAME_MIN_PADDING_CHECK);
        fieldList.add(OnFrameProcessedEvent.FRAME_CONFIDENCE_CHECK);
        fieldList.add(OnFrameProcessedEvent.FRAME_ROTATION_ANGLE_CHECK);
        fieldList.add(OnFrameProcessedEvent.FRAME_MAX_SKEW_ANGLE_CHECK);
        fieldList.add(OnFrameProcessedEvent.FRAME_WRONG_DOCUMENT_CHECK);
        fieldList.add(OnFrameProcessedEvent.FRAME_GLARE_CHECK);

        init();


        EventBus.getDefault().register(this);
    }

    public void deInit(){
        // Unregister the event bus
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public ArrayList<String> rankFailures(){
        LinkedHashMap<Integer, Integer> rank = new LinkedHashMap<>();
        ArrayList<String> result = new ArrayList<>();
        int failCount = 0;

        for(Integer tag: fieldList){
            failCount += failures.get(tag);
        }

        for(Integer tag: fieldList){
            rank.put(tag, failures.get(tag));
        }
        rank = sortByComparator(rank);

        for (Map.Entry<Integer, Integer> entry : rank.entrySet()){
            if(entry != null && getPercentage(entry.getValue(), failCount) >= MINIMUM_ERROR_THRESHOLD
                    && getPercentage(entry.getValue(), frameCount) >= MINIMUM_ERROR_THRESHOLD){
                JSONObject holder = new JSONObject();
                try {
                    holder.put(FAILURE_TYPE, entry.getKey());
                    holder.put(FAILURE_PERCENT, getPercentage(entry.getValue(), failCount));

                    if (entry.getKey().equals(OnFrameProcessedEvent.FRAME_CONFIDENCE_CHECK)) {
                        if (isCheckFront()) {
                            JSONObject extraMessage = getMessageEnsureNumbersAreVisible();
                            result.add(extraMessage.toString());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                result.add(holder.toString());
            }
        }
        // If no failover reason stood out, return a default one
        if (result.size() == 0) {
            JSONObject holder = new JSONObject();
            try {
                holder.put(FAILURE_TYPE, 0);
                holder.put(FAILURE_PERCENT, 100);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            result.add(holder.toString());

            if (isCheckFront()) {
                JSONObject extraMessage = getMessageEnsureNumbersAreVisible();
                result.add(extraMessage.toString());
            }
        }

        init();
        return result;
    }

    private boolean isCheckFront() {
        return docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_CHECK_FRONT)
                || docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_AUTOMATIC_CHECK_HANDLING);
    }

    private JSONObject getMessageEnsureNumbersAreVisible() {
        JSONObject holder = new JSONObject();
        if (docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_CHECK_FRONT)
                || docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_AUTOMATIC_CHECK_HANDLING)) {
            try {
                holder.put(FAILURE_TYPE, OnFrameProcessedEvent.FRAME_WRONG_DOCUMENT_CHECK);
                holder.put(FAILURE_PERCENT, 100);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return holder;
    }

    private LinkedHashMap<Integer, Integer> sortByComparator(LinkedHashMap<Integer, Integer> enteredData)
    {

        List<Map.Entry<Integer, Integer>> sortedData = new LinkedList<Map.Entry<Integer, Integer>>(enteredData.entrySet());

        Collections.sort(sortedData, new Comparator<Map.Entry<Integer, Integer>>()
        {
            public int compare(Map.Entry<Integer, Integer> entryA,
                               Map.Entry<Integer, Integer> entryB)
            {
                return entryB.getValue().compareTo(entryA.getValue());
            }
        });

        LinkedHashMap<Integer, Integer> returnedData = new LinkedHashMap<Integer, Integer>();
        for (Map.Entry<Integer, Integer> entry : sortedData)
        {
            returnedData.put(entry.getKey(), entry.getValue());
        }

        return returnedData;
    }

    public void onEvent(OnFrameProcessedEvent event) {
        ++frameCount;
        if(event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_GLARE_CHECK)){
            incrementFailure(OnFrameProcessedEvent.FRAME_GLARE_CHECK);
        }
        if(event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_BUSY_BACKGROUND_CHECK)){
            incrementFailure(OnFrameProcessedEvent.FRAME_BUSY_BACKGROUND_CHECK);
        }
        if(event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_LOW_CONTRAST_CHECK)){
            incrementFailure(OnFrameProcessedEvent.FRAME_LOW_CONTRAST_CHECK);
        }

        if(event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_WRONG_DOCUMENT_CHECK)){
            int weight = 1; // B-03473 - handle invalid doc type in auto mode
            if(ParamsHelper.isCheckBack(docType)) {
                weight = frameCount;
            }else if(event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_BUSY_BACKGROUND_CHECK) ||
                    event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_LOW_CONTRAST_CHECK)){
                weight = 0;
            }
            incrementFailureByWeight(OnFrameProcessedEvent.FRAME_WRONG_DOCUMENT_CHECK, weight);
        }


        boolean minBrightnessFailed = event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_MIN_BRIGHTNESS_CHECK);
        boolean maxBrightnessFailed = event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_MAX_BRIGHTNESS_CHECK);
        if (minBrightnessFailed || maxBrightnessFailed) {
            int brightnessCheckThatFailed = minBrightnessFailed ? OnFrameProcessedEvent.FRAME_MIN_BRIGHTNESS_CHECK : OnFrameProcessedEvent.FRAME_MAX_BRIGHTNESS_CHECK;
            //if four corners failed, only take half the errors
            if(event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_CONFIDENCE_CHECK) && ++lowConfidenceBrightnessFailCount % REJECT_MODULATOR == 0){
                incrementFailure(brightnessCheckThatFailed);
            }else if(event.didFrameCheckPass(OnFrameProcessedEvent.FRAME_CONFIDENCE_CHECK)){
                //rotation still brings in lots of noisy background, ignore half of those brightness errors
                if(((event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_ROTATION_ANGLE_CHECK) ||
                        event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_MAX_SKEW_ANGLE_CHECK)) && ++angleBrightnessFailCount % REJECT_MODULATOR == 0)){
                    incrementFailure(brightnessCheckThatFailed);
                }else if(event.didFrameCheckPass(OnFrameProcessedEvent.FRAME_ROTATION_ANGLE_CHECK) &&
                        event.didFrameCheckPass(OnFrameProcessedEvent.FRAME_MAX_SKEW_ANGLE_CHECK)){
                    incrementFailure(brightnessCheckThatFailed);
                }
            }
        }


        //following metrics depend on accurate four corners
        if(event.didFrameCheckPass(OnFrameProcessedEvent.FRAME_CONFIDENCE_CHECK)){
            if(event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_SHARPNESS_CHECK)){
                incrementFailure(OnFrameProcessedEvent.FRAME_SHARPNESS_CHECK);
            }
            if(event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_ROTATION_ANGLE_CHECK) ||
                    event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_MAX_SKEW_ANGLE_CHECK)){
                if(event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_MAX_SKEW_ANGLE_CHECK)){
                    incrementFailure(OnFrameProcessedEvent.FRAME_MAX_SKEW_ANGLE_CHECK);
                }
                if (event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_ROTATION_ANGLE_CHECK)){
                    incrementFailure(OnFrameProcessedEvent.FRAME_ROTATION_ANGLE_CHECK);
                }
            }else{
                //high angles can cause these issues, but they are symptoms rather than causes
                if(event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_MIN_PADDING_CHECK)){
                    incrementFailure(OnFrameProcessedEvent.FRAME_MIN_PADDING_CHECK);
                }
                if(event.didFrameCheckFail(OnFrameProcessedEvent.FRAME_HORIZONTAL_MINFILL_CHECK)){
                    incrementFailure(OnFrameProcessedEvent.FRAME_HORIZONTAL_MINFILL_CHECK);
                }
            }
        }
    }

    private void init(){
        lowConfidenceBrightnessFailCount = 0;
        angleBrightnessFailCount = 0;
        failures.clear();

        for(Integer field:fieldList){
            failures.put(field, 0);
        }
    }

    private int getPercentage(int numerator, int denominator){
        if(denominator == 0){
            return 0;
        }
        return (int)(((float)numerator/denominator)*100);
    }

    private void incrementFailure(Integer tag){
        incrementFailureByWeight(tag, 1);
    }

    private void incrementFailureByWeight(Integer tag, int amount) {
        failures.put(tag, failures.get(tag) + amount);
    }
}
