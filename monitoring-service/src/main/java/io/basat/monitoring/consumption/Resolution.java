package io.basat.monitoring.consumption;

/** Size of one point on a consumption chart, and the continuous aggregate it is read from. */
public enum Resolution {

    HOUR("readings_hourly"),
    DAY("readings_daily");

    private final String view;

    Resolution(String view) {
        this.view = view;
    }

    String view(){
        return view;
    }

}
