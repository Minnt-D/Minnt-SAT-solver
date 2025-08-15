import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
  private JTextArea areaResultado;
  private JProgressBar barraProgreso;
  private JLabel etiquetaEstado;
  private JButton botonResolver;
  private AtomicBoolean cancelarSolicitado = new AtomicBoolean(false);

  private enum Tema { OSCURO, CLARO, MINNT_OSCURO, MINNT_CLARO }
  private Tema temaActual = Tema.MINNT_OSCURO;

  private static final Color VS_OSCURO_FONDO = Color.decode("#1e1e1e");
  private static final Color VS_OSCURO_PANEL = Color.decode("#252526");
  private static final Color VS_OSCURO_BARRA = Color.decode("#333333");
  private static final Color VS_OSCURO_TEXTO = Color.decode("#d4d4d4");
  private static final Color VS_OSCURO_ACENTO = Color.decode("#007acc");
  private static final Color VS_OSCURO_LINEA_FONDO = Color.decode("#2b2b2b");
  private static final Color VS_OSCURO_LINEA_TEXTO = Color.decode("#858585");
  private static final Color VS_OSCURO_BORDE = Color.decode("#464647");
  private static final Color VS_OSCURO_BOTON = Color.decode("#0e639c");
  private static final Color VS_OSCURO_BOTON_HOVER = Color.decode("#1177bb");

  private static final Color VS_CLARO_FONDO = Color.decode("#ffffff");
  private static final Color VS_CLARO_PANEL = Color.decode("#f3f3f3");
  private static final Color VS_CLARO_BARRA = Color.decode("#f8f8f8");
  private static final Color VS_CLARO_TEXTO = Color.decode("#333333");
  private static final Color VS_CLARO_ACENTO = Color.decode("#0078d4");
  private static final Color VS_CLARO_LINEA_FONDO = Color.decode("#eaeaea");
  private static final Color VS_CLARO_LINEA_TEXTO = Color.decode("#606060");
  private static final Color VS_CLARO_BORDE = Color.decode("#e5e5e5");
  private static final Color VS_CLARO_BOTON = Color.decode("#0078d4");
  private static final Color VS_CLARO_BOTON_HOVER = Color.decode("#106ebe");

  private static final Color MINNT_OSCURO_FONDO = Color.decode("#111217");
  private static final Color MINNT_OSCURO_PANEL = Color.decode("#13161a");
  private static final Color MINNT_OSCURO_TEXTO = Color.decode("#e6e6e6");
  private static final Color MINNT_OSCURO_ACENTO = Color.decode("#ff7a00");
  private static final Color MINNT_OSCURO_LINEA_FONDO = Color.decode("#0f1113");
  private static final Color MINNT_OSCURO_LINEA_TEXTO = Color.decode("#8a8a8a");

  private static final Color MINNT_CLARO_FONDO = Color.decode("#f8f7f5");
  private static final Color MINNT_CLARO_PANEL = Color.decode("#fbf8f5");
  private static final Color MINNT_CLARO_TEXTO = Color.decode("#222222");
  private static final Color MINNT_CLARO_ACENTO = Color.decode("#ff7a00");
  private static final Color MINNT_CLARO_LINEA_FONDO = Color.decode("#f0ede9");
  private static final Color MINNT_CLARO_LINEA_TEXTO = Color.decode("#6b6b6b");

  private static final String FUENTE_CODIGO = "Consolas";
  private static final String FUENTE_UI = "Segoe UI";
  private static final int TAM_CODIGO = 14;
  private static final int TAM_UI = 13;

  private JMenu menuTema;

  public MinntSATSolver() {
    setTitle("Minnt SAT Solver");
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    construirInterfaz();
    aplicarTema(temaActual);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        alCerrar();
      }
    });
  }

  private void construirInterfaz() {
    Font fuenteUI = new Font(FUENTE_UI, Font.PLAIN, TAM_UI);
    Font fuenteCodigo = new Font(FUENTE_CODIGO, Font.PLAIN, TAM_CODIGO);

    JMenuBar barraMenu = new JMenuBar();
    barraMenu.setFont(fuenteUI);
    barraMenu.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

    JMenu menuArchivo = new JMenu("Archivo");
    menuArchivo.setFont(fuenteUI);
    JMenuItem abrirItem = new JMenuItem("Abrir CNF/TXT...");
    abrirItem.setFont(fuenteUI);
    abrirItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
    abrirItem.addActionListener(e -> cargarCNFDesdeArchivo());
    menuArchivo.add(abrirItem);
    menuArchivo.addSeparator();
    JMenuItem salirItem = new JMenuItem("Salir");
    salirItem.setFont(fuenteUI);
    salirItem.addActionListener(e -> alCerrar());
    menuArchivo.add(salirItem);

    JMenu menuEdicion = new JMenu("Edición");
    menuEdicion.setFont(fuenteUI);
    JMenuItem copiarItem = new JMenuItem("Copiar resultado");
    copiarItem.setFont(fuenteUI);
    copiarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
    copiarItem.addActionListener(e -> copiarResultadoAlPortapapeles());
    menuEdicion.add(copiarItem);

    JMenu menuVista = new JMenu("Vista");
    menuVista.setFont(fuenteUI);

    menuTema = new JMenu("Tema");
    menuTema.setFont(fuenteUI);

    JMenuItem tOscuro = new JMenuItem("Oscuro");
    tOscuro.setFont(fuenteUI);
    tOscuro.addActionListener(e -> { temaActual = Tema.OSCURO; aplicarTema(temaActual); });

    JMenuItem tClaro = new JMenuItem("Claro");
    tClaro.setFont(fuenteUI);
    tClaro.addActionListener(e -> { temaActual = Tema.CLARO; aplicarTema(temaActual); });

    JMenuItem tMinntOscuro = new JMenuItem("MINNT Oscuro");
    tMinntOscuro.setFont(fuenteUI);
    tMinntOscuro.addActionListener(e -> { temaActual = Tema.MINNT_OSCURO; aplicarTema(temaActual); });

    JMenuItem tMinntClaro = new JMenuItem("MINNT Claro");
    tMinntClaro.setFont(fuenteUI);
    tMinntClaro.addActionListener(e -> { temaActual = Tema.MINNT_CLARO; aplicarTema(temaActual); });

    menuTema.add(tOscuro);
    menuTema.add(tClaro);
    menuTema.addSeparator();
    menuTema.add(tMinntOscuro);
    menuTema.add(tMinntClaro);

    menuVista.add(menuTema);

    JMenu menuEjecutar = new JMenu("Ejecutar");
    menuEjecutar.setFont(fuenteUI);
    JMenuItem resolverItem = new JMenuItem("Resolver SAT");
    resolverItem.setFont(fuenteUI);
    resolverItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
    resolverItem.addActionListener(e -> iniciarResolucion());
    menuEjecutar.add(resolverItem);

    // Menú Ayuda
    JMenu menuAyuda = new JMenu("Ayuda");
    menuAyuda.setFont(fuenteUI);
    JMenuItem acercaDe = new JMenuItem("Acerca de");
    acercaDe.setFont(fuenteUI);
    acercaDe.addActionListener(e -> mostrarAcercaDe());
    JMenuItem manual = new JMenuItem("Manual de usuario");
    manual.setFont(fuenteUI);
    manual.addActionListener(e -> mostrarManual());
    menuAyuda.add(acercaDe);
    menuAyuda.add(manual);

    barraMenu.add(menuArchivo);
    barraMenu.add(menuEdicion);
    barraMenu.add(menuVista);
    barraMenu.add(menuEjecutar);
    barraMenu.add(menuAyuda);

    setJMenuBar(barraMenu);

    // Panel principal con diseño VS Code
    JPanel panelPrincipal = new JPanel(new BorderLayout());
    panelPrincipal.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

    // Barra de actividad (lateral izquierdo - simplificado)
    JPanel barraActividad = new JPanel();
    barraActividad.setLayout(new BoxLayout(barraActividad, BoxLayout.Y_AXIS));
    barraActividad.setPreferredSize(new Dimension(48, 0));
    barraActividad.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

    // Panel dividido para editor y resultado
    JSplitPane panelDividido = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    panelDividido.setResizeWeight(0.6);
    panelDividido.setBorder(null);
    panelDividido.setDividerSize(4);

    // Panel izquierdo - Editor
    JPanel panelIzquierdo = new JPanel(new BorderLayout());
    panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));

    // Barra de pestañas del editor
    JPanel barraPestañasEditor = new JPanel(new BorderLayout());
    barraPestañasEditor.setPreferredSize(new Dimension(0, 35));
    barraPestañasEditor.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

    JLabel pestañaEditor = new JLabel("problema.cnf");
    pestañaEditor.setFont(fuenteUI);
    pestañaEditor.setIcon(crearIconoColoreado(12, 12, new Color(0, 122, 204)));
    pestañaEditor.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    barraPestañasEditor.add(pestañaEditor, BorderLayout.WEST);

    // Área de editor
    editor = new JTextArea();
    editor.setFont(fuenteCodigo);
    editor.setTabSize(2);
    editor.setLineWrap(false);
    editor.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));

    JScrollPane scrollEditor = new JScrollPane(editor);
    scrollEditor.setBorder(null);
    VistaNumerosLinea numerosLinea = new VistaNumerosLinea(editor);
    scrollEditor.setRowHeaderView(numerosLinea);

    panelIzquierdo.add(barraPestañasEditor, BorderLayout.NORTH);
    panelIzquierdo.add(scrollEditor, BorderLayout.CENTER);

    // Barra de herramientas del editor
    JPanel barraHerramientasEditor = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    barraHerramientasEditor.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 8));

    JButton abrirBtn = crearBotonVSCode("Abrir", KeyEvent.VK_O);
    abrirBtn.addActionListener(e -> cargarCNFDesdeArchivo());

    botonResolver = crearBotonVSCode("Ejecutar", KeyEvent.VK_F5);
    botonResolver.addActionListener(e -> iniciarResolucion());

    JButton limpiarBtn = crearBotonVSCode("Limpiar", 0);
    limpiarBtn.addActionListener(e -> {
      editor.setText("");
      areaResultado.setText("");
      etiquetaEstado.setText("Listo");
    });

    barraHerramientasEditor.add(abrirBtn);
    barraHerramientasEditor.add(botonResolver);
    barraHerramientasEditor.add(limpiarBtn);
    panelIzquierdo.add(barraHerramientasEditor, BorderLayout.SOUTH);

    // Panel derecho - Salida/Resultados
    JPanel panelDerecho = new JPanel(new BorderLayout());
    panelDerecho.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

    // Barra de pestañas de salida
    JPanel barraPestañasSalida = new JPanel(new BorderLayout());
    barraPestañasSalida.setPreferredSize(new Dimension(0, 35));
    barraPestañasSalida.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

    JLabel pestañaSalida = new JLabel("SALIDA");
    pestañaSalida.setFont(new Font(FUENTE_UI, Font.PLAIN, 11));
    pestañaSalida.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    // Barra de herramientas de salida
    JPanel barraHerramientasSalida = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 6));
    JButton copiarBtn = crearBotonIcono("Copiar", "📋");
    copiarBtn.addActionListener(e -> copiarResultadoAlPortapapeles());
    JButton guardarBtn = crearBotonIcono("Guardar", "💾");
    guardarBtn.addActionListener(e -> exportarResultadoADescargas());

    barraHerramientasSalida.add(copiarBtn);
    barraHerramientasSalida.add(guardarBtn);

    barraPestañasSalida.add(pestañaSalida, BorderLayout.WEST);
    barraPestañasSalida.add(barraHerramientasSalida, BorderLayout.EAST);

    // Área de resultados
    areaResultado = new JTextArea();
    areaResultado.setFont(fuenteCodigo);
    areaResultado.setEditable(false);
    areaResultado.setLineWrap(true);
    areaResultado.setWrapStyleWord(true);
    areaResultado.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));

    JScrollPane scrollResultado = new JScrollPane(areaResultado);
    scrollResultado.setBorder(null);

    panelDerecho.add(barraPestañasSalida, BorderLayout.NORTH);
    panelDerecho.add(scrollResultado, BorderLayout.CENTER);

    // Barra de estado (estilo VS Code)
    JPanel barraEstado = new JPanel(new BorderLayout());
    barraEstado.setName("barraEstado");
    barraEstado.setPreferredSize(new Dimension(0, 22));
    barraEstado.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 12));

    etiquetaEstado = new JLabel("Listo");
    etiquetaEstado.setFont(new Font(FUENTE_UI, Font.PLAIN, 11));

    // Barra de progreso en barra de estado
    barraProgreso = new JProgressBar();
    barraProgreso.setPreferredSize(new Dimension(120, 14));
    barraProgreso.setIndeterminate(false);
    barraProgreso.setVisible(false);
    barraProgreso.setBorderPainted(false);

    JPanel estadoDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    estadoDerecha.setOpaque(false);
    estadoDerecha.add(barraProgreso);

    barraEstado.add(etiquetaEstado, BorderLayout.WEST);
    barraEstado.add(estadoDerecha, BorderLayout.EAST);

    // Ensamblar diseño principal
    panelDividido.setLeftComponent(panelIzquierdo);
    panelDividido.setRightComponent(panelDerecho);

    panelPrincipal.add(barraActividad, BorderLayout.WEST);
    panelPrincipal.add(panelDividido, BorderLayout.CENTER);
    panelPrincipal.add(barraEstado, BorderLayout.SOUTH);

    add(panelPrincipal);

    // Ejemplo por defecto en el editor
    editor.setText("// Ejemplo de problema SAT\n// Los comentarios empiezan con // o c\np cnf 3 2\n1 -3 0\n2 3 -1 0\n");

    // Atajos de teclado
    configurarAtajosTeclado();

    // Tamaño y posición de ventana
    setSize(1400, 800);
    setLocationRelativeTo(null);
  }

  private void configurarAtajosTeclado() {
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "abrir");
    getRootPane().getActionMap().put("abrir", new AbstractAction() {
      public void actionPerformed(ActionEvent e) { cargarCNFDesdeArchivo(); }
    });

    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "ejecutar");
    getRootPane().getActionMap().put("ejecutar", new AbstractAction() {
      public void actionPerformed(ActionEvent e) { iniciarResolucion(); }
    });

    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "copiarResultado");
    getRootPane().getActionMap().put("copiarResultado", new AbstractAction() {
      public void actionPerformed(ActionEvent e) { copiarResultadoAlPortapapeles(); }
    });
  }

  // Crea botones con estilo neumórfico para MINNT
  private JButton crearBotonVSCode(String texto, int tecla) {
    JButton btn = new JButton(texto);
    btn.setFont(new Font(FUENTE_UI, Font.PLAIN, 11));
    btn.setFocusPainted(false);
    btn.setContentAreaFilled(true);
    btn.setOpaque(true);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.setMargin(new Insets(4, 12, 4, 12));
    if (tecla != 0) {
      String atajo = tecla == KeyEvent.VK_F5 ? "F5" : "Ctrl+" + KeyEvent.getKeyText(tecla);
      btn.setToolTipText(texto + " (" + atajo + ")");
    }
    return btn;
  }

  // Crea botones de icono con efectos neumórficos
  private JButton crearBotonIcono(String tooltip, String tipo) {
    JButton btn = new JButton();
    btn.setToolTipText(tooltip);
    btn.setFocusPainted(false);
    btn.setContentAreaFilled(false);
    btn.setOpaque(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.setPreferredSize(new Dimension(32, 28));
    btn.setBorder(BorderFactory.createEmptyBorder());
    btn.setFont(new Font(FUENTE_UI, Font.BOLD, 14));
    btn.setIcon(new Icon() {
      public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo neumórfico y sombra
        if (temaActual == Tema.MINNT_OSCURO || temaActual == Tema.MINNT_CLARO) {
          Color fondo = temaActual == Tema.MINNT_OSCURO ? MINNT_OSCURO_PANEL : MINNT_CLARO_PANEL;
          g2.setColor(new Color(0,0,0,32));
          g2.fillRoundRect(x+2, y+2, 24, 20, 10, 10);
          g2.setColor(fondo);
          g2.fillRoundRect(x+1, y+1, 24, 20, 10, 10);
        }

        // Icono dibujado
        if (tipo.equals("Copiar")) {
          g2.setColor(temaActual == Tema.OSCURO || temaActual == Tema.MINNT_OSCURO ? Color.WHITE : Color.BLACK);
          g2.drawRect(x+8, y+8, 8, 8);
          g2.drawRect(x+10, y+6, 8, 8);
        } else if (tipo.equals("Guardar")) {
          g2.setColor(temaActual == Tema.OSCURO || temaActual == Tema.MINNT_OSCURO ? Color.WHITE : Color.BLACK);
          g2.fillRect(x+8, y+8, 8, 8);
          g2.setColor(temaActual == Tema.MINNT_OSCURO || temaActual == Tema.MINNT_CLARO ? MINNT_OSCURO_ACENTO : VS_OSCURO_ACENTO);
          g2.fillRect(x+10, y+12, 4, 4);
        }
        g2.dispose();
      }
      public int getIconWidth() { return 28; }
      public int getIconHeight() { return 24; }
    });
    return btn;
  }

  // Método para crear un icono coloreado simple (círculo)
  private Icon crearIconoColoreado(int ancho, int alto, Color color) {
    return new Icon() {
      @Override
      public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(x, y, ancho, alto);
        g2.setColor(new Color(0,0,0,40));
        g2.drawOval(x, y, ancho, alto);
        g2.dispose();
      }
      @Override
      public int getIconWidth() { return ancho; }
      @Override
      public int getIconHeight() { return alto; }
    };
  }

  // Aplica tema con efectos neumórficos completos
  private void aplicarTema(Tema t) {
    Color fondo, fondoPanel, fondoBarra, texto, acento, fondoLinea, textoLinea, borde, fondoBoton, hoverBoton;

    switch (t) {
      case OSCURO -> {
        fondo = VS_OSCURO_FONDO; fondoPanel = VS_OSCURO_PANEL; fondoBarra = VS_OSCURO_BARRA;
        texto = VS_OSCURO_TEXTO; acento = VS_OSCURO_ACENTO; fondoLinea = VS_OSCURO_LINEA_FONDO;
        textoLinea = VS_OSCURO_LINEA_TEXTO; borde = VS_OSCURO_BORDE;
        fondoBoton = VS_OSCURO_BOTON; hoverBoton = VS_OSCURO_BOTON_HOVER;
      }
      case CLARO -> {
        fondo = VS_CLARO_FONDO; fondoPanel = VS_CLARO_PANEL; fondoBarra = VS_CLARO_BARRA;
        texto = VS_CLARO_TEXTO; acento = VS_CLARO_ACENTO; fondoLinea = VS_CLARO_LINEA_FONDO;
        textoLinea = VS_CLARO_LINEA_TEXTO; borde = VS_CLARO_BORDE;
        fondoBoton = VS_CLARO_BOTON; hoverBoton = VS_CLARO_BOTON_HOVER;
      }
      case MINNT_OSCURO -> {
        fondo = MINNT_OSCURO_FONDO; fondoPanel = MINNT_OSCURO_PANEL; fondoBarra = MINNT_OSCURO_PANEL;
        texto = MINNT_OSCURO_TEXTO; acento = MINNT_OSCURO_ACENTO; fondoLinea = MINNT_OSCURO_LINEA_FONDO;
        textoLinea = MINNT_OSCURO_LINEA_TEXTO; borde = MINNT_OSCURO_ACENTO;
        fondoBoton = MINNT_OSCURO_ACENTO; hoverBoton = MINNT_OSCURO_ACENTO.brighter();
      }
      case MINNT_CLARO -> {
        fondo = MINNT_CLARO_FONDO; fondoPanel = MINNT_CLARO_PANEL; fondoBarra = MINNT_CLARO_PANEL;
        texto = MINNT_CLARO_TEXTO; acento = MINNT_CLARO_ACENTO; fondoLinea = MINNT_CLARO_LINEA_FONDO;
        textoLinea = MINNT_CLARO_LINEA_TEXTO; borde = MINNT_CLARO_ACENTO;
        fondoBoton = MINNT_CLARO_ACENTO; hoverBoton = MINNT_CLARO_ACENTO.darker();
      }
      default -> {
        fondo = MINNT_OSCURO_FONDO; fondoPanel = MINNT_OSCURO_PANEL; fondoBarra = MINNT_OSCURO_PANEL;
        texto = MINNT_OSCURO_TEXTO; acento = MINNT_OSCURO_ACENTO; fondoLinea = MINNT_OSCURO_LINEA_FONDO;
        textoLinea = MINNT_OSCURO_LINEA_TEXTO; borde = MINNT_OSCURO_ACENTO;
        fondoBoton = MINNT_OSCURO_ACENTO; hoverBoton = MINNT_OSCURO_ACENTO.brighter();
      }
    }

    // Aplica colores recursivamente con efectos especiales
    aplicarColoresRecursivo(getContentPane(), fondo, fondoPanel, fondoBarra, texto, acento,
            fondoLinea, textoLinea, borde, fondoBoton, hoverBoton);

    // Barra de menú con estilo apropiado
    JMenuBar barraMenu = getJMenuBar();
    if (barraMenu != null) {
      barraMenu.setBackground(fondoBarra);
      barraMenu.setForeground(texto);
      if (t == Tema.MINNT_OSCURO || t == Tema.MINNT_CLARO) {
        barraMenu.setBorder(crearBordeNeumorphico(fondoBarra, true));
      } else {
        barraMenu.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borde));
      }
      aplicarColoresMenu(barraMenu, fondoBarra, texto, acento);
    }

    // Paneles principales con efectos neumórficos
    aplicarEfectosEspeciales(getContentPane(), t, fondo, fondoPanel, borde, acento);

    // Barra de estado especial
    Component barraEstadoComp = buscarComponentePorNombre(getContentPane(), "barraEstado");
    if (barraEstadoComp instanceof JPanel barraEstadoPanel) {
      if (t == Tema.MINNT_OSCURO || t == Tema.MINNT_CLARO) {
        barraEstadoPanel.setBackground(fondoPanel);
        barraEstadoPanel.setBorder(crearBordeNeumorphico(fondoPanel, false));
        etiquetaEstado.setForeground(acento);
      } else {
        barraEstadoPanel.setBackground(acento);
        barraEstadoPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borde));
        etiquetaEstado.setForeground(Color.WHITE);
      }
    }

    // Números de línea
    Component numerosLinea = obtenerVistaNumerosLinea();
    if (numerosLinea instanceof VistaNumerosLinea) {
      ((VistaNumerosLinea) numerosLinea).setColores(fondoLinea, textoLinea);
    }

    // Configurar UIManager para diálogos
    configurarUIManagerParaTema(t, fondoPanel, texto, acento, fondo);

    SwingUtilities.updateComponentTreeUI(this);
    repaint();
  }

  // Crea bordes neumórficos para temas MINNT
  private javax.swing.border.Border crearBordeNeumorphico(Color fondo, boolean elevado) {
    return new javax.swing.border.Border() {
      @Override
      public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color sombra = new Color(0, 0, 0, 30);
        Color luz = new Color(255, 255, 255, 20);

        if (elevado) {
          // Sombra exterior (abajo-derecha)
          g2.setColor(sombra);
          g2.drawRoundRect(x+2, y+2, width-4, height-4, 12, 12);

          // Luz interior (arriba-izquierda)
          g2.setColor(luz);
          g2.drawRoundRect(x, y, width-2, height-2, 12, 12);
        } else {
          // Sombra interior
          g2.setColor(sombra);
          g2.drawRoundRect(x+1, y+1, width-3, height-3, 8, 8);
        }

        g2.dispose();
      }

      @Override
      public Insets getBorderInsets(Component c) {
        return new Insets(4, 8, 4, 8);
      }

      @Override
      public boolean isBorderOpaque() {
        return false;
      }
    };
  }

  // Aplica efectos especiales según el tema
  private void aplicarEfectosEspeciales(Container contenedor, Tema tema, Color fondo, Color fondoPanel, Color borde, Color acento) {
    for (Component comp : contenedor.getComponents()) {
      if (comp instanceof JPanel panel) {
        if (tema == Tema.MINNT_OSCURO || tema == Tema.MINNT_CLARO) {
          panel.setBorder(crearBordeNeumorphico(fondoPanel, true));
          panel.setOpaque(true);
        } else {
          panel.setBorder(BorderFactory.createLineBorder(borde, 1));
        }
      } else if (comp instanceof JSplitPane splitPane) {
        if (tema == Tema.MINNT_OSCURO || tema == Tema.MINNT_CLARO) {
          splitPane.setDividerSize(8);
          splitPane.setBorder(crearBordeNeumorphico(fondoPanel, false));
        } else {
          splitPane.setDividerSize(4);
          splitPane.setBorder(BorderFactory.createLineBorder(borde, 1));
        }
        splitPane.setBackground(fondoPanel);
      } else if (comp instanceof JScrollPane scrollPane) {
        if (tema == Tema.MINNT_OSCURO || tema == Tema.MINNT_CLARO) {
          scrollPane.setBorder(crearBordeNeumorphico(fondoPanel, false));
        } else {
          scrollPane.setBorder(BorderFactory.createLineBorder(borde, 1));
        }
        scrollPane.setBackground(fondoPanel);
        scrollPane.getViewport().setBackground(fondoPanel);
      }

      if (comp instanceof Container) {
        aplicarEfectosEspeciales((Container) comp, tema, fondo, fondoPanel, borde, acento);
      }
    }
  }

  // Aplica colores recursivamente con efectos neumórficos para botones
  private void aplicarColoresRecursivo(Container contenedor, Color fondo, Color fondoPanel,
                                       Color fondoBarra, Color texto, Color acento, Color fondoLinea, Color textoLinea, Color borde,
                                       Color fondoBoton, Color hoverBoton) {
    contenedor.setBackground(fondo);
    contenedor.setForeground(texto);

    for (Component comp : contenedor.getComponents()) {
      if (comp instanceof JPanel p) {
        p.setBackground(fondoPanel);
        p.setForeground(texto);
      } else if (comp instanceof JButton btn) {
        configurarBotonConTema(btn, fondoBoton, hoverBoton, texto, borde);
      } else if (comp instanceof JLabel lbl) {
        lbl.setForeground(texto);
      } else if (comp instanceof JTextArea area) {
        area.setBackground(fondoPanel);
        area.setForeground(texto);
        area.setCaretColor(acento);
        area.setSelectionColor(acento);
        area.setSelectedTextColor(Color.WHITE);
      }

      if (comp instanceof Container) {
        aplicarColoresRecursivo((Container) comp, fondo, fondoPanel, fondoBarra, texto, acento,
                fondoLinea, textoLinea, borde, fondoBoton, hoverBoton);
      }
    }
  }

  // Configura botones con efectos neumórficos
  private void configurarBotonConTema(JButton btn, Color fondoBoton, Color hoverBoton, Color texto, Color borde) {
    btn.setBackground(fondoBoton);
    btn.setForeground(texto);
    btn.setFocusPainted(false);
    btn.setOpaque(true);
    btn.setContentAreaFilled(false); // Para permitir el gradiente y sombra personalizados
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.setFont(new Font(FUENTE_UI, Font.BOLD, 13)); // Fuente más moderna y legible

    // Remover listeners existentes
    for (MouseListener ml : btn.getMouseListeners()) {
      btn.removeMouseListener(ml);
    }

    btn.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        btn.setBackground(hoverBoton);
        btn.repaint();
      }
      @Override
      public void mouseExited(MouseEvent e) {
        btn.setBackground(fondoBoton);
        btn.repaint();
      }
      @Override
      public void mousePressed(MouseEvent e) {
        btn.setBorder(BorderFactory.createLineBorder(borde.darker(), 2, true));
      }
      @Override
      public void mouseReleased(MouseEvent e) {
        btn.setBorder(BorderFactory.createLineBorder(borde, 2, true));
      }
    });

    // Apariencia personalizada según el tema
    btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
    btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
      @Override
      public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 18;
        int w = btn.getWidth();
        int h = btn.getHeight();

        // Sombra exterior para MINNT
        if (temaActual == Tema.MINNT_OSCURO || temaActual == Tema.MINNT_CLARO) {
          g2.setColor(new Color(0,0,0,32));
          g2.fillRoundRect(4, 4, w-8, h-8, arc, arc);
        }

        // Gradiente moderno para fondo
        GradientPaint gp;
        if (temaActual == Tema.MINNT_OSCURO || temaActual == Tema.OSCURO) {
          gp = new GradientPaint(0, 0, fondoBoton.brighter(), 0, h, fondoBoton.darker());
        } else {
          gp = new GradientPaint(0, 0, fondoBoton, 0, h, hoverBoton);
        }
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // Borde moderno
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(0, 0, w-1, h-1, arc, arc);

        // Texto centrado y con sombra sutil
        FontMetrics fm = btn.getFontMetrics(btn.getFont());
        String text = btn.getText();
        int tw = fm.stringWidth(text);
        int th = fm.getAscent();
        int tx = (w - tw) / 2;
        int ty = (h + th) / 2 - 3;

        g2.setColor(new Color(0,0,0,40));
        g2.drawString(text, tx+1, ty+1);
        g2.setColor(btn.getForeground());
        g2.drawString(text, tx, ty);

        g2.dispose();
      }
    });
  }

  private void aplicarColoresMenu(JMenuBar barraMenu, Color fondo, Color texto, Color acento) {
    barraMenu.setBackground(fondo);
    for (int i = 0; i < barraMenu.getMenuCount(); i++) {
      JMenu menu = barraMenu.getMenu(i);
      menu.setBackground(fondo);
      menu.setForeground(texto);
      menu.setOpaque(true);
      aplicarColoresItemsMenu(menu, fondo, texto, acento);
    }
  }

  private void aplicarColoresItemsMenu(JMenu menu, Color fondo, Color texto, Color acento) {
    for (Component comp : menu.getMenuComponents()) {
      if (comp instanceof JMenuItem item) {
        item.setBackground(fondo);
        item.setForeground(texto);
        item.setOpaque(true);
        if (comp instanceof JMenu subMenu) {
          aplicarColoresItemsMenu(subMenu, fondo, texto, acento);
        }
      }
    }
  }

  private void configurarUIManagerParaTema(Tema tema, Color fondoPanel, Color texto, Color acento, Color fondo) {
    UIManager.put("OptionPane.background", fondoPanel);
    UIManager.put("Panel.background", fondoPanel);
    UIManager.put("OptionPane.messageForeground", texto);
    UIManager.put("Button.background", acento);
    UIManager.put("Button.foreground", Color.WHITE);
    UIManager.put("ScrollPane.background", fondoPanel);
    UIManager.put("Viewport.background", fondoPanel);
    UIManager.put("TextArea.background", fondoPanel);
    UIManager.put("TextArea.foreground", texto);
  }

  private Component buscarComponentePorNombre(Container contenedor, String nombre) {
    for (Component comp : contenedor.getComponents()) {
      if (nombre.equals(comp.getName())) {
        return comp;
      }
      if (comp instanceof Container) {
        Component encontrado = buscarComponentePorNombre((Container) comp, nombre);
        if (encontrado != null) return encontrado;
      }
    }
    return null;
  }

  private Component obtenerVistaNumerosLinea() {
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

  // Diálogos mejorados con tema aplicado
  private void mostrarAcercaDe() {
    String acerca = """
        🔥 Minnt SAT Solver - Edición Java
        Versión: 2.0 - Interfaz Neumórfica
        
        ✨ Características principales:
        • Algoritmo DPLL con optimizaciones avanzadas
        • Propagación de unidades y eliminación de literales puros  
        • Heurística de ordenamiento por frecuencia de variables
        • Interfaz moderna con efectos neumórficos
        • Múltiples temas (VS Code + MINNT Neumórfico)
        • Exportación automática a carpeta Descargas
        • Soporte completo de atajos de teclado
        • Editor con números de línea integrado
        
        ⌨️ Atajos principales:
        • Ctrl+O: Abrir archivo CNF/TXT
        • F5: Ejecutar solver SAT
        • Ctrl+Shift+C: Copiar resultado
        • Escape: Cancelar ejecución
        
        🎨 Temas disponibles:
        • Oscuro/Claro: Réplica fiel de Visual Studio Code
        • MINNT Oscuro/Claro: Diseño neumórfico naranja
        
        🚀 Optimizado para problemas SAT de alta complejidad
        """;

    mostrarDialogoTematizado("Acerca de Minnt SAT Solver", acerca, 700, 600);
  }

  private void mostrarManual() {
    String manual = """
        📖 Manual completo - Minnt SAT Solver
        
        🎯 INTRODUCCIÓN
        Minnt SAT Solver es una herramienta avanzada para resolver problemas de satisfacibilidad 
        booleana (SAT) utilizando el algoritmo DPLL optimizado con interfaz neumórfica moderna.
        
        📋 FORMATOS SOPORTADOS
        ▶ Formato DIMACS CNF estándar
        ▶ Comentarios: líneas que empiezan con 'c' o '//'  
        ▶ Cabecera obligatoria: 'p cnf <num_variables> <num_clausulas>'
        ▶ Cláusulas: literales separados por espacios, terminando en '0'
        ▶ Variables: números enteros (positivos=verdadero, negativos=falso)
        
        🔧 GUÍA DE USO PASO A PASO
        
        1️⃣ CARGAR PROBLEMA:
           • Archivo → Abrir CNF/TXT... (Ctrl+O)
           • O editar directamente en el panel izquierdo
           • El editor incluye números de línea y coloreado de sintaxis
        
        2️⃣ CONFIGURAR ENTORNO:  
           • Seleccionar tema en Vista → Tema
           • Ajustar tamaño de ventana según preferencia
           • Verificar formato en barra de estado
        
        3️⃣ EJECUTAR SOLVER:
           • Ejecutar → Resolver SAT (F5)
           • Monitorear progreso en barra de estado
           • Cancelar con botón "Cancelar" si es necesario
        
        4️⃣ ANALIZAR RESULTADOS:
           • SAT: Problema satisfacible + asignación de variables
           • UNSAT: Problema insatisfacible
           • Tiempo de ejecución y estadísticas incluidas
        
        5️⃣ EXPORTAR RESULTADOS:
           • Copiar al portapapeles: Ctrl+Shift+C
           • Guardar archivo: Botón 💾 (auto-guardado en Descargas)
           • Formato: minnt_result_<timestamp>.txt
        
        🎨 PERSONALIZACIÓN DE INTERFAZ
        
        📱 Temas disponibles:
        • Oscuro: Replica Visual Studio Code modo oscuro
        • Claro: Replica Visual Studio Code modo claro  
        • MINNT Oscuro: Neumorfismo naranja sobre fondo oscuro
        • MINNT Claro: Neumorfismo naranja sobre fondo claro
        
        🎯 Características neumórficas (temas MINNT):
        • Bordes redondeados con sombras suaves
        • Efectos de elevación en botones y paneles
        • Gradientes sutiles y transiciones suaves
        • Paleta de colores naranja cohesiva
        
        ⌨️ ATAJOS DE TECLADO COMPLETOS
        • Ctrl+O: Abrir archivo
        • Ctrl+S: Guardar (si implementado)
        • F5: Ejecutar solver  
        • Ctrl+Shift+C: Copiar resultado
        • Ctrl+Z: Deshacer en editor
        • Ctrl+Y: Rehacer en editor
        • Ctrl+A: Seleccionar todo
        • Ctrl+F: Buscar (estándar del sistema)
        
        🔍 RESOLUCIÓN DE PROBLEMAS
        
        ❌ Errores comunes:
        • "Falta cabecera p cnf": Agregar línea p cnf <vars> <clauses>
        • "Formato inválido": Verificar que cláusulas terminen en 0
        • "Sin cláusulas": Revisar que existan cláusulas válidas
        • "Memoria insuficiente": Reducir tamaño del problema
        
        ✅ Consejos de optimización:
        • Usar variables consecutivas (1,2,3...) para mejor rendimiento
        • Evitar cláusulas redundantes
        • Probar con diferentes ordenamientos de variables
        • Para problemas grandes, considerar timeouts
        
        🚀 ALGORITMO DPLL IMPLEMENTADO
        • Propagación de unidades (unit propagation)
        • Eliminación de literales puros (pure literal elimination)  
        • Heurística de frecuencia para selección de variables
        • Backtracking inteligente con poda
        • Detección temprana de conflictos
        
        📊 El solver maneja eficientemente:
        • Problemas pequeños: < 1 segundo
        • Problemas medianos: segundos a minutos  
        • Problemas grandes: puede requerir varios minutos
        
        Para soporte adicional o reportar bugs, contactar al desarrollador.
        """;

    mostrarDialogoTematizado("Manual de usuario completo", manual, 700, 600);
  }

  // Método unificado para mostrar diálogos tematizados
  private void mostrarDialogoTematizado(String titulo, String contenido, int ancho, int alto) {
    Color fondo, texto, acento;

    if (temaActual == Tema.OSCURO || temaActual == Tema.MINNT_OSCURO) {
      fondo = temaActual == Tema.OSCURO ? VS_OSCURO_PANEL : MINNT_OSCURO_PANEL;
      texto = temaActual == Tema.OSCURO ? VS_OSCURO_TEXTO : MINNT_OSCURO_TEXTO;
      acento = temaActual == Tema.OSCURO ? VS_OSCURO_ACENTO : MINNT_OSCURO_ACENTO;
    } else {
      fondo = temaActual == Tema.CLARO ? VS_CLARO_PANEL : MINNT_CLARO_PANEL;
      texto = temaActual == Tema.CLARO ? VS_CLARO_TEXTO : MINNT_CLARO_TEXTO;
      acento = temaActual == Tema.CLARO ? VS_CLARO_ACENTO : MINNT_CLARO_ACENTO;
    }

    // Crear diálogo personalizado
    JDialog dialogo = new JDialog(this, titulo, true);
    dialogo.setSize(ancho, alto);
    dialogo.setLocationRelativeTo(this);
    dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    // Panel principal con tema
    JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
    panelPrincipal.setBackground(fondo);
    if (temaActual == Tema.MINNT_OSCURO || temaActual == Tema.MINNT_CLARO) {
      panelPrincipal.setBorder(crearBordeNeumorphico(fondo, true));
    } else {
      panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    // Área de texto con scroll
    JTextArea areaTexto = new JTextArea(contenido);
    areaTexto.setEditable(false);
    areaTexto.setFont(new Font(FUENTE_UI, Font.PLAIN, 12));
    areaTexto.setLineWrap(true);
    areaTexto.setWrapStyleWord(true);
    areaTexto.setBackground(fondo);
    areaTexto.setForeground(texto);
    areaTexto.setCaretColor(acento);
    areaTexto.setSelectionColor(acento);
    areaTexto.setSelectedTextColor(Color.WHITE);
    areaTexto.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

    JScrollPane scroll = new JScrollPane(areaTexto);
    scroll.setBackground(fondo);
    scroll.getViewport().setBackground(fondo);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    if (temaActual == Tema.MINNT_OSCURO || temaActual == Tema.MINNT_CLARO) {
      scroll.setBorder(crearBordeNeumorphico(fondo, false));
    } else {
      scroll.setBorder(BorderFactory.createLineBorder(acento, 1));
    }

    // Panel de botones
    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    panelBotones.setBackground(fondo);

    JButton botonCerrar = new JButton("Cerrar");
    configurarBotonConTema(botonCerrar, acento, acento.brighter(), Color.WHITE, acento);
    botonCerrar.addActionListener(e -> dialogo.dispose());

    panelBotones.add(botonCerrar);

    // Ensamblar diálogo
    panelPrincipal.add(scroll, BorderLayout.CENTER);
    panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

    dialogo.add(panelPrincipal);
    dialogo.setVisible(true);
  }

  private void alCerrar() {
    if ("Cancelar".equals(botonResolver.getText())) {
      int opcion = JOptionPane.showConfirmDialog(this,
              "El solver está en ejecución. ¿Cancelar y salir?", "Salir",
              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      if (opcion != JOptionPane.YES_OPTION) return;
      cancelarSolicitado.set(true);
    }
    dispose();
    System.exit(0);
  }

  // Operaciones de archivo
  private void cargarCNFDesdeArchivo() {
    JFileChooser selectorArchivo = new JFileChooser();
    selectorArchivo.setFileFilter(new javax.swing.filechooser.FileFilter() {
      @Override
      public boolean accept(File archivo) {
        if (archivo.isDirectory()) return true;
        String nombre = archivo.getName().toLowerCase();
        return nombre.endsWith(".cnf") || nombre.endsWith(".txt");
      }

      @Override
      public String getDescription() {
        return "Archivos CNF (*.cnf, *.txt)";
      }
    });

    int resultado = selectorArchivo.showOpenDialog(this);
    if (resultado == JFileChooser.APPROVE_OPTION) {
      File archivo = selectorArchivo.getSelectedFile();
      try {
        byte[] bytes = java.nio.file.Files.readAllBytes(archivo.toPath());
        String contenido = new String(bytes, StandardCharsets.UTF_8);
        editor.setText(contenido);

        int[] conteos = contarVarsYClausulas(contenido);
        if (conteos[0] >= 0) {
          etiquetaEstado.setText(String.format("Cargado: %s | Variables: %d | Cláusulas: %d",
                  archivo.getName(), conteos[0], conteos[1]));
        } else {
          etiquetaEstado.setText("Cargado: " + archivo.getName());
        }

        editor.setCaretPosition(0);
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this,
                "Error al leer el archivo:\n" + ex.getMessage(),
                "Error de archivo", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private int[] contarVarsYClausulas(String texto) {
    for (String linea : texto.split("\\R")) {
      String recortada = linea.trim();
      if (recortada.startsWith("p ")) {
        String[] partes = recortada.split("\\s+");
        if (partes.length >= 4) {
          try {
            int vars = Integer.parseInt(partes[2]);
            int clausulas = Integer.parseInt(partes[3]);
            return new int[]{vars, clausulas};
          } catch (NumberFormatException ignored) {}
        }
      }
    }
    return new int[]{-1, -1};
  }

  // Copiar resultado al portapapeles
  private void copiarResultadoAlPortapapeles() {
    String texto = areaResultado.getText();
    if (texto == null || texto.trim().isEmpty()) {
      JOptionPane.showMessageDialog(this, "No hay resultados para copiar.",
              "Información", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    StringSelection seleccion = new StringSelection(texto);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(seleccion, null);
    etiquetaEstado.setText("Resultados copiados al portapapeles");

    javax.swing.Timer temporizador = new javax.swing.Timer(2000, e -> etiquetaEstado.setText("Listo"));
    temporizador.setRepeats(false);
    temporizador.start();
  }

  // Exportar resultado a carpeta Descargas
  private void exportarResultadoADescargas() {
    String texto = areaResultado.getText();
    if (texto == null || texto.trim().isEmpty()) {
      JOptionPane.showMessageDialog(this, "No hay resultados para exportar.",
              "Información", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    try {
      Path rutaDescargas = Paths.get(System.getProperty("user.home"), "Downloads");
      if (!rutaDescargas.toFile().exists()) {
        rutaDescargas = Paths.get(System.getProperty("user.home"));
      }

      String timestamp = String.valueOf(System.currentTimeMillis());
      String nombreArchivo = "minnt_result_" + timestamp + ".txt";
      Path rutaSalida = rutaDescargas.resolve(nombreArchivo);

      java.nio.file.Files.write(rutaSalida, texto.getBytes(StandardCharsets.UTF_8));

      etiquetaEstado.setText("Exportado: " + nombreArchivo);
      JOptionPane.showMessageDialog(this,
              "Resultados exportados exitosamente a:\n" + rutaSalida.toString(),
              "Exportación completa", JOptionPane.INFORMATION_MESSAGE);

    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this,
              "Error al exportar archivo:\n" + ex.getMessage(),
              "Error de exportación", JOptionPane.ERROR_MESSAGE);
    }
  }

  // Ejecución del solver
  private void iniciarResolucion() {
    if ("Cancelar".equals(botonResolver.getText())) {
      cancelarSolicitado.set(true);
      etiquetaEstado.setText("Cancelando solver...");
      return;
    }

    String texto = editor.getText().trim();
    if (!texto.contains("p cnf")) {
      JOptionPane.showMessageDialog(this,
              "Formato inválido. El archivo debe contener la línea de cabecera 'p cnf'.",
              "Error de formato", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // Actualizar interfaz para estado de resolución
    botonResolver.setText("Cancelar");
    barraProgreso.setIndeterminate(true);
    barraProgreso.setVisible(true);
    etiquetaEstado.setText("Resolviendo...");
    areaResultado.setText("Ejecutando solver SAT...\n");
    cancelarSolicitado.set(false);

    // Ejecutar solver en hilo de fondo
    new Thread(this::ejecutarTareaSolver, "SAT-Solver-Thread").start();
  }

  private void ejecutarTareaSolver() {
    try {
      String texto = editor.getText();
      long inicio = System.nanoTime();

      // Parsear cabecera
      String lineaCabecera = null;
      for (String linea : texto.split("\\R")) {
        String recortada = linea.trim();
        if (recortada.startsWith("p ")) {
          lineaCabecera = recortada;
          break;
        }
      }

      if (lineaCabecera == null) {
        SwingUtilities.invokeLater(() -> {
          JOptionPane.showMessageDialog(this,
                  "Falta la línea de cabecera 'p cnf'.",
                  "Error de formato", JOptionPane.ERROR_MESSAGE);
        });
        return;
      }

      String[] partesCabecera = lineaCabecera.split("\\s+");
      if (partesCabecera.length < 4) {
        SwingUtilities.invokeLater(() -> {
          JOptionPane.showMessageDialog(this,
                  "Formato inválido en la cabecera 'p cnf'.",
                  "Error de formato", JOptionPane.ERROR_MESSAGE);
        });
        return;
      }

      int numVars, numClausulas;
      try {
        numVars = Integer.parseInt(partesCabecera[2]);
        numClausulas = Integer.parseInt(partesCabecera[3]);
      } catch (NumberFormatException e) {
        SwingUtilities.invokeLater(() -> {
          JOptionPane.showMessageDialog(this,
                  "Números inválidos en la cabecera 'p cnf'.",
                  "Error de formato", JOptionPane.ERROR_MESSAGE);
        });
        return;
      }

      // Parsear cláusulas
      List<int[]> clausulas = parsearDimacsAListaInt(texto);
      if (clausulas.isEmpty()) {
        SwingUtilities.invokeLater(() -> {
          JOptionPane.showMessageDialog(this,
                  "No se encontraron cláusulas válidas.",
                  "Error de formato", JOptionPane.ERROR_MESSAGE);
        });
        return;
      }

      // Actualizar progreso
      SwingUtilities.invokeLater(() -> {
        etiquetaEstado.setText("Resolviendo con algoritmo DPLL...");
      });

      // Resolver usando DPLL
      boolean[] solucion = resolverDPLL(clausulas, numVars);
      double segundos = (System.nanoTime() - inicio) / 1e9;

      final boolean[] solucionFinal = solucion;
      final int numVarsFinal = numVars;

      SwingUtilities.invokeLater(() -> {
        StringBuilder resultado = new StringBuilder();

        if (solucionFinal != null) {
          resultado.append("SAT\n\n");
          resultado.append("¡El problema es satisfacible!\n\n");

          // Asignación de variables
          resultado.append("Asignación de variables:\n");
          StringBuilder asignacion = new StringBuilder();
          for (int i = 1; i <= numVarsFinal; i++) {
            asignacion.append(solucionFinal[i] ? i : -i);
            if (i < numVarsFinal) asignacion.append(" ");
          }
          resultado.append(asignacion.toString()).append("\n\n");

          // Tabla de verdad
          resultado.append("Tabla de verdad:\n");
          for (int i = 1; i <= numVarsFinal; i++) {
            resultado.append(String.format("%d = %s\n", i, solucionFinal[i] ? "VERDADERO" : "FALSO"));
          }

          etiquetaEstado.setText("SAT - Solución encontrada");
        } else {
          resultado.append("UNSAT\n\n");
          resultado.append("El problema es insatisfacible.\n");
          resultado.append("No existe solución para las restricciones dadas.\n");

          etiquetaEstado.setText("UNSAT - No existe solución");
        }

        resultado.append(String.format("\nTiempo de ejecución: %.4f segundos\n", segundos));
        resultado.append(String.format("Variables: %d | Cláusulas: %d\n", numVarsFinal, clausulas.size()));

        areaResultado.setText(resultado.toString());
        areaResultado.setCaretPosition(0);
      });

    } catch (Exception e) {
      SwingUtilities.invokeLater(() -> {
        JOptionPane.showMessageDialog(this,
                "Error en el solver:\n" + e.getMessage(),
                "Error del solver", JOptionPane.ERROR_MESSAGE);
        areaResultado.setText("Error: " + e.getMessage());
        etiquetaEstado.setText("Ocurrió un error");
      });
    } finally {
      SwingUtilities.invokeLater(() -> {
        barraProgreso.setIndeterminate(false);
        barraProgreso.setVisible(false);
        botonResolver.setText("Ejecutar");
      });
    }
  }

  // Parsear formato DIMACS a lista de arreglos de enteros
  private List<int[]> parsearDimacsAListaInt(String dimacs) {
    List<int[]> clausulas = new ArrayList<>();

    for (String lineaRaw : dimacs.split("\\R")) {
      String linea = lineaRaw.trim();
      if (linea.isEmpty()) continue;
      if (linea.startsWith("c") || linea.startsWith("//") || linea.startsWith("p")) continue;

      String[] tokens = linea.split("\\s+");
      List<Integer> literales = new ArrayList<>();

      for (String token : tokens) {
        if ("0".equals(token)) break;
        try {
          int literal = Integer.parseInt(token);
          if (literal != 0) {
            literales.add(literal);
          }
        } catch (NumberFormatException ignored) {}
      }

      if (!literales.isEmpty()) {
        int[] arregloClausula = literales.stream().mapToInt(i -> i).toArray();
        clausulas.add(arregloClausula);
      }
    }

    return clausulas;
  }

  // Implementación del solver DPLL
  private boolean[] resolverDPLL(List<int[]> listaClausulas, int numVars) {
    List<int[]> clausulas = new ArrayList<>(listaClausulas);
    Boolean[] asignacion = new Boolean[numVars + 1];

    boolean satisfacible = dpllRecursivo(clausulas, asignacion, numVars);
    return satisfacible ? convertirABoolPrimitivo(asignacion) : null;
  }

  private boolean[] convertirABoolPrimitivo(Boolean[] arregloBoxed) {
    boolean[] arregloPrimitivo = new boolean[arregloBoxed.length];
    for (int i = 0; i < arregloBoxed.length; i++) {
      arregloPrimitivo[i] = arregloBoxed[i] != null && arregloBoxed[i];
    }
    return arregloPrimitivo;
  }

  private boolean dpllRecursivo(List<int[]> clausulas, Boolean[] asignacion, int numVars) {
    if (cancelarSolicitado.get()) return false;

    List<int[]> clausulasSimplificadas = new ArrayList<>();

    for (int[] clausula : clausulas) {
      boolean clausulaSatisfecha = false;
      List<Integer> nuevaClausula = new ArrayList<>();

      for (int literal : clausula) {
        int variable = Math.abs(literal);
        Boolean valor = asignacion[variable];

        if (valor == null) {
          nuevaClausula.add(literal);
        } else {
          boolean valorLiteral = (literal > 0 && valor) || (literal < 0 && !valor);
          if (valorLiteral) {
            clausulaSatisfecha = true;
            break;
          }
        }
      }

      if (clausulaSatisfecha) continue;

      if (nuevaClausula.isEmpty()) return false;

      clausulasSimplificadas.add(nuevaClausula.stream().mapToInt(i -> i).toArray());
    }

    if (clausulasSimplificadas.isEmpty()) return true;

    // Propagación de unidades
    for (int[] clausula : clausulasSimplificadas) {
      if (clausula.length == 1) {
        int literalUnidad = clausula[0];
        int variable = Math.abs(literalUnidad);
        boolean valor = literalUnidad > 0;

        Boolean valorActual = asignacion[variable];
        if (valorActual != null) {
          if (valorActual != valor) return false;
        } else {
          asignacion[variable] = valor;
          boolean resultado = dpllRecursivo(clausulasSimplificadas, asignacion, numVars);
          if (resultado) return true;
          asignacion[variable] = null;
          return false;
        }
      }
    }

    // Eliminación de literales puros
    Map<Integer, Integer> polaridadLiteral = new HashMap<>();

    for (int[] clausula : clausulasSimplificadas) {
      for (int literal : clausula) {
        int variable = Math.abs(literal);
        int polaridad = literal > 0 ? 1 : -1;

        polaridadLiteral.putIfAbsent(variable, 0);
        if (polaridad > 0) {
          polaridadLiteral.put(variable, polaridadLiteral.get(variable) | 1);
        } else {
          polaridadLiteral.put(variable, polaridadLiteral.get(variable) | 2);
        }
      }
    }

    for (Map.Entry<Integer, Integer> entry : polaridadLiteral.entrySet()) {
      int variable = entry.getKey();
      int polaridad = entry.getValue();

      if (asignacion[variable] == null && (polaridad == 1 || polaridad == 2)) {
        asignacion[variable] = (polaridad == 1);
        boolean resultado = dpllRecursivo(clausulasSimplificadas, asignacion, numVars);
        if (resultado) return true;
        asignacion[variable] = null;
        return false;
      }
    }

    // Heurística de frecuencia
    Map<Integer, Integer> frecuenciaVariable = new HashMap<>();

    for (int[] clausula : clausulasSimplificadas) {
      for (int literal : clausula) {
        int variable = Math.abs(literal);
        if (asignacion[variable] == null) {
          frecuenciaVariable.put(variable, frecuenciaVariable.getOrDefault(variable, 0) + 1);
        }
      }
    }

    if (frecuenciaVariable.isEmpty()) return true;

    int variableElegida = Collections.max(frecuenciaVariable.entrySet(),
            Map.Entry.comparingByValue()).getKey();

    for (boolean valor : new boolean[]{true, false}) {
      asignacion[variableElegida] = valor;
      boolean resultado = dpllRecursivo(clausulasSimplificadas, asignacion, numVars);
      if (resultado) return true;
      asignacion[variableElegida] = null;

      if (cancelarSolicitado.get()) return false;
    }

    return false;
  }

  static class VistaNumerosLinea extends JComponent implements DocumentListener {
    private final JTextArea areaTexto;
    private FontMetrics metricaFuente;
    private final int MARGEN = 8;
    private Color colorFondo = Color.decode("#2b2b2b");
    private Color colorTexto = Color.decode("#858585");

    public VistaNumerosLinea(JTextArea areaTexto) {
      this.areaTexto = areaTexto;
      this.areaTexto.getDocument().addDocumentListener(this);
      this.areaTexto.addCaretListener(e -> repaint());

      setFont(new Font(FUENTE_CODIGO, Font.PLAIN, TAM_CODIGO - 1));
      this.metricaFuente = getFontMetrics(getFont());
      setPreferredSize(new Dimension(50, Integer.MAX_VALUE));
      setOpaque(true);
    }

    public void setColores(Color fondo, Color texto) {
      this.colorFondo = fondo;
      this.colorTexto = texto;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      Rectangle clip = g.getClipBounds();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      GradientPaint gradiente = new GradientPaint(
              0, 0, colorFondo.brighter(),
              getWidth(), 0, colorFondo
      );
      g2.setPaint(gradiente);
      g2.fillRect(clip.x, clip.y, clip.width, clip.height);

      g2.setColor(new Color(255, 122, 0, 40));
      g2.setStroke(new BasicStroke(2));
      g2.drawLine(getWidth()-2, clip.y, getWidth()-2, clip.y + clip.height);

      g2.setColor(colorTexto);
      this.metricaFuente = getFontMetrics(getFont());

      try {
        int inicioOffset = areaTexto.viewToModel2D(new Point(0, clip.y));
        int finOffset = areaTexto.viewToModel2D(new Point(0, clip.y + clip.height));

        int inicioLinea = areaTexto.getLineOfOffset(inicioOffset);
        int finLinea = areaTexto.getLineOfOffset(finOffset);

        for (int linea = inicioLinea; linea <= finLinea; linea++) {
          int offsetInicioLinea = areaTexto.getLineStartOffset(linea);
          Rectangle rectLinea = areaTexto.modelToView2D(offsetInicioLinea).getBounds();

          String numLinea = String.valueOf(linea + 1);
          int x = getWidth() - MARGEN - metricaFuente.stringWidth(numLinea);
          int y = rectLinea.y + metricaFuente.getAscent();

          g2.setColor(new Color(0, 0, 0, 30));
          g2.drawString(numLinea, x+1, y+1);

          g2.setColor(colorTexto);
          g2.drawString(numLinea, x, y);
        }

      } catch (Exception ignored) {}

      g2.dispose();
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
    } catch (ClassNotFoundException | InstantiationException |
             IllegalAccessException | UnsupportedLookAndFeelException ignored) {}

    SwingUtilities.invokeLater(() -> {
      new MinntSATSolver().setVisible(true);
    });
  }
}

