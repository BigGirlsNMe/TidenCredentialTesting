package com.miteksystems.misnap.misnapworkflow.params;

/**
 * Created by awood on 10/13/16.
 */

public class WorkflowApi {

    public static final String WORKFLOW_SETTINGS = "MiSnapWorkflowSettings";

    /**
     * If enabled, a red outline will be drawn around any glare that is detected on the document.
     * The outline's lifecycle is tied to the glare hint bubble.
     * <p/>
     * <b>Values:</b><br>
     * Range: {@link #TRACK_GLARE_MIN} - {@link #TRACK_GLARE_MAX}
     * Default: {@link #TRACK_GLARE_DEFAULT}
     */
    public static final String MISNAP_WORKFLOW_TRACK_GLARE = "MiSnapTrackGlare";
    public static final int TRACK_GLARE_MIN = 0;
    public static final int TRACK_GLARE_MAX = 1;
    public static final int TRACK_GLARE_DEFAULT = 1;
}
