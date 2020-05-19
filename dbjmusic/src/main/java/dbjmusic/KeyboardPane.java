package dbjmusic;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.swing.SwingUtilities;
import javax.swing.plaf.DimensionUIResource;

public class KeyboardPane extends Container implements KeyListener, MouseListener, Receiver {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private Synthesizer syn = null;
    private MidiChannel[] midChannel;
    private int midiChannel = 0; //

    //Keeps track of which key is pressed
    private boolean[] keyDown = new boolean[1024];
    //Holds which key plays what note
    private HashMap<Integer, Integer> notes = new HashMap<Integer, Integer>();

    public static final int NUM_CHANNEL = 16;
    public static final int NUM_PITCH = 128;

    public static int whiteKeyWidth = 24;
    public static int whiteKeyHeight = 160;
    public static int blackKeyWidth = 12;
    public static int blackKeyHeight = 100;

    public static Color lightColor = new Color(255, 198, 0, 100);
    public static Color shadowColor = new Color(0, 0, 0, 50);

    private int top = 0;
    private int bottom = 0;
    private int left = 0;
    private int right = 0;

    private final boolean[] keyPressed = new boolean[NUM_PITCH];
    private final boolean[] displayKeyPressed = new boolean[NUM_PITCH];
    private final int[] steps = {2, 2, 1, 2, 2, 2, 1};
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static final String[] NOTE_NAMES2 = {"1", "", "2", "", "3", "4", "", "5", "", "6", "", "7"};
    private int countC = -1;

    private int offset = 5;

    public boolean isMouseEnabled=true;
    private int lastIndex = -1;

    public KeyboardPane() {
        // this.setPreferredSize(new DimensionUIResource(1800, 350));
        this.setPreferredSize(new Dimension(left + whiteKeyWidth * 75 + right, top + whiteKeyHeight + bottom));
        this.setLayout(null);

        this.addKeyListener(this);
        this.addMouseListener(this);
//        this.setVisible(true);
        this.repaint();

        try {
            this.syn = MidiSystem.getSynthesizer();
            this.syn.open();
            System.out.println("open:" + this.syn);

            Soundbank soundbank = this.syn.getDefaultSoundbank();
            System.out.println("getbank:" + soundbank);
            if (soundbank != null) {
                Instrument[] instruments = soundbank.getInstruments();
                System.out.println("get instrument" + this.syn.getAvailableInstruments());
                this.syn.loadInstrument(instruments[0]);
            }
//            Instrument[] instrument = this.syn.getAvailableInstruments();
//            this.syn.loadInstrument(instrument[50]);

            /*
             * 频道：所有16个MIDI频道都是支持的。
             * 每个频道都可以播放可变数量的声音（复音）。
             * 每个频道都可以发挥不同乐器（声音/补丁/音色）。
             * 基于键的打击乐总是在MIDI上第10频道。
             */
            this.midChannel = this.syn.getChannels();
            // System.out.println(this.midChannel.length); // 16
            for (MidiChannel channel : this.midChannel) {
                System.out.println(channel);
            }
            this.midChannel[this.midiChannel].programChange(0);
            // No.　英语　中文
            // Piano（钢琴）　　
            // 1　Acoustic Grand Piano　平台钢琴
            // 2　Bright Acoustic Piano　亮音钢琴
            // 3　Electric Grand Piano　电钢琴
            // 4　Honky-tonk Piano　酒吧钢琴
            // 5　Electric Piano 1　电钢琴1
            // 6　Electric Piano 2　电钢琴2
            // 7　Harpsichord　大键琴
            // 8　Clavinet　电翼琴
            // Chromatic Percussion（固定音高敲击乐器）　　
            // 9　Celesta　钢片琴
            // 10　Glockenspiel　钟琴
            // 11　Musical box　音乐盒
            // 12　Vibraphone　颤音琴
            // 13　Marimba　马林巴琴
            // 14　Xylophone　木琴
            // 15　Tubular Bell　管钟
            // 16　Dulcimer　洋琴
            // Organ（风琴）　　
            // 17　Drawbar Organ　音栓风琴
            // 18　Percussive Organ　敲击风琴
            // 19　Rock Organ　摇滚风琴
            // 20　Church organ　教堂管风琴
            // 21　Reed organ　簧风琴
            // 22　Accordion　手风琴
            // 23　Harmonica　口琴
            // 24　Tango Accordion　探戈手风琴
            // Guitar（吉他）　　
            // 25　Acoustic Guitar(nylon)　木吉他（尼龙弦）
            // 26　Acoustic Guitar(steel)　木吉他（钢弦）
            // 27　Electric Guitar(jazz)　电吉他（爵士）
            // 28　Electric Guitar(clean)　电吉他（原音）
            // 29　Electric Guitar(muted)　电吉他（闷音）
            // 30　Overdriven Guitar　电吉他（破音）
            // 31　Distortion Guitar　电吉他（失真）
            // 32　Guitar harmonics　吉他泛音
            // Bass（贝斯）　　
            // 33　Acoustic Bass　民谣贝斯
            // 34　Electric Bass(finger)　电贝斯（指奏）
            // 35　Electric Bass(pick)　电贝斯（拨奏）
            // 36　Fretless Bass　无格贝斯
            // 37　Slap Bass 1　捶钩贝斯 1
            // 38　Slap Bass 2　捶钩贝斯 2
            // 39　Synth Bass 1　合成贝斯1
            // 40　Synth Bass 2　合成贝斯2
            // Strings（弦乐 器）　　
         // 41　Violin　小提琴
         // 42　Viola　中提琴
         // 43　Cello　大提琴
         // 44　Contrabass　低音大提琴
         // 45　Tremolo Strings　颤弓弦乐
         // 46　Pizzicato Strings　弹拨弦乐
         // 47　Orchestral Harp　竖琴
         // 48　Timpani　定音鼓
         // Ensemble（合奏）　　
         // 49　String Ensemble 1　弦乐合奏1
         // 50　String Ensemble 2　弦乐合奏2
         // 51　Synth Strings 1　合成弦乐1
         // 52　Synth Strings 2　合成弦乐2
         // 53　Voice Aahs　人声“啊”
         // 54　Voice Oohs　人声“喔”
         // 55　Synth Voice　合成人声
         // 56　Orchestra Hit　交响打击乐
         // Brass（铜管 乐器）　　
         // 57　Trumpet　小号
         // 58　Trombone　长号
         // 59　Tuba　大号（吐巴号、低音号）
         // 60　Muted Trumpet　闷音小号
         // 61　French horn　法国号（圆号）
         // 62　Brass Section　铜管乐
         // 63　Synth Brass 1　合成铜管1
         // 64　Synth Brass 2　合成铜管2
            // Reed（簧乐 器）　　
            // 65　Soprano Sax　高音萨克斯风
            // 66　Alto Sax　中音萨克斯风
            // 67　Tenor Sax　次中音萨克斯风
            // 68　Baritone Sax　上低音萨克斯风
            // 69　Oboe　双簧管
            // 70　English Horn　英国管
         // 71　Bassoon　低音管（巴颂管）
         // 72　Clarinet　单簧管（黑管、竖笛）
         // Pipe（吹管 乐器）　　
         // 73　Piccolo　短笛
         // 74　Flute　长笛
         // 75　Recorder　直笛
         // 76　Pan Flute　排笛
         // 77　Blown Bottle　瓶笛
         // 78　Shakuhachi　尺八
         // 79　Whistle　哨子
         // 80　Ocarina　陶笛
         // Synth Lead（合成音主旋律）　　
         // 81　Lead 1(square)　方波
         // 82　Lead 2(sawtooth)　锯齿波
         // 83　Lead 3(calliope)　汽笛风琴
         // 84　Lead 4(chiff)　合成吹管
         // 85　Lead 5(charang)　合成电吉他
         // 86　Lead 6(voice)　人声键盘
         // 87　Lead 7(fifths)　五度音
         // 88　Lead 8(bass + lead)　贝斯吉他合奏
         // Synth Pad（合成音和弦衬底）　　
         // 89　Pad 1(new age)　新世纪
         // 90　Pad 2(warm)　温暖
         // 91　Pad 3(polysynth)　多重合音
         // 92　Pad 4(choir)　人声合唱
         // 93　Pad 5(bowed)　玻璃
         // 94　Pad 6(metallic)　金属
         // 95　Pad 7(halo)　光华
         // 96　Pad 8(sweep)　扫掠
         // Synth Effects（合成音效果）　　
         // 97　FX 1(rain)　雨
         // 98　FX 2(soundtrack)　电影音效
         // 99　FX 3(crystal)　水晶
         // 100　FX 4(atmosphere)　气氛
         // 101　FX 5(brightness)　明亮
         // 102　FX 6(goblins)　魅影
         // 103　FX 7(echoes)　回音
         // 104　FX 8(sci-fi)　科幻
         // Ethnic（民族 乐器）　　
         // 105　Sitar　西塔琴
         // 106　Banjo　五弦琴（斑鸠琴）
         // 107　Shamisen　三味线
         // 108　Koto　十三弦琴（古筝）
         // 109　Kalimba　卡林巴铁片琴
         // 110　Bagpipe　苏格兰风笛
         // 111　Fiddle　古提琴
         // 112　Shanai　兽笛，发声机制类似唢呐
         // Percussive（打击 乐器）　　
         // 113　Tinkle Bell　叮当铃
         // 114　Agogo　阿哥哥鼓
         // 115　Steel Drums　钢鼓
         // 116　Woodblock　木鱼
         // 117　Taiko Drum　太鼓
         // 118　Melodic Tom　定音筒鼓
         // 119　Synth Drum　合成鼓
         // 120　Reverse Cymbal　逆转钹声
         // Sound effects（特殊 音效）　　
         // 121　Guitar Fret Noise　吉他滑弦杂音
         // 122　Breath Noise　呼吸杂音
         // 123　Seashore　海岸
         // 124　Bird Tweet　鸟鸣
            // 125　Telephone Ring　电话铃声
            // 126　Helicopter　直升机
            // 127　Applause　拍手
            // 128　Gunshot　枪声
//            this.midChannel[10].programChange(53);
         // No.　English　中文
         // 35　Bass Drum 2　大鼓2
         // 36　Bass Drum 1　大鼓1
         // 37　Side Stick　小鼓鼓边
         // 38　Snare Drum 1　小鼓1
         // 39　Hand Clap　拍手
         // 40　Snare Drum 2　小鼓2
         // 41　Low Tom 2　低音筒鼓2
         // 42　Closed Hi-hat　闭合开合钹
         // 43　Low Tom 1　低音筒鼓1
         // 44　Pedal Hi-hat　脚踏开合钹
         // 45　Mid Tom 2　中音筒鼓2
         // 46　Open Hi-hat　开放开合钹
         // 47　Mid Tom 1　中音筒鼓1
         // 48　High Tom 2　高音筒鼓2
         // 49　Crash Cymbal 1　强音钹1
         // 50　High Tom 1　高音筒鼓1
         // 51　Ride Cymbal 1　打点钹1
         // 52　Chinese Cymbal　钹
         // 53　Ride Bell　响铃
         // 54　Tambourine　铃鼓
         // 55　Splash Cymbal　小钹铜钹
         // 56　Cowbell　牛铃
         // 57　Crash Cymbal 2　强音钹2
         // 58　Vibra Slap　噪音器
         // 59　Ride Cymbal 2　打点钹2
         // 60　High Bongo　高音邦加鼓
         // 61　Low Bongo　低音邦加鼓
         // 62　Mute High Conga　闷音高音康加鼓
         // 63　Open High Conga　开放高音康加鼓
         // 64　Low Conga　低音康加鼓
         // 65　High Timbale　高音天巴雷鼓
         // 66　Low Timbale　低音天巴雷鼓
         // 67　High Agogo　高音阿哥哥
         // 68　Low Agogo　低音阿哥哥
         // 69　Cabasa　铁沙铃
         // 70　Maracas　沙槌
         // 71　Short Whistle　短口哨
         // 72　Long Whistle　长口哨
         // 73　Short Guiro　短刮瓜
         // 74　Long Guiro　长刮瓜
         // 75　Claves　击木
         // 76　High Wood Block　高音木鱼
         // 77　Low Wood Block　低音木鱼
         // 78　Mute Cuica　
         // 79　Open Cuica　
         // 80　Mute Triangle　闷音三角铁
         // 81　Open Triangle　开放三角铁

        } catch (MidiUnavailableException e) {
            if (this.syn == null)
                System.out.println("fail to open midi");
            e.printStackTrace();
        }

        for (int i = 0; i < this.keyDown.length; i ++) {
            this.keyDown[i] = false;
        }
        this.setKeys(notes);
        System.out.println("KeyboardPane is Created.");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.requestFocus();
     }

//    @Override
//    public void removeNotify() {
//        super.removeNotify();
//    }

    @Override
    public void paint(Graphics g) {
        System.out.println("paint");
        super.paint(g);
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.translate(left, top);
        int stepIndex = 0;
        int x = 0;
        int index = 1;
        countC = -1;
        // draw white key
        for(int p = 0; p < keyPressed.length;) {
           if(keyPressed[p]) {
               g.setColor(Color.orange); 
               g.fillRect(x, 0, whiteKeyWidth, whiteKeyHeight);
           } else {
               if (p >= (offset * 12) - 7 && p < ((offset + 1) * 12)) {
                   g.setColor(Color.white);
               } else {
                   g.setColor(Color.gray);
               }
               g.fill3DRect(x, 0, whiteKeyWidth, whiteKeyHeight, true);
           }
           if(displayKeyPressed[p]) {
               g.setColor(lightColor);
               g.fillRect(x, 0, whiteKeyWidth, whiteKeyHeight);
           }
           g.setColor(Color.gray);
//           g.drawString("" + index / 10, x + 8, whiteKeyHeight - 40);
//           g.drawString("" + index % 10, x + 8, whiteKeyHeight - 24);
           g.drawString(NOTE_NAMES[p % 12], x + 8, whiteKeyHeight - 40);
           g.drawString(NOTE_NAMES2[p % 12], x + 8, whiteKeyHeight - 24);
           if(p == 0 || p == 12 || p == 24 || p == 36 || p == 48 || 
              p == 60|| p == 72 || p == 84 || p == 96 || p == 108|| p == 120) {
               g.setColor(Color.green);
               g.drawString("C" + countC, x + 7, whiteKeyHeight - 5);
               countC ++;
           }

           final int s=steps[stepIndex++%steps.length];
           p += s;
           x += whiteKeyWidth;
           index ++;
        }

        //[ draw black key
        stepIndex = 0;
        x = 0;
        index = 1;
        for(int p = 0; p < keyPressed.length; ) {
           final int s=steps[stepIndex++%steps.length];
           if(s == 2) { //[ draw black key
              if(p+1>=keyPressed.length) break;
              if(keyPressed[p+1]) {
                  g.setColor(Color.orange);
                  g.fillRect(x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight);   
              } else {
                  g.setColor(Color.black);
                  g.fillRect(x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight);
              }
              if(displayKeyPressed[p+1]) {
                  g.setColor(lightColor);
                  g.fillRect(x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight);
              }
           }
           p += s;
           x += whiteKeyWidth;
        }
        g.translate(-left, -top);
    }

    public void setKeys(HashMap<Integer, Integer> map) {
        // Maps notes to keys
        map.put(65, 53);// 'A' 1
        map.put(87, 54);// 'W' 2
        map.put(83, 55);// 'S' 3
        map.put(69, 56);// 'E' 4
        map.put(68, 57);// 'D' 5
        map.put(82, 58);// 'R' 6
        map.put(70, 59);// 'F' 7
        map.put(71, 60);// 'G' 8 //middle C
        map.put(89, 61);// 'Y' 9
        map.put(72, 62);// 'H'10
        map.put(85, 63);// 'U'11
        map.put(74, 64);// 'J'12
        map.put(75, 65);// 'K'13
        map.put(79, 66);// 'O'14
        map.put(76, 67);// 'L'15
        map.put(80, 68);// 'P'16
        map.put(59, 69);// ';'17
        map.put(91, 70);// '['18
        map.put(93, 71);// ']'19
      }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyChar() == '+') {
            offset ++;
            if(offset >= (NUM_PITCH/12)) offset= (NUM_PITCH/12);
            this.repaint();
            return ;
         } else if(e.getKeyChar()=='-') {
            offset --;
            if(offset < 0) offset = 0;
            this.repaint();
            return ;
         }
        if(93 < e.getKeyCode() || e.getKeyCode() < 59) {
            System.out.println("KeyboardPane::keyPressed :" + e.getKeyCode() + ":" + KeyEvent.getKeyText(e.getKeyCode()));
            return ;
        }
        //play a note if the key has an assigned note
        if (this.notes.containsKey(e.getKeyCode())) {
            //Adds key to the keyDown hashmap if key is pressed
            if (this.keyDown[e.getKeyCode()]) {
                return;
            } else {
                this.keyPressed[this.notes.get(e.getKeyCode())] = true;
                this.repaint();
                System.out.println(e.getKeyCode() + "," + this.notes.get(e.getKeyCode()));
                // channel[0].noteOn( 60, 80);ピアノを60（中央のド）を80の強さで演奏します。
                //Plays a note with specified note number
                this.midChannel[this.midiChannel].noteOn(this.notes.get(e.getKeyCode()), 127);
//                this.midChannel[7].noteOn(this.notes.get(e.getKeyCode()), 127);
//                this.midChannel[10].noteOn(this.notes.get(e.getKeyCode()), 127);
            }
            this.keyDown[e.getKeyCode()] = true;
        } else {
            System.out.println("KeyboardPane::keyPressed :" + e.getKeyCode() + ":" + KeyEvent.getKeyText(e.getKeyCode()));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(93 < e.getKeyCode() || e.getKeyCode() < 59) {
            System.out.println("KeyboardPane::keyReleased:" + e.getKeyCode() + ":" + KeyEvent.getKeyText(e.getKeyCode()));
            return ;
        }

        // If the key played a note, stop that note
        if (notes.containsKey(e.getKeyCode())) {
            // If the key played a note, stop that note
            this.midChannel[this.midiChannel].noteOff(this.notes.get(e.getKeyCode()), 64);
//            this.midChannel[7].noteOff(this.notes.get(e.getKeyCode()), 64);
//            this.midChannel[10].noteOff(this.notes.get(e.getKeyCode()), 64);

            if (this.keyDown[e.getKeyCode()]) {
                //Remove the key from the keyDown hashmap if key is released
                this.keyDown[e.getKeyCode()] = false;
            }
            this.keyPressed[this.notes.get(e.getKeyCode())] = false;
            this.repaint();
        } else {
            System.out.println("KeyboardPane::keyReleased:" + e.getKeyCode() + ":" + KeyEvent.getKeyText(e.getKeyCode()));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(!isMouseEnabled) return;
        final int index=getKeyIndex(e.getX(), e.getY());
        if(index<0 || index>=keyPressed.length) return;
        pressKey(index, 100, true);
        repaint();
        lastIndex=index;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(!isMouseEnabled) return;

        for(int i=0; i < keyPressed.length; i++) {
            if(keyPressed[i]) {
                releaseKey(i, 100, true);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public int getKeyIndex(int ex, int ey) {
        ex-=left;
        ey-=top;
        int stepIndex=0;
        int x=0;
        
        //[ black key
        
        for(int p=0; p<keyPressed.length;) {
           final int s=steps[stepIndex++%steps.length];
           if(s==2) { //[ draw black key
              if(p+1>=keyPressed.length) break;
              final Rectangle r=new Rectangle(x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight);
              if(r.contains(ex, ey)) return p+1;
           }
           p+=s;
           x+=whiteKeyWidth;
        }
        
        //[ white key
        stepIndex=0;
        x=0;
        for(int p=0; p<keyPressed.length;) {
           final Rectangle r=new Rectangle(x, 0, whiteKeyWidth, whiteKeyHeight);
           if(r.contains(ex, ey)) return p;
           final int s=steps[stepIndex++%steps.length];
           p+=s;
           x+=whiteKeyWidth;
        }
        return -1;
     }
    public void pressKey(int pitch, int velocity, boolean sing) {
        if(!sing) {
           displayKeyPressed[pitch]=true;
        } else {
           keyPressed[pitch] = true;
           //System.err.println("press "+Util.getPitchName(pitch)+"("+pitch+")");
           
           
           //[ note on
           final ShortMessage sm = new ShortMessage();
           try {
               sm.setMessage(ShortMessage.NOTE_ON, this.midiChannel, pitch, velocity);
           } catch (InvalidMidiDataException e) {
               e.printStackTrace();
           }
           this.midChannel[this.midiChannel].noteOn(pitch, velocity);
           
//           //outDeviceReceiver.send(sm, 0);
//           final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
//           if(vf==null) return;
//           final ScoreView sheet=vf.scoreView;
//
//           OutDeviceManager.instance.send(sm, 0);
//           if(isRecording) sheet.send(sm, 0);
        }
        System.out.println("repaint");
        repaint();
     }
     public void releaseKey(int pitch, int velocity, boolean sing) {
        if(!sing) {
           displayKeyPressed[pitch]=false;
        } else {
           keyPressed[pitch]=false;
           //System.err.println("release "+Util.getPitchName(pitch)+"("+pitch+")");
           
           final ShortMessage sm = new ShortMessage();
           try {
               sm.setMessage(ShortMessage.NOTE_OFF, this.midiChannel, pitch, velocity);
           } catch (InvalidMidiDataException e) {
               e.printStackTrace();
           }
           this.midChannel[this.midiChannel].noteOff(pitch, velocity);
           
//           //outDeviceReceiver.send(sm, 0);
//           final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
//           if(vf==null) return;
//           final ScoreView sheet=vf.scoreView;
//           
//           OutDeviceManager.instance.send(sm, 0);
//           if(isRecording) sheet.send(sm, 0);
        }
        repaint();
     }

    public void close() {
        if (this.syn != null)
            this.syn.close();
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if(message instanceof ShortMessage) {
            final ShortMessage sm=(ShortMessage)message;
            if(sm.getCommand()==0x90 && sm.getData2()!=0) { //note on
                this.repaint();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        pressKey(sm.getData1(), sm.getData2(), true);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            } else if(sm.getCommand()==0x80 || (sm.getCommand()==0x90 && sm.getData2()==0)) { //note off
                this.repaint();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        releaseKey(sm.getData1(), sm.getData2(), true);
                    }
               });
            }
         }
    }

    @Override
    public void paintComponents(Graphics g) {
        System.out.println("paintComponents");
        super.paintComponents(g);
    }

}
