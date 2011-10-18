package net.pms.external.vdr;

public class VDRRecording {

    /**
     * Parses a VDR recording info line as returned by the LSTR command, e.g.  
     * 250-1 16.01.10 11:03* BERND DAS BROT
     * @return a VDRRecording object or null 
     */
    public static VDRRecording parseLSTRLine(String lstrString) {
        // strip off LSTC info
        return parseRecordingLine(lstrString.substring(4));
    }
    
    /**
     * Parses a VDR recording info line, e.g.   
     * 1 16.01.10 11:03* BERND DAS BROT
     * @return a VDRRecording object or null 
     */
    public static VDRRecording parseRecordingLine(String recordingLine) {
        int index = recordingLine.indexOf(' ');
        int nr = Integer.parseInt(recordingLine.substring(0, index));
        
        recordingLine = recordingLine.substring(index + 1);
        index = recordingLine.indexOf(' ');
        String date = recordingLine.substring(0, index); 
        
        recordingLine = recordingLine.substring(index + 1);
        index = recordingLine.indexOf(' ');
        String time = recordingLine.substring(0, index); 
        
        String name = recordingLine.substring(index + 1);
        return new VDRRecording(nr, date, time, name);
    }
    
    private final int nr;
   
    private final String date;

    private final String time;

    private final String name;

    /** private ctor */
    private VDRRecording(int nr, String date, String time, String name) {
        this.nr = nr;
        this.date = date;
        this.time = time;
        this.name = name;
    }

    public int getNumber() {
        return nr;
    }
    
    public String  getTime() {
        return time;
    }
    
    public String  getDate() {
        return date;
    }
    
    public String  getName() {
        return name;
    }
    
    public String getURI(String host, String format) {
        return "http://" + host + "/" + format + name.replace(" ", "_") + "/1";
    }
}
