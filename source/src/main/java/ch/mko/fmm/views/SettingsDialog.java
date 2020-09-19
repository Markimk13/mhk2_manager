package main.java.ch.mko.fmm.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.AbstractDocument;

import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.score.HighscoreSettings;
import main.java.ch.mko.fmm.util.LimitDocumentFilter;
import main.java.ch.mko.fmm.util.SwingUtils;

public class SettingsDialog extends JDialog {

	private static final long serialVersionUID = -7510236403737992293L;
	
	private final JPanel m_propertiesPanel = new JPanel(new GridBagLayout());
	private final GridBagConstraints m_gbcPropertiesPanel = SwingUtils.createGbcConstraints();
	
	private final JCheckBox m_autoAddCheckBox = new JCheckBox(I18NLocale.getString(I18N.ADD_HIGHSCORES_WHILE_PLAYING));
	
	private final JCheckBox m_showDetailsInTooltipsCheckBox = new JCheckBox(I18NLocale.getString(I18N.SHOW_DETAILS_IN_TOOLTIP));

	private final JTextField m_nameField = new JTextField();
	
	private final JTextField m_nameField2 = new JTextField();
	
	private final JComboBox<I18NLocale> m_languageField = new JComboBox<>(I18NLocale.values());

	public SettingsDialog(JComponent parent) {
		super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent), I18NLocale.getString(I18N.SETTINGS_MENU_ITEM), true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		Dimension size = new Dimension(800, 600);
		setPreferredSize(size);
		setSize(size);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		JScrollPane jsp = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(jsp, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(buttonPanel, BorderLayout.SOUTH);
		
		JButton cancelButton = new JButton(I18NLocale.getString(I18N.CANCEL));
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		JButton saveButton = new JButton(I18NLocale.getString(I18N.SAVE));
		buttonPanel.add(saveButton);
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveSettings();
				setVisible(false);
			}
		});
		saveButton.setMnemonic(KeyEvent.VK_ENTER);
		getRootPane().setDefaultButton(saveButton);

		contentPanel.add(m_propertiesPanel);
		m_propertiesPanel.setBorder(BorderFactory.createTitledBorder(I18NLocale.getString(I18N.SETTINGS_MENU_ITEM)));
		
		initProperties();
		loadSettings();
	}
	
	private void initProperties() {
		addProperty(null, m_autoAddCheckBox, false);
		addProperty(null, m_showDetailsInTooltipsCheckBox, false);

		addProperty("Name " + I18NLocale.getString(I18N.PLAYER) + " 1:", m_nameField, false);
		((AbstractDocument) m_nameField.getDocument()).setDocumentFilter(new LimitDocumentFilter(12));

		addProperty("Name " + I18NLocale.getString(I18N.PLAYER) + " 2:", m_nameField2, false);		
		((AbstractDocument) m_nameField2.getDocument()).setDocumentFilter(new LimitDocumentFilter(12));
		
		JPanel languagePanel = new JPanel(new BorderLayout());
		languagePanel.add(m_languageField, BorderLayout.WEST);
		JLabel label = new JLabel();
		languagePanel.add(label, BorderLayout.CENTER);
		label.setForeground(Color.RED);
		label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		addProperty(I18NLocale.getString(I18N.LANGUAGE) +":", languagePanel, false);
		final I18NLocale locale = HighscoreSettings.loadSettings().getLocale();
		m_languageField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				I18NLocale newLocale = (I18NLocale) m_languageField.getSelectedItem();
				label.setText(locale != newLocale ? I18NLocale.getString(newLocale, I18N.LANGUAGE_RESTART_INFO) : null);
			}
		});
		
		addProperty(null, null, true);
	}
	
	protected void addProperty(String name, JComponent component, boolean useWeighty) {
		SwingUtils.addComponentToPanel(name != null ? new JLabel(name) : null,
				component, useWeighty, m_propertiesPanel, m_gbcPropertiesPanel);
	}
	
	private void loadSettings() {
		HighscoreSettings settings = HighscoreSettings.loadSettings();
		m_autoAddCheckBox.setSelected(settings.isAutoAddHighscores());
		m_showDetailsInTooltipsCheckBox.setSelected(settings.isShowDetailsInTooltips());
		m_nameField.setText(settings.getCurrentName());
		m_nameField2.setText(settings.getCurrentName2());
		m_languageField.setSelectedItem(settings.getLocale());
	}
	
	private void saveSettings() {
		HighscoreSettings settings = HighscoreSettings.loadSettings();
		settings.setAutoAddHighscores(m_autoAddCheckBox.isSelected());
		settings.setShowDetailsInTooltips(m_showDetailsInTooltipsCheckBox.isSelected());
		settings.setCurrentName(m_nameField.getText());
		settings.setCurrentName2(m_nameField2.getText());
		settings.setLocale((I18NLocale) m_languageField.getSelectedItem());
	}
}
