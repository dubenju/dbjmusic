package dbjmusic;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

public class OutputManager implements Receiver {
    private MidiDevice outDevice=null;
    private Receiver outDeviceReceiver=null;
    private final List<Receiver> receivers=new ArrayList<Receiver>();

    private volatile static OutputManager singleton;  //1:volatile修饰
    private OutputManager (){
        try {
            setOutDevice(MidiSystem.getSynthesizer());
         } catch(MidiUnavailableException e) {
            e.printStackTrace();
         }
    }
    public static OutputManager getInstance() { 
        if (singleton == null) {  //2:减少不要同步，优化性能
            synchronized (OutputManager.class) {  // 3：同步，线程安全
                if (singleton == null) { 
                    singleton = new OutputManager();  //4：创建singleton 对象
                }
            } 
        }
        return singleton;  
    }

    @Override
    public synchronized void send(MidiMessage message, long timeStamp) {
        if(outDeviceReceiver!=null) outDeviceReceiver.send(message, timeStamp);
        for(int i=0; i<receivers.size(); i++) {
           receivers.get(i).send(message, timeStamp);
        }
    }
    @Override
    public synchronized void close() {
    }
    public synchronized void setOutDevice(MidiDevice device) throws MidiUnavailableException {
        if(device==null) throw new IllegalArgumentException();
        //clean old
        if(outDeviceReceiver!=null) outDeviceReceiver.close();
        if(outDevice!=null && outDevice.isOpen()) outDevice.close();
        
        //[ set new
        outDevice=device;
        outDevice.open();
        outDeviceReceiver=outDevice.getReceiver();
//        for(Transmitter t: registeredTransmitters) {
//           t.setReceiver(outDeviceReceiver);
//        }
     }
    public synchronized void addReceiver(Receiver receiver) {
        receivers.add(receiver);
     }
}
