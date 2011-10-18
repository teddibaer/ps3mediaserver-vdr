package net.pms.external.vdr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.*;
import java.net.Socket;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.WebAudioStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.external.AdditionalFolderAtRoot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin main class.
 */
public class VDRPlugin implements AdditionalFolderAtRoot {

    // config keys
    private static final String VDRHOST_KEY = "net.pms.external.vdr.vdrHost";
    private static final String VDPPORT_KEY = "net.pms.external.vdr.vdpPort";
    private static final String STREAMDEVPORT_KEY = "net.pms.external.vdr.streamdevPort";
    private static final String VIDEOFORMAT_KEY = "net.pms.external.vdr.videoFormat";
    private static final String AUDIOFORMAT_KEY = "net.pms.external.vdr.audioFormat";
    private static final String RECFORMAT_KEY = "net.pms.external.vdr.recFormat";

    // the max number of channels to read
    private static final int CHANNELS = 100;
    
    // the logger used for all logging.
    private static final Logger logger = LoggerFactory.getLogger(PMS.class);

    // our root folder
    private final DLNAResource root = new VirtualFolder("VDR", ""); 

    // our root folder
    private final DLNAResource tv = new VirtualFolder("TV", "");

    // our root folder
    private final DLNAResource recordings = new VirtualFolder("Recordings", "");

    // update time
    private final java.util.Timer timer = new java.util.Timer(true);
    
    // the vdp host text field
    private final JTextField vdrHost = new JTextField(15);

    // the vdp port
    private final JTextField streamdevPort = new JTextField(4);

    // the streamdev port
    private final JTextField vdpPort = new JTextField(4);

    // the video format
    private final JTextField videoFormat = new JTextField(15);

    // the video format
    private final JTextField audioFormat = new JTextField(15);

    // the video format
    private final JTextField recFormat = new JTextField(15);

    public VDRPlugin() {
        root.addChild(tv);
        root.addChild(recordings);

        String cvdrHost = (String) PMS.getConfiguration().getCustomProperty(VDRHOST_KEY);
        vdrHost.setText(cvdrHost == null ? "frodo" : cvdrHost);

        String cvdpPort = (String) PMS.getConfiguration().getCustomProperty(VDPPORT_KEY);
        vdpPort.setText(cvdpPort == null ? "2004" : cvdpPort);

        String cstreamdevPort = (String) PMS.getConfiguration().getCustomProperty(STREAMDEVPORT_KEY);
        streamdevPort.setText(cstreamdevPort == null ? "3000" : cstreamdevPort);

        String cvformat = (String) PMS.getConfiguration().getCustomProperty(VIDEOFORMAT_KEY);
        videoFormat.setText(cstreamdevPort == null ? "TS" : cvformat);

        String caformat = (String) PMS.getConfiguration().getCustomProperty(AUDIOFORMAT_KEY);
        audioFormat.setText(cstreamdevPort == null ? "ES" : caformat);

        String crecformat = (String) PMS.getConfiguration().getCustomProperty(RECFORMAT_KEY);
        recFormat.setText(cstreamdevPort == null ? "EXT;COPY;rec:" : crecformat);

        // add a container listener so we can update the config whenever the
        // config dialog is closed
        vdrHost.addAncestorListener(new AncestorListener() {
            
            @Override
            public void ancestorAdded(AncestorEvent event) {
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                PMS.getConfiguration().setCustomProperty(VDRHOST_KEY, vdrHost.getText());
                PMS.getConfiguration().setCustomProperty(VDPPORT_KEY, vdpPort.getText());
                PMS.getConfiguration().setCustomProperty(STREAMDEVPORT_KEY, streamdevPort.getText());
                PMS.getConfiguration().setCustomProperty(VIDEOFORMAT_KEY, videoFormat.getText());
                PMS.getConfiguration().setCustomProperty(AUDIOFORMAT_KEY, audioFormat.getText());
                PMS.getConfiguration().setCustomProperty(RECFORMAT_KEY, recFormat.getText());
                try {
                    PMS.getConfiguration().save();
                }
                catch (Exception e) {
                }

                timer.schedule(new UpdateTask(), 0);
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });

        timer.schedule(new UpdateTask(), 0, 10 * 60 * 1000);
    }

    @Override
    public JComponent config() {
        JComponent config = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        config.setLayout(gridbag);

        // vdr host and vdp port
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        config.add(new JLabel("VDR host (e.g. 'frodo'): "), c);

        config.add(vdrHost, c);

        config.add(new JLabel("  VDP port (e.g. '2004'): "), c);

        config.add(vdpPort, c);

        // streamdev port
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridx = 2;
        config.add(new JLabel("  Streamdev port (e.g. '3000'): "), c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 3;
        config.add(streamdevPort, c);

        // video format
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        config.add(new JLabel("Video format (e.g. 'TS'): "), c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        config.add(videoFormat, c);

        // audio format
//        c = new GridBagConstraints();
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx = 1.0;
//        config.add(new JLabel("Audio format (e.g. 'ES'): "), c);

//        c.gridwidth = GridBagConstraints.REMAINDER;
//        config.add(audioFormat, c);

        // recordings format
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        config.add(new JLabel("Recordings format (e.g. 'EXT;COPY;rec:'): "), c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        config.add(recFormat, c);

        return config;
    }

    @Override
    public String name() {
        return "ps3mediaserver-vdr";
    }

    @Override
    public void shutdown() {
    }

    @Override
    public DLNAResource getChild() {
        return root;
    }

    class UpdateTask extends java.util.TimerTask {
        
        public void run() {
            tv.getChildren().clear();
            recordings.getChildren().clear();

            try {
                final Socket s = new Socket(vdrHost.getText(), Integer.parseInt(vdpPort.getText()));
                final BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                in.readLine(); // read welcome message

                final String streamdevHostPort = vdrHost.getText() + ":" + streamdevPort.getText();
                
                // read channels
                s.getOutputStream().write("LSTC\r\n".getBytes());
                int n = 1;
                String line = in.readLine();
                while (line != null  &&  line.startsWith("250")) {
                    logger.debug("read line: " + line);

                    VDRChannel channel = VDRChannel.parseLSTCLine(line);
                    if (channel != null  &&  n < CHANNELS) {
                        if (channel.isTV())
                            // add direct link
                            tv.addChild(new VDRStream(n + ". " + channel.getName(), channel.getURI(streamdevHostPort, videoFormat.getText()), null));
                        else
                            // add as web audio stream to allow re-encoding
                            tv.addChild(new WebAudioStream(n + "." + channel.getName(), channel.getURI(streamdevHostPort, "ES"), null));
                        n++;
                    }

                    if (line.startsWith("250 "))
                        break;
                    line = in.readLine();
                }
                logger.info("VDR channel update finished.");
                
                s.getOutputStream().write("LSTR\r\n".getBytes());
                line = in.readLine();
                while (line != null  &&  line.startsWith("250")) {
                    logger.debug("read line: " + line);

                    VDRRecording rec = VDRRecording.parseLSTRLine(line);
                    if (rec != null)
                        recordings.addChild(new VDRStream(rec.getNumber() + ". " + rec.getName(), rec.getURI(streamdevHostPort, recFormat.getText()), null));

                    if (line.startsWith("250 "))
                        break;
                    line = in.readLine();
                }
                logger.info("VDR recordings update finished.");
                
                s.close();
            }
            catch (IOException e) {
                logger.error("Reading VDR channels failed.", e);
            }
        }
    }

    // just for testing purposes
    public static void main(String[] args) {
        String lines[] = { "250-7 RTL Television,RTL;RTL World:12187:HC34M2O0S0:S19.2E:27500:163=2:104=deu@3;106=deu@106:105:0:12003:1:1089:0",
                "250 204 test - test:11973:vC34:S19.2E:27500:4071+8190=2:4072=fra@4:4074:D00,100,500,1811:28677:1:1101:0",
                "250-81 viacombrandsolutions - Comedy Central:11973:vC34:S19.2E:27500:4071+8190=2:4072=fra@4:4074:D00,100,500,1811:28677:1:1101:0" };
        for (int i = 0; i < lines.length; i++) {
            VDRChannel channel = VDRChannel.parseLSTCLine(lines[i]);
            System.out.println(channel.getName() + " - " + channel.getURI("host:port", "TS"));
        }
        
        String lines2[] = { "250-1 16.01.10 11:03* BERND DAS BROT",
                "250 2 16.01.10 11:03* BERND DAS BROT" };
        for (int i = 0; i < lines2.length; i++) {
            VDRRecording rec = VDRRecording.parseLSTRLine(lines2[i]);
            System.out.println(rec.getName() + " - " + rec.getURI("host:port", "EXT;COPY;rec:"));
        }
    }
}
