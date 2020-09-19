package main.java.ch.mko.fmm.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class LogPanel extends JPanel {

	private static final long serialVersionUID = 8275955556404502673L;
	
	private static enum LogMode {
		LOG, WARN, ERROR;
	}
	
	private final JTextPane m_logTextPane = new JTextPane();
	
	private final String m_logFilePath;

	public LogPanel(String logFilePath, int width, int height) {
		super(new GridBagLayout());
		m_logFilePath = logFilePath;
		
		setBorder(BorderFactory.createTitledBorder("Log:"));
		Dimension panelSize = new Dimension(width, height + 20);
		setPreferredSize(panelSize);
		JScrollPane jsp = new JScrollPane(m_logTextPane);
		Dimension jspSize = new Dimension(width, height);
		jsp.setPreferredSize(jspSize);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(jsp, gbc);

		DefaultCaret caret = (DefaultCaret) m_logTextPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		m_logTextPane.setEditable(false);
		clearLogFile();
	}
	
	private synchronized void appendToTextPane(String text, Color color) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        try {
            StyledDocument doc = m_logTextPane.getStyledDocument();
			doc.insertString(doc.getLength(), text, aset);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
    }
	
	private void addEntry(LogMode mode, String text, Throwable cause) {
		String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
		String logText = String.format("%-5s %s - %s", mode.toString(), dateString, text);
		if (cause != null) {
			StringWriter exceptionWriter = new StringWriter();
			cause.printStackTrace(new PrintWriter(exceptionWriter));
			logText += System.getProperty("line.separator") + exceptionWriter.toString();
		}
		logText += System.getProperty("line.separator");

		appendToTextPane(logText, mode == LogMode.ERROR ? Color.RED : mode == LogMode.WARN ? Color.MAGENTA : Color.BLACK);
		
		try {
			File log = new File(m_logFilePath);
			FileWriter out = new FileWriter(log, true);
			out.write(logText);
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void log(String text) {
		addEntry(LogMode.LOG, text, null);
	}
	
	public void warn(String text, Throwable cause) {
		addEntry(LogMode.WARN, text, cause);
	}
	
	public void warn(String text) {
		warn(text, null);
	}
	
	public void error(String text, Throwable cause) {
		addEntry(LogMode.ERROR, text, cause);
	}
	
	public void error(String text) {
		error(text, null);
	}
	
	private void clearLogFile() {
		try {
			File log = new File(m_logFilePath);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(log), "UTF-8"));
			out.write("");
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
