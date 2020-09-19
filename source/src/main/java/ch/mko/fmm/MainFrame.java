package main.java.ch.mko.fmm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;

import main.java.ch.mko.fmm.i18n.I18N;
import main.java.ch.mko.fmm.i18n.I18NLocale;
import main.java.ch.mko.fmm.model.enums.Version;
import main.java.ch.mko.fmm.model.score.HighscoreSettings;
import main.java.ch.mko.fmm.util.SwingUtils;
import main.java.ch.mko.fmm.views.CustomPhantom;
import main.java.ch.mko.fmm.views.HighscorePanel;
import main.java.ch.mko.fmm.views.LogPanel;
import main.java.ch.mko.fmm.views.PhantomsDialog;
import main.java.ch.mko.fmm.views.SettingsDialog;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -4394316019464672763L;

	public static final LogPanel LOG_PANEL = new LogPanel("data" + File.separator + "log_highscores.txt", 800, 100);
	
	private HighscorePanel m_highscorePanel;
	
	private final JButton m_openGameButton = new JButton(I18NLocale.getString(I18N.OPEN_GAME));
	
	private final JComboBox<Version> m_versionChooser = new JComboBox<>(
			Version.IS_V1_1_CONTAINED_IN_V1_0 ? new Version[] { Version.V1_1 } : Version.values());
	
	private final JButton m_phantomsButton = new JButton(I18NLocale.getString(I18N.MANAGE_PHANTOMS));
	
	private final JLabel m_nameField = new JLabel();
	
	private final JLabel m_nameField2 = new JLabel();
	
	private SettingsUpdateChecker m_settingsUpdateChecker;
	
	public static void main(String[] args) {
		MainFrame frame = null;
		try {
			// set global defaults
			ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
			
			// init locale
			I18NLocale.updateLocale();
			
			DefaultLoader.createDirectories();
			DefaultLoader.createFiles();

			frame = new MainFrame();
			frame.initMainFrame();
			
		} catch (Exception e) {
			e.printStackTrace();
			LOG_PANEL.error("Program ended with error: " + e.getMessage(), e);
			
		} finally {
			if (frame != null) {
				frame.setVisible(true);
			}
		}
	}
	
	private MainFrame() throws ClassNotFoundException, IOException {
		super(Application.getNameWithVersion());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(925, 600));
		
        setJMenuBar(createMenuBar());
	}
	
	private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        
        JMenuItem menuItemS = new JMenuItem(I18NLocale.getString(I18N.SETTINGS_MENU_ITEM));
        menuItemS.setMnemonic(KeyEvent.VK_S);
        menuItemS.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        menu.add(menuItemS);
        menuItemS.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					HighscoreSettings settings = HighscoreSettings.loadSettings();
					boolean autoAddHighscores = settings.isAutoAddHighscores();
					SettingsDialog dialog = new SettingsDialog(menu);
					dialog.setVisible(true);
					updateNameSettings();
					if (autoAddHighscores != settings.isAutoAddHighscores()) {
						m_settingsUpdateChecker.updateSettingsUpdateCheck();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					LOG_PANEL.error("Cannot show settings: " + e1.getMessage(), e1);
				}
			}
		});
        
        JMenuItem menuItemC = new JMenuItem(I18NLocale.getString(I18N.CREDITS_MENU_ITEM));
        menuItemC.setMnemonic(KeyEvent.VK_C);
        menuItemC.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
        menu.add(menuItemC);
        menuItemC.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
				    String message = Application.getApplicationInfo()
				    		+ "<br/><br/>"
				    		+ String.format("GitHub: <a href=\"%s\">%s</a>", Application.getGithubLink(), Application.getGithubLink())
				    		+ "<br/><br/>"
				    		+ Application.getCredits();
				    SwingUtils.openHtmlMessageDialog(menuBar, I18NLocale.getString(I18N.CREDITS_MENU_ITEM), message);
					
				} catch (Exception e1) {
					e1.printStackTrace();
					LOG_PANEL.error("Cannot show credits: " + e1.getMessage(), e1);
				}
			}
		});

        JMenuItem menuItemH = new JMenuItem(I18NLocale.getString(I18N.HELP_MENU_ITEM));
        menuItemH.setMnemonic(KeyEvent.VK_H);
        menuItemH.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
        menu.add(menuItemH);
        menuItemH.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
				    String message = String.format(I18NLocale.getString(I18N.HELP_VIDEO_TEXT), Application.getHelpVideo(), Application.getHelpVideo())
						    + "<br/><br/>"
						    + String.format(I18NLocale.getString(I18N.HELP_EMAIL_TEXT), Application.getEmail(), Application.getEmail());
				    SwingUtils.openHtmlMessageDialog(menuBar, I18NLocale.getString(I18N.HELP_MENU_ITEM), message);

				} catch (Exception e1) {
					e1.printStackTrace();
					LOG_PANEL.error("Cannot show help: " + e1.getMessage(), e1);
				}
			}
		});
        
        return menuBar;
    }
	
	private void initMainFrame() throws IOException, ClassNotFoundException {
		JPanel contentPanel = new JPanel(new BorderLayout());
		JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, contentPanel, LOG_PANEL);
		add(mainPane, BorderLayout.CENTER);
		
		JPanel settingsPanel = new JPanel();
		contentPanel.add(settingsPanel, BorderLayout.NORTH);
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.X_AXIS));

		m_highscorePanel = new HighscorePanel();
		m_settingsUpdateChecker = new SettingsUpdateChecker(m_highscorePanel);
		
		settingsPanel.add(m_openGameButton);
		m_openGameButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				m_openGameButton.setEnabled(false);
				m_versionChooser.setEnabled(false);
				ExecutorService es = Executors.newSingleThreadExecutor();
				es.execute(() -> {
					try {
						CustomPhantom.updateShownPhantoms();
					} catch (IOException e1) {
						e1.printStackTrace();
						LOG_PANEL.error("Error when updating shown phantoms for opening game: " + e1.getMessage(), e1);
					}
					try {
						String cwd = new File(".").getAbsolutePath();
						String exeName = "MHK2-XXL.exe";
						Path exePath = Paths.get(cwd, exeName);
						if (!exePath.toFile().exists()) {
							throw new IOException("exe-file " + exeName + " does not exist!");
						}
						
						Process process = Runtime.getRuntime().exec(exePath.toString(), null, new File(cwd));
						Version.updateGameVersion();
						MainFrame.LOG_PANEL.log("Started game with version " + Version.getGameVersion().getName());
						
						MainFrame.LOG_PANEL.log("Exited game with version " + Version.getGameVersion().getName()
								+ ", Exit code: "+ process.waitFor());
						Version.resetGameVersion();
						
					} catch (IOException e1) {
						e1.printStackTrace();
						MainFrame.LOG_PANEL.error("Game cannot be opened: " + e1.getMessage(), e1);
						
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						MainFrame.LOG_PANEL.error("Game was interrupted: " + e1.getMessage(), e1);
						
					} finally {
						m_openGameButton.setEnabled(true);
						m_versionChooser.setEnabled(!Version.IS_V1_1_CONTAINED_IN_V1_0);
						m_versionChooser.setToolTipText(Version.IS_V1_1_CONTAINED_IN_V1_0 ? Version.IS_V1_1_CONTAINED_MESSAGE : null);
					}
				});
				es.shutdown();
			}
		});
		
		settingsPanel.add(m_versionChooser);
		m_versionChooser.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Version.setCurrentVersion((Version) m_versionChooser.getSelectedItem());
					CustomPhantom.updateShownPhantoms();
				} catch (Exception e1) {
					e1.printStackTrace();
					LOG_PANEL.error("Error when updating shown phantoms for version: " + e1.getMessage(), e1);
				}
			}
		});
		m_versionChooser.setMaximumSize(new Dimension(200, 30));
		m_versionChooser.setSelectedItem(Version.getCurrentVersion());
		m_versionChooser.setEnabled(!Version.IS_V1_1_CONTAINED_IN_V1_0);
		m_versionChooser.setToolTipText(Version.IS_V1_1_CONTAINED_IN_V1_0 ? Version.IS_V1_1_CONTAINED_MESSAGE : null);
		
		settingsPanel.add(Box.createHorizontalGlue());
		settingsPanel.add(m_nameField);
		settingsPanel.add(Box.createRigidArea(new Dimension(20, 30)));
		settingsPanel.add(m_nameField2);

		settingsPanel.add(Box.createHorizontalGlue());
		settingsPanel.add(m_phantomsButton);
		m_phantomsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					PhantomsDialog dialog = new PhantomsDialog(settingsPanel);
					dialog.setVisible(true);
					
				} catch (Exception e1) {
					e1.printStackTrace();
					LOG_PANEL.error("Cannot open manage phantoms: " + e1.getMessage(), e1);
				}
			}
		});

		contentPanel.add(m_highscorePanel, BorderLayout.CENTER);
		
		pack();
		mainPane.setDividerLocation(0.8);
        
        updateNameSettings();
	    m_settingsUpdateChecker.updateSettingsUpdateCheck();
	}
	
	private void updateNameSettings() {
		HighscoreSettings settings = HighscoreSettings.loadSettings();
		m_nameField.setText(I18NLocale.getString(I18N.PLAYER) + " 1: " + settings.getCurrentName());
		m_nameField2.setText(I18NLocale.getString(I18N.PLAYER) + " 2: " + settings.getCurrentName2());
	}
	
}
