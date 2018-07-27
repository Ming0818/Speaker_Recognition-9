  import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
 
 
@SuppressWarnings("unused")
class Frame1 implements ActionListener
{
     
    JPanel panel;
    JButton pres;
     
    Frame1()
    {
     
    panel = new JPanel(new FlowLayout(5,150,150));  
    pres = new JButton(" PRES ");
    pres.setSize(100,100);
    pres.addActionListener(this);
    panel.add(pres);
         
        JFrame frame = new JFrame("FRAME");
        frame.add(panel);
        frame.setSize(400,400);
        frame.setVisible(true);
    }
     
public void actionPerformed(ActionEvent e)
    {
        if(e.getSource()==pres)
        {
             
         
        }
    }
     
     
    public static void main(String args[])
    {
        new Frame1();
    }
}