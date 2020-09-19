package main.java.ch.mko.fmm.util;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import main.java.ch.mko.fmm.MainFrame;

public class SwingUtils {

	public static void setEnabledDescendants(JComponent component, boolean enable) {
		component.setEnabled(enable);
		for (Component comp : component.getComponents()) {
			if (comp instanceof JComponent) {
				setEnabledDescendants((JComponent) comp, enable);
			}
		}
	}

	/**
	 * @return create new constraints for the GridBagLayout with initialized values
	 */
	public static GridBagConstraints createGbcConstraints() {
		GridBagConstraints gbcConstraints = new GridBagConstraints();
		gbcConstraints.gridx = 0;
		gbcConstraints.gridy = 0;
		gbcConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbcConstraints.anchor = GridBagConstraints.NORTHWEST;
		gbcConstraints.insets = new Insets(2, 2, 2, 2);
		return gbcConstraints;
	}

	public static void addComponentToPanel(JComponent description, JComponent component, boolean useWeighty, JPanel panel,
			GridBagConstraints gbcConstraints) {
		gbcConstraints.gridx = 0;
		gbcConstraints.weightx = 0;
		gbcConstraints.weighty = useWeighty ? 1 : 0;
		panel.add(description != null ? description : new JPanel(), gbcConstraints);
		gbcConstraints.gridx = 1;
		gbcConstraints.weightx = 1;
		panel.add(component != null ? component : new JPanel(), gbcConstraints);
		gbcConstraints.gridy++;
	}
	
	public static void openHtmlMessageDialog(Component parent, String title, String htmlInnerMessage) {

		// for copying style
	    JLabel label = new JLabel();
	    Font font = label.getFont();

	    // create some css from the label's font
	    StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
	    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
	    style.append("font-size:" + font.getSize() + "pt;");

	    String message = "<html><body style=\"" + style + "\">" + htmlInnerMessage + "</body></html>";
	    JEditorPane ep = new JEditorPane("text/html", message);

	    ep.addHyperlinkListener(new HyperlinkListener()
	    {
	        @Override
	        public void hyperlinkUpdate(HyperlinkEvent e)
	        {
	            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
	            	try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
						MainFrame.LOG_PANEL.error("Link cannot be opened: " + e1.getMessage(), e1);
					}
	            }
	        }
	    });
	    ep.setEditable(false);
	    ep.setBackground(label.getBackground());

	    JOptionPane.showMessageDialog(parent, ep, title, JOptionPane.INFORMATION_MESSAGE);
	    
	}
}
