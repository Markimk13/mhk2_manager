package main.java.ch.mko.fmm.views;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Utils {
	
	public static JTextField addFileChooser(JPanel parent, String name, String defaultFilePath, boolean useDirectories) {
		JPanel chooserPanel = new JPanel(new BorderLayout());
		parent.add(chooserPanel);
		JFileChooser fileChooser = new JFileChooser(new File(defaultFilePath).getParent());
		chooserPanel.add(new JLabel(name), BorderLayout.WEST);
		JTextField textField = new JTextField();
		chooserPanel.add(textField, BorderLayout.CENTER);
		textField.setText(defaultFilePath);

		if (useDirectories) {
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		
		JButton openButton = new JButton("Browse");
		chooserPanel.add(openButton, BorderLayout.EAST);
		openButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser.showOpenDialog(openButton);
				File file = fileChooser.getSelectedFile();
				if (file != null) {
					textField.setText(file.getPath());
				}
			}
		});
		
		return textField;
	}
	
	public static JTextField addTextField(JPanel parent, String name) {
		JPanel chooserPanel = new JPanel(new BorderLayout());
		parent.add(chooserPanel);
		chooserPanel.add(new JLabel(name), BorderLayout.WEST);
		JTextField textField = new JTextField();
		chooserPanel.add(textField, BorderLayout.CENTER);
		
		return textField;
	}
}
