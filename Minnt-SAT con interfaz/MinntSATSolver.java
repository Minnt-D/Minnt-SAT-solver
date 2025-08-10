import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MinntSATSolver extends JFrame {
  private JTextArea editor;
  private JTextArea resultArea;
  private JProgressBar progressBar;
  private JLabel statusLabel;
  private JButton solveButton;
  private AtomicBoolean cancelRequested = new AtomicBoolean(false);
  private boolean darkMode = true;

  public MinntSATSolver() {
    setTitle("Minnt SAT Solver");
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH); 
    buildUI();
    applyTheme();
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        onClose();
      }
    });
  }

  private void buildUI() {
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("Archivo");
    JMenuItem openItem = new JMenuItem("Abrir CNF...");
    openItem.addActionListener(e -> loadCNFFromFile());
    fileMenu.add(openItem);
    fileMenu.addSeparator();
    JMenuItem exitItem = new JMenuItem("Salir");
    exitItem.addActionListener(e -> onClose());
    fileMenu.add(exitItem);
    menuBar.add(fileMenu);

    JMenu configMenu = new JMenu("Configuración");
    JMenuItem toggleTheme = new JMenuItem("Cambiar a tema claro");
    toggleTheme.addActionListener(e -> {
      darkMode = !darkMode;
      applyTheme();
      toggleTheme.setText(darkMode ? "Cambiar a tema claro" : "Cambiar a tema oscuro");
    });
    configMenu.add(toggleTheme);
    menuBar.add(configMenu);

    JMenu helpMenu = new JMenu("Ayuda");
    JMenuItem aboutItem = new JMenuItem("Acerca de");
    aboutItem.addActionListener(e -> showAbout());
    helpMenu.add(aboutItem);
    menuBar.add(helpMenu);

    setJMenuBar(menuBar);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    split.setResizeWeight(0.55);

    editor = new JTextArea();
    editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    editor.setTabSize(4);
    JScrollPane editorScroll = new JScrollPane(editor);
    editorScroll.setRowHeaderView(new LineNumberView(editor));
    JPanel leftPanel = new JPanel(new BorderLayout(6,6));
    leftPanel.setBorder(new EmptyBorder(8,8,8,4));
    leftPanel.add(new JLabel("Editor CNF"), BorderLayout.NORTH);
    leftPanel.add(editorScroll, BorderLayout.CENTER);

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    JButton loadBtn = new JButton("Abrir .cnf");
    loadBtn.addActionListener(e -> loadCNFFromFile());
    solveButton = new JButton("Resolver");
    solveButton.addActionListener(e -> startSolve());
    JButton clearBtn = new JButton("Limpiar");
    clearBtn.addActionListener(e -> { editor.setText(""); resultArea.setText(""); statusLabel.setText("Listo"); });
    btnPanel.add(loadBtn);
    btnPanel.add(solveButton);
    btnPanel.add(clearBtn);
    leftPanel.add(btnPanel, BorderLayout.SOUTH);

    resultArea = new JTextArea();
    resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    resultArea.setEditable(false);
    JScrollPane resultScroll = new JScrollPane(resultArea);
    JPanel rightPanel = new JPanel(new BorderLayout(6,6));
    rightPanel.setBorder(new EmptyBorder(8,4,8,8));
    rightPanel.add(new JLabel("Resultado"), BorderLayout.NORTH);
    rightPanel.add(resultScroll, BorderLayout.CENTER);

    JPanel bottom = new JPanel(new BorderLayout(6,6));
    progressBar = new JProgressBar();
    progressBar.setIndeterminate(false);
    progressBar.setVisible(false);
    statusLabel = new JLabel("Listo");
    bottom.add(progressBar, BorderLayout.CENTER);
    bottom.add(statusLabel, BorderLayout.WEST);

    split.setLeftComponent(leftPanel);
    split.setRightComponent(rightPanel);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(split, BorderLayout.CENTER);
    getContentPane().add(bottom, BorderLayout.SOUTH);

    editor.setText("// Example DIMACS (comments with // or c are ignored)\n" +
            "p cnf 3 2\n" +
            "1 -3 0\n" +
            "2 3 -1 0\n");

    editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "open");
    editor.getActionMap().put("open", new AbstractAction(){ public void actionPerformed(ActionEvent e){ loadCNFFromFile(); }});
    editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "run");
    editor.getActionMap().put("run", new AbstractAction(){ public void actionPerformed(ActionEvent e){ startSolve(); }});
  }

  private void applyTheme() {
    Color bg = darkMode ? Color.decode("#1e1e1e") : Color.decode("#ffffff");
    Color fg = darkMode ? Color.decode("#d4d4d4") : Color.decode("#000000");
    Color secondary = darkMode ? Color.decode("#252526") : Color.decode("#f3f3f3");
    Color accent = Color.decode("#0078d7");

    getContentPane().setBackground(bg);
    editor.setBackground(secondary);
    editor.setForeground(fg);
    resultArea.setBackground(secondary);
    resultArea.setForeground(fg);
    statusLabel.setBackground(accent);
    statusLabel.setForeground(Color.white);
    SwingUtilities.updateComponentTreeUI(this);
  }

  private void showAbout() {
    String about = "Minnt SAT Solver - Java\n" +
            "Versión: 1.0\n" +
            "Algoritmo: DPLL optimizado (propagación de unidades, literales puros, heurística por frecuencia)\n\n" +
            "Uso:\n" +
            "- Pegue o cargue archivos en formato DIMACS CNF.\n" +
            "- Comentarios soportados con 'c' o '//' (son ignorados al parsear).\n" +
            "- Pulse 'Resolver' para ejecutar el solver; el resultado aparecerá a la derecha.\n\n" +
            "Salida:\n" +
            "- Muestra SAT/UNSAT\n" +
            "- Asignación en formato DIMACS (ej: 1 2 -3)\n" +
            "- Valores de cada variable\n" +
            "- Tiempo de procesado\n\n" +
            "Desarrollado por Kari y Minnt.";
    JOptionPane.showMessageDialog(this, about, "Acerca de Minnt SAT Solver", JOptionPane.INFORMATION_MESSAGE);
  }

  private void onClose() {
    if (solveButton.getText().equals("Cancelar")) {
      int o = JOptionPane.showConfirmDialog(this, "Resolución en curso. ¿Desea cancelar y salir?", "Salir", JOptionPane.YES_NO_OPTION);
      if (o != JOptionPane.YES_OPTION) return;
    }
    dispose();
    System.exit(0);
  }

  private void loadCNFFromFile() {
    JFileChooser fc = new JFileChooser();
    int ret = fc.showOpenDialog(this);
    if (ret == JFileChooser.APPROVE_OPTION) {
      File f = fc.getSelectedFile();
      try {
        byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
        String s = new String(bytes, StandardCharsets.UTF_8);
        editor.setText(s);
        statusLabel.setText("Archivo cargado: " + f.getName());
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "Error al leer el archivo:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void startSolve() {
    if (solveButton.getText().equals("Cancelar")) {
      cancelRequested.set(true);
      statusLabel.setText("Cancelando...");
      return;
    }
    solveButton.setText("Cancelar");
    progressBar.setIndeterminate(true);
    progressBar.setVisible(true);
    resultArea.setText("");
    cancelRequested.set(false);

    new Thread(() -> {
      try {
        runSolveTask();
      } finally {
        SwingUtilities.invokeLater(() -> {
          progressBar.setIndeterminate(false);
          progressBar.setVisible(false);
          solveButton.setText("Resolver");
        });
      }
    }, "solver-thread").start();
  }

  private void runSolveTask() {
    String text = editor.getText();
    long t0 = System.nanoTime();

    String header = null;
    for (String line : text.split("\\R")) {
      String l = line.trim();
      if (l.startsWith("p ")) { header = l; break; }
    }
    if (header == null) {
      SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Falta línea 'p cnf' en el archivo.", "Formato inválido", JOptionPane.ERROR_MESSAGE));
      return;
    }
    String[] parts = header.split("\\s+");
    if (parts.length < 4) {
      SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Cabecera 'p cnf' inválida.", "Formato inválido", JOptionPane.ERROR_MESSAGE));
      return;
    }
    int numVars;
    int numClauses;
    try {
      numVars = Integer.parseInt(parts[2]);
      numClauses = Integer.parseInt(parts[3]);
    } catch (NumberFormatException e) {
      SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Cabecera 'p cnf' con números inválidos.", "Formato inválido", JOptionPane.ERROR_MESSAGE));
      return;
    }

    List<int[]> clauses = parseDimacsToIntList(text);

    boolean[] solution = solveDPLL(clauses, numVars);

    double elapsed = (System.nanoTime() - t0) / 1e9;

    final int nv = numVars;
    SwingUtilities.invokeLater(() -> {
      resultArea.setText("");
      if (solution != null) {
        StringBuilder assignSb = new StringBuilder();
        for (int i = 1; i <= nv; i++) {
          assignSb.append(solution[i] ? i : -i);
          if (i < nv) assignSb.append(" ");
        }
        resultArea.append("SAT\n\n");
        resultArea.append("Asignación: " + assignSb.toString() + "\n\n");
        for (int i = 1; i <= nv; i++) {
          resultArea.append(i + " = " + (solution[i] ? "True" : "False") + "\n");
        }
      } else {
        resultArea.append("UNSAT\n");
      }
      resultArea.append(String.format("\nVelocidad de procesado: %.4f seg\n", elapsed));
      statusLabel.setText(solution != null ? "SAT" : "UNSAT");
    });
  }

  private List<int[]> parseDimacsToIntList(String dimacs) {
    List<int[]> out = new ArrayList<>();
    for (String raw : dimacs.split("\\R")) {
      String line = raw.trim();
      if (line.isEmpty()) continue;
      if (line.startsWith("c") || line.startsWith("//") || line.startsWith("p")) continue;
      String[] tokens = line.split("\\s+");
      List<Integer> lits = new ArrayList<>();
      for (String t : tokens) {
        if (t.equals("0")) break;
        try {
          int v = Integer.parseInt(t);
          lits.add(v);
        } catch (NumberFormatException ignore) {}
      }
      if (!lits.isEmpty()) {
        int[] arr = new int[lits.size()];
        for (int i = 0; i < lits.size(); i++) arr[i] = lits.get(i);
        out.add(arr);
      }
    }
    return out;
  }

  private boolean[] solveDPLL(List<int[]> clausesList, int numVars) {
    List<int[]> clauses = new ArrayList<>(clausesList);

    Boolean[] assignment = new Boolean[numVars + 1];

    return dpllRecursive(clauses, assignment, numVars) ? toPrimitiveBool(assignment) : null;
  }

  private boolean[] toPrimitiveBool(Boolean[] arr) {
    boolean[] out = new boolean[arr.length];
    for (int i = 0; i < arr.length; i++) out[i] = arr[i] != null && arr[i];
    return out;
  }

  private boolean dpllRecursive(List<int[]> clauses, Boolean[] assignment, int numVars) {
    if (cancelRequested.get()) return false;

    List<int[]> simplified = new ArrayList<>();
    for (int[] clause : clauses) {
      boolean satisfied = false;
      List<Integer> newClause = new ArrayList<>();
      for (int lit : clause) {
        int v = Math.abs(lit);
        Boolean val = assignment[v];
        if (val == null) {
          newClause.add(lit);
        } else {
          if ((lit > 0 && val) || (lit < 0 && !val)) {
            satisfied = true;
            break;
          }
        }
      }
      if (satisfied) continue;
      if (newClause.isEmpty()) return false;
      simplified.add(newClause.stream().mapToInt(i->i).toArray());
    }

    if (simplified.isEmpty()) return true; 

    for (int[] c : simplified) {
      if (c.length == 1) {
        int lit = c[0];
        int var = Math.abs(lit);
        Boolean cur = assignment[var];
        boolean val = lit > 0;
        if (cur != null) {
          if (cur != val) return false;
        } else {
          assignment[var] = val;
          boolean ok = dpllRecursive(simplified, assignment, numVars);
          if (ok) return true;
          assignment[var] = null;
          return false;
        }
      }
    }

    Map<Integer, Integer> polarity = new HashMap<>();
    for (int[] c : simplified) for (int lit : c) {
      int v = Math.abs(lit);
      int sign = lit > 0 ? 1 : -1;
      polarity.putIfAbsent(v, 0);
      polarity.put(v, polarity.get(v) | (sign == 1 ? 1 : 2));
    }
    for (Map.Entry<Integer,Integer> e : polarity.entrySet()) {
      int v = e.getKey();
      int mask = e.getValue();
      if (assignment[v] == null && (mask == 1 || mask == 2)) {
        assignment[v] = (mask == 1);
        boolean ok = dpllRecursive(simplified, assignment, numVars);
        if (ok) return true;
        assignment[v] = null;
        return false;
      }
    }

    Map<Integer, Integer> freq = new HashMap<>();
    for (int[] c : simplified) for (int lit : c) {
      int v = Math.abs(lit);
      if (assignment[v] == null) freq.put(v, freq.getOrDefault(v, 0) + 1);
    }
    if (freq.isEmpty()) return true;
    int choose = Collections.max(freq.entrySet(), Map.Entry.comparingByValue()).getKey();

    for (boolean val : new boolean[]{true, false}) {
      assignment[choose] = val;
      boolean ok = dpllRecursive(simplified, assignment, numVars);
      if (ok) return true;
      assignment[choose] = null;
      if (cancelRequested.get()) return false;
    }
    return false;
  }

  private void showResultSync(String text) {
    resultArea.setText(text);
  }

  static class LineNumberView extends JComponent implements DocumentListener {
    private final JTextArea textArea;
    private final FontMetrics fontMetrics;
    private final int MARGIN = 6;
    public LineNumberView(JTextArea textArea) {
      this.textArea = textArea;
      this.textArea.getDocument().addDocumentListener(this);
      this.textArea.addCaretListener(e -> repaint());
      this.fontMetrics = getFontMetrics(new Font(Font.MONOSPACED, Font.PLAIN, 12));
      setPreferredSize(new Dimension(40, Integer.MAX_VALUE));
    }
    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Rectangle clip = g.getClipBounds();
      g.setColor(Color.decode("#2b2b2b"));
      g.fillRect(clip.x, clip.y, clip.width, clip.height);
      g.setColor(Color.decode("#858585"));
      int startOffset = textArea.viewToModel2D(new Point(0, clip.y));
      int endOffset = textArea.viewToModel2D(new Point(0, clip.y + clip.height));
      try {
        int startLine = textArea.getLineOfOffset(startOffset);
        int endLine = textArea.getLineOfOffset(endOffset);
        for (int line = startLine; line <= endLine; line++) {
          int y = textArea.modelToView2D(textArea.getLineStartOffset(line)).getBounds().y;
          String num = String.valueOf(line + 1);
          int x = getWidth() - MARGIN - fontMetrics.stringWidth(num);
          g.drawString(num, x, y + fontMetrics.getAscent());
        }
      } catch (Exception ex) {
      }
    }
    @Override public void insertUpdate(DocumentEvent e) { repaint(); }
    @Override public void removeUpdate(DocumentEvent e) { repaint(); }
    @Override public void changedUpdate(DocumentEvent e) { repaint(); }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      MinntSATSolver app = new MinntSATSolver();
      app.setVisible(true);
    });
  }
}

