package dbjmusic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.plaf.ColorUIResource;

public class DbjMusic {
    private GoThread t = null;

    private Runnable run = null; // 更新组件的线程
    private Component component = null;
    private KeyboardPane panel = null;
//    private JScrollPane jsp = null;

    public DbjMusic() {
        final JPanel panelBtn = new JPanel();
        JButton dftBtn = new JButton("DFT");
        dftBtn.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            System.out.println("DFT begin");
            System.out.println("DFT end size:" + panel.getComponentCount());
//            t = new GoThread();
//            t.start();
            try {
                Sequence sequence = MidiSystem.getSequence(new File("./test/test.mid"));
                int trackNumber = 0;
                for (Track track :  sequence.getTracks()) {
                    trackNumber++;
                    for (int i=0; i < track.size(); i++) { 
                        MidiEvent event = track.get(i);
                        System.out.print("@" + event.getTick() + " ");
                        MidiMessage message = event.getMessage();
                        if (message instanceof ShortMessage) {
                            ShortMessage sm = (ShortMessage) message;
                            System.out.print("Channel: " + sm.getChannel() + " ");
                            if (sm.getCommand() == 0x90) {
                                long        lTicks = event.getTick();
                                panel.send(sm, lTicks);
                                System.out.println();
                            } else if (sm.getCommand() == 0x80) {
                                long        lTicks = event.getTick();
                                panel.send(sm, lTicks);
                                System.out.println();
                            } else {
                                System.out.println("Command:" + sm.getCommand());
                            }
                        } else if (message instanceof  MetaMessage) {
                            MetaMessage mm = (MetaMessage) message;
                            System.out.println(Test.printMetaMessage(mm));
                        } else {
                            System.out.println("Other message: " + message.getClass());
                        }
                    }
                }
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
          }
        });
        
        final PlayBar playBar = new PlayBar();
        playBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        playBar.setVisible(true);
        
        final JPanel bottomToolPane=new JPanel();
        bottomToolPane.setLayout(new BoxLayout(bottomToolPane, BoxLayout.PAGE_AXIS));
        bottomToolPane.add(playBar);

        final JFrame frame = new JFrame();
        this.panel = new KeyboardPane();

//        this.jsp = new JScrollPane(this.panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
//        JScrollBar jsb= this.jsp.getHorizontalScrollBar();//
//        jsb.setValue(jsb.getMaximum());//

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.panel, BorderLayout.CENTER);
        contentPane.add(panelBtn, BorderLayout.LINE_END);
        contentPane.add(bottomToolPane, BorderLayout.SOUTH);

        panelBtn.setLayout(new GridLayout(5, 1));
        panelBtn.add(dftBtn);

        frame.setLocation(0, 0);
        frame.setTitle("Qin");
//        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setExtendedState(JFrame.MAXIMIZED_HORIZ);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                panel.close();
                System.exit(0);
            }

         });
        frame.setVisible(true);


        this.run = new Runnable() { //实例化更新组件的线程
            public void run() {
              component.repaint();
            }
        };
        System.out.println("DbjMusic is Created.");
    }


//    public void paintComponent(Graphics g) {
//      //Clear the window
//      g.clearRect(0, 0, getWidth(), getHeight());
//      
//      boolean caps = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
//      if(!caps){
//        g.drawString("Turn Capslock on!", 50, getHeight() / 3);
//      }
//      
//      //Draw keys that are held down
//      g.drawString("The key pressed is " + getKeyDown(), 50, getHeight() / 2);
//      
//      g.drawString("Keys:"+notes.keySet().toString(), 50, getHeight() * 2 / 3);
//      repaint();
//    }

//    public String getKeyDown() {
//      //this function returns the keys that are currently pressed
//      String keys = "";
//      for (int i = 0; i < keyDown.length; i++) {
//        if (keyDown[i]) {
//          keys += KeyEvent.getKeyText(i);
//        }
//      }
//
//      return keys;
//    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Thread thread = Thread.currentThread();
                System.out.println(thread.getId() + ":" + thread.getName());
                @SuppressWarnings("unused")
                DbjMusic fg = new DbjMusic();
            }
        });
    }

    private void go() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(run);//将对象排到事件派发线程的队列中
    }

    class GoThread extends Thread {
        public void run() {
          Thread thread = Thread.currentThread();
          System.out.println("GoThread::" + thread.getId() + ":" + thread.getName());
          for (int i = 0; i < panel.getComponentCount(); i ++) {
              component = panel.getComponent(i);
              //do something...
              go();
          }
          panel.repaint();
        }
    }
}
