package DemoOfVoicePrints;
/* File DemoOfVoicePrints.java

This program is for real-time demonstration of voiceprints technologies
(aka speaker recogntion).

By Qiguang Lin
March 10, 2018

A GUI appears on the screen containing the following buttons:
Exit
Save
Train    (talk to the server, training)
Test     (talk to the server, testing)
Rec'd   (to record audio for training and trsting)
Play     (enabled after first clicking on Record)
Stop     (toggling text of Record or Playback)

Input data from microphone is captured and saved in a ByteArrayOutputStream object
      when the user clicks the Record button.
The audio data is also saved into a file whose name is automatically created,
      based on the current time.
Data capture stops when the user clicks the Stop button. Playback begins when the
      user clicks the Playback button.
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

import java.net.*;  // socket programming

@SuppressWarnings("serial")
public class DemoOfVoicePrints extends JFrame implements ActionListener, WindowListener, WindowStateListener, MouseListener {
    protected float elapseTime=0;
    protected String serverAnswer=" ";
    
    protected int sampleRate=8000;  // or 16000
    protected String hostName="127.0.0.1";
	protected AudioFormat adFormat;
    protected Properties props=null;
    protected boolean stopRecord   = false;
    protected boolean stopPlayback = false;
    protected ByteArrayOutputStream byteArrayOutputStream;
    protected AudioFormat audioFormat;
    protected TargetDataLine targetDataLine;
    protected AudioInputStream audioInputStream;
    protected SourceDataLine sourceDataLine;
	ByteArrayOutputStream byteOutputStream;
	AudioInputStream InputStream;
	boolean stopaudioCapture = false;
    protected Font font = new Font("Serif", Font.PLAIN, 22);
    protected JTextArea transcript;
    protected JTextArea transcribed;
    protected JScrollPane jsp;

    private JButton exitBtn, saveBtn, playBtn, recordBtn, trainBtn, testBtn;
    private JTextField spkrTextField;
    private JLabel spkrLabel, resultLabel;

    private boolean PlaybackClicked=false;

    private int topMargin = 280;
    private int bottomMargin = 100;
    private int leftMargin = 120;
    private int rightMargin = 120;
    private long counter=0;
    private int width1=-1;    // the width and height of the paint for drawing
    private int height1=-1;
    private int width2=-1;    // the width and height of the paint for drawing
    private int height2=-1;
    private int BOS = -1;
    private int EOS = -1;
    @SuppressWarnings("unused")
	private int iBOS=-1;
    @SuppressWarnings("unused")
	private int iEOS=-1;
    private int xOrig;        // waveform origin
    private int yOrig;
    private int m_value;      // to store the max amplitude
    private float xScale;
    private float yScale;
    private short [] buffer = null;

    protected Container container = null;

    public static int byteswap(int i) {  // hope the input is > 0 - unsigned
	return ((i << 24)|((i << 8)&0x00ff0000)|((i >> 8)&0x0000ff00)|(i >> 24));
	
    }

    public static short byteswap(short s) {
	byte b1 = (byte) (s & 0xff);
	byte b2 = (byte) ((s >> 8) & 0xff);
	return (short) ((b1 << 8) | b2);
    }

    public DemoOfVoicePrints(Properties prps) {
	props = prps;
	
	exitBtn  = new JButton(props.getProperty("ExitButtonLabel", "Exit"));
	saveBtn  = new JButton(props.getProperty("SaveButtonLabel", "Save"));
	playBtn  = new JButton(props.getProperty("PlayButtonLabel", "Play"));
	trainBtn = new JButton(props.getProperty("TrainButtonLabel", "Train"));
	testBtn  = new JButton(props.getProperty("TestButtonLabel", "Test"));
	recordBtn= new JButton(props.getProperty("RecordButtonLabel", "Record"));

	exitBtn.setFont(font);
	saveBtn.setFont(font);
	playBtn.setFont(font);
	trainBtn.setFont(font);
	testBtn.setFont(font);
	recordBtn.setFont(font);

	exitBtn.setEnabled(true);
	saveBtn.setEnabled(false);
	playBtn.setEnabled(false);
	trainBtn.setEnabled(false);
	testBtn.setEnabled(false);
	recordBtn.setEnabled(true);

	exitBtn.addActionListener(this);
	saveBtn.addActionListener(this);
	playBtn.addActionListener(this);
	trainBtn.addActionListener(this);
	testBtn.addActionListener(this);
	recordBtn.addActionListener(this);

	container = getContentPane();
	
	setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

	JPanel p1 = new JPanel();
	p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
	JLabel title= new JLabel("Zibot Voice Printing Technologies");
	title.setFont(new Font("Serif", Font.PLAIN, 22));
	title.setForeground(Color.blue);
	p1.add(title);
	p1.add(Box.createRigidArea(new Dimension(0,50)));
	container.add(p1);

	p1 = new JPanel();
	p1.setOpaque(true);
	p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
	p1.add(exitBtn);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	p1.add(saveBtn);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	p1.add(playBtn);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	p1.add(trainBtn);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	p1.add(testBtn);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	p1.add(recordBtn);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	container.add(p1);

	p1 = new JPanel();
	p1.setOpaque(true);
	p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
	spkrLabel = new JLabel("Speaker label:");
	p1.add(spkrLabel);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	spkrTextField = new JTextField("<optional>");
	//spkrTextField.setMinimumSize(30,1);
	//p1.add(spkrTextField);
	container.add(p1);

	p1 = new JPanel();
	p1.setOpaque(true);
	p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
	resultLabel = new JLabel("VoicePrint Result:");
	resultLabel.setText("QL:");
	p1.add(resultLabel);
	container.add(p1);

	setTitle("Zibot Voice Printing");
	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	setSize(800,400);
	setVisible(true);
	setResizable(false);
    }

    
    
    public void fulfillIt() {
	exitBtn.setFont(font);
	saveBtn.setFont(font);
	playBtn.setFont(font);
	trainBtn.setFont(font);
	testBtn.setFont(font);
	recordBtn.setFont(font);

	exitBtn.setEnabled(true);
	saveBtn.setEnabled(false);
	playBtn.setEnabled(false);
	trainBtn.setEnabled(false);
	testBtn.setEnabled(false);
	recordBtn.setEnabled(true);

	exitBtn.addActionListener(this);
	saveBtn.addActionListener(this);
	playBtn.addActionListener(this);
	trainBtn.addActionListener(this);
	testBtn.addActionListener(this);
	recordBtn.addActionListener(this);

	setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	container.add(Box.createRigidArea(new Dimension(0,15)));

	JPanel p1 = new JPanel();
	p1.setOpaque(true);
	p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
	JLabel title= new JLabel("Zibot VoicePrinting Technologies");
	title.setFont(new Font("Serif", Font.PLAIN, 22));
	title.setForeground(Color.blue);
	p1.add(title);
	p1.add(Box.createRigidArea(new Dimension(0,50)));
	container.add(p1);

	p1 = new JPanel();
	p1.setOpaque(true);
	p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
	p1.add(exitBtn);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	p1.add(saveBtn);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	p1.add(playBtn);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	p1.add(trainBtn);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	p1.add(testBtn);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	p1.add(recordBtn);
	p1.add(Box.createRigidArea(new Dimension(0,50)));
	container.add(p1);

	p1 = new JPanel();
	p1.setOpaque(true);
	p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
	spkrLabel = new JLabel("Speaker label:");
	p1.add(spkrLabel);
	p1.add(Box.createRigidArea(new Dimension(10,0)));
	spkrTextField = new JTextField("<optional>");
	//spkrTextField.setMinimumSize(30,1);
	//p1.add(spkrTextField);
	container.add(p1);

	p1 = new JPanel();
	p1.setOpaque(true);
	p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
	resultLabel = new JLabel("VoicePrint Result:");
	resultLabel.setText("QL:");
	p1.add(resultLabel);
	container.add(p1);
    }

    public void actionPerformed(ActionEvent ev) {
	if(ev.getSource()==exitBtn) {
	    System.exit(0);
	} else if(ev.getSource()==saveBtn) {
	    save_audio_to_file();
	} else if(ev.getSource()==trainBtn) {
	    if(trainBtn.getText().equals("Train")) {
		trainBtn.setText("Busy");
		playBtn.setEnabled(false);
		exitBtn.setEnabled(true);
		recordBtn.setEnabled(false);
		saveBtn.setEnabled(false);
		trainBtn.setEnabled(false);
		testBtn.setEnabled(false);
		train();
		//playAudio();
		//save_audio_to_file();
	    }
	} else if(ev.getSource()==testBtn) {
	    if(testBtn.getText().equals("Test")) {
		testBtn.setText("Busy");
		playBtn.setEnabled(false);
		exitBtn.setEnabled(true);
		recordBtn.setEnabled(false);
		saveBtn.setEnabled(false);
		trainBtn.setEnabled(false);
		testBtn.setEnabled(false);
		talk2server();
		playAudio();
	    }
	} else if(ev.getSource()==recordBtn) {
	    if(recordBtn.getText().equals("Recd")) {
		recordBtn.setText("Stop");
		playBtn.setEnabled(false);
		stopRecord = false;
		recordAudio();
	    } else {
		recordBtn.setText("Recd");
		stopRecord = true;
		//targetDataLine.close();
		recordBtn.setEnabled(true);
		playBtn.setEnabled(true);
		playBtn.setText(playBtn.getText());
		saveBtn.setEnabled(true);
		trainBtn.setEnabled(true);
		//if(!serverAnswer.startsWith("-")) update_text_time(serverAnswer);
		/*
		  trainBtn.setText("Busy");
		  playBtn.setEnabled(false);
		  exitBtn.setEnabled(false);
		  recordBtn.setEnabled(false);
		  saveBtn.setEnabled(false);
		  trainBtn.setEnabled(false);
		  testBtn.setEnabled(false);

		//talk2server();
		playAudio();
		*/
		repaint();

	    }

	} else if(ev.getSource()==playBtn) {
	    if(playBtn.getText().equals("Play")) {
		playBtn.setText("Stop");
		recordBtn.setEnabled(false);
		stopPlayback = false;
		PlaybackClicked=true;
		playAudio();
	    } else {
		stopPlayback = true;
		playBtn.setText("Play");
		playBtn.setEnabled(true);
		recordBtn.setEnabled(true);
	    }
	}
    }

    private void update_text_time(String answer) {
	transcribed.setText(answer);
	int at=Float.toString(elapseTime).indexOf('.');
	resultLabel.setText("Transcribed Text ("+Float.toString(elapseTime).substring(0,at+2) +" s)");
    }
    private void save_audio_to_file() {
	Calendar cal = Calendar.getInstance();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
	String currentTime = (sdf.format(cal.getTime())).replace('-', 'T');

	String spkrName = spkrTextField.getText().trim();
	if(spkrName.equals("") || spkrName.equals("<optional>") )
	    spkrName = "Q__";

	String osName = System.getProperty("os.name");
	String currentPath=null;
	if(osName.startsWith("Windows"))
	    currentPath="logs\\" + currentTime.substring(0,11) + "\\";
	else
	    currentPath="logs/" + currentTime.substring(0,11) + "/";

	boolean exists = (new File(currentPath)).exists();
	if(! exists) {
	    boolean success = (new File(currentPath)).mkdirs();
	    if(! success) {
		JOptionPane.showMessageDialog(this, 
			    "WARNING: Cannot create directory, "+currentPath+" No save made.", "ALERT", 
			    JOptionPane.WARNING_MESSAGE);
		return;
	    }
	}

	currentPath += spkrName + currentTime;
	byte audioData[] = byteArrayOutputStream.toByteArray();
	DataOutputStream dos = null;
	try {
	    dos = new DataOutputStream(new FileOutputStream(currentPath+".wav"));
	    dos.writeByte('R');dos.writeByte('I');dos.writeByte('F');dos.writeByte('F');
	    int tmp = audioData.length + 36;
	    dos.writeInt(byteswap((int) tmp));
	    dos.writeByte('W');dos.writeByte('A');dos.writeByte('V');dos.writeByte('E');
	    dos.writeByte('f');dos.writeByte('m');dos.writeByte('t');dos.writeByte(' ');
	    dos.writeInt(byteswap((int) 16));
	    dos.writeShort(byteswap((short) 1));  // PCM
	    dos.writeShort(byteswap((short) 1));  // Number of Channel
	    dos.writeInt(byteswap(sampleRate));   // 8kHz
	    dos.writeInt(byteswap((int) sampleRate*2)); // For 8kHz
	    dos.writeShort(byteswap((short) 2));
	    dos.writeShort(byteswap((short) 16));
	    dos.writeByte('d');dos.writeByte('a');dos.writeByte('t');dos.writeByte('a');
	    tmp = audioData.length;
	    dos.writeInt(byteswap((int) tmp));

	    for(int i=0; i < audioData.length; i++) {
		dos.writeByte(audioData[i]);
	    }
	    dos.close();
	}
	catch(IOException ex) {
	    JOptionPane.showMessageDialog(this,
			props.getProperty("ErrorCannotSaveAudio", "Error in saving audio file: ") + ex,
			"ALERT", JOptionPane.ERROR_MESSAGE);
	    return;
	}
    }

    private void setScales() {
        Dimension size = getSize();
        // to handle resize of Frame
        if(width1 != size.width || height1 != size.height) {
            BOS=-1;
            EOS=-1;
            iBOS=-1;
            iEOS=-1;
            width1=size.width;
            height1=size.height;
        }
	width2  = width1 -leftMargin-rightMargin;
	height2 = height1-topMargin-bottomMargin;
	OUT("w1="+width1 + " h1=" + height1 + " w2="+width2 + " h2=" + height2);
	xOrig = leftMargin;
	yOrig = (int)(height2*1.25)/2 + topMargin;
	xScale=(float)  width2/(1.01F*buffer.length);
        yScale=(float) height2/(2.25F*m_value);
	OUT("xO="+xOrig + " yO=" + yOrig + " xS="+xScale + " yS=" + yScale);
    }

    public void paint(Graphics g) {
	OUT("Inside paint, counter=" +counter++);
	if(byteArrayOutputStream==null)
	    return;
	byte audioData[] = byteArrayOutputStream.toByteArray();
	OUT("audioData.length="+ audioData.length);
	if(audioData.length==0)
	    return;

	buffer  = new short [audioData.length/2];
	// Convert byte to short fot plotting
	int max_value=0, min_value=0;
	m_value=0;
	for(int i=0, j=0; i<audioData.length; i+=2, j++) {
	    buffer[j] = (short) ((audioData[i]) | (audioData[i+1]<<8));
	    if(j>90 && j<100)
		OUT("wave["+j+"]="+buffer[j]);
	    if(buffer[j] > max_value)
		max_value =buffer[j] ;
	    else if(buffer[j] < min_value)
		min_value = buffer[j];
	}
	m_value = Math.max(Math.abs(max_value), Math.abs(min_value));
	// g.drawLine(50, 100, 200, 250);
	setScales();

	// faked setbackground :-(
	g.setColor(Color.WHITE);
	g.fillRect(leftMargin, topMargin, width2, (int)(height2*1.25));
	g.setFont(font);
	g.setColor(Color.BLUE);
	//setBackground(Color.WHITE);
        int x1, x2, y1, y2;

        x1 = xOrig;
        y1 = yOrig - (int)(yScale*buffer[0]);
        Random generator = new Random( 19580427 );
        for(int i=1; i<buffer.length; i++) {
            x2 = xOrig + (int)(i*xScale);
            short tmp = buffer[i];
            if(x1 < BOS || (EOS > 0 && x1 > EOS))
            {
                tmp = (short)(generator.nextInt(20) - 10); // thus ranging from -10 to +10.
            }
            buffer[i] = tmp;
            y2 = yOrig - (int)(yScale*tmp);
            g.drawLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
	    if(i>90 && i<100) OUT("x1="+x1 + " y1="+y1 + " tmp=" +tmp);
        }
    }

    // Implement the inherited interface methods. Many of them need only an empty block
    public void windowClosing(WindowEvent e)
    {
        ;
    }
    public void windowClosed(WindowEvent e)
    {
        ;
    }
    public void windowOpened(WindowEvent e)
    {
        ;
    }
    public void windowIconified(WindowEvent e)
    {
        ;
    }
    public void windowDeiconified(WindowEvent e)
    {
        ;
    }
    public void windowActivated(WindowEvent e)
    {
        repaint();
    }
    public void windowDeactivated(WindowEvent e)
    {
        repaint();
    }
    public void windowGainedFocus(WindowEvent e)
    {
        repaint();
    }
    public void windowLostFocus(WindowEvent e)
    {
        ;
    }
    public void windowStateChanged(WindowEvent e)
    {
        BOS=-1;
        EOS=-1;
        iBOS=-1;
        iEOS=-1;
    }
    public void mousePressed(MouseEvent ev)
    {
        //setScales();
        if(ev.isMetaDown()) 
        {
            EOS = ev.getX();
            iEOS = (int) ((EOS-xOrig)/xScale);
        }
        else
        {
            BOS = ev.getX();
            iBOS = (int) ((BOS-xOrig)/xScale);
        }
        repaint();
    }

    // Implement the inherited interface methods. Many of them need only an empty block
    public void mouseExited(MouseEvent ev)
    {
        ;
    }
    public void mouseEntered(MouseEvent ev)
    {
        ;
    }
    public void mouseClicked(MouseEvent ev)
    {
        ;
    }
    public void mouseReleased(MouseEvent ev)
    {
        ;
    }
    public void itemStateChanged(ItemEvent ev)
    {
        ;
    }

    // This method captures audio input from mic and saves it in a ByteArrayOutputStream object.
    private void recordAudio() {
		try {
			adFormat = getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, adFormat);
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(adFormat);
			targetDataLine.start();
	
			Thread recordThread = new Thread(new recordThread());
			recordThread.start();
		} catch (Exception e) {
			StackTraceElement stackEle[] = e.getStackTrace();
			for (StackTraceElement val : stackEle) {
				System.out.println(val);
			}
			System.exit(0);
		}
	}

    // This method plays back the audio data that has been saved in the ByteArrayOutputStream
    private void playAudio() {
	try {
	    //Get everything set up for playback.
	    //Get the previously-saved data into a byte array object.
	    byte audioData[] = byteArrayOutputStream.toByteArray();
	    //Get an input stream on the byte array containing the data
	    InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
	    AudioFormat audioFormat = getAudioFormat();
	    audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, 
						    audioData.length/audioFormat. getFrameSize());
	    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
	    sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
	    sourceDataLine.open(audioFormat);
	    sourceDataLine.start();
	    Thread playThread = new Thread(new PlayThread());
	    playThread.start();
	} catch(Exception e) {
	    JOptionPane.showMessageDialog(this, props.getProperty("ErrorLiteral", "Error") + "- playAudio: " + e,
					  "ALERT", JOptionPane.ERROR_MESSAGE);
	    System.exit(2);
	}
    }

    // This method creates and returns an AudioFormat object for a given set of format parameters.
    private AudioFormat getAudioFormat() {
	float sampleRateInFloat = (float) sampleRate;	//8000,11025,16000,22050,44100
	int sampleSizeInBits = 16;	//8,16
	int channels = 1;       	//1,2
	boolean signed = true;   	//true,false
	boolean bigEndian = false;	//true,false
	return new AudioFormat(sampleRateInFloat, sampleSizeInBits, channels, signed, bigEndian);
    }

    // to capture data from microphone
    class recordThread extends Thread {

		byte tempBuffer[] = new byte[10000];
	
		public void run() {
	
			byteOutputStream = new ByteArrayOutputStream();
			stopaudioCapture = false;
			try {
				DatagramSocket clientSocket = new DatagramSocket(8786);
				InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
				while (!stopaudioCapture) {
					int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
					if (cnt > 0) {
						DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, IPAddress, 9786);
						clientSocket.send(sendPacket);
						byteOutputStream.write(tempBuffer, 0, cnt);
					}
				}
				byteOutputStream.close();
			} catch (Exception e) {
				System.out.println("CaptureThread::run()" + e);
				System.exit(0);
			}
		}
	}
    // to playback the data
    class PlayThread extends Thread {
	byte tempBuffer[] = new byte[1024];
	public void run() {
	    SourceDataLine line = null;
	    DataLine.Info  info = null;
	    try {
		info = new DataLine.Info(SourceDataLine.class, audioFormat);
	    } catch(Exception e) {
		JOptionPane.showMessageDialog(null, props.getProperty("ErrorLiteral", "Error") + " - info: " + e,
					      "ALERT", JOptionPane.ERROR_MESSAGE);
		return;
	    }
	    try {
		line = (SourceDataLine) AudioSystem.getLine(info);
		// The line is there, but not yet ready to receive audio data. We have to open the line.
		line.open(audioFormat);
	    } catch(LineUnavailableException e) {
		JOptionPane.showMessageDialog(null, props.getProperty("ErrorLiteral", "Error") + " - line: " + e,
					      "ALERT", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	    } catch(Exception e) {
		JOptionPane.showMessageDialog(null, props.getProperty("ErrorLiteral", "Error") + " - line: " + e,
					      "ALERT", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	    }

	    // Still not enough. The line now can receive data, but will not pass them on to
	    // the audio output device (which means to your sound card). This has to be activated.
	    line.start();

	    // The line is finally prepared. Now comes the real job: we have to write data to the line. We do this
	    // in a loop. First, we read data from the AudioInputStream to a buffer. Then, we write from this
	    // buffer to the Line. This is done until the end of the file is reached, which is detected by a
	    // return value of -1 from the read method of the AudioInputStream.
	    int cnt = 0;
	    try {
		stopPlayback = false;
		while((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1 && !stopPlayback) {
		    if(cnt > 0) line.write(tempBuffer, 0, cnt);
		    else break;
		}
		line.drain(); // Wait until all data are played.
		line.close(); // All data are played. We can close the shop.
		playBtn.setText("Play");
		if(PlaybackClicked==true) recordBtn.setEnabled(true);
		PlaybackClicked=false;
	    } catch(Exception e) {
		JOptionPane.showMessageDialog(null, props.getProperty("ErrorLiteral", "Error") + " - playback: " + e,
					      "ALERT", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	    }
	}
    }

    public static final byte[] intToByteArray(int value) {
	//return new byte[] {(byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)value};
	return new byte[] {(byte)value, (byte)(value >>> 8), (byte)(value >>> 16), (byte)(value >>> 24)};
    }
    public static final byte[] shtToByteArray(short value) {
        //return new byte[] {(byte)(value >>> 8), (byte)value};
        return new byte[] {(byte)value, (byte)(value >>> 8)};
	}
	
	private void train() {
		try {
			adFormat = getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, adFormat);
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(adFormat);
			targetDataLine.start();
	
			Thread trainThread = new Thread(new trainThread());
			trainThread.start();
		} catch (Exception e) {
			StackTraceElement stackEle[] = e.getStackTrace();
			for (StackTraceElement val : stackEle) {
				System.out.println(val);
			}
			System.exit(0);
		}
	}

	class train extends Thread {

		byte tempBuffer[] = new byte[10000];
	
		public void run() {
	
			byteOutputStream = new ByteArrayOutputStream();
			stopaudioCapture = false;
			try {
				DatagramSocket clientSocket = new DatagramSocket(8786);
				InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
				while (!stopaudioCapture) {
					int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
					if (cnt > 0) {
						DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, IPAddress, 9786);
						clientSocket.send(sendPacket);
						byteOutputStream.write(tempBuffer, 0, cnt);
					}
				}
				byteOutputStream.close();
				clientSocket.close();
			} catch (Exception e) {
				System.out.println("CaptureThread::run()" + e);
				System.exit(0);
			}
		}
	}	
	
	
    private void talk2server() {
	try {
	    Thread serverThread = new Thread(new serverThread());
	    serverThread.start();
	} catch(Exception e) {
	    JOptionPane.showMessageDialog(this, props.getProperty("ErrorLiteral", "Error") + "- serverThread: " + e,
					  "ALERT", JOptionPane.ERROR_MESSAGE);
	    return;
	}
	return;
	}
	class trainThread extends Thread {
			
	}

    class serverThread extends Thread {
	public void run() {
	    Socket socket = null;
	    PrintWriter out = null;   // text data only
	    BufferedReader in = null; // text data only

	    // http://www.idevelopment.info/data/Programming/java/networking/JavaNetworkingProgrammingOverview.html
	    // DataInputStream is = null;  // for both binary and text data
	    BufferedReader is = null;
	    DataOutputStream os = null; // for both binary and text data
	    String hostName=props.getProperty("hostName", "127.0.0.1");

	    serverAnswer = "-1";
	    try {
		socket = new Socket(hostName, 8786);
		// out = new PrintWriter(socket.getOutputStream(), true);
		// in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		// is = new DataInputStream(socket.getInputStream());
		// os = new DataOutputStream(socket.getOutputStream());
		// is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		is = new BufferedReader(new InputStreamReader(new BufferedInputStream(socket.getInputStream())));
		os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	    } catch (UnknownHostException e) {
		System.err.println("Don't know about host (" + hostName + "): "+ e);
		return;
	    } catch (IOException e) {
		System.err.println("Couldn't get I/O for the connection to" + e);
		// If connect refused, check the port number!
		return;
	    }

	    byte audioData[] = byteArrayOutputStream.toByteArray();

	    String str1 = "content-length="+audioData.length;
	    str1 = str1 + "&audio=";
	    ByteArrayOutputStream byteArrayOutputStream2 = null;
	    try {
		byteArrayOutputStream2 = new ByteArrayOutputStream();
		byteArrayOutputStream2.write(str1.getBytes(), 0, str1.length());
		byteArrayOutputStream2.write("RIFF".getBytes(), 0, 4);
		byteArrayOutputStream2.write(intToByteArray(audioData.length + 36), 0, 4);
		byteArrayOutputStream2.write("WAVE".getBytes(), 0, 4);
		byteArrayOutputStream2.write("fmt ".getBytes(), 0, 4);
		byteArrayOutputStream2.write(intToByteArray(16), 0, 4);
		byteArrayOutputStream2.write(shtToByteArray((short)1), 0, 2);
		byteArrayOutputStream2.write(shtToByteArray((short)1), 0, 2);
		byteArrayOutputStream2.write(intToByteArray(sampleRate), 0, 4);
		byteArrayOutputStream2.write(intToByteArray(sampleRate*2), 0, 4);
		byteArrayOutputStream2.write(shtToByteArray((short)2), 0, 2);
		byteArrayOutputStream2.write(shtToByteArray((short)16), 0, 2);
		byteArrayOutputStream2.write("data".getBytes(), 0, 4);
		byteArrayOutputStream2.write(intToByteArray(audioData.length), 0, 4);

		byteArrayOutputStream2.write(audioData, 0, audioData.length);
		byteArrayOutputStream2.close();
		socket.close();
	    }
	    catch (Exception e) {
		System.err.println("e="+e);
		return;
		
	    }
	    byte queryData[] = byteArrayOutputStream2.toByteArray();

	    String answer = null;
	    try {
		//out.println(str1);
		//answer = in.readLine();
		long now = System.currentTimeMillis();
		os.write(queryData, 0, queryData.length);
		os.flush();
		//System.out.println("Length="+queryData.length);
		answer = is.readLine();
		long end = System.currentTimeMillis();
		os.close();
		is.close();
		elapseTime = (float)(int)(end-now)/1000F;
		//System.out.println("Answer="+answer + " seconds used " +elapseTime);
		//out.close();
		//in.close();
		socket.close();
		if(answer.startsWith("content-length=")) {
		    int at = answer.indexOf('=', 20);
		    answer=answer.substring(at+1);
		}
	    } catch(Exception e) {
		System.err.println("Here, Error="+e);
		serverAnswer="-1";
		return;
	    }

	    serverAnswer=answer;
	    if(!serverAnswer.startsWith("-")) update_text_time(serverAnswer);
	    trainBtn.setText("Train");
	    trainBtn.setEnabled(true);
	    testBtn.setText("Test");
	    testBtn.setEnabled(true);
	    playBtn.setEnabled(true);
	    saveBtn.setEnabled(true);
	    exitBtn.setEnabled(true);
	    recordBtn.setEnabled(true);
	    return;
	}
    }

    public static void main(String args[]) {
	String prpFil="Property.xml";
	try {
	    String usage="Usage: java DettaClient [-P <PropertyFile>] [-v] [-h|-help]\n";
	    for(int i=0; i<args.length; i++) {
		if(args[i].startsWith("-h")) {
		    JOptionPane.showMessageDialog(null, usage, "Info", JOptionPane.INFORMATION_MESSAGE);
		    System.err.println(usage);
		    System.exit(1);
		} else if(args[i].startsWith("-v")) {
		    String prompt="Version: DettaClient 1.0.1, built with J2SE 1.5.0";
		    System.out.println(prompt);
		    JOptionPane.showMessageDialog(null, prompt, "Info", JOptionPane.INFORMATION_MESSAGE);
		    System.exit(1);
		} else if(args[i].startsWith("-P")) {
		    prpFil = args[++i];
		}
	    }
	} catch(Exception e) {
	    String prompt="Error in parsing command line";
	    JOptionPane.showMessageDialog(null, prompt, "ALERT", JOptionPane.ERROR_MESSAGE);
	    System.err.println(prompt);
	    System.exit(1);
	}

	Properties prps = new Properties();
	try {
	    prps.load(new FileInputStream(prpFil));
	} catch (Exception e) {
	    String prompt="Cannot load Property file: "+prpFil + ". Use default. ";
	    //System.err.println(prompt);
	    //JOptionPane.showMessageDialog(null, prompt+e, "Info", JOptionPane.INFORMATION_MESSAGE);
	}

	new DemoOfVoicePrints(prps);
    }

    static void OUT(String s) {
	System.out.println(s);
    }
    static void ERR(String s) {
	System.err.println(s);
    }
}
