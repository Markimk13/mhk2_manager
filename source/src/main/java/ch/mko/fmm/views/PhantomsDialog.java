package main.java.ch.mko.fmm.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import main.java.ch.mko.fmm.MainFrame;
import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.enums.Engine;
import main.java.ch.mko.fmm.model.enums.GameCharacter;
import main.java.ch.mko.fmm.model.enums.Level;
import main.java.ch.mko.fmm.model.enums.Version;
import main.java.ch.mko.fmm.model.score.HighscoreSettings;
import main.java.ch.mko.fmm.model.score.HighscoreSettings.PhantomMode;
import main.java.ch.mko.fmm.model.score.Phantom;
import main.java.ch.mko.fmm.model.score.Player.PlayerOrigin;
import main.java.ch.mko.fmm.util.FileUtils;
import main.java.ch.mko.fmm.util.FilterPanel;
import main.java.ch.mko.fmm.views.CustomPhantom.CharacterFilter;
import main.java.ch.mko.fmm.views.CustomPhantom.VersionFilter;

public class PhantomsDialog extends JDialog {

	private static final long serialVersionUID = 1640394179285108367L;

	private final HashMap<PhantomMode, JRadioButton> m_modeButtonMap = new LinkedHashMap<>();
	
	private final JCheckBox m_onlyCurrentVersionCheckBox = new JCheckBox(I18NLocale.getString(I18N.ONLY_CURRENT_VERSION));

	private final JCheckBox m_onlyCurrentCharacterCheckBox = new JCheckBox(I18NLocale.getString(I18N.ONLY_CURRENT_CHARACTER));
	
	private final JCheckBox m_useCustomPhantomsCheckBox = new JCheckBox(I18NLocale.getString(I18N.USE_CUSTOM_PHANTOMS));
	
	private final JButton m_addCustomPhantomsButton = new JButton(I18NLocale.getString(I18N.ADD_CUSTOM_PHANTOMS));

	private final FilterPanel<Boolean> m_usedFilter = new FilterPanel<>(
			new Boolean[] { Boolean.TRUE, Boolean.FALSE }, new String[] { "Used", "Not used" }
	);
	private final FilterPanel<Engine> m_engineFilter = new FilterPanel<>(Engine.values());
	private final FilterPanel<Level> m_levelFilter = new FilterPanel<>(Level.values());
	private final FilterPanel<Version> m_versionFilter = new FilterPanel<>(Version.values());
	private final FilterPanel<GameCharacter> m_characterFilter = new FilterPanel<>(GameCharacter.values());
	private final FilterPanel<PlayerOrigin> m_playerOriginFilter = new FilterPanel<>(PlayerOrigin.values());
	
	private List<CustomPhantom> m_customPhantoms;
	
	private Version m_currentVersion;
	
	private GameCharacter m_currentCharacter;
	
	private JTable m_table;

	public PhantomsDialog(JComponent parent) {
		super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent), I18NLocale.getString(I18N.MANAGE_PHANTOMS), true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		Dimension size = new Dimension(925, 600);
		setPreferredSize(size);
		setSize(size);

		add(createContentPanel(), BorderLayout.CENTER);

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
		
		loadSettings();
	}
	
	private JPanel createContentPanel() {
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(BorderFactory.createTitledBorder(I18NLocale.getString(I18N.PHANTOM_OPTIONS)));
		
		for (PhantomMode mode : PhantomMode.values()) {
			m_modeButtonMap.put(mode, new JRadioButton(mode.getName() + " - " + mode.getDescription()));
		}
		
		JToolBar modeToolBar = new JToolBar(JToolBar.VERTICAL);
		contentPanel.add(modeToolBar);
		modeToolBar.setAlignmentX(LEFT_ALIGNMENT);
		modeToolBar.setFloatable(false);
		ButtonGroup modeButtonGroup = new ButtonGroup();
		ActionListener tableDataListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTableData();
			}
		};
		
		for (Entry<PhantomMode, JRadioButton> entry : m_modeButtonMap.entrySet()) {
			if (entry.getKey() == PhantomMode.BEST_TIMES) {
				JPanel modeButtonPanel = new JPanel();
				modeButtonPanel.setLayout(new BoxLayout(modeButtonPanel, BoxLayout.X_AXIS));
				modeButtonPanel.add(entry.getValue());
				modeButtonPanel.add(m_onlyCurrentVersionCheckBox);
				m_onlyCurrentVersionCheckBox.addActionListener(tableDataListener);
				modeButtonPanel.add(m_onlyCurrentCharacterCheckBox);
				m_onlyCurrentCharacterCheckBox.addActionListener(tableDataListener);
				modeToolBar.add(modeButtonPanel);
				modeButtonPanel.setAlignmentX(LEFT_ALIGNMENT);
			} else {
				modeToolBar.add(entry.getValue());
				entry.getValue().setAlignmentX(LEFT_ALIGNMENT);
			}
			
			modeButtonGroup.add(entry.getValue());
			entry.getValue().addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if (entry.getValue().isSelected()) {
						updateTableData();	
					}
				}
			});
		}
		
		JPanel customPhantomsPanel = new JPanel(new BorderLayout());
		contentPanel.add(customPhantomsPanel);
		customPhantomsPanel.setAlignmentX(LEFT_ALIGNMENT);
		customPhantomsPanel.add(m_useCustomPhantomsCheckBox, BorderLayout.WEST);
		m_useCustomPhantomsCheckBox.addActionListener(tableDataListener);
		customPhantomsPanel.add(new JPanel(), BorderLayout.CENTER);
		customPhantomsPanel.add(m_addCustomPhantomsButton, BorderLayout.EAST);
		m_addCustomPhantomsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Frame parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, customPhantomsPanel);
				FileDialog fileDialog = new FileDialog(parentFrame, I18NLocale.getString(I18N.ADD_CUSTOM_PHANTOMS));
				fileDialog.setDirectory(Paths.get(System.getProperty("user.home"), "Downloads").toString());
				fileDialog.setMultipleMode(true);
				fileDialog.setFilenameFilter(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return new File(dir, name).isFile() && name.endsWith(".mhk2");
					}
				});
				fileDialog.setVisible(true);
				File[] selectedFiles = fileDialog.getFiles();
				for (File selectedFile : selectedFiles) {
					String name = FileUtils.getUniqueName(new File(Phantom.CUSTOM_DIR), selectedFile.getName());
					Path phantomPath = Paths.get(Phantom.CUSTOM_DIR, name);
					try {
						Phantom selectedPhantom = new Phantom(selectedFile.getPath());
						if (selectedPhantom.isValid()) {
							if (!m_customPhantoms.stream().anyMatch(cp -> selectedPhantom.contentEquals(cp.getPhantom()))) {
								Files.copy(selectedFile.toPath(), phantomPath);
								Phantom phantom = new Phantom(phantomPath.toString());
								m_customPhantoms.add(new CustomPhantom(phantom));	
							} else {
								MainFrame.LOG_PANEL.warn("Did not add custom phantom " + selectedFile.getPath() + " because it is already existing");
							}
						} else {
							MainFrame.LOG_PANEL.error("Cannot add invalid custom phantom " + selectedFile.getPath());
						}
					} catch (IOException ioe) {
						ioe.printStackTrace();
						MainFrame.LOG_PANEL.error("Could not add custom phantom: " + ioe.getMessage(), ioe);
					}
				}
				updateTableData();
			}
		});

		JPanel filterPanel = new JPanel(new BorderLayout());
		contentPanel.add(filterPanel);
		filterPanel.setAlignmentX(LEFT_ALIGNMENT);
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
		filterPanel.add(new JLabel("Filters:"));
		FilterPanel<?>[] filters = new FilterPanel[] {
				m_usedFilter, m_engineFilter, m_levelFilter, m_versionFilter, m_characterFilter, m_playerOriginFilter
		};
		ActionListener filterListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (m_table.getRowSorter() != null) {
					m_table.getRowSorter().allRowsChanged();
				}
				m_table.repaint();
			}
		};
		for (FilterPanel<?> filter : filters) {
			filterPanel.add(Box.createRigidArea(new Dimension(10, 30)));
			filterPanel.add(filter);
			filter.addActionListener(filterListener);
		}
		
		HighscoreSettings settings = HighscoreSettings.loadSettings();
		final PhantomsModel model = new PhantomsModel(settings);
		m_table = new JTable(model) {
			
			private static final long serialVersionUID = -502704413660835791L;

			protected JTableHeader createDefaultTableHeader() {
		        return new JTableHeader(columnModel) {

					private static final long serialVersionUID = 1917168521682623659L;

					public String getToolTipText(MouseEvent e) {
		                java.awt.Point p = e.getPoint();
		                int index = columnModel.getColumnIndexAtX(p.x);
		                int realIndex = columnModel.getColumn(index).getModelIndex();
		                return CustomPhantom.isColumnEditable(realIndex) ? I18NLocale.getString(I18N.CLICK_TO_EDIT_TEXT) : null;
		            }
		        };
		    }
		};
		
		JScrollPane jsp = new JScrollPane(m_table);
		contentPanel.add(jsp);
		jsp.setAlignmentX(LEFT_ALIGNMENT);
		m_table.setFillsViewportHeight(true);
		m_table.setRowHeight(20);
		
		m_table.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					createMenu(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					createMenu(e);
				}
			}

			private void createMenu(MouseEvent e) {
				int rowIndex = m_table.rowAtPoint(e.getPoint());
				if (rowIndex != -1) {
					CustomPhantom customPhantom = getTableData()[rowIndex];
					m_table.setRowSelectionInterval(rowIndex, rowIndex);

					JPopupMenu menu = new JPopupMenu();

					JMenuItem itemDelete = new JMenuItem(I18NLocale.getString(I18N.DELETE));
					menu.add(itemDelete);
					itemDelete.setEnabled(customPhantom.getPlayerOrigin() == PlayerOrigin.CUSTOM);
					itemDelete.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent ae) {
							int dialogResult = JOptionPane.showConfirmDialog(menu,
									I18NLocale.getString(I18N.DELETE_PHANTOM_WARNING), I18NLocale.getString(I18N.WARNING), JOptionPane.YES_NO_OPTION);
							if (dialogResult == JOptionPane.YES_OPTION) {
								m_customPhantoms.remove(customPhantom);
								updateTableData();
							}
						}
					});

					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		
		DefaultTableCellRenderer stringRenderer = new DefaultTableCellRenderer() {

			private static final long serialVersionUID = -6098275404366514024L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				JComponent comp = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				int modelRow = m_table.getRowSorter() != null ? m_table.getRowSorter().convertRowIndexToModel(row) : row;
				CustomPhantom customPhantom = model.getCustomPhantoms()[modelRow];
				
				if (m_table.getColumnName(column).equals(I18NLocale.getString(I18N.TIME))) {
					comp.setFont(comp.getFont().deriveFont(Font.BOLD));
				}
				
				boolean showDetails = HighscoreSettings.loadSettings().isShowDetailsInTooltips();
				comp.setToolTipText(customPhantom.getTooltipText(showDetails));
				
				if (column == m_table.getColumnCount() - 2) {
					JComboBox<VersionFilter> comboBox = new JComboBox<>(VersionFilter.values());
					comboBox.setSelectedItem(value);
					m_table.getColumnModel().getColumn(column).setCellEditor(new DefaultCellEditor(comboBox));
					
				} else if (column == m_table.getColumnCount() - 1) {
					JComboBox<CharacterFilter> comboBox = new JComboBox<>(CharacterFilter.values());
					comboBox.setSelectedItem(value);
					m_table.getColumnModel().getColumn(column).setCellEditor(new DefaultCellEditor(comboBox));
				}
					
				comp.setEnabled(customPhantom.getPlayerOrigin() == PlayerOrigin.CUSTOM);

				int prevModelRow = m_table.getRowSorter() != null && row > 0 ? m_table.getRowSorter().convertRowIndexToModel(row-1) : row-1;
				CustomPhantom prevCustomPhantom = prevModelRow != -1 ? model.getCustomPhantoms()[prevModelRow] : null;
				boolean isPrevDifferentLevel = prevCustomPhantom != null
						&& !customPhantom.getPhantom().getDefaultName().equals(prevCustomPhantom.getPhantom().getDefaultName());
				int nextModelRow = m_table.getRowSorter() != null && row < m_table.getRowSorter().getViewRowCount() - 1 ? m_table.getRowSorter().convertRowIndexToModel(row+1) : row+1;
				CustomPhantom nextCustomPhantom = nextModelRow != m_table.getRowCount() ? model.getCustomPhantoms()[nextModelRow] : null;
				boolean isNextDifferentLevel = nextCustomPhantom != null
						&& !customPhantom.getPhantom().getDefaultName().equals(nextCustomPhantom.getPhantom().getDefaultName());
				comp.setBorder(BorderFactory.createMatteBorder(isPrevDifferentLevel ? 2 : 0, 0, isNextDifferentLevel ? 2 : 0, 0, Color.BLACK));
				m_table.setRowHeight(row, 20 + (isPrevDifferentLevel ? 2 : 0) + (isNextDifferentLevel ? 2 : 0));

				return comp;
			}
		};
		stringRenderer.setHorizontalAlignment(JLabel.CENTER);
		m_table.setDefaultRenderer(String.class, stringRenderer);
		
		return contentPanel;
	}
	
	private PhantomMode getSelectedMode() {
		PhantomMode phantomMode = null;
		
		List<Entry<PhantomMode, JRadioButton>> entries = new ArrayList<>(m_modeButtonMap.entrySet());
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getValue().isSelected()) {
				phantomMode = entries.get(i).getKey();
				break;
			}
		}
		
		return phantomMode;
	}
	
	private void loadSettings() {
		HighscoreSettings settings = HighscoreSettings.loadSettings();
		m_modeButtonMap.get(settings.getPhantomMode()).setSelected(true);
		
		try {
			m_currentVersion = Version.getCurrentVersion();
			m_onlyCurrentVersionCheckBox.setText(I18NLocale.getString(I18N.ONLY_CURRENT_VERSION) + " (" + m_currentVersion.toString() + ")");
		} catch (IOException e) {
			e.printStackTrace();
			MainFrame.LOG_PANEL.error("Could not find current version: " + e.getMessage(), e);
		}
		m_onlyCurrentVersionCheckBox.setSelected(settings.isOnlyCurrentVersion());
		
		try {
			m_currentCharacter = GameCharacter.getCurrentCharacter();
			m_onlyCurrentCharacterCheckBox.setText(I18NLocale.getString(I18N.ONLY_CURRENT_CHARACTER) + " (" + m_currentCharacter.toString() + ")");
		} catch (IOException e) {
			e.printStackTrace();
			MainFrame.LOG_PANEL.error("Could not find current character: " + e.getMessage(), e);
		}
		m_onlyCurrentCharacterCheckBox.setSelected(settings.isOnlyCurrentCharacter());
		
		m_useCustomPhantomsCheckBox.setSelected(settings.isUseCustomPhantoms());
		m_customPhantoms = settings.getCustomPhantoms();
		
		updateTableData();
	}
	
	private void saveSettings() {
		HighscoreSettings settings = HighscoreSettings.loadSettings();
		settings.setPhantomMode(getSelectedMode());
		settings.setOnlyCurrentVersion(m_onlyCurrentVersionCheckBox.isSelected());
		settings.setOnlyCurrentCharacter(m_onlyCurrentCharacterCheckBox.isSelected());
		settings.setUseCustomPhantoms(m_useCustomPhantomsCheckBox.isSelected());
		settings.setCustomPhantoms(m_customPhantoms);
		try {
			CustomPhantom.updateShownPhantoms();
		} catch (IOException e) {
			e.printStackTrace();
			MainFrame.LOG_PANEL.error("Could not update phantoms: " + e.getMessage(), e);
		}
	}
	
	private class PhantomsModel extends AbstractTableModel {

		private static final long serialVersionUID = 3711851195546838162L;

		private List<CustomPhantom> m_tableCustomPhantoms = new ArrayList<>();
		
		private String[] m_columnNames = CustomPhantom.getColumnNames();
		
		private int[] m_maxColWidths = CustomPhantom.getMaximumColumnWidths();
		
		private PhantomsModel(HighscoreSettings settings) {
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
		public boolean isCellEditable(int row, int column) {
			CustomPhantom customPhantom = m_tableCustomPhantoms.get(row);
			boolean used = customPhantom.isActuallyUsedInList(m_useCustomPhantomsCheckBox.isSelected(),
					m_customPhantoms, m_onlyCurrentVersionCheckBox.isSelected(), m_currentVersion,
					m_onlyCurrentCharacterCheckBox.isSelected(), m_currentCharacter);
			return CustomPhantom.isColumnEditable(column)
					&& (customPhantom.getPlayerOrigin() == PlayerOrigin.CUSTOM 
							&& (column <= getColumnCount() - 3 || customPhantom.isUsedPhantom())
					|| customPhantom.getPlayerOrigin() != PlayerOrigin.CUSTOM
							&& column == 0 && !used);
		}
		
		@Override
		public int getRowCount() {
			return m_tableCustomPhantoms.size();
		}
		
		private void updateColumns() {
			fireTableStructureChanged();
			for (int i = 0; i < m_table.getColumnCount(); i++) {
				if (m_maxColWidths[i] != Integer.MAX_VALUE) {
					TableColumn col = m_table.getColumnModel().getColumn(i);
					col.setPreferredWidth(m_maxColWidths[i]);
					col.setMaxWidth(m_maxColWidths[i]);
				}
			}
		}
		
		private void updateTable() {
			m_tableCustomPhantoms.clear();
			try {
				m_tableCustomPhantoms.addAll(CustomPhantom.getAllPhantoms(getSelectedMode(),
						m_onlyCurrentVersionCheckBox.isSelected(), m_onlyCurrentCharacterCheckBox.isSelected(), 
						m_useCustomPhantomsCheckBox.isSelected(), m_customPhantoms));
			} catch (IOException e) {
				MainFrame.LOG_PANEL.error("Phantoms could not be updated in table: " + e.getMessage(), e);
			}
			
			if (m_table.getRowSorter() == null) {
				TableRowSorter<PhantomsModel> sorter = new TableRowSorter<PhantomsModel>(this) {
					public int getMaxSortKeys() {
						return 0;
					}
				};
				m_table.setRowSorter(sorter);
			    sorter.setRowFilter(new RowFilter<PhantomsModel, Integer>() {

					@Override
					public boolean include(Entry<? extends PhantomsModel, ? extends Integer> entry) {
						int rowID = (Integer) entry.getIdentifier();
						CustomPhantom customPhantom = m_tableCustomPhantoms.get(rowID);
						boolean used = customPhantom.isActuallyUsedInList(m_useCustomPhantomsCheckBox.isSelected(),
								m_customPhantoms, m_onlyCurrentVersionCheckBox.isSelected(), m_currentVersion,
								m_onlyCurrentCharacterCheckBox.isSelected(), m_currentCharacter);
						return m_usedFilter.include(used)
								&& m_engineFilter.include(customPhantom.getPhantom().getTrackTime().getEngine())
								&& m_levelFilter.include(customPhantom.getPhantom().getTrackTime().getLevel())
								&& m_versionFilter.include(customPhantom.getPhantom().getTrackTime().getVersion())
								&& m_characterFilter.include(customPhantom.getPhantom().getTrackTime().getCharacter())
								&& m_playerOriginFilter.include(customPhantom.getPlayerOrigin());
					}
					
				});
			}
			
			SwingUtilities.invokeLater(() -> {
				updateColumns();
				fireTableDataChanged();
			});
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			return m_tableCustomPhantoms.get(row).getProperties(m_useCustomPhantomsCheckBox.isSelected(),
					m_customPhantoms, m_onlyCurrentVersionCheckBox.isSelected(), m_currentVersion,
					m_onlyCurrentCharacterCheckBox.isSelected(), m_currentCharacter)[col];
		}
		
		@Override
	    public void setValueAt(Object newValue, int row, int column) {
			if (isCellEditable(row, column)) {
				CustomPhantom customPhantom = m_tableCustomPhantoms.get(row);
				int[] rows = IntStream.range(0, getRowCount())
						.filter(i -> customPhantom.getPhantom().getDefaultName().equals(
								m_tableCustomPhantoms.get(i).getPhantom().getDefaultName()))
						.toArray();
				
				if (column == 0) {
					boolean used = (boolean) newValue;
					customPhantom.setUsedPhantom(used);
					if (used) {
						Arrays.stream(rows)
								.filter(r -> r != row && m_tableCustomPhantoms.get(r).getPlayerOrigin() == PlayerOrigin.CUSTOM)
								.forEach(r -> m_tableCustomPhantoms.get(r).setUsedPhantom(false));
					}
				} else if (column == getColumnCount() - 3) {
					customPhantom.setUsedPhantom((boolean) newValue);
				} else if (column == getColumnCount() - 2) {
					customPhantom.setVersionFilter((VersionFilter) newValue);
				} else if (column == getColumnCount() - 1) {
					customPhantom.setCharacterFilter((CharacterFilter) newValue);
				}
				
				fireTableRowsUpdated(IntStream.of(rows).min().getAsInt(), IntStream.of(rows).max().getAsInt());
			}
	    }
		
		@Override
		public Class<?> getColumnClass(int col) {
			return getValueAt(0, col).getClass();
		}
		
		public CustomPhantom[] getCustomPhantoms() {
			return m_tableCustomPhantoms.toArray(new CustomPhantom[0]);
		}
	}
	
	public void updateTableData() {
		if (m_table != null) {
			((PhantomsModel) m_table.getModel()).updateTable();
		}
	}
	
	public CustomPhantom[] getTableData() {
		return ((PhantomsModel) m_table.getModel()).getCustomPhantoms();
	}
}
