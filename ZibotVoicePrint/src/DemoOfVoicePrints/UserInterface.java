package DemoOfVoicePrints;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public class UserInterface extends JFrame {
	protected float elapseTime = 0;
	protected String serverAnswer = " ";

	protected int sampleRate = 8000; // or 16000
	protected String hostName = "127.0.0.1";
	protected AudioFormat adFormat;
	protected Properties props = null;
	protected boolean stopRecord = false;
	protected boolean stopPlayback = false;
	protected ByteArrayOutputStream byteArrayOutputStream;
	protected AudioFormat audioFormat;
	protected TargetDataLine targetDataLine;
	protected AudioInputStream audioInputStream;
	protected SourceDataLine sourceDataLine;
	ByteArrayOutputStream byteOutputStream;
	AudioInputStream InputStream;
	boolean stopaudioCapture = false;
    private boolean PlaybackClicked=false;

	protected Font tfont = new Font("Serif", Font.PLAIN, 22);
	protected Font font = new Font("Serif", Font.PLAIN, 15);
	protected Font bfont = new Font("Serif", Font.PLAIN, 20);

	JLabel lbl1, lbl2, mainimg, resultlbl;
	JPanel titlePanel, recordPanel, trainPanel, resultPanel;
	JButton savebtn, testbtn, trainbtn, playbtn, recordbtn, pausebtn;
	ImageIcon frecordicon,fstopicon;
	
	public UserInterface(Properties prps) {
		props = prps;
		// FlowLayout사용
		setLayout(new FlowLayout());
		// Border로 영역 생성

		titlePanel = new JPanel();
		// 레이블 생성
		lbl1 = new JLabel("Zibot Voice Printing Technologies");
		lbl2 = new JLabel("record your voice to test and train the I-Vector recognition model.");

		lbl1.setFont(tfont);
		lbl2.setFont(font);

		ImageIcon icon = new ImageIcon("./src/main.png");
		mainimg = new JLabel(icon);

		// 레이블 추가
		titlePanel.add(lbl1);
		titlePanel.add(mainimg);
		titlePanel.add(lbl2);

		// id패널과 pw 패널생성
		recordPanel = new JPanel();

		// 버튼안에 이미지 넣기
		ImageIcon playicon = new ImageIcon("./src/play.png");
		ImageIcon recordicon = new ImageIcon("./src/record.png");
		ImageIcon pauseicon = new ImageIcon("./src/pause.png");
		ImageIcon stopicon = new ImageIcon("./src/stop.png");

		Image playiconImg = playicon.getImage();
		Image recordiconImg = recordicon.getImage();
		Image pauseiconImg = pauseicon.getImage();
		Image stopiconImg = stopicon.getImage();

		Image szplayiconImg = playiconImg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		Image szrecordiconImg = recordiconImg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		Image szpauseiconImg = pauseiconImg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		Image szstopiconImg = stopiconImg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);

		ImageIcon fplayicon = new ImageIcon(szplayiconImg);
		frecordicon = new ImageIcon(szrecordiconImg);
		ImageIcon fpauseicon = new ImageIcon(szpauseiconImg);
		fstopicon = new ImageIcon(szstopiconImg);

		playbtn = new JButton(fplayicon);
		recordbtn = new JButton(frecordicon);
		pausebtn = new JButton(fpauseicon);
		
		
		playbtn.setName("plb");
		recordbtn.setName("rcb");
		pausebtn.setName("pab");
		
		
		playbtn.setMargin(new Insets(5, 5, 5, 5));
		recordbtn.setMargin(new Insets(5, 5, 5, 5));
		pausebtn.setMargin(new Insets(5, 5, 5, 5));

		recordPanel.add(playbtn);
		recordPanel.add(recordbtn);
		recordPanel.add(pausebtn);

		// tb1.setSelectedIcon(tb2); //Toggle Button을 선택하면 Icon Image가 ii2에서 ii4로 변경
		trainPanel = new JPanel();

//          idPanel.add(la3);
//          idPanel.add(id);
//          paPanel.add( la2 );
//          paPanel.add( passwd );
//          // 로그인과 회원가입을 위한 패널 생성
//          loginPanel = new JPanel();
		savebtn = new JButton(props.getProperty("saveButtonLabel", "SAVE"));
		testbtn = new JButton(props.getProperty("testButtonLabel","TEST"));
		trainbtn = new JButton(props.getProperty("trainButtonLabel","TRAIN"));

		savebtn.setFont(bfont);
		testbtn.setFont(bfont);
		trainbtn.setFont(bfont);

		savebtn.setPreferredSize(new Dimension(100, 33));
		testbtn.setPreferredSize(new Dimension(100, 33));
		trainbtn.setPreferredSize(new Dimension(100, 33));

		trainPanel.add(savebtn);
		trainPanel.add(testbtn);
		trainPanel.add(trainbtn);

		resultPanel = new JPanel();
		resultPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		resultlbl = new JLabel("Result : ");
		resultlbl.setFont(font);

		resultPanel.add(resultlbl);

		titlePanel.setPreferredSize(new Dimension(500, 250));
		recordPanel.setPreferredSize(new Dimension(500, 60));
		trainPanel.setPreferredSize(new Dimension(500, 50));
		resultPanel.setPreferredSize(new Dimension(500, 80));

		playbtn.setEnabled(false);
		recordbtn.setEnabled(true);
		pausebtn.setEnabled(false);
		savebtn.setEnabled(false);
		testbtn.setEnabled(false);
		trainbtn.setEnabled(false);

		playbtn.addActionListener(new ActionListener()
		{
			  public void actionPerformed(ActionEvent e) {
				  actionPerformedin(e);
			}
		});
		
		recordbtn.addActionListener(new ActionListener()
		{
			  public void actionPerformed(ActionEvent e) {
				  actionPerformedin(e);
			}
		});
		
		pausebtn.addActionListener(new ActionListener()
		{
			  public void actionPerformed(ActionEvent e) {
				  actionPerformedin(e);
			}
		});
		
		savebtn.addActionListener(new ActionListener()
		{
			  public void actionPerformed(ActionEvent e) {
				  actionPerformedin(e);
			}
		});
		
		testbtn.addActionListener(new ActionListener()
		{
			  public void actionPerformed(ActionEvent e) {
				  actionPerformedin(e);
			}
		});
		
		trainbtn.addActionListener(new ActionListener()
		{
			  public void actionPerformed(ActionEvent e) {
				  actionPerformedin(e);
			}
		});
		
		add(titlePanel);
		add(recordPanel);
		add(trainPanel);
		add(resultPanel);
		
		setTitle("Zibot Voice Printing");
		setSize(700, 500);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

	}

	public void actionPerformedin(ActionEvent ev) {
		if (ev.getSource() == savebtn) {
			save_audio_to_file();
		} else if (ev.getSource() == trainbtn) {
			if (trainbtn.getText().equals("Train")) {
				trainbtn.setText("Processing");
				playbtn.setEnabled(false);
				recordbtn.setEnabled(false);
				pausebtn.setEnabled(false);
				savebtn.setEnabled(false);
				trainbtn.setEnabled(false);
				testbtn.setEnabled(false);
				resultlbl.setText("Processing . . . ");
				train();
				// playAudio();
				// save_audio_to_file();
			}
		} else if (ev.getSource() == testbtn) {
			if (testbtn.getText().equals("Test")) {
				testbtn.setText("Processing");
				playbtn.setEnabled(false);
				pausebtn.setEnabled(true);
				recordbtn.setEnabled(false);
				savebtn.setEnabled(false);
				trainbtn.setEnabled(false);
				testbtn.setEnabled(false);
				talk2server();
				playAudio();
			}
		} else if (ev.getSource() == recordbtn) {
			if (recordbtn.getName().equals("rcb")) {
				recordbtn.setIcon(fstopicon);
				recordbtn.setName("stb");
				playbtn.setEnabled(false);
				stopRecord = false;
				recordAudio();
			} else if(recordbtn.getName().equals("stb")){
				recordbtn.setIcon(frecordicon);
				stopRecord = true;
				recordbtn.setName("rcb");
				
				// targetDataLine.close();
				recordbtn.setEnabled(true);
				playbtn.setEnabled(true);
				savebtn.setEnabled(true);
				trainbtn.setEnabled(true);
				// if(!serverAnswer.startsWith("-")) update_text_time(serverAnswer);
				/*
				 * trainBtn.setText("Busy"); playBtn.setEnabled(false);
				 * exitBtn.setEnabled(false); recordBtn.setEnabled(false);
				 * saveBtn.setEnabled(false); trainBtn.setEnabled(false);
				 * testBtn.setEnabled(false);
				 * 
				 * //talk2server(); playAudio();
				 */
				repaint();

			}

		} else if (ev.getSource() == playbtn) {
				playbtn.setText("Stop");
				recordbtn.setEnabled(false);
				playbtn.setEnabled(false);
				pausebtn.setEnabled(true);
				stopPlayback = false;
				PlaybackClicked = true;
				playAudio();
		}else if(ev.getSource() == pausebtn){
			stopPlayback = true;
			playbtn.setEnabled(true);
			pausebtn.setEnabled(false);
			recordbtn.setEnabled(true);
		}
	}
	
    private void save_audio_to_file() {
	Calendar cal = Calendar.getInstance();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
	String currentTime = (sdf.format(cal.getTime())).replace('-', 'T');

	String spkrName = resultlbl.getText().trim();
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

    
	// This method captures audio input from mic and saves it in a
	// ByteArrayOutputStream object.
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

	// This method plays back the audio data that has been saved in the
	// ByteArrayOutputStream
	private void playAudio() {
		try {
			// Get everything set up for playback.
			// Get the previously-saved data into a byte array object.
			byte audioData[] = byteArrayOutputStream.toByteArray();
			// Get an input stream on the byte array containing the data
			InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
			AudioFormat audioFormat = getAudioFormat();
			audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat,
					audioData.length / audioFormat.getFrameSize());
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
			sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			sourceDataLine.open(audioFormat);
			sourceDataLine.start();
			Thread playThread = new Thread(new PlayThread());
			playThread.start();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, props.getProperty("ErrorLiteral", "Error") + "- playAudio: " + e,
					"ALERT", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
		}
	}

	// This method creates and returns an AudioFormat object for a given set of
	// format parameters.
	private AudioFormat getAudioFormat() {
		float sampleRateInFloat = (float) sampleRate; // 8000,11025,16000,22050,44100
		int sampleSizeInBits = 16; // 8,16
		int channels = 1; // 1,2
		boolean signed = true; // true,false
		boolean bigEndian = false; // true,false
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
			DataLine.Info info = null;
			try {
				info = new DataLine.Info(SourceDataLine.class, audioFormat);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, props.getProperty("ErrorLiteral", "Error") + " - info: " + e,
						"ALERT", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				line = (SourceDataLine) AudioSystem.getLine(info);
				// The line is there, but not yet ready to receive audio data. We have to open
				// the line.
				line.open(audioFormat);
			} catch (LineUnavailableException e) {
				JOptionPane.showMessageDialog(null, props.getProperty("ErrorLiteral", "Error") + " - line: " + e,
						"ALERT", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, props.getProperty("ErrorLiteral", "Error") + " - line: " + e,
						"ALERT", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}

			// Still not enough. The line now can receive data, but will not pass them on to
			// the audio output device (which means to your sound card). This has to be
			// activated.
			line.start();

			// The line is finally prepared. Now comes the real job: we have to write data
			// to the line. We do this
			// in a loop. First, we read data from the AudioInputStream to a buffer. Then,
			// we write from this
			// buffer to the Line. This is done until the end of the file is reached, which
			// is detected by a
			// return value of -1 from the read method of the AudioInputStream.
			int cnt = 0;
			try {
				stopPlayback = false;
				while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1 && !stopPlayback) {
					if (cnt > 0)
						line.write(tempBuffer, 0, cnt);
					else
						break;
				}
				line.drain(); // Wait until all data are played.
				line.close(); // All data are played. We can close the shop.
				playtn.setText("Play");
				if (PlaybackClicked == true)
					recordBtn.setEnabled(true);
				PlaybackClicked = false;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, props.getProperty("ErrorLiteral", "Error") + " - playback: " + e,
						"ALERT", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}
	}
	

    public static int byteswap(int i) {  // hope the input is > 0 - unsigned
	return ((i << 24)|((i << 8)&0x00ff0000)|((i >> 8)&0x0000ff00)|(i >> 24));
	
    }

    public static short byteswap(short s) {
	byte b1 = (byte) (s & 0xff);
	byte b2 = (byte) ((s >> 8) & 0xff);
	return (short) ((b1 << 8) | b2);
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

		new UserInterface(prps);
	    }

	    static void OUT(String s) {
		System.out.println(s);
	    }
	    static void ERR(String s) {
		System.err.println(s);
	}

}