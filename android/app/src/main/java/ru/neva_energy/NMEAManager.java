package ru.neva_energy;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NMEAManager {
    private static Pattern GENERAL_PATTERN = Pattern.compile("^\\$([^,]*).*");;
    private static Pattern GGA = Pattern.compile("GGA");
    private static Pattern GST = Pattern.compile("GST");
    private static Pattern GLL = Pattern.compile("GLL");
    private static Pattern GSA = Pattern.compile("GSA");
    private static Pattern GSV = Pattern.compile("GSV");
    private static Pattern RMC = Pattern.compile("RMC");

    private LocationManager locationManager;
    private String provider;
    private int quality = 0;
    private Float altError = null;
    private Float DRMS = null;
    private Double tempAltitude = null;
    private Double altitude = null;
    private Double latitude = null;
    private Double longitude = null;
    private String error= null;
    private Integer satsUsed = null;
    private Integer DGPSAge = null;
    private Float PDOP = null;
    private Float velocity = null;
    private Integer total_gsv_messages = null;
    private ArrayList<Satellite> currentSatellites = null;
    private int altRange;
    private ArrayList<Satellite> tempSatellites = new ArrayList<>();

    NMEAManager(LocationManager locationManager, String provider) {
        this.locationManager = locationManager;
        this.provider = provider;
    }

    public void parse(String message, int altRange) {
        this.altRange = altRange;
        Matcher matcher = GENERAL_PATTERN.matcher(message);
        if (GGA.matcher(message).find()) {
            parseGPGGA(message);
        } else if (GLL.matcher(message).find()) {
            parseGPGLL(message);
        } else if (RMC.matcher(message).find()) {
            parseGPRMC(message);
        } else if (GST.matcher(message).find()) {
            parseGST(message);
        } else if (GSA.matcher(message).find()) {
            parseGPGSA(message);
        } else if (GSV.matcher(message).find()) {
            parseGSV(message);
        }
    }

    private static Double ParseLatitude(String token) {
        try {
            double temp = Double.parseDouble(token);

            double degree = (int)((int)temp / 100.0);
            double minutes = ((int)temp - degree * 100.0);
            double seconds = (temp - (int)temp) * 60.0;

            return degree + minutes / 60.0 + seconds / 3600.0;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double ParseLongitude(String token) {
        try {
            double temp = Double.parseDouble(token);

            double degree = (int)((int)temp / 100.0);
            double minutes = ((int)temp - degree * 100.0);
            double seconds = (temp - (int)temp) * 60.0;

            return degree + minutes / 60.0 + seconds / 3600.0;
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private void parseGPGGA(String sentence) {
        String[] result = sentence.split("\\,");
        Double latitude = null;
        String latDirection = null;
        Double longitude = null;
        String longDirection = null;
        int iter = 0;
        for (String res : result) {
            switch (iter) {
                case 2:
                    latitude = ParseLatitude(result[2]);
                    break;
                case 3:
                    latDirection = result[3];
                    break;
                case 4:
                    longitude = ParseLongitude(result[4]);
                    break;
                case 5:
                    longDirection = result[5];
                    break;
                case 6:
                    quality = Integer.valueOf(result[6]);
                    break;
                case 7:
                    try {
                        satsUsed = Integer.parseInt(result[7]);
                    } catch (NumberFormatException e) {
                        satsUsed = null;
                    }
                    break;
                case 9:
                    try {
                        double currentAltitude = Double.parseDouble(result[9]);
                        if (tempAltitude != null) {
                            if (Math.abs(altitude - currentAltitude) > altRange) {
                                if (Math.abs(tempAltitude - currentAltitude) > altRange) {
                                    tempAltitude = currentAltitude;
                                    break;
                                }
                            }
                        }
                        altitude = currentAltitude;
                        tempAltitude = altitude;
                    } catch (NumberFormatException e) {
                        altitude = null;
                    }
                    break;
                case 13:
                    try {
                        DGPSAge = Integer.parseInt(result[13]);
                    } catch (NumberFormatException e) {
                        DGPSAge = null;
                    }
                    break;
            }
            iter++;
        }
        if (latitude != null && longitude != null) {
            latitudeDirect(latitude, latDirection);
            longitudeDirect(longitude, longDirection);
            setProviderLocation(latitude, longitude);
        }
    }

    private void parseGPGLL(String sentence) {
        String[] result = sentence.split("\\,");
        Double latitude = null;
        String latDirection = null;
        Double longitude = null;
        String longDirection = null;
        int iter = 0;
        for (String res : result) {
            switch (iter) {
                case 1:
                    latitude = ParseLatitude(result[1]);
                    break;
                case 2:
                    latDirection = result[2];
                    break;
                case 3:
                    longitude = ParseLongitude(result[3]);
                    break;
                case 4:
                    longDirection = result[4];
                    break;
            }
            iter++;
        }
        if (latitude != null && longitude != null) {
            latitudeDirect(latitude, latDirection);
            longitudeDirect(longitude, longDirection);
            setProviderLocation(latitude, longitude);
        }
    }

    private void parseGST(String sentence) {
        String[] result = sentence.split("\\,");
        Float latStd = null;
        Float lonStd = null;
        Float altStd = null;
        int iter = 0;
        for (String res : result) {
            switch (iter) {
                case 6:
                    try {
                        latStd = Float.parseFloat(res);
                    } catch (NumberFormatException e) {
                        latStd = null;
                    }
                    break;
                case 7:
                    try {
                        lonStd = Float.parseFloat(res);
                    } catch (NumberFormatException e) {
                        lonStd = null;
                    }
                    break;
                case 8:
                    try {
                        altStd = Float.parseFloat(res.split("\\*")[0]);
                    } catch (NumberFormatException e) {
                        altStd = null;
                    }
                    break;
            }
            iter++;
        }
        if (altStd != null) {
            altError = altStd;
        }
        if (latStd != null && lonStd != null) {
            DRMS = (float) Math.sqrt(latStd*latStd+lonStd*lonStd);
        }
    }

    private void parseGPRMC(String sentence) {
        String[] result = sentence.split("\\,");
        Double latitude = null;
        String latDirection = null;
        Double longitude = null;
        String longDirection = null;
        int iter = 0;
        for (String res : result) {
            switch (iter) {
                case 3:
                    latitude = ParseLatitude(result[3]);
                    break;
                case 4:
                    latDirection = result[4];
                    break;
                case 5:
                    longitude = ParseLongitude(result[5]);
                    break;
                case 6:
                    longDirection = result[6];
                    break;
                case 7:
                    try {
                        velocity = Float.parseFloat(result[7]);
                    } catch (NumberFormatException e) {
                        velocity = null;
                    }
                    break;
            }
            iter++;
        }
        if (latitude != null && longitude != null) {
            latitudeDirect(latitude, latDirection);
            longitudeDirect(longitude, longDirection);
            setProviderLocation(latitude, longitude);
        }
    }

    private void parseGPGSA(String sentence) {
        String[] results = sentence.split("\\,");
        Float hdop = null;
        Float vdop = null;
        if (results.length > 17) {
            try {
                PDOP = Float.parseFloat(results[15]);
            } catch (NumberFormatException e) {
                PDOP = null;
            }
            try {
                hdop = Float.parseFloat(results[16]);
            } catch (NumberFormatException e) {
                hdop = null;
            }
            try {
                vdop = Float.parseFloat(results[17].split("\\*")[0]);
            } catch (NumberFormatException e) {
                vdop = null;
            }
        }
    }

    private void parseGSV(String sentence) {
        String[] result = sentence.split("\\,");
        int total_messages;
        int message_number;
        try {
            message_number = Integer.parseInt(result[2]);
            total_messages = Integer.parseInt(result[1]);
        } catch (NumberFormatException e) {
            return;
        }
        if (result.length < 20) {
            return;
        }
        if (message_number == 1) {
            total_gsv_messages = total_messages;
        }
        for (int i = 0; i < 4; i++) {
            int iter = i*4;
            Satellite satellite = new Satellite();
            try {
                satellite.prn = Integer.parseInt(result[4 + iter]);
            } catch (NumberFormatException e) {}
            try {
                satellite.elev = Integer.parseInt(result[5 + iter]);
            } catch (NumberFormatException e) {}
            try {
                satellite.azim = Integer.parseInt(result[6 + iter]);
            } catch (NumberFormatException e) {}
            try {
                if (result[7 + iter].indexOf("*") != -1) {
                    satellite.snr = Integer.parseInt(result[7 + iter].substring(0, result[7 + iter].indexOf("*")));
                } else {
                    satellite.snr = Integer.parseInt(result[7 + iter]);
                }
            } catch (NumberFormatException e) {}
            if (result[0].equals("$GLGSV")) {
                satellite.type = "glonass";
            } else if (result[0].equals("$GPGSV")) {
                satellite.type = "gps";
            } else {
                satellite.type = "galileo";
            }
            tempSatellites.add(satellite);
        }
        if (message_number == total_gsv_messages) {
            currentSatellites = tempSatellites;
            tempSatellites = new ArrayList<>();
        }
    }

    private Double latitudeDirect(Double latitude, String latDir) {
        if (latDir.startsWith("N")) {
            return latitude;
        } else {
            return -latitude;
        }
    }

    private Double longitudeDirect(Double longitude, String longDir) {
        if (longDir.startsWith("E")) {
            return longitude;
        } else {
            return -longitude;
        }
    }

    void setProviderLocation(Double newLatitude, Double newLongitude) {
        if (altitude == null) {
            error = "Altitude не найден";
        } else if (provider != null && newLatitude != null && newLongitude != null && DRMS != null) {
            Location mockLocation = new Location(provider);
            mockLocation.setLatitude(newLatitude);
            mockLocation.setLongitude(newLongitude);
            mockLocation.setAltitude(altitude);
            mockLocation.setAccuracy(DRMS);
            if (altError != null) {
                mockLocation.setVerticalAccuracyMeters(altError);
            }
            mockLocation.setTime(System.currentTimeMillis());
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            mockLocation.setBearing(0F);
            locationManager.setTestProviderLocation( provider, mockLocation);
            latitude = newLatitude;
            longitude = newLongitude;
            error = null;
        } else {
            if (provider == null) {
                error = "Фиктивное местоположение не включено";
            } else if(DRMS == null) {
                error = "Точность еще не определена";
            } else {
                error = "Широта, долгота - не получены";
            }
        }
    }

    DataLocation getData() {
        DataLocation dataLocation = new DataLocation();
        dataLocation.error = error;
        dataLocation.latitude = latitude;
        dataLocation.longitude = longitude;
        dataLocation.alt = altitude;
        dataLocation.accuracyXY = DRMS;
        dataLocation.accuracyZ = altError;
        dataLocation.satsUsed = satsUsed;
        dataLocation.DGPSAge = DGPSAge;
        dataLocation.PDOP = PDOP;
        dataLocation.velocity = velocity;
        dataLocation.status = quality;
        return dataLocation;
    }

   int getStatus() {
        return quality;
   }

    Map<String, Double> getGeolocation() {
        Map<String, Double> map = new HashMap<>();
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("altitude", altitude);
        return map;
    }

    ArrayList<Satellite> getSatellites() {
        return currentSatellites;
    }
}
