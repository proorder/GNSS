package ru.neva_energy;

import java.util.Deque;

public class DataLocation {
    public String error = null;
    public int status;
    public Double alt = null;
    public Double latitude = null;
    public Double longitude = null;
    public Float accuracyXY = null;
    public Float accuracyZ = null;
    public Integer satsUsed = null;
    public Integer DGPSAge = null;
    public Float PDOP = null;
    public Deque<String> nmea;
    public Float velocity = null;
}
