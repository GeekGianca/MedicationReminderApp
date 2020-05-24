package io.gksoftwaresolutions.medicationreminder.model;

public class BDevice {
    private String uuid;
    private String deviceName;
    private int status;

    public BDevice(String uuid, String deviceName, int status) {
        this.uuid = uuid;
        this.deviceName = deviceName;
        this.status = status;
    }

    public BDevice() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
