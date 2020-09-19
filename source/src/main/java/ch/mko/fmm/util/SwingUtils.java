package main.java.ch.mko.fmm.util;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;

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
}
