package net.pms.external.vdr;

/**
 * Represents a single TV or Radio channel of the VDR.
 */
public class VDRChannel {

    private static enum Field {    
        NAME, FREQUENCY, PARAMETER, SIGNALSOURCE, SYMBOLRATE, VPID, APID, TPID, CAID, SID, NID, TID, RID
    };
    
    private final String[] fields;
    
    /**
     * Parses a VDR channel info line as returned by the LSTC command, e.g.  
     * 250-7 RTL Television,RTL;RTL World:12187:HC34M2O0S0:S19.2E:27500:163=2:104=deu@3;106=deu@106:105:0:12003:1:1089:0
     * 250 3456 RTL Television,RTL;RTL World:12187:HC34M2O0S0:S19.2E:27500:163=2:104=deu@3;106=deu@106:105:0:12003:1:1089:0
     * @return a VDRChannel object or null 
     */
    public static VDRChannel parseLSTCLine(String lstcString) {
        // strip off LSTC info
        lstcString = lstcString.substring(lstcString.indexOf(' ', 4) + 1);
        return parseChannelLine(lstcString);
    }
    
    /**
     * Parses a VDR channel info line, e.g.   
     * RTL Television,RTL;RTL World:12187:HC34M2O0S0:S19.2E:27500:163=2:104=deu@3;106=deu@106:105:0:12003:1:1089:0
     * @return a VDRChannel object or null 
     */
    public static VDRChannel parseChannelLine(String channelLine) {
        String[] fields = channelLine.split(":");
        if (fields.length == Field.values().length)
            return new VDRChannel(fields);
        return null;
    }

    /** private ctor */
    private VDRChannel(String[] fields) {
        this.fields = fields;
    }
    
    /**
     * @return true if this is a TV channel
     */
    boolean isTV() {
        return !fields[Field.VPID.ordinal()].equals("0");
    }
    
    /**
     * @return the name of the channel
     */
    public String getName() {
        String names = fields[Field.NAME.ordinal()];
        // split name from bouquet
        int bouquetIndex = names.indexOf(';');
        if (bouquetIndex > 0)
            names = names.substring(0, bouquetIndex);
        // get primary name (there may be multiple names)
        String names2[] = names.split(",");
        return names2.length >= 2 ? names2[1] : names2[0];
    }
    
    /**
     * @param host the host/port of the VDR host
     * @return an URI to the transport stream, e.g. http://<host>:<port>/<format>/S19.2E-1-1089-12003.ts 
     */
    public String getURI(String host, String format) {
        return "http://" + host + "/" + format + "/" + fields[Field.SIGNALSOURCE.ordinal()] + "-" + fields[Field.NID.ordinal()] 
                + "-" + fields[Field.TID.ordinal()] + "-" + fields[Field.SID.ordinal()] + ".ts";
    }
}