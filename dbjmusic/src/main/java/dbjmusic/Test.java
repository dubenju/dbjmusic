package dbjmusic;

import java.io.File;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

public class Test {
    private static final String[]       SYSTEM_MESSAGE_TEXT =
    {
        "System Exclusive (should not be in ShortMessage!)",
        "MTC Quarter Frame: ",
        "Song Position: ",
        "Song Select: ",
        "Undefined",
        "Undefined",
        "Tune Request",
        "End of SysEx (should not be in ShortMessage!)",
        "Timing clock",
        "Undefined",
        "Start",
        "Continue",
        "Stop",
        "Undefined",
        "Active Sensing",
        "System Reset"
    };
    private static final String[]       QUARTER_FRAME_MESSAGE_TEXT =
    {
        "frame count LS: ",
        "frame count MS: ",
        "seconds count LS: ",
        "seconds count MS: ",
        "minutes count LS: ",
        "minutes count MS: ",
        "hours count LS: ",
        "hours count MS: "
    };

    private static final String[]       FRAME_TYPE_TEXT =
    {
        "24 frames/second",
        "25 frames/second",
        "30 frames/second (drop)",
        "30 frames/second (non-drop)",
    };
    private static final String[]       sm_astrKeySignatures = {"Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#"};
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static String getKeyName(int nKeyNumber)
    {
        if (nKeyNumber > 127)
        {
            return "illegal value";
        }
        else
        {
            int nNote = nKeyNumber % 12;
            int nOctave = nKeyNumber / 12;
            return NOTE_NAMES[nNote] + (nOctave - 1);
        }
    }
    public static int get14bitValue(int nLowerPart, int nHigherPart) {
        return (nLowerPart & 0x7F) | ((nHigherPart & 0x7F) << 7);
    }
    private static char hexDigits[] = 
        {'0', '1', '2', '3', 
         '4', '5', '6', '7', 
         '8', '9', 'A', 'B', 
         'C', 'D', 'E', 'F'};
    public static String getHexString(byte[] aByte) {
        StringBuffer    sbuf = new StringBuffer(aByte.length * 3 + 2);
        for (int i = 0; i < aByte.length; i++) {
            sbuf.append(' ');
            sbuf.append(hexDigits[(aByte[i] & 0xF0) >> 4]);
            sbuf.append(hexDigits[aByte[i] & 0x0F]);
            /*byte  bhigh = (byte) ((aByte[i] &  0xf0) >> 4);
            sbuf.append((char) (bhigh > 9 ? bhigh + 'A' - 10: bhigh + '0'));
            byte    blow = (byte) (aByte[i] & 0x0f);
            sbuf.append((char) (blow > 9 ? blow + 'A' - 10: blow + '0'));*/
        }
        return new String(sbuf);
    }
    
    public static String getHexString(ShortMessage sm)
    {
        // bug in J2SDK 1.4.1
        // return getHexString(sm.getMessage());
        int status = sm.getStatus();
        String res = Integer.toHexString(status).toUpperCase();
        if (res.length() < 2) {
            res = "0" + res;
        }
        // if one-byte message, return
        switch (status) {
            case 0xF6:          // Tune Request
            case 0xF7:          // EOX
                // System real-time messages
            case 0xF8:          // Timing Clock
            case 0xF9:          // Undefined
            case 0xFA:          // Start
            case 0xFB:          // Continue
            case 0xFC:          // Stop
            case 0xFD:          // Undefined
            case 0xFE:          // Active Sensing
            case 0xFF: return res;
        }

        String str = Integer.toHexString(sm.getData1()).toUpperCase();
        if (str.length() < 2) {
            str = "0" + str;
        }
        res = res + ' ' + str;

        // if 2-byte message, return
        switch (status) {
            case 0xF1:          // MTC Quarter Frame
            case 0xF3:          // Song Select
                    return res;
        }
        switch (sm.getCommand()) {
            case 0xC0:
            case 0xD0:
                    return res;
        }
        // 3-byte messages left
        str = Integer.toHexString(sm.getData2()).toUpperCase();
        if (str.length() < 2) {
            str = "0" + str;
        }
        res = res + ' ' + str;
        return res;
    }

    // convert from microseconds per quarter note to beats per minute and vice versa
    private static float convertTempo(float value) {
        if (value <= 0) {
            value = 0.1f;
        }
        return 60000000.0f / value;
    }

    private static String printShortMessage(ShortMessage sm) {

        String  strMessage = null;
        switch (sm.getCommand()) {
        case 0x80:
            strMessage = "note Off " + getKeyName(sm.getData1()) + " velocity: " + sm.getData2();
            break;
        case 0x90:
            strMessage = "note On " + getKeyName(sm.getData1()) + " velocity: " + sm.getData2();
            break;
        case 0xa0:
            strMessage = "polyphonic key pressure " + getKeyName(sm.getData1()) + " pressure: " + sm.getData2();
            break;
        case 0xb0:
            strMessage = "control change " + sm.getData1() + " value: " + sm.getData2();
            break;
        case 0xc0:
            strMessage = "program change " + sm.getData1();
            break;
        case 0xd0:
            strMessage = "key pressure " + getKeyName(sm.getData1()) + " pressure: " + sm.getData2();
            break;
        case 0xe0:
            strMessage = "pitch wheel change " + get14bitValue(sm.getData1(), sm.getData2());
            break;
        case 0xF0:
            strMessage = SYSTEM_MESSAGE_TEXT[sm.getChannel()];
            switch (sm.getChannel()) {
            case 0x1:
                int nQType = (sm.getData1() & 0x70) >> 4;
                int nQData = sm.getData1() & 0x0F;
                if (nQType == 7) {
                    nQData = nQData & 0x1;
                }
                strMessage += QUARTER_FRAME_MESSAGE_TEXT[nQType] + nQData;
                if (nQType == 7) {
                    int nFrameType = (sm.getData1() & 0x06) >> 1;
                    strMessage += ", frame type: " + FRAME_TYPE_TEXT[nFrameType];
                }
                break;
            case 0x2:
                strMessage += get14bitValue(sm.getData1(), sm.getData2());
                break;
            case 0x3:
                strMessage += sm.getData1();
                break;
            }
            break;
        default:
            strMessage = "unknown message: status = " + sm.getStatus() + ", byte1 = " + sm.getData1() + ", byte2 = " + sm.getData2();
            break;
        } // switch (sm.getCommand()) {

        if (sm.getCommand() != 0xF0) {
            strMessage = "channel " + sm.getChannel() + ": " + strMessage;
        }
        return ("[" + getHexString(sm) + "] " + strMessage);
    }

    public static String printMetaMessage(MetaMessage mm) {

//      byte[]  abMessage = message.getMessage();
      byte[]  abData = mm.getData();
//      int nDataLength = message.getLength();
      String  strMessage = null;
      switch (mm.getType()) {
      case 0:
          int nSequenceNumber = ((abData[0] & 0xFF) << 8) | (abData[1] & 0xFF);
          strMessage = "Sequence Number: " + nSequenceNumber;
          break;
      case 1:
          strMessage = "Text Event: " + new String(abData);
          break;
      case 2:
          strMessage = "Copyright Notice: " + new String(abData);
          break;
      case 3:
          strMessage = "Sequence/Track Name: " +  new String(abData);
          break;
      case 4:
          strMessage = "Instrument Name: " + new String(abData);
          break;
      case 5:
          strMessage = "Lyric: " + new String(abData);
          break;
      case 6:
          strMessage = "Marker: " + new String(abData);
          break;
      case 7:
          strMessage = "Cue Point: " + new String(abData);
          break;
      case 0x20:
          int nChannelPrefix = abData[0] & 0xFF;
          strMessage = "MIDI Channel Prefix: " + nChannelPrefix;
          break;
      case 0x2F:
          strMessage = "End of Track";
          break;
      case 0x51:
          int nTempo = ((abData[0] & 0xFF) << 16)
                  | ((abData[1] & 0xFF) << 8)
                  | (abData[2] & 0xFF);           // tempo in microseconds per beat
          float bpm = convertTempo(nTempo);
          // truncate it to 2 digits after dot
          bpm = (float) (Math.round(bpm*100.0f)/100.0f);
          strMessage = "Set 速度(Tempo):每分钟演奏" + bpm + "个四分音符(bpm)Allegro（♩＝" + bpm + "）";
          break;
      case 0x54:
          // System.out.println("data array length: " + abData.length);
          strMessage = "SMTPE Offset: "
              + (abData[0] & 0xFF) + ":"
              + (abData[1] & 0xFF) + ":"
              + (abData[2] & 0xFF) + "."
              + (abData[3] & 0xFF) + "."
              + (abData[4] & 0xFF);
          break;
      case 0x58:
          strMessage = "拍子记号(Time Signature)"
              + (abData[0] & 0xFF) + "/" + (1 << (abData[1] & 0xFF))
              + ", MIDI clocks per 节拍器(metronome) tick: " + (abData[2] & 0xFF)
              + ", 1/32 per 24 MIDI clocks: " + (abData[3] & 0xFF);
          break;
      case 0x59:
          strMessage = "音调符号(Key Signature)" + sm_astrKeySignatures[abData[0] + 7] + " " + ((abData[1] == 1) ? "minor(小)" : "major(大)");
          break;
      case 0x7F:
          strMessage = "Sequencer-Specific Meta event: " + getHexString(abData);
          break;
      default:
          strMessage = "unknown Meta event: " + getHexString(abData);
          break;
      } // switch (mm.getType()) {
      return strMessage;
    }
    public static String printSysexMessage(SysexMessage sm) {
        byte[]  abData = sm.getData();
        String  strMessage = null;
        if (sm.getStatus() == SysexMessage.SYSTEM_EXCLUSIVE) {
            strMessage = "Sysex message: F0" + getHexString(abData);
        } else if (sm.getStatus() == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
            strMessage = "Continued Sysex message F7" + getHexString(abData);
        }
        return (strMessage);
    }

    public static void main(String[] args) throws Exception {
//        KeyboardPane p = new KeyboardPane();
        Sequence sequence = MidiSystem.getSequence(new File("./test/test.mid"));
        System.out.println("------------------------------------------------------------");
        System.out.println("Length: " + sequence.getTickLength() + " ticks");
        System.out.println("Duration: " + sequence.getMicrosecondLength() + " microseconds");
        System.out.println("------------------------------------------------------------");
        float fDivisionType = sequence.getDivisionType();
        String strDivisionType = null;
        if (fDivisionType == Sequence.PPQ) { // 0.0f
            strDivisionType = "PPQ";
        } else if (fDivisionType == Sequence.SMPTE_24) { // 24.0f
            strDivisionType = "SMPTE, 24 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_25) { // 25.0f
            strDivisionType = "SMPTE, 25 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_30DROP) { // 29.97f
            strDivisionType = "SMPTE, 29.97 frames per second";
        } else if (fDivisionType == Sequence.SMPTE_30) { // 30.0f
            strDivisionType = "SMPTE, 30 frames per second";
        }
        System.out.println("DivisionType: " + strDivisionType);
        String strResolutionType = null;
        if (fDivisionType == Sequence.PPQ) {
              strResolutionType = " ticks per beat";
        } else {
              strResolutionType = " ticks per frame";
        }
        System.out.println("Resolution: " + sequence.getResolution() + strResolutionType);
        System.out.println("------------------------------------------------------------");

//        Sequence seq = new Sequence(Sequence.PPQ, 240);
//        Track trk = seq.createTrack();
//        MetaMessage tempo = new MetaMessage();
//        tempo.setMessage(0x51, new byte[] {0x07, (byte)0xa1, 0x20}, 3);
//        trk.add(new MidiEvent(tempo, 0));

        int trackNumber = 0;
        Track[] tracks = sequence.getTracks();
        for (int idx = 0; idx < tracks.length; idx ++) {
            Track track = tracks[idx];
            System.out.println("Track " + trackNumber + ": size = " + track.size());
            System.out.println("-----------------------");
            for (int i = 0; i < track.size(); i ++) { 
                MidiEvent event = track.get(i);
                long lTicks = event.getTick();

                StringBuilder buf = new StringBuilder();
                buf.append("@" + i + "," + event.getTick() + ",tick " + lTicks + ": ");
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    buf.append("<ShortMessage>" + printShortMessage((ShortMessage) message));
                } else if (message instanceof MetaMessage) {
                    buf.append("<MetaMessage>" + printMetaMessage((MetaMessage) message));
                } else if (message instanceof SysexMessage) {
                    buf.append("<SysexMessage>" + printSysexMessage((SysexMessage) message));
                } else if (true) {
                    buf.append("<Other message>");
                    buf.append("Other message: " + message.getClass());
                }
                System.out.println(buf.toString());
            } // for (int i=0; i < track.size(); i++) {
            trackNumber ++;
        }

//            Sequencer sequencer = MidiSystem.getSequencer(true); // Get the default Sequencer
//            if (sequencer==null) {
//                System.err.println("Sequencer device not supported");
//                return;
//            } 
//            sequencer.open(); // Open device
//
//             sequencer.setSequence(sequence); // load it into sequencer
////            sequencer.setSequence(seq); // load it into sequencer
//            sequencer.start();  // start the playback
//            while(sequencer.isRunning()) {
//                Thread.sleep(1000);
//            }
//            sequencer.close();

            System.out.println("------------------------------------------------------------");

    }
}
