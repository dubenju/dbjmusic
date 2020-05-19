package dbjmusic;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PlayBar  extends JToolBar {
    final JSlider progress;
    public PlayBar() {
        progress=new JSlider(0, 0, 0);
        progress.setEnabled(false);
        progress.addChangeListener(new ChangeListener() {
           public void stateChanged(ChangeEvent e) {
//              //if(progress.getValueIsAdjusting()) return;
//              if(!MainFrame.player.isPlaying()) {
//                 MainFrame.player.setMicrosecondPosition(progress.getValue());
//                 timeButton.setTime(MainFrame.player.getMicrosecondPosition());
//                 //System.err.println(progress.getValue());
//              }
           }
        });
        
        //[ speed
        final JButton tempoLabel=new JButton("1.00x");
        final int DEFAULT_VALUE=0;
        final JSlider tempoFactorSlider=new JSlider(-10, 10, DEFAULT_VALUE);
        tempoFactorSlider.setPreferredSize(new Dimension(100, 30));
        tempoFactorSlider.setMajorTickSpacing(1);
        //tempoFactorSlider.setMinorTickSpacing(1);
        tempoFactorSlider.setSnapToTicks(true);
        tempoFactorSlider.addChangeListener(new ChangeListener() {
           public void stateChanged(ChangeEvent e) {
              final float factor=(float) Math.pow(2, tempoFactorSlider.getValue()/10.0); 
              //tempoFactorSlider.getValue()/100.0f;
              tempoLabel.setText(new DecimalFormat("0.00x").format(factor));
//              MainFrame.player.setTempoFactor(factor);
           }
        });
        tempoLabel.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
              tempoFactorSlider.setValue(DEFAULT_VALUE);
           }
        });
    }
}
