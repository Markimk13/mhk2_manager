package main.java.ch.mko.fmm.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import main.java.ch.mko.fmm.MainFrame;
import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.enums.Engine;
import main.java.ch.mko.fmm.model.enums.GameCharacter;
import main.java.ch.mko.fmm.model.enums.Level;
import main.java.ch.mko.fmm.model.enums.Version;
import main.java.ch.mko.fmm.model.score.Championship;
import main.java.ch.mko.fmm.model.score.Highscore;
import main.java.ch.mko.fmm.model.score.HighscoreList;
import main.java.ch.mko.fmm.model.score.HighscoreSettings;
import main.java.ch.mko.fmm.model.score.HighscoreSettings.HighscoreMode;
import main.java.ch.mko.fmm.model.score.Player;

public class HighscorePanel extends JPanel {

	private static final long serialVersionUID = -6310090499999019898L;

	private static final HashMap<String, Integer> ROUNDS_MAP = new LinkedHashMap<>();
	static {
		ROUNDS_MAP.put(I18NLocale.getString(I18N.ALL_ROUNDS), 0);
		ROUNDS_MAP.put(I18NLocale.getString(I18N.ROUND) + " 1", 1);
		ROUNDS_MAP.put(I18NLocale.getString(I18N.ROUND) + " 2", 2);
		ROUNDS_MAP.put(I18NLocale.getString(I18N.ROUND) + " 3", 3);
	}

	private final List<JRadioButton> m_modeButtonList = new ArrayList<>();
	private final JComboBox<Level> m_levelChooser = new JComboBox<>(Level.values());
	private final JComboBox<String> m_champChooser = new JComboBox<>();
	private final JComboBox<Engine> m_engineChooser = new JComboBox<>(Engine.values());
	private final JCheckBox m_roundsBox = new JCheckBox();
	private final JComboBox<String> m_roundsChooser = new JComboBox<>(ROUNDS_MAP.keySet().toArray(new String[ROUNDS_MAP.size()]));
	private final JCheckBox m_characterBox = new JCheckBox();
	private final JComboBox<GameCharacter> m_characterChooser = new JComboBox<>(GameCharacter.values());
	private final JCheckBox m_playersBox = new JCheckBox();
	private final JComboBox<I18N> m_playersChooser = new JComboBox<>(Player.OPTIONS.keySet().toArray(new I18N[0]));
	private final JCheckBox m_versionBox = new JCheckBox();
	private final JComboBox<Version> m_versionChooser = new JComboBox<>(Version.values());
	private final JCheckBox m_rankLimitBox = new JCheckBox(I18NLocale.getString(I18N.RANK_LIMIT) + ":");
	private final JSpinner m_rankLimitSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 999, 1));
	
	private final JTable m_table;
	
	public HighscorePanel() {
		super(new BorderLayout());
		
		JPanel settingsPanel = new JPanel();
		add(settingsPanel, BorderLayout.NORTH);
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

		JPanel modePanel = new JPanel();
		settingsPanel.add(modePanel);
		modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.X_AXIS));
		
		for (HighscoreMode mode : HighscoreMode.values()) {
			m_modeButtonList.add(new JRadioButton(mode.toString()));
		}
		
		ActionListener updateTableListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					updateTableData();
				} catch (Exception ex) {
					MainFrame.LOG_PANEL.error(ex.getMessage(), ex);
				}
			}
		};
		ActionListener updateSelectionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					updateHighscoreSelection();
				} catch (Exception ex) {
					MainFrame.LOG_PANEL.error(ex.getMessage(), ex);
				}
			}
		};
		
		JToolBar modeToolBar = new JToolBar(JToolBar.HORIZONTAL);
		modePanel.add(modeToolBar);
		modeToolBar.setFloatable(false);
		ButtonGroup modeButtonGroup = new ButtonGroup();
		for (JRadioButton rb : m_modeButtonList) {
			modeToolBar.add(rb);
			modeButtonGroup.add(rb);
			rb.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					try {
						if (rb.isSelected()) {
							updateModeChange();
							updateTableData();
						}
					} catch (Exception ex) {
						MainFrame.LOG_PANEL.error(ex.getMessage(), ex);
					}
				}
			});
		}
		
		JPanel optionsPanel = new JPanel();
		settingsPanel.add(optionsPanel);
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));		

		optionsPanel.add(m_champChooser);
		m_champChooser.addActionListener(updateTableListener);
		m_champChooser.setVisible(false);
		m_champChooser.setRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 2208013686453878991L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				DefaultListCellRenderer cell = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				String text = (String) value;
				if (text.equals("01-02-03-04")) {
					text = I18NLocale.getString(I18N.CHAMPIONSHIP) + " 1";
				} else if (text.equals("05-06-07-08")) {
					text = I18NLocale.getString(I18N.CHAMPIONSHIP) + " 2";
				}
				
				cell.setText(text);
				
				return cell;
			}
		});	
		
		optionsPanel.add(m_levelChooser);
		m_levelChooser.addActionListener(updateTableListener);
		optionsPanel.add(m_engineChooser);
		m_engineChooser.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					updateTableData();
				} catch (Exception ex) {
					MainFrame.LOG_PANEL.error(ex.getMessage(), ex);
				}
			}
		});

		JPanel roundsPanel = new JPanel(new BorderLayout());
		optionsPanel.add(roundsPanel);
		roundsPanel.add(m_roundsBox, BorderLayout.WEST);
		roundsPanel.add(m_roundsChooser, BorderLayout.CENTER);
		m_roundsChooser.addActionListener(updateSelectionListener);
		m_roundsBox.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					m_roundsChooser.setEnabled(m_roundsBox.isSelected());
					updateHighscoreSelection();
				} catch (Exception ex) {
					MainFrame.LOG_PANEL.error(ex.getMessage(), ex);
				}
			}
		});
		m_roundsChooser.setEnabled(false);
		
		JPanel characterPanel = new JPanel(new BorderLayout());
		optionsPanel.add(characterPanel);
		characterPanel.add(m_characterBox, BorderLayout.WEST);
		characterPanel.add(m_characterChooser, BorderLayout.CENTER);
		m_characterChooser.addActionListener(updateSelectionListener);
		m_characterBox.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					m_characterChooser.setEnabled(m_characterBox.isSelected());
					updateHighscoreSelection();
				} catch (Exception ex) {
					MainFrame.LOG_PANEL.error(ex.getMessage(), ex);
				}
			}
		});
		m_characterChooser.setEnabled(false);
		
		JPanel playerPanel = new JPanel(new BorderLayout());
		optionsPanel.add(playerPanel);
		playerPanel.add(m_playersBox, BorderLayout.WEST);
		playerPanel.add(m_playersChooser, BorderLayout.CENTER);
		m_playersChooser.addActionListener(updateSelectionListener);
		m_playersBox.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					m_playersChooser.setEnabled(m_playersBox.isSelected());
					updateHighscoreSelection();
				} catch (Exception ex) {
					MainFrame.LOG_PANEL.error(ex.getMessage(), ex);
				}
			}
		});
		m_playersChooser.setEnabled(false);
		m_playersBox.setVisible(false);
		m_playersChooser.setVisible(false);
		m_playersChooser.setRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 2560286041925199738L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				DefaultListCellRenderer cell = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				cell.setText(I18NLocale.getString((I18N) value));
				
				return cell;
			}
		});	
		
		JPanel versionPanel = new JPanel(new BorderLayout());
		optionsPanel.add(versionPanel);
		versionPanel.add(m_versionBox, BorderLayout.WEST);
		versionPanel.add(m_versionChooser, BorderLayout.CENTER);
		m_versionChooser.addActionListener(updateSelectionListener);
		m_versionBox.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					m_versionChooser.setEnabled(m_versionBox.isSelected());
					updateHighscoreSelection();
				} catch (Exception ex) {
					MainFrame.LOG_PANEL.error(ex.getMessage(), ex);
				}
			}
		});
		m_versionChooser.setEnabled(false);
		m_versionBox.setVisible(false);
		m_versionChooser.setVisible(false);

		optionsPanel.add(m_rankLimitBox);
		m_rankLimitBox.addActionListener(updateSelectionListener);
		optionsPanel.add(m_rankLimitSpinner);
		((JSpinner.DefaultEditor) m_rankLimitSpinner.getEditor()).getTextField().setColumns(2);
		m_rankLimitSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					updateHighscoreSelection();
				} catch (Exception ex) {
					MainFrame.LOG_PANEL.error(ex.getMessage(), ex);
				}
			}
		});
		m_rankLimitBox.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					m_rankLimitSpinner.setEnabled(m_rankLimitBox.isSelected());
					updateHighscoreSelection();
				} catch (Exception ex) {
					MainFrame.LOG_PANEL.error(ex.getMessage(), ex);
				}
			}
		});
		m_rankLimitSpinner.setEnabled(false);

		HighscoreSettings settings = HighscoreSettings.loadSettings();
		updateSettingsInit(settings);
		
		final HighscoreModel model = new HighscoreModel(settings);
		m_table = new JTable(model);
		JScrollPane jsp = new JScrollPane(m_table);
		add(jsp, BorderLayout.CENTER);
		m_table.setFillsViewportHeight(true);
		m_table.setRowHeight(20);
		DefaultTableCellRenderer stringRenderer = new DefaultTableCellRenderer() {

			private static final long serialVersionUID = -4981634793818076336L;
			
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				JComponent comp = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				
				if (m_table.getColumnName(column).equals(I18NLocale.getString(I18N.TIME))) {
					comp.setFont(comp.getFont().deriveFont(Font.BOLD));
				}
				
				int transparency = 127;
				comp.setBackground((row+1) % 10 == 0 ? new Color(225, 225, 225, transparency) : Color.WHITE);
				if (row == 0) {
					comp.setBackground(new Color(255, 215, 0, transparency));
				} else if (row == 1) {
					comp.setBackground(new Color(192, 192, 192, transparency));
				} else if (row == 2) {
					comp.setBackground(new Color(205, 127, 50, transparency));
				}

				boolean showDetails = HighscoreSettings.loadSettings().isShowDetailsInTooltips();
				comp.setToolTipText(model.getHighscores()[row].getTooltipText(showDetails));
				
				return comp;
			}
		};
		stringRenderer.setHorizontalAlignment(JLabel.CENTER);
		m_table.setDefaultRenderer(String.class, stringRenderer);
		
		updateTableData();
	}
	
	private void updateSettingsInit(HighscoreSettings settings) {
		m_modeButtonList.get(settings.getMode().ordinal()).setSelected(true);
		updateModeChange();
		m_levelChooser.setSelectedItem(settings.getLevel());
		Level[] orderedLevels = settings.getChampionshipLevelsOrdered();
		String champName = String.format("%02d-%02d-%02d-%02d", orderedLevels[0].ordinal()+1,
				orderedLevels[1].ordinal()+1, orderedLevels[2].ordinal()+1, orderedLevels[3].ordinal()+1);
		m_engineChooser.setSelectedItem(settings.getEngine());
		updateChampionships();
		m_champChooser.setSelectedItem(champName);
		m_roundsBox.setSelected(settings.getRoundsFilter() != -1);
		m_roundsChooser.setSelectedItem(settings.getRoundsFilter());
		m_roundsChooser.setEnabled(m_roundsBox.isSelected());
		boolean isCharacterFiltered = settings.getCharacterFilter() != null;
		m_characterBox.setSelected(isCharacterFiltered);
		if (isCharacterFiltered) {
			m_characterChooser.setSelectedItem(settings.getCharacterFilter());
		}
		m_characterChooser.setEnabled(m_characterBox.isSelected());
		m_playersBox.setSelected(!Arrays.equals(settings.getPlayerFilter(), Player.HUMAN_PLAYERS));
		m_playersChooser.setSelectedItem(settings.getPlayerFilter());
		m_playersChooser.setEnabled(m_playersBox.isSelected());
		boolean isVersionFiltered = settings.getVersionFilter() != null;
		m_versionBox.setSelected(isVersionFiltered);
		if (isVersionFiltered) {
			m_versionChooser.setSelectedItem(settings.getVersionFilter());	
		}
		m_versionChooser.setEnabled(m_versionBox.isSelected());
		boolean isRankLimited = settings.getRankLimit() != -1;
		m_rankLimitBox.setSelected(isRankLimited);
		if (isRankLimited) {
			m_rankLimitSpinner.setValue(settings.getRankLimit());
		}
		m_rankLimitSpinner.setEnabled(m_rankLimitBox.isSelected());
	}
	
	private HighscoreMode getSelectedMode() {
		int selectedIndex = -1;
		for (int i = 0; i < m_modeButtonList.size(); i++) {
			if (m_modeButtonList.get(i).isSelected()) {
				selectedIndex = i;
				break;
			}
		}
		return HighscoreMode.values()[selectedIndex];
	}

	private void updateModeChange() {
		HighscoreMode mode = getSelectedMode();
		m_champChooser.setVisible(mode == HighscoreMode.CHAMPIONSHIP);
		m_levelChooser.setVisible(mode != HighscoreMode.CHAMPIONSHIP);
		m_roundsBox.setVisible(mode != HighscoreMode.CHAMPIONSHIP);
		m_roundsChooser.setVisible(mode != HighscoreMode.CHAMPIONSHIP);
		m_playersBox.setVisible(mode == HighscoreMode.CHAMPIONSHIP || mode == HighscoreMode.CHAMPIONSHIP_TIMES);
		m_playersChooser.setVisible(mode == HighscoreMode.CHAMPIONSHIP || mode == HighscoreMode.CHAMPIONSHIP_TIMES);
		m_versionBox.setVisible(true);
		m_versionChooser.setVisible(true);
	}
	
	private void updateChampionships() {
		File champsDir = new File(Championship.CHAMPIONSHIP_DIR);
		List<String> champs = new ArrayList<>();
		champs.add("01-02-03-04");
		champs.add("05-06-07-08");
		String prefix = "championship-" + (((Engine) m_engineChooser.getSelectedItem()).ordinal() + 1);
		for (File champDir : champsDir.listFiles()) {
			if (champDir.isDirectory() && champDir.getName().startsWith(prefix)) {
				String name = champDir.getName().substring(prefix.length() + 1);
				if (!champs.contains(name)) {
					champs.add(name);
				}
			}
		}
		m_champChooser.setModel(new DefaultComboBoxModel<String>(champs.toArray(new String[champs.size()])));
	}
	
	private int getSelectedRound() {
		return m_roundsBox.isSelected() ? ROUNDS_MAP.get((String) m_roundsChooser.getSelectedItem()) : -1;
	}
	
	private GameCharacter getSelectedCharacter() {
		return m_characterBox.isSelected() ? (GameCharacter) m_characterChooser.getSelectedItem() : null;
	}
	
	private int getSelectedRankLimit() {
		return m_rankLimitBox.isSelected() ? (int) m_rankLimitSpinner.getValue() : -1;
	}
	
	private Player[] getSelectedPlayers() {
		return m_playersBox.isSelected() ? Player.OPTIONS.get(m_playersChooser.getSelectedItem()) : Player.HUMAN_PLAYERS;
	}
	
	private Version getSelectedVersion() {
		return m_versionBox.isSelected() ? (Version) m_versionChooser.getSelectedItem() : null;
	}
	
	private class HighscoreModel extends AbstractTableModel {

		private static final long serialVersionUID = -611927914564002591L;

		private final HighscoreList m_highscoreList;
		
		private Highscore[] m_highscores = new Highscore[0];
		
		private String[] m_columnNames = new String[0];
		
		private int[] m_maxColWidths = new int[0];
		
		private HighscoreModel(HighscoreSettings settings) {
			m_highscoreList = new HighscoreList(settings);
		}
		
		@Override
		public int getColumnCount() {
			return m_columnNames.length;
		}
		
		@Override
		public String getColumnName(int col) {
			return m_columnNames[col];
		}

		@Override
		public int getRowCount() {
			return m_highscores.length;
		}
		
		private void updateColumns() {
			m_columnNames = m_highscoreList.getColumnNames();
			fireTableStructureChanged();
			m_maxColWidths = m_highscoreList.getMaximumColumnWidths();
			for (int i = 0; i < m_table.getColumnCount(); i++) {
				if (m_maxColWidths[i] != Integer.MAX_VALUE) {
					TableColumn col = m_table.getColumnModel().getColumn(i);
					col.setPreferredWidth(m_maxColWidths[i]);
					col.setMaxWidth(m_maxColWidths[i]);
				}
			}
		}
		
		private void updateHighscoreSelection() {
			m_highscoreList.selectHighscores(getSelectedCharacter(), getSelectedRound(), getSelectedPlayers(), getSelectedVersion());
			m_highscores = m_highscoreList.getHighscores(getSelectedRankLimit());
			SwingUtilities.invokeLater(() -> {
				updateColumns();
				fireTableDataChanged();
			});
		}
		
		private void updateTable() {
			HighscoreMode mode = getSelectedMode();
			if (mode == HighscoreMode.CHAMPIONSHIP) {
				String selected = (String) m_champChooser.getSelectedItem();
				updateChampionships();
				m_champChooser.setSelectedItem(selected);
			}
			m_highscoreList.generateHighscores(mode, (String) m_champChooser.getSelectedItem(),
					(Level) m_levelChooser.getSelectedItem(), (Engine) m_engineChooser.getSelectedItem());
			updateHighscoreSelection();
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			String[] properties = m_highscores[row].getProperties();
			properties[0] = "" + (row+1);
			return properties[col];
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			return getValueAt(0, col).getClass();
		}
		
		public Highscore[] getHighscores() {
			return Arrays.copyOf(m_highscores, m_highscores.length);
		}
	}
	
	public void updateTableData() {
		if (m_table != null) {
			((HighscoreModel) m_table.getModel()).updateTable();
		}
	}
	
	private void updateHighscoreSelection() {
		if (m_table != null) {
			((HighscoreModel) m_table.getModel()).updateHighscoreSelection();
		}
	}
}
