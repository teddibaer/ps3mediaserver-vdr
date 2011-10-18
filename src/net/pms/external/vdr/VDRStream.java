package net.pms.external.vdr;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.WebVideoStream;
import net.pms.formats.MPG;
import net.pms.network.HTTPResource;

/**
 * A customized WebVideoStream that returns the direct VDR URL to the renderer.
 */
public class VDRStream extends WebVideoStream {
    
    VDRStream(String name, String url, String thumbURL) {
        super(name, url, thumbURL);
        
        media = new DLNAMediaInfo();
        media.codecV = "mpeg2";
        media.frameRate = "25";
        media.mediaparsed = true;
        media.mimeType = HTTPResource.MPEG_TYPEMIME;
        media.container = "mpegts";
        // TODO anything else we need to set there?
    }
    
    @Override
    protected String getURL(String prefix) {
        return URL;
    }
    
    @Override
    protected String getThumbnailURL() {
        // need to overwrite since we need to call the super.getURL(...) method instead of our own
        return thumbURL == null ? super.getURL("thumbnail0000") : thumbURL;
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
    
    @Override
    protected void checktype() {
        ext = new MPG();
    }
}

