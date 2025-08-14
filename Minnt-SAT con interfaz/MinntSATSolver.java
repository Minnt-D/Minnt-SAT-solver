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

  // Componentes de la interfaz
  private JTextArea editor;
  private JTextArea areaResultado;
  private JProgressBar barraProgreso;
  private JLabel etiquetaEstado;
  private JButton botonResolver;
  private AtomicBoolean cancelarSolicitado = new AtomicBoolean(false);

  // Tema y paletas
  private enum Tema { OSCURO, CLARO, MINNT_OSCURO, MINNT_CLARO }
  private Tema temaActual = Tema.MINNT_OSCURO;

  // Colores tema VS Code Oscuro
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

  // Colores tema VS Code Claro
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

  // Tema MINNT (neumorfismo naranja)
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

  // Fuentes (estilo VS Code)
  private static final String FUENTE_CODIGO = "Consolas";
  private static final String FUENTE_UI = "Segoe UI";
  private static final int TAM_CODIGO = 14;
  private static final int TAM_UI = 13;

  // Referencia al menú de tema
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

    // BARRA DE MENÚ (estilo VS Code)
    JMenuBar barraMenu = new JMenuBar();
    barraMenu.setFont(fuenteUI);
    barraMenu.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

    // Menú Archivo
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

    // Menú Edición
    JMenu menuEdicion = new JMenu("Edición");
    menuEdicion.setFont(fuenteUI);
    JMenuItem copiarItem = new JMenuItem("Copiar resultado");
    copiarItem.setFont(fuenteUI);
    copiarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
    copiarItem.addActionListener(e -> copiarResultadoAlPortapapeles());
    menuEdicion.add(copiarItem);

    // Menú Vista
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

    // Menú Ejecutar
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

  // Cambia crearBotonVSCode para soportar bordes redondeados y sombra en MINNT
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
    // Bordes redondeados y sombra para MINNT
    if (temaActual == Tema.MINNT_OSCURO || temaActual == Tema.MINNT_CLARO) {
      btn.setBorder(BorderFactory.createCompoundBorder(
        new javax.swing.border.LineBorder(new Color(255,122,0,80), 2, true),
        BorderFactory.createEmptyBorder(2, 8, 2, 8)
      ));
      btn.setBackground(temaActual == Tema.MINNT_OSCURO ? MINNT_OSCURO_ACENTO : MINNT_CLARO_ACENTO);
      btn.setForeground(temaActual == Tema.MINNT_OSCURO ? MINNT_OSCURO_TEXTO : MINNT_CLARO_TEXTO);
      btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
        @Override
        public void paint(Graphics g, JComponent c) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(btn.getBackground());
          g2.fillRoundRect(0, 0, btn.getWidth(), btn.getHeight(), 18, 18);
          g2.setColor(new Color(0,0,0,30));
          g2.fillRoundRect(3, 3, btn.getWidth()-6, btn.getHeight()-6, 14, 14);
          super.paint(g2, c);
          g2.dispose();
        }
      });
    }
    return btn;
  }

  // Cambia crearBotonIcono para usar iconos dibujados
  private JButton crearBotonIcono(String tooltip, String tipo) {
    JButton btn = new JButton();
    btn.setToolTipText(tooltip);
    btn.setFocusPainted(false);
    btn.setContentAreaFilled(false);
    btn.setOpaque(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.setPreferredSize(new Dimension(28, 24));
    btn.setBorder(BorderFactory.createEmptyBorder());
    btn.setIcon(new Icon() {
      public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (tipo.equals("Copiar")) {
          g2.setColor(temaActual == Tema.OSCURO || temaActual == Tema.MINNT_OSCURO ? Color.WHITE : Color.BLACK);
          g2.drawRect(x+6, y+6, 12, 12);
          g2.drawRect(x+10, y+2, 8, 8);
        } else if (tipo.equals("Guardar")) {
          g2.setColor(temaActual == Tema.OSCURO || temaActual == Tema.MINNT_OSCURO ? Color.WHITE : Color.BLACK);
          g2.fillRect(x+6, y+6, 12, 12);
          g2.setColor(temaActual == Tema.MINNT_OSCURO || temaActual == Tema.MINNT_CLARO ? MINNT_OSCURO_ACENTO : VS_OSCURO_ACENTO);
          g2.fillRect(x+10, y+12, 4, 4);
        }
        g2.dispose();
      }
      public int getIconWidth() { return 24; }
      public int getIconHeight() { return 20; }
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

  // Aplica fondo y borde a paneles principales y JSplitPane
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

    // Aplica colores recursivamente
    aplicarColoresRecursivo(getContentPane(), fondo, fondoPanel, fondoBarra, texto, acento,
        fondoLinea, textoLinea, borde, fondoBoton, hoverBoton);

    // Paneles principales y JSplitPane
    for (Component comp : getContentPane().getComponents()) {
        if (comp instanceof JPanel p) {
            p.setBackground(fondoPanel);
            if (t == Tema.MINNT_OSCURO || t == Tema.MINNT_CLARO) {
                p.setBorder(BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(new Color(255,122,0,60), 2, true),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
            } else {
                p.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            }
        }
        if (comp instanceof JSplitPane sp) {
            sp.setBackground(fondoPanel);
            sp.setDividerSize(6);
            sp.setBorder(BorderFactory.createLineBorder(borde, 2, true));
        }
    }

    // Barra de estado
    Component barraEstadoComp = buscarComponentePorNombre(getContentPane(), "barraEstado");
    if (barraEstadoComp instanceof JPanel barraEstadoPanel) {
        barraEstadoPanel.setBackground(acento);
        barraEstadoPanel.setBorder(BorderFactory.createMatteBorder(0,0,0,0,acento));
        etiquetaEstado.setForeground(Color.WHITE);
        if (t == Tema.MINNT_OSCURO || t == Tema.MINNT_CLARO) {
            barraEstadoPanel.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(255,122,0,60), 2, true),
                BorderFactory.createEmptyBorder(2, 12, 2, 12)
            ));
        }
    }

    // Modales con tema
    UIManager.put("OptionPane.background", fondoPanel);
    UIManager.put("Panel.background", fondoPanel);
    UIManager.put("OptionPane.messageForeground", texto);

    // Números de línea
    Component numerosLinea = obtenerVistaNumerosLinea();
    if (numerosLinea instanceof VistaNumerosLinea) {
        ((VistaNumerosLinea) numerosLinea).setColores(fondoLinea, textoLinea);
    }

    SwingUtilities.updateComponentTreeUI(this);
    repaint();
  }

  // Aplica colores recursivamente a todos los componentes
  private void aplicarColoresRecursivo(Container contenedor, Color fondo, Color fondoPanel,
    Color fondoBarra, Color texto, Color acento, Color fondoLinea, Color textoLinea, Color borde,
    Color fondoBoton, Color hoverBoton) {
    contenedor.setBackground(fondo);
    contenedor.setForeground(texto);

    for (Component comp : contenedor.getComponents()) {
        if (comp instanceof JPanel p) {
            p.setBackground(fondoPanel);
            p.setForeground(texto);
            if (temaActual == Tema.MINNT_OSCURO || temaActual == Tema.MINNT_CLARO) {
                p.setBorder(BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(new Color(255,122,0,60), 2, true),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
            } else {
                p.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            }
        } else if (comp instanceof JButton btn) {
            btn.setBackground(fondoBoton);
            btn.setForeground(texto);
            btn.setBorder(BorderFactory.createLineBorder(borde, temaActual == Tema.MINNT_OSCURO || temaActual == Tema.MINNT_CLARO ? 2 : 1, true));
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            for (MouseListener ml : btn.getMouseListeners()) {
                btn.removeMouseListener(ml);
            }
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(hoverBoton);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(fondoBoton);
                }
            });
            if (temaActual == Tema.MINNT_OSCURO || temaActual == Tema.MINNT_CLARO) {
                btn.setBorder(BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(new Color(255,122,0,80), 2, true),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)
                ));
                btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                    @Override
                    public void paint(Graphics g, JComponent c) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(btn.getBackground());
                        g2.fillRoundRect(0, 0, btn.getWidth(), btn.getHeight(), 18, 18);
                        g2.setColor(new Color(0,0,0,30));
                        g2.fillRoundRect(3, 3, btn.getWidth()-6, btn.getHeight()-6, 14, 14);
                        super.paint(g2, c);
                        g2.dispose();
                    }
                });
            } else {
                btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            }
        } else if (comp instanceof JLabel lbl) {
            lbl.setForeground(texto);
        } else if (comp instanceof JScrollPane sp) {
            sp.setBackground(fondoPanel);
            sp.getViewport().setBackground(fondoPanel);
            sp.setBorder(BorderFactory.createLineBorder(borde, 1));
        }
        if (comp instanceof Container) {
            aplicarColoresRecursivo((Container) comp, fondo, fondoPanel, fondoBarra, texto, acento,
                fondoLinea, textoLinea, borde, fondoBoton, hoverBoton);
        }
    }
  }

  private void aplicarColoresMenu(JMenuBar barraMenu, Color fondo, Color texto, Color acento) {
    for (int i = 0; i < barraMenu.getMenuCount(); i++) {
      JMenu menu = barraMenu.getMenu(i);
      menu.setBackground(fondo);
      menu.setForeground(texto);
      aplicarColoresItemsMenu(menu, fondo, texto, acento);
    }
  }

  private void aplicarColoresItemsMenu(JMenu menu, Color fondo, Color texto, Color acento) {
    for (Component comp : menu.getMenuComponents()) {
      if (comp instanceof JMenuItem) {
        comp.setBackground(fondo);
        comp.setForeground(texto);
        if (comp instanceof JMenu) {
          aplicarColoresItemsMenu((JMenu) comp, fondo, texto, acento);
        }
      }
    }
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

  // Diálogos
  private void mostrarAcercaDe() {
    String acerca = """
        Minnt SAT Solver - Edición Java
        Versión: 2.0

        Características:
        • Algoritmo DPLL con optimizaciones
        • Propagación de unidades y eliminación de literales puros
        • Heurística de ordenamiento por frecuencia de variables
        • Múltiples temas (VS Oscuro/Claro, MINNT Oscuro/Claro)
        • Exporta resultados a la carpeta Descargas
        • Soporte completo de atajos de teclado

        Atajos:
        • Ctrl+O: Abrir archivo
        • F5: Ejecutar solver
        • Ctrl+Shift+C: Copiar resultado
        """;

    JTextArea areaTexto = new JTextArea(acerca);
    areaTexto.setEditable(false);
    areaTexto.setFont(new Font(FUENTE_UI, Font.PLAIN, 12));
    areaTexto.setOpaque(true);
    areaTexto.setBackground(temaActual == Tema.OSCURO || temaActual == Tema.MINNT_OSCURO ? VS_OSCURO_PANEL : VS_CLARO_PANEL);
    areaTexto.setForeground(temaActual == Tema.OSCURO || temaActual == Tema.MINNT_OSCURO ? VS_OSCURO_TEXTO : VS_CLARO_TEXTO);

    JOptionPane.showMessageDialog(this, areaTexto, "Acerca de Minnt SAT Solver",
            JOptionPane.INFORMATION_MESSAGE);
  }

  private void mostrarManual() {
    String manual = """
        Minnt SAT Solver - Manual de usuario

        FORMATOS SOPORTADOS:
        • Formato DIMACS CNF
        • Comentarios: líneas que empiezan con 'c' o '//'
        • Cabecera: 'p cnf <variables> <cláusulas>'
        • Cláusulas: literales separados por espacios terminando en '0'

        USO:
        1. Abre un archivo CNF (Archivo → Abrir o Ctrl+O)
        2. Edita el problema en el panel izquierdo
        3. Ejecuta el solver (Ejecutar → Resolver SAT o F5)
        4. Visualiza resultados en el panel derecho
        5. Copia resultados (Ctrl+Shift+C) o guarda en archivo

        EXPORTAR:
        Los resultados se guardan en tu carpeta Descargas con marca de tiempo:
        'minnt_result_<timestamp>.txt'

        TEMAS:
        • Oscuro/Claro: Temas Visual Studio Code
        • MINNT Oscuro/Claro: Temas personalizados naranja neumorfismo

        ATAJOS DE TECLADO:
        • Ctrl+O: Abrir archivo
        • F5: Ejecutar solver
        • Ctrl+Shift+C: Copiar resultado al portapapeles
        • Atajos estándar de edición de texto en el editor
        """;

    JTextArea areaTexto = new JTextArea(manual);
    areaTexto.setEditable(false);
    areaTexto.setFont(new Font(FUENTE_CODIGO, Font.PLAIN, 12));
    areaTexto.setLineWrap(true);
    areaTexto.setWrapStyleWord(true);
    areaTexto.setOpaque(true);
    areaTexto.setBackground(temaActual == Tema.OSCURO || temaActual == Tema.MINNT_OSCURO ? VS_OSCURO_PANEL : VS_CLARO_PANEL);
    areaTexto.setForeground(temaActual == Tema.OSCURO || temaActual == Tema.MINNT_OSCURO ? VS_OSCURO_TEXTO : VS_CLARO_TEXTO);

    JScrollPane scroll = new JScrollPane(areaTexto);
    scroll.setPreferredSize(new Dimension(600, 400));
    scroll.setBackground(areaTexto.getBackground());
    scroll.getViewport().setBackground(areaTexto.getBackground());

    JOptionPane.showMessageDialog(this, scroll, "Manual de usuario",
            JOptionPane.INFORMATION_MESSAGE);
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

  // Componente de números de línea para el editor
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
      g.setColor(colorFondo);
      g.fillRect(clip.x, clip.y, clip.width, clip.height);

      g.setColor(colorTexto);
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

          g.drawString(numLinea, x, y);
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

  // Método principal
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
