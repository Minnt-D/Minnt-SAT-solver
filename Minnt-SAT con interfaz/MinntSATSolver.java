import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MinntSATSolver extends JFrame {
  private JTextArea editor;
  private JTextArea resultArea;
  private JProgressBar progressBar;
  private JLabel statusLabel;
  private JButton solveButton;
  private JButton openBtn;
  private JButton clearBtn;
  private AtomicBoolean cancelRequested = new AtomicBoolean(false);

  private enum Theme { DARK, LIGHT, MINNT_DARK, MINNT_LIGHT }
  private Theme currentTheme = Theme.DARK;

  private static final Color VS_DARK_BG = Color.decode("#1e1e1e");
  private static final Color VS_DARK_PANEL = Color.decode("#252526");
  private static final Color VS_DARK_SIDEBAR = Color.decode("#333333");
  private static final Color VS_DARK_FG = Color.decode("#d4d4d4");
  private static final Color VS_DARK_ACCENT = Color.decode("#007acc");
  private static final Color VS_DARK_LINE_BG = Color.decode("#2b2b2b");
  private static final Color VS_DARK_LINE_FG = Color.decode("#858585");
  private static final Color VS_DARK_BORDER = Color.decode("#464647");
  private static final Color VS_DARK_BUTTON = Color.decode("#0e639c");
  private static final Color VS_DARK_BUTTON_HOVER = Color.decode("#1177bb");

  // VS Code Light Theme Colors
  private static final Color VS_LIGHT_BG = Color.decode("#ffffff");
  private static final Color VS_LIGHT_PANEL = Color.decode("#f3f3f3");
  private static final Color VS_LIGHT_SIDEBAR = Color.decode("#f8f8f8");
  private static final Color VS_LIGHT_FG = Color.decode("#333333");
  private static final Color VS_LIGHT_ACCENT = Color.decode("#0078d4");
  private static final Color VS_LIGHT_LINE_BG = Color.decode("#eaeaea");
  private static final Color VS_LIGHT_LINE_FG = Color.decode("#606060");
  private static final Color VS_LIGHT_BORDER = Color.decode("#e5e5e5");
  private static final Color VS_LIGHT_BUTTON = Color.decode("#0078d4");
  private static final Color VS_LIGHT_BUTTON_HOVER = Color.decode("#106ebe");

  private static final Color MINNT_DARK_BG = Color.decode("#111217");
  private static final Color MINNT_DARK_PANEL = Color.decode("#13161a");
  private static final Color MINNT_DARK_FG = Color.decode("#e6e6e6");
  private static final Color MINNT_DARK_ACCENT = Color.decode("#ff7a00");
  private static final Color MINNT_DARK_LINE_BG = Color.decode("#0f1113");
  private static final Color MINNT_DARK_LINE_FG = Color.decode("#8a8a8a");

  private static final Color MINNT_LIGHT_BG = Color.decode("#f8f7f5");
  private static final Color MINNT_LIGHT_PANEL = Color.decode("#fbf8f5");
  private static final Color MINNT_LIGHT_FG = Color.decode("#222222");
  private static final Color MINNT_LIGHT_ACCENT = Color.decode("#ff7a00");
  private static final Color MINNT_LIGHT_LINE_BG = Color.decode("#f0ede9");
  private static final Color MINNT_LIGHT_LINE_FG = Color.decode("#6b6b6b");

  private static final String CODE_FONT_NAME = "Consolas";
  private static final String UI_FONT_NAME = "Segoe UI";
  private static final int CODE_FONT_SIZE = 14;
  private static final int UI_FONT_SIZE = 13;

  private JMenu themeMenu;

  public MinntSATSolver() {
    setTitle("Minnt SAT Solver");
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    buildUI();
    applyTheme(currentTheme);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        onClose();
      }
    });
  }

  private void buildUI() {
    Font uiFont = new Font(UI_FONT_NAME, Font.PLAIN, UI_FONT_SIZE);
    Font codeFont = new Font(CODE_FONT_NAME, Font.PLAIN, CODE_FONT_SIZE);

    JMenuBar menuBar = new JMenuBar();
    menuBar.setFont(uiFont);
    menuBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

    JMenu fileMenu = new JMenu("File");
    fileMenu.setFont(uiFont);
    JMenuItem openItem = new JMenuItem("Open CNF/TXT...");
    openItem.setFont(uiFont);
    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
    openItem.addActionListener(e -> loadCNFFromFile());
    fileMenu.add(openItem);
    fileMenu.addSeparator();
    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.setFont(uiFont);
    exitItem.addActionListener(e -> onClose());
    fileMenu.add(exitItem);

    JMenu editMenu = new JMenu("Edit");
    editMenu.setFont(uiFont);
    JMenuItem copyItem = new JMenuItem("Copy Result");
    copyItem.setFont(uiFont);
    copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
    copyItem.addActionListener(e -> copyResultToClipboard());
    editMenu.add(copyItem);

    JMenu viewMenu = new JMenu("View");
    viewMenu.setFont(uiFont);
    themeMenu = new JMenu("Theme");
    themeMenu.setFont(uiFont);
    JMenuItem tDark = new JMenuItem("Dark");
    tDark.setFont(uiFont);
    tDark.addActionListener(e -> { currentTheme = Theme.DARK; applyTheme(currentTheme); });
    JMenuItem tLight = new JMenuItem("Light");
    tLight.setFont(uiFont);
    tLight.addActionListener(e -> { currentTheme = Theme.LIGHT; applyTheme(currentTheme); });
    JMenuItem tMinntDark = new JMenuItem("MINNT Dark");
    tMinntDark.setFont(uiFont);
    tMinntDark.addActionListener(e -> { currentTheme = Theme.MINNT_DARK; applyTheme(currentTheme); });
    JMenuItem tMinntLight = new JMenuItem("MINNT Light");
    tMinntLight.setFont(uiFont);
    tMinntLight.addActionListener(e -> { currentTheme = Theme.MINNT_LIGHT; applyTheme(currentTheme); });
    themeMenu.add(tDark);
    themeMenu.add(tLight);
    themeMenu.addSeparator();
    themeMenu.add(tMinntDark);
    themeMenu.add(tMinntLight);
    viewMenu.add(themeMenu);

    JMenu runMenu = new JMenu("Run");
    runMenu.setFont(uiFont);
    JMenuItem solveItem = new JMenuItem("Solve SAT");
    solveItem.setFont(uiFont);
    solveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
    solveItem.addActionListener(e -> startSolve());
    runMenu.add(solveItem);

    JMenu helpMenu = new JMenu("Help");
    helpMenu.setFont(uiFont);
    JMenuItem about = new JMenuItem("About");
    about.setFont(uiFont);
    about.addActionListener(e -> showAbout());
    JMenuItem manual = new JMenuItem("User Manual");
    manual.setFont(uiFont);
    manual.addActionListener(e -> showManual());
    helpMenu.add(about);
    helpMenu.add(manual);

    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(viewMenu);
    menuBar.add(runMenu);
    menuBar.add(helpMenu);
    setJMenuBar(menuBar);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

    JPanel activityBar = new JPanel();
    activityBar.setLayout(new BoxLayout(activityBar, BoxLayout.Y_AXIS));
    activityBar.setPreferredSize(new Dimension(48, 0));
    activityBar.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setResizeWeight(0.6);
    splitPane.setBorder(null);
    splitPane.setDividerSize(4);

    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));

    JPanel editorTabBar = new JPanel(new BorderLayout());
    editorTabBar.setPreferredSize(new Dimension(0, 35));
    editorTabBar.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
    JLabel editorTab = new JLabel("problem.cnf");
    editorTab.setFont(uiFont);
    editorTab.setIcon(createColoredIcon(12, 12, new Color(0, 122, 204)));
    editorTab.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    editorTabBar.add(editorTab, BorderLayout.WEST);

    editor = new JTextArea();
    editor.setFont(codeFont);
    editor.setTabSize(2);
    editor.setLineWrap(false);
    editor.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));

    JScrollPane editorScroll = new JScrollPane(editor);
    editorScroll.setBorder(null);
    LineNumberView lineNumbers = new LineNumberView(editor);
    editorScroll.setRowHeaderView(lineNumbers);

    leftPanel.add(editorTabBar, BorderLayout.NORTH);
    leftPanel.add(editorScroll, BorderLayout.CENTER);

    JPanel editorToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    editorToolbar.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 8));
    openBtn = createVSCodeButton("Open", KeyEvent.VK_O);
    openBtn.addActionListener(e -> loadCNFFromFile());
    solveButton = createVSCodeButton("Run", KeyEvent.VK_F5);
    solveButton.addActionListener(e -> startSolve());
    clearBtn = createVSCodeButton("Clear", 0);
    clearBtn.addActionListener(e -> {
      editor.setText("");
      resultArea.setText("");
      statusLabel.setText("Ready");
    });
    editorToolbar.add(openBtn);
    editorToolbar.add(solveButton);
    editorToolbar.add(clearBtn);
    leftPanel.add(editorToolbar, BorderLayout.SOUTH);

    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

    JPanel outputTabBar = new JPanel(new BorderLayout());
    outputTabBar.setPreferredSize(new Dimension(0, 35));
    outputTabBar.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
    JLabel outputTab = new JLabel("OUTPUT");
    outputTab.setFont(new Font(UI_FONT_NAME, Font.PLAIN, 11));
    outputTab.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    JPanel outputToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 6));
    JButton copyBtn = createIconButton("Copy", "📋");
    copyBtn.addActionListener(e -> copyResultToClipboard());
    JButton saveBtn = createIconButton("Save", "💾");
    saveBtn.addActionListener(e -> exportResultToDownloads());
    outputToolbar.add(copyBtn);
    outputToolbar.add(saveBtn);

    outputTabBar.add(outputTab, BorderLayout.WEST);
    outputTabBar.add(outputToolbar, BorderLayout.EAST);

    resultArea = new JTextArea();
    resultArea.setFont(codeFont);
    resultArea.setEditable(false);
    resultArea.setLineWrap(true);
    resultArea.setWrapStyleWord(true);
    resultArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));

    JScrollPane resultScroll = new JScrollPane(resultArea);
    resultScroll.setBorder(null);
    rightPanel.add(outputTabBar, BorderLayout.NORTH);
    rightPanel.add(resultScroll, BorderLayout.CENTER);

    JPanel statusBar = new JPanel(new BorderLayout());
    statusBar.setName("statusBar");
    statusBar.setPreferredSize(new Dimension(0, 22));
    statusBar.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 12));
    statusLabel = new JLabel("Ready");
    statusLabel.setFont(new Font(UI_FONT_NAME, Font.PLAIN, 11));
    progressBar = new JProgressBar();
    progressBar.setPreferredSize(new Dimension(120, 14));
    progressBar.setIndeterminate(false);
    progressBar.setVisible(false);
    progressBar.setBorderPainted(false);
    JPanel statusRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    statusRight.setOpaque(false);
    statusRight.add(progressBar);
    statusBar.add(statusLabel, BorderLayout.WEST);
    statusBar.add(statusRight, BorderLayout.EAST);

    splitPane.setLeftComponent(leftPanel);
    splitPane.setRightComponent(rightPanel);
    mainPanel.add(activityBar, BorderLayout.WEST);
    mainPanel.add(splitPane, BorderLayout.CENTER);
    mainPanel.add(statusBar, BorderLayout.SOUTH);
    add(mainPanel);

    editor.setText("// SAT Problem Example\n// Comments start with // or c\np cnf 3 2\n1 -3 0\n2 3 -1 0\n");

    setupKeyboardShortcuts();
    setSize(1400, 800);
    setLocationRelativeTo(null);
  }

  static class RoundedBorder implements Border {
    private final int radius;
    private final Color shadowColor;

    RoundedBorder(int radius, Color shadowColor) {
      this.radius = radius;
      this.shadowColor = shadowColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(shadowColor);
      g2.fillRoundRect(x + 1, y + 1, width - 2, height - 2, radius, radius);
      g2.setColor(((JComponent) c).getForeground());
      g2.setStroke(new BasicStroke(1.2f));
      g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
      g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(3, 3, 3, 3);
    }

    @Override
    public boolean isBorderOpaque() {
      return false;
    }
  }

  private void applyTheme(Theme t) {
    Color bg, panelBg, sidebarBg, fg, accent, lineBg, lineFg, border, buttonBg, buttonHover;
    boolean isDark = t == Theme.DARK || t == Theme.MINNT_DARK;

    switch (t) {
      case DARK -> {
        bg = VS_DARK_BG; panelBg = VS_DARK_PANEL; sidebarBg = VS_DARK_SIDEBAR;
        fg = VS_DARK_FG; accent = VS_DARK_ACCENT; lineBg = VS_DARK_LINE_BG;
        lineFg = VS_DARK_LINE_FG; border = VS_DARK_BORDER;
        buttonBg = VS_DARK_BUTTON; buttonHover = VS_DARK_BUTTON_HOVER;
      }
      case LIGHT -> {
        bg = VS_LIGHT_BG; panelBg = VS_LIGHT_PANEL; sidebarBg = VS_LIGHT_SIDEBAR;
        fg = VS_LIGHT_FG; accent = VS_LIGHT_ACCENT; lineBg = VS_LIGHT_LINE_BG;
        lineFg = VS_LIGHT_LINE_FG; border = VS_LIGHT_BORDER;
        buttonBg = Color.WHITE; buttonHover = new Color(240, 240, 240);
      }
      case MINNT_DARK -> {
        bg = MINNT_DARK_BG; panelBg = MINNT_DARK_PANEL; sidebarBg = MINNT_DARK_PANEL;
        fg = MINNT_DARK_FG; accent = MINNT_DARK_ACCENT; lineBg = MINNT_DARK_LINE_BG;
        lineFg = MINNT_DARK_LINE_FG; border = MINNT_DARK_ACCENT;
        buttonBg = MINNT_DARK_ACCENT; buttonHover = MINNT_DARK_ACCENT.brighter();
      }
      case MINNT_LIGHT -> {
        bg = MINNT_LIGHT_BG; panelBg = MINNT_LIGHT_PANEL; sidebarBg = MINNT_LIGHT_PANEL;
        fg = MINNT_LIGHT_FG; accent = MINNT_LIGHT_ACCENT; lineBg = MINNT_LIGHT_LINE_BG;
        lineFg = MINNT_LIGHT_LINE_FG; border = MINNT_LIGHT_ACCENT;
        buttonBg = MINNT_LIGHT_ACCENT; buttonHover = MINNT_LIGHT_ACCENT.darker();
      }
      default -> throw new IllegalStateException("Unexpected theme: " + t);
    }

    applyColorsRecursively(getContentPane(), bg, panelBg, sidebarBg, fg, accent,
            lineBg, lineFg, border, buttonBg, buttonHover, isDark);

    editor.setBackground(bg);
    editor.setForeground(fg);
    editor.setCaretColor(fg);
    editor.setSelectionColor(accent.darker());
    editor.setSelectedTextColor(Color.WHITE);

    resultArea.setBackground(bg);
    resultArea.setForeground(fg);
    resultArea.setSelectionColor(accent.darker());
    resultArea.setSelectedTextColor(Color.WHITE);

    JMenuBar menuBar = getJMenuBar();
    if (menuBar != null) {
      menuBar.setBackground(panelBg);
      menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, border));
      applyMenuColors(menuBar, panelBg, fg, accent);
    }

    Component statusBarComp = findComponentByName(getContentPane(), "statusBar");
    if (statusBarComp instanceof JPanel) {
      JPanel statusBarPanel = (JPanel) statusBarComp;
      statusBarPanel.setBackground(accent);
      statusLabel.setForeground(Color.WHITE);
    }

    progressBar.setBackground(panelBg);
    progressBar.setForeground(accent);

    Component lineNumbers = getEditorRowHeader();
    if (lineNumbers instanceof LineNumberView) {
      ((LineNumberView) lineNumbers).setColors(lineBg, lineFg);
    }

    if (t == Theme.MINNT_DARK || t == Theme.MINNT_LIGHT) {
      Color shadow = t == Theme.MINNT_DARK ? new Color(0, 0, 0, 50) : new Color(0, 0, 0, 20);
      for (JButton btn : new JButton[]{solveButton, clearBtn, openBtn}) {
        btn.setBorder(new RoundedBorder(8, shadow));
        btn.setOpaque(true);
      }
      resultArea.setBorder(BorderFactory.createCompoundBorder(
              new RoundedBorder(8, shadow),
              BorderFactory.createEmptyBorder(8, 12, 8, 8)
      ));
      editor.setBorder(BorderFactory.createCompoundBorder(
              new RoundedBorder(8, shadow),
              BorderFactory.createEmptyBorder(8, 12, 8, 8)
      ));
    } else {
      solveButton.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
      clearBtn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
      openBtn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
      resultArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
      editor.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
    }

    SwingUtilities.updateComponentTreeUI(this);
    repaint();
  }

  private void applyColorsRecursively(Container container, Color bg, Color panelBg,
                                      Color sidebarBg, Color fg, Color accent,
                                      Color lineBg, Color lineFg, Color border,
                                      Color buttonBg, Color buttonHover, boolean isDark) {
    container.setBackground(panelBg);
    container.setForeground(fg);

    for (Component comp : container.getComponents()) {
      if (comp instanceof JPanel) {
        comp.setBackground(panelBg);
        comp.setForeground(fg);
        if (comp instanceof Container) {
          applyColorsRecursively((Container) comp, bg, panelBg, sidebarBg, fg, accent,
                  lineBg, lineFg, border, buttonBg, buttonHover, isDark);
        }
      } else if (comp instanceof JButton) {
        JButton btn = (JButton) comp;
        if (btn.getText().equals("Run") || btn.getText().equals("Cancel") ||
                btn.getText().equals("Clear") || btn.getText().equals("Open")) {
          btn.setBackground(buttonBg);
          btn.setForeground(isDark ? Color.WHITE : Color.BLACK);
          btn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
          btn.setFocusPainted(false);
          btn.setOpaque(true);
          btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          Color finalButtonHover = buttonHover;
          btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(finalButtonHover); }
            public void mouseExited(MouseEvent e) { btn.setBackground(buttonBg); }
          });
        } else {
          btn.setBackground(new Color(0, true));
          btn.setForeground(fg);
          btn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
          btn.setOpaque(false);
        }
      } else if (comp instanceof JLabel) {
        comp.setForeground(fg);
      } else if (comp instanceof JScrollPane) {
        comp.setBackground(panelBg);
        JScrollPane sp = (JScrollPane) comp;
        sp.getViewport().setBackground(bg);
        sp.setBorder(BorderFactory.createLineBorder(border, 1));
      }
      if (comp instanceof Container) {
        applyColorsRecursively((Container) comp, bg, panelBg, sidebarBg, fg, accent,
                lineBg, lineFg, border, buttonBg, buttonHover, isDark);
      }
    }
  }

  private void applyMenuColors(JMenuBar menuBar, Color bg, Color fg, Color accent) {
    for (int i = 0; i < menuBar.getMenuCount(); i++) {
      JMenu menu = menuBar.getMenu(i);
      menu.setBackground(bg);
      menu.setForeground(fg);
      applyMenuItemColors(menu, bg, fg, accent);
    }
  }

  private void applyMenuItemColors(JMenu menu, Color bg, Color fg, Color accent) {
    for (Component comp : menu.getMenuComponents()) {
      if (comp instanceof JMenuItem) {
        comp.setBackground(bg);
        comp.setForeground(fg);
        if (comp instanceof JMenu) {
          applyMenuItemColors((JMenu) comp, bg, fg, accent);
        }
      }
    }
  }

  private Component findComponentByName(Container container, String name) {
    for (Component comp : container.getComponents()) {
      if (name.equals(comp.getName())) return comp;
      if (comp instanceof Container) {
        Component found = findComponentByName((Container) comp, name);
        if (found != null) return found;
      }
    }
    return null;
  }

  private Component getEditorRowHeader() {
    Container p = editor.getParent();
    if (p instanceof JViewport) {
      Container sp = p.getParent();
      if (sp instanceof JScrollPane) {
        JScrollPane jsp = (JScrollPane) sp;
        return jsp.getRowHeader() != null ? jsp.getRowHeader().getView() : null;
      }
    }
    return null;
  }

  private void showThemedDialog(String title, JTextArea content) {
    JDialog dialog = new JDialog(this, title, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setSize(700, 500);
    dialog.setLocationRelativeTo(this);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    JPanel panel = new JPanel(new BorderLayout());
    Color bg = currentTheme == Theme.DARK || currentTheme == Theme.MINNT_DARK ? VS_DARK_BG : VS_LIGHT_BG;
    panel.setBackground(bg);

    content.setFont(new Font(UI_FONT_NAME, Font.PLAIN, 12));
    content.setEditable(false);
    content.setLineWrap(true);
    content.setWrapStyleWord(true);
    content.setBackground(bg);
    content.setForeground(currentTheme == Theme.DARK || currentTheme == Theme.MINNT_DARK ? VS_DARK_FG : VS_LIGHT_FG);

    JScrollPane scroll = new JScrollPane(content);
    scroll.setBorder(null);

    JButton closeBtn = new JButton("Close");
    closeBtn.addActionListener(e -> dialog.dispose());

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    btnPanel.setBackground(bg);
    btnPanel.add(closeBtn);

    panel.add(scroll, BorderLayout.CENTER);
    panel.add(btnPanel, BorderLayout.SOUTH);
    dialog.add(panel);
    dialog.setVisible(true);
  }

  private void showAbout() {
    JTextArea textArea = new JTextArea("""
                Minnt SAT Solver - Java Edition
                Version: 2.0
                Features:
                • DPLL algorithm with optimizations
                • Unit propagation and pure literal elimination  
                • Frequency-based variable ordering heuristic
                • Multiple themes (VS Dark/Light, MINNT Dark/Light)
                • Export results to Downloads folder
                • Full keyboard shortcut support
                
                Shortcuts:
                • Ctrl+O: Open file
                • F5: Run solver
                • Ctrl+Shift+C: Copy result
                """);
    showThemedDialog("About Minnt SAT Solver", textArea);
  }

  private void showManual() {
    JTextArea textArea = new JTextArea("""
                Minnt SAT Solver - User Manual
                SUPPORTED FORMATS:
                • DIMACS CNF format
                • Comments: lines starting with 'c' or '//'
                • Header: 'p cnf <variables> <clauses>'
                • Clauses: space-separated literals ending with '0'
                
                USAGE:
                1. Open a CNF file (File → Open or Ctrl+O)
                2. Edit the problem in the left editor panel
                3. Run the solver (Run → Solve SAT or F5)
                4. View results in the right output panel
                5. Copy results (Ctrl+Shift+C) or save to file
                
                EXPORT:
                Results are saved to your Downloads folder with timestamp:
                'minnt_result_<timestamp>.txt'
                
                THEMES:
                • Dark/Light: Visual Studio Code themes
                • MINNT Dark/Light: Custom neumorphic orange themes
                
                KEYBOARD SHORTCUTS:
                • Ctrl+O: Open file
                • F5: Run solver  
                • Ctrl+Shift+C: Copy result to clipboard
                • Standard text editing shortcuts in editor
                """);
    showThemedDialog("User Manual", textArea);
  }

  private void onClose() {
    if ("Cancel".equals(solveButton.getText())) {
      int option = JOptionPane.showConfirmDialog(this,
              "Solver is running. Cancel and exit?", "Exit",
              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      if (option != JOptionPane.YES_OPTION) return;
      cancelRequested.set(true);
    }
    dispose();
    System.exit(0);
  }

  private void setupKeyboardShortcuts() {
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "open");
    getRootPane().getActionMap().put("open", new AbstractAction() {
      public void actionPerformed(ActionEvent e) { loadCNFFromFile(); }
    });
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "run");
    getRootPane().getActionMap().put("run", new AbstractAction() {
      public void actionPerformed(ActionEvent e) { startSolve(); }
    });
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "copyResult");
    getRootPane().getActionMap().put("copyResult", new AbstractAction() {
      public void actionPerformed(ActionEvent e) { copyResultToClipboard(); }
    });
  }

  private JButton createVSCodeButton(String text, int keyCode) {
    JButton btn = new JButton(text);
    btn.setFont(new Font(UI_FONT_NAME, Font.PLAIN, 11));
    btn.setFocusPainted(false);
    btn.setBorderPainted(true);
    btn.setContentAreaFilled(true);
    btn.setOpaque(true);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.setMargin(new Insets(4, 12, 4, 12));
    if (keyCode != 0) {
      String shortcut = keyCode == KeyEvent.VK_F5 ? "F5" : "Ctrl+" + KeyEvent.getKeyText(keyCode);
      btn.setToolTipText(text + " (" + shortcut + ")");
    }
    return btn;
  }

  private JButton createIconButton(String tooltip, String icon) {
    JButton btn = new JButton(icon);
    btn.setFont(new Font("Arial", Font.PLAIN, 12));
    btn.setToolTipText(tooltip);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setContentAreaFilled(false);
    btn.setOpaque(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.setMargin(new Insets(2, 4, 2, 4));
    btn.setPreferredSize(new Dimension(24, 20));
    return btn;
  }

  private Icon createColoredIcon(int width, int height, Color color) {
    return new Icon() {
      public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillOval(x + 2, y + 2, width - 4, height - 4);
      }
      public int getIconWidth() { return width; }
      public int getIconHeight() { return height; }
    };
  }

  // File operations
  private void loadCNFFromFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
      @Override
      public boolean accept(File file) {
        if (file.isDirectory()) return true;
        String name = file.getName().toLowerCase();
        return name.endsWith(".cnf") || name.endsWith(".txt");
      }
      @Override
      public String getDescription() {
        return "CNF Files (*.cnf, *.txt)";
      }
    });
    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      try {
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        String content = new String(bytes, StandardCharsets.UTF_8);
        editor.setText(content);
        int[] counts = countVarsAndClauses(content);
        if (counts[0] >= 0) {
          statusLabel.setText(String.format("Loaded: %s | Variables: %d | Clauses: %d",
                  file.getName(), counts[0], counts[1]));
        } else {
          statusLabel.setText("Loaded: " + file.getName());
        }
        editor.setCaretPosition(0);
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this,
                "Error reading file:\n" + ex.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private int[] countVarsAndClauses(String text) {
    for (String line : text.split("\\R")) {
      String trimmed = line.trim();
      if (trimmed.startsWith("p ")) {
        String[] parts = trimmed.split("\\s+");
        if (parts.length >= 4) {
          try {
            int vars = Integer.parseInt(parts[2]);
            int clauses = Integer.parseInt(parts[3]);
            return new int[]{vars, clauses};
          } catch (NumberFormatException ignored) {}
        }
      }
    }
    return new int[]{-1, -1};
  }

  private void copyResultToClipboard() {
    String text = resultArea.getText();
    if (text == null || text.trim().isEmpty()) {
      JOptionPane.showMessageDialog(this, "No results to copy.",
              "Information", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    StringSelection selection = new StringSelection(text);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    statusLabel.setText("Results copied to clipboard");
    javax.swing.Timer timer = new javax.swing.Timer(2000, e -> statusLabel.setText("Ready"));
    timer.setRepeats(false);
    timer.start();
  }

  private void exportResultToDownloads() {
    String text = resultArea.getText();
    if (text == null || text.trim().isEmpty()) {
      JOptionPane.showMessageDialog(this, "No results to export.",
              "Information", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    try {
      Path downloadsPath = Paths.get(System.getProperty("user.home"), "Downloads");
      if (!downloadsPath.toFile().exists()) {
        downloadsPath = Paths.get(System.getProperty("user.home")); // Fallback
      }
      String timestamp = String.valueOf(System.currentTimeMillis());
      String filename = "minnt_result_" + timestamp + ".txt";
      Path outputPath = downloadsPath.resolve(filename);
      java.nio.file.Files.write(outputPath, text.getBytes(StandardCharsets.UTF_8));
      statusLabel.setText("Exported: " + filename);
      JOptionPane.showMessageDialog(this,
              "Results exported successfully to:\n" + outputPath.toString(),
              "Export Complete", JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this,
              "Error exporting file:\n" + ex.getMessage(),
              "Export Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void startSolve() {
    if ("Cancel".equals(solveButton.getText())) {
      cancelRequested.set(true);
      statusLabel.setText("Canceling solver...");
      return;
    }
    String text = editor.getText().trim();
    if (!text.contains("p cnf")) {
      JOptionPane.showMessageDialog(this,
              "Invalid format. File must contain 'p cnf' header line.",
              "Format Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    solveButton.setText("Cancel");
    progressBar.setIndeterminate(true);
    progressBar.setVisible(true);
    statusLabel.setText("Solving...");
    resultArea.setText("Running SAT solver...\n");
    cancelRequested.set(false);
    new Thread(this::runSolverTask, "SAT-Solver-Thread").start();
  }

  private void runSolverTask() {
    try {
      String text = editor.getText();
      long startTime = System.nanoTime();
      String headerLine = null;
      for (String line : text.split("\\R")) {
        String trimmed = line.trim();
        if (trimmed.startsWith("p ")) {
          headerLine = trimmed;
          break;
        }
      }
      if (headerLine == null) {
        SwingUtilities.invokeLater(() -> {
          JOptionPane.showMessageDialog(this,
                  "Missing 'p cnf' header line.",
                  "Format Error", JOptionPane.ERROR_MESSAGE);
        });
        return;
      }
      String[] headerParts = headerLine.split("\\s+");
      if (headerParts.length < 4) {
        SwingUtilities.invokeLater(() -> {
          JOptionPane.showMessageDialog(this,
                  "Invalid 'p cnf' header format.",
                  "Format Error", JOptionPane.ERROR_MESSAGE);
        });
        return;
      }
      int numVars, numClauses;
      try {
        numVars = Integer.parseInt(headerParts[2]);
        numClauses = Integer.parseInt(headerParts[3]);
      } catch (NumberFormatException e) {
        SwingUtilities.invokeLater(() -> {
          JOptionPane.showMessageDialog(this,
                  "Invalid numbers in 'p cnf' header.",
                  "Format Error", JOptionPane.ERROR_MESSAGE);
        });
        return;
      }
      List<int[]> clauses = parseDimacsToIntList(text);
      if (clauses.isEmpty()) {
        SwingUtilities.invokeLater(() -> {
          JOptionPane.showMessageDialog(this,
                  "No valid clauses found.",
                  "Format Error", JOptionPane.ERROR_MESSAGE);
        });
        return;
      }
      SwingUtilities.invokeLater(() -> {
        statusLabel.setText("Solving with DPLL algorithm...");
      });
      boolean[] solution = solveDPLL(clauses, numVars);
      double elapsedSeconds = (System.nanoTime() - startTime) / 1e9;
      final boolean[] finalSolution = solution;
      final int finalNumVars = numVars;
      SwingUtilities.invokeLater(() -> {
        StringBuilder result = new StringBuilder();
        if (finalSolution != null) {
          result.append("SAT\n");
          result.append("Problem is satisfiable!\n");
          StringBuilder assignment = new StringBuilder();
          for (int i = 1; i <= finalNumVars; i++) {
            assignment.append(finalSolution[i] ? i : -i);
            if (i < finalNumVars) assignment.append(" ");
          }
          result.append("Variable Assignment:\n");
          result.append(assignment.toString()).append("\n");
          result.append("Truth Table:\n");
          for (int i = 1; i <= finalNumVars; i++) {
            result.append(String.format("x%d = %s\n", i, finalSolution[i] ? "TRUE" : "FALSE"));
          }
          statusLabel.setText("SAT - Solution found");
        } else {
          result.append("UNSAT\n");
          result.append("Problem is unsatisfiable.\n");
          result.append("No solution exists for the given constraints.\n");
          statusLabel.setText("UNSAT - No solution exists");
        }
        result.append(String.format("\nExecution time: %.4f seconds\n", elapsedSeconds));
        result.append(String.format("Variables: %d | Clauses: %d\n", finalNumVars, clauses.size()));
        resultArea.setText(result.toString());
        resultArea.setCaretPosition(0);
      });
    } catch (Exception e) {
      SwingUtilities.invokeLater(() -> {
        JOptionPane.showMessageDialog(this,
                "Solver error:\n" + e.getMessage(),
                "Solver Error", JOptionPane.ERROR_MESSAGE);
        resultArea.setText("Error: " + e.getMessage());
        statusLabel.setText("Error occurred");
      });
    } finally {
      SwingUtilities.invokeLater(() -> {
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        solveButton.setText("Run");
      });
    }
  }

  private List<int[]> parseDimacsToIntList(String dimacs) {
    List<int[]> clauses = new ArrayList<>();
    for (String rawLine : dimacs.split("\\R")) {
      String line = rawLine.trim();
      if (line.isEmpty()) continue;
      if (line.startsWith("c") || line.startsWith("//") || line.startsWith("p")) continue;
      String[] tokens = line.split("\\s+");
      List<Integer> literals = new ArrayList<>();
      for (String token : tokens) {
        if ("0".equals(token)) break;
        try {
          int literal = Integer.parseInt(token);
          if (literal != 0) {
            literals.add(literal);
          }
        } catch (NumberFormatException ignored) {}
      }
      if (!literals.isEmpty()) {
        clauses.add(literals.stream().mapToInt(i -> i).toArray());
      }
    }
    return clauses;
  }

  private boolean[] solveDPLL(List<int[]> clausesList, int numVars) {
    List<int[]> clauses = new ArrayList<>(clausesList);
    Boolean[] assignment = new Boolean[numVars + 1];
    boolean satisfiable = dpllRecursive(clauses, assignment, numVars);
    return satisfiable ? convertToPrimitiveBoolArray(assignment) : null;
  }

  private boolean[] convertToPrimitiveBoolArray(Boolean[] boxedArray) {
    boolean[] primitiveArray = new boolean[boxedArray.length];
    for (int i = 0; i < boxedArray.length; i++) {
      primitiveArray[i] = boxedArray[i] != null && boxedArray[i];
    }
    return primitiveArray;
  }

  private boolean dpllRecursive(List<int[]> clauses, Boolean[] assignment, int numVars) {
    if (cancelRequested.get()) return false;
    List<int[]> simplifiedClauses = new ArrayList<>();
    for (int[] clause : clauses) {
      boolean clauseSatisfied = false;
      List<Integer> newClause = new ArrayList<>();
      for (int literal : clause) {
        int variable = Math.abs(literal);
        Boolean value = assignment[variable];
        if (value == null) {
          newClause.add(literal);
        } else {
          boolean literalValue = (literal > 0 && value) || (literal < 0 && !value);
          if (literalValue) {
            clauseSatisfied = true;
            break;
          }
        }
      }
      if (clauseSatisfied) continue;
      if (newClause.isEmpty()) return false;
      simplifiedClauses.add(newClause.stream().mapToInt(i -> i).toArray());
    }
    if (simplifiedClauses.isEmpty()) return true;
    for (int[] clause : simplifiedClauses) {
      if (clause.length == 1) {
        int unitLiteral = clause[0];
        int variable = Math.abs(unitLiteral);
        boolean value = unitLiteral > 0;
        Boolean currentValue = assignment[variable];
        if (currentValue != null) {
          if (currentValue != value) return false;
        } else {
          assignment[variable] = value;
          boolean result = dpllRecursive(simplifiedClauses, assignment, numVars);
          if (result) return true;
          assignment[variable] = null;
          return false;
        }
      }
    }
    Map<Integer, Integer> literalPolarity = new HashMap<>();
    for (int[] clause : simplifiedClauses) {
      for (int literal : clause) {
        int variable = Math.abs(literal);
        int polarity = literal > 0 ? 1 : -1;
        literalPolarity.putIfAbsent(variable, 0);
        if (polarity > 0) {
          literalPolarity.put(variable, literalPolarity.get(variable) | 1);
        } else {
          literalPolarity.put(variable, literalPolarity.get(variable) | 2);
        }
      }
    }
    for (Map.Entry<Integer, Integer> entry : literalPolarity.entrySet()) {
      int variable = entry.getKey();
      int polarity = entry.getValue();
      if (assignment[variable] == null && (polarity == 1 || polarity == 2)) {
        assignment[variable] = (polarity == 1);
        boolean result = dpllRecursive(simplifiedClauses, assignment, numVars);
        if (result) return true;
        assignment[variable] = null;
        return false;
      }
    }
    Map<Integer, Integer> variableFrequency = new HashMap<>();
    for (int[] clause : simplifiedClauses) {
      for (int literal : clause) {
        int variable = Math.abs(literal);
        if (assignment[variable] == null) {
          variableFrequency.put(variable, variableFrequency.getOrDefault(variable, 0) + 1);
        }
      }
    }
    if (variableFrequency.isEmpty()) return true;
    int chosenVariable = Collections.max(variableFrequency.entrySet(),
            Map.Entry.comparingByValue()).getKey();
    for (boolean value : new boolean[]{true, false}) {
      assignment[chosenVariable] = value;
      boolean result = dpllRecursive(simplifiedClauses, assignment, numVars);
      if (result) return true;
      assignment[chosenVariable] = null;
      if (cancelRequested.get()) return false;
    }
    return false;
  }

  static class LineNumberView extends JComponent implements DocumentListener {
    private final JTextArea textArea;
    private FontMetrics fontMetrics;
    private final int MARGIN = 8;
    private Color backgroundColor = Color.decode("#2b2b2b");
    private Color foregroundColor = Color.decode("#858588");

    public LineNumberView(JTextArea textArea) {
      this.textArea = textArea;
      this.textArea.getDocument().addDocumentListener(this);
      this.textArea.addCaretListener(e -> repaint());
      setFont(new Font(CODE_FONT_NAME, Font.PLAIN, CODE_FONT_SIZE - 1));
      this.fontMetrics = getFontMetrics(getFont());
      setPreferredSize(new Dimension(50, Integer.MAX_VALUE));
      setOpaque(true);
    }

    public void setColors(Color bg, Color fg) {
      this.backgroundColor = bg;
      this.foregroundColor = fg;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Rectangle clipBounds = g.getClipBounds();
      g.setColor(backgroundColor);
      g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
      g.setColor(foregroundColor);
      this.fontMetrics = getFontMetrics(getFont());
      try {
        int startOffset = textArea.viewToModel2D(new Point(0, clipBounds.y));
        int endOffset = textArea.viewToModel2D(new Point(0, clipBounds.y + clipBounds.height));
        int startLine = textArea.getLineOfOffset(startOffset);
        int endLine = textArea.getLineOfOffset(endOffset);
        for (int line = startLine; line <= endLine; line++) {
          int lineStartOffset = textArea.getLineStartOffset(line);
          Rectangle lineRect = textArea.modelToView2D(lineStartOffset).getBounds();
          String lineNumber = String.valueOf(line + 1);
          int x = getWidth() - MARGIN - fontMetrics.stringWidth(lineNumber);
          int y = lineRect.y + fontMetrics.getAscent();
          g.drawString(lineNumber, x, y);
        }
      } catch (Exception ignored) {}
    }

    @Override
    public void insertUpdate(DocumentEvent e) { repaint(); }
    @Override
    public void removeUpdate(DocumentEvent e) { repaint(); }
    @Override
    public void changedUpdate(DocumentEvent e) { repaint(); }
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {}
    SwingUtilities.invokeLater(() -> new MinntSATSolver().setVisible(true));
  }
}
