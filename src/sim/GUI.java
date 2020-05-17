package sim;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import sim.SaveUtil.Saveable;
import sim.Simulator.Position;
import sim.Simulator.ShotType;
import sim.Simulator.Target;
import sim.graph.Graph;
import sim.graph.Graph.Point;
import sim.graph.GraphCanvas;
import sim.graph.GraphCanvas.GraphMode;

public class GUI extends JPanel implements ActionListener {

	private GeneralPanel generalPanel;
	private TargetPanel targetPanel;
	private SoldierPanel soldierPanel;
	private ResultPanel resultPanel;
	
	private List<WeaponPanel> weaponPanels = new ArrayList<>();
	

	public GUI(JFrame frame) {
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		generalPanel = new GeneralPanel();
		soldierPanel = new SoldierPanel();
		
		weaponPanels.add(new WeaponPanel(soldierPanel, 0, true));
		weaponPanels.add(new WeaponPanel(soldierPanel, 3, false));
		weaponPanels.add(new WeaponPanel(soldierPanel, 6, false));
		
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
		tabs.setMinimumSize(new Dimension(500, 450));
		tabs.addTab("Weapon 1", weaponPanels.get(0));
		tabs.addTab("Weapon 2", weaponPanels.get(1));
		tabs.addTab("Weapon 3", weaponPanels.get(2));
		
		targetPanel = new TargetPanel();
		resultPanel = new ResultPanel(this);

		JPanel firstPanel = new JPanel();
		firstPanel.setLayout(new GridBagLayout());
		firstPanel.setMinimumSize(new Dimension(300, 600));
		JPanel secondPanel = new JPanel();
		secondPanel.setMinimumSize(new Dimension(500, 600));
		secondPanel.setLayout(new GridBagLayout());
		JPanel thirdPanel = new JPanel();
		thirdPanel.setMinimumSize(new Dimension(500, 600));
		thirdPanel.setLayout(new GridBagLayout());

		addComponent(this, firstPanel, 0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE);
		addComponent(this, secondPanel, 1, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL);
		addComponent(this, thirdPanel, 2, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL);

		addComponent(firstPanel, generalPanel, 0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE);
		addComponent(firstPanel, targetPanel, 0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE);
		addComponent(firstPanel, soldierPanel, 0, 2, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE);
		addComponent(secondPanel, tabs, 0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE);
		addComponent(thirdPanel, resultPanel, 0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE);
	}

	private static class ResultPanel extends JPanel implements ActionListener {

		private JButton calculate;
		private GraphCanvas canvas;

		private JTextField maxY;

		private JComboBox<GraphMode> mode;

		public ResultPanel(GUI gui) {
			setMinimumSize(new Dimension(500, 600));
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
			setLayout(new GridBagLayout());
			addComponent(this, new JLabel("Result"), 0, 0);
			calculate = new JButton("Calculate");
			calculate.addActionListener(gui);
			addComponent(this, calculate, 0, 1);

//			JButton rescale = new JButton("Apply max Y");
//			rescale.addActionListener(this);
//			addComponent(this, rescale, 0, 2);
			JLabel yLabel = new JLabel("Max Y value");
			addComponent(this, yLabel, 0, 2);

			maxY = new JTextField(3);
			maxY.setText("250");
			maxY.addActionListener(this);

			addComponent(this, maxY, 1, 2);

			mode = new JComboBox<>(GraphMode.values());
			addComponent(this, mode, 2, 2);
			mode.addActionListener(this);
			mode.setLightWeightPopupEnabled(false);

			canvas = addComponent(this, new GraphCanvas(250), 0, 5, 6, 6, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			canvas.setSettings(getInt(maxY), (GraphMode) mode.getSelectedItem());
			revalidate();
		}

	}

	public static final Color[] COLORS = new Color[] { Color.RED, Color.GREEN, Color.ORANGE, Color.PINK, Color.YELLOW,
			Color.MAGENTA, Color.CYAN, Color.BLUE, Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY,
			Color.BLACK};
	
	private static class WeaponPanel extends JPanel implements Saveable {


		static class ShotSettings implements Saveable {
			final JCheckBox enabled;
			final JTextField range;
			final JTextField shots;
			final JTextField acc;
			final JTextField tu;
			final StatPanel unitStats;
			final ShotType shotType;
			final JComboBox<Color> color;

			public ShotSettings(String name, String range, String shots, String acc, String tu, StatPanel unitStats,
					int colorIdx, boolean initial) {
				this.enabled = new JCheckBox(name);
				shotType = ShotType.valueOf(name);
				enabled.setSelected(initial);
				this.range = new JTextField(8);
				this.range.setText(range);
				this.shots = new JTextField(8);
				this.shots.setText(shots);
				this.acc = new JTextField(8);
				this.acc.setText(acc);
				this.tu = new JTextField(8);
				this.tu.setText(tu);
				this.unitStats = unitStats;
				this.color = new JComboBox<Color>(COLORS);
				color.setRenderer(new ColorCellRenderer());
				color.setSelectedIndex(colorIdx);
			}

			void addToPanel(Container c, int y) {
				addComponent(c, enabled, 0, y);
				addComponent(c, range, 1, y);
				addComponent(c, shots, 2, y);
				addComponent(c, acc, 3, y);
				addComponent(c, tu, 4, y);
				addComponent(c, color, 5, y);
			}

			public int getTuForShot() {
				String r = tu.getText();
				try {
					int result = 0;
					tu.setBackground(Color.WHITE);
					if (r.endsWith("%")) {
						r = r.substring(0, r.length() - 1);
						JTextField s = unitStats.getStat(TU);
						result = getInt(s);
						result = result / 100 * Integer.valueOf(r);
					} else {
						result = Integer.valueOf(r);
					}

					tu.setBackground(Color.WHITE);

					return result;
				} catch (Exception e) {
					tu.setBackground(Color.RED);
					throw new RuntimeException(e);
				}
			}

			@Override
			public String save() {
				Map<String, String> result = new HashMap<>();
				result.put("enabled", "" + enabled.isSelected());
				result.put("range", range.getText());
				result.put("acc", acc.getText());
				result.put("tu", tu.getText());
				result.put("shots", shots.getText());
				result.put("color", "" + color.getSelectedIndex());
				return SaveUtil.write(result);
			}

			@Override
			public void load(Map<String, String> values) {
				SaveUtil.setEnabled(enabled, values.get("enabled"));
				SaveUtil.setText(range, values.get("range"));
				SaveUtil.setText(acc, values.get("acc"));
				SaveUtil.setText(tu, values.get("tu"));
				SaveUtil.setText(shots, values.get("shots"));
				SaveUtil.setIndex(color, values.get("color"));
			}
			
			

		}


		private ShotSettings aimed;
		private ShotSettings snap;
		private ShotSettings auto;
		
		private JTextField minRange;
		private JTextField dropoff;
		private JTextField dmg;
		
		private JTextField kneelBonus;

		private SoldierPanel soldierPanel;

		private StatPanel accBonus;
		private StatPanel dmgBonus;
		
		private SaveLoadPanel slp;

		public WeaponPanel(SoldierPanel p, int colorIndex, boolean selected) {
			this.soldierPanel = p;
			setMinimumSize(new Dimension(500, 450));
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
			setLayout(new GridBagLayout());
			int y = 0;
			addComponent(this, new JLabel("Weapon"), 0, y++);

			addComponent(this, new JLabel("Range (int)"), 1, y);
			addComponent(this, new JLabel("Shots (int)"), 2, y);
			addComponent(this, new JLabel("Accucacy (%)"), 3, y);
			addComponent(this, new JLabel("TU (% or flat)"), 4, y);
			addComponent(this, new JLabel("Color"), 5, y++);
//			int colorIndex = 0;
			aimed = new ShotSettings("Aimed", "200", "1", "90%", "90%", p.stats, colorIndex++, selected);
			aimed.addToPanel(this, y++);

			snap = new ShotSettings("Snap", "15", "1", "60%", "40%", p.stats, colorIndex++, selected);
			snap.addToPanel(this, y++);

			auto = new ShotSettings("Auto", "7", "3", "30%", "40", p.stats, colorIndex++, selected);
			auto.addToPanel(this, y++);

			minRange = addLabeledTextField("Min range", y++, this);
			minRange.setText("0");

			dropoff = addLabeledTextField("DropOff/Tile", y++, this);
			dropoff.setText("2");
			dmg = addLabeledTextField("Damage", y++, this);
			dmg.setText("30");

			kneelBonus = addLabeledTextField("Kneel Modifier (%)", y++, this);
			kneelBonus.setText("115%");

			accBonus = new StatPanel("Acc bonus:");
			GUI.addComponent(this, accBonus, 0, y, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);

			accBonus.addField("acc", "1");
			accBonus.addField(FLAT_100, "0");

			dmgBonus = new StatPanel("Dmg bonus:");
			GUI.addComponent(this, dmgBonus, 2, y++, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);

			dmgBonus.addField("str", "0.0");
			dmgBonus.addField(FLAT_100, "0");

			addComponent(this, new JLabel(" "), 0, y++);

			slp = new SaveLoadPanel("weapons", this::save, this::load);
			addComponent(this, slp, 0, y, 2, 1);

		}
		
		public String save() {
			Map<String, String> result = new HashMap<>();
			result.put("aimed", aimed.save());
			result.put("snap", snap.save());
			result.put("auto", auto.save());
			result.put("minRange", minRange.getText());
			result.put("dropoff", dropoff.getText());
			result.put("dmg", dmg.getText());
			result.put("kneelBonus", kneelBonus.getText());
			result.put("accBonus", accBonus.save());
			result.put("dmgBonus", dmgBonus.save());
			return SaveUtil.write(result);
		}
		

		@Override
		public void load(Map<String, String> values) {
			SaveUtil.load(aimed, values.get("aimed"));
			SaveUtil.load(snap, values.get("snap"));
			SaveUtil.load(auto, values.get("auto"));
			SaveUtil.setText(minRange, values.get("minRange"));
			SaveUtil.setText(dropoff, values.get("dropoff"));
			SaveUtil.setText(dmg, values.get("dmg"));
			SaveUtil.setText(kneelBonus, values.get("kneelBonus"));
			SaveUtil.load(accBonus, values.get("accBonus"));
			SaveUtil.load(dmgBonus, values.get("dmgBonus"));
			revalidate();
		}

		
		private static final String FLAT_100 = "flat100";

		public double getSoldierAcc() {
			double result = 0;
			for (Map.Entry<JTextField, JTextField> e : accBonus.values.entrySet()) {
				JTextField key = e.getKey();
				JTextField value = e.getValue();
				result += processStat(key, value);
			}
			return result;
		}

		public int getWeaponDmg() {
			int result = getInt(dmg);
			for (Map.Entry<JTextField, JTextField> e : dmgBonus.values.entrySet()) {
				JTextField key = e.getKey();
				JTextField value = e.getValue();
				result += processStat(key, value) * 100;
			}
			return result;
		}

		private double processStat(JTextField key, JTextField value) {
			key.setBackground(Color.WHITE);
			value.setBackground(Color.WHITE);
			String k = key.getText();
			String v = key.getText();
			if (nullOrEmpty(k) != nullOrEmpty(v)) {
				value.setBackground(Color.RED);
				throw new RuntimeException();
			}
			if (nullOrEmpty(k)) {
				key.setBackground(Color.WHITE);
				value.setBackground(Color.white);
				return 0;
			}
			k = k.trim();
			if (k.equals(FLAT_100)) {
				return getDouble(value);
			}
			v = v.trim();
			boolean squared = k.endsWith("²");
			if (squared) {
				k = k.substring(0, k.length() - 1);
			}
			JTextField sv = soldierPanel.stats.getStat(k);

			if (sv == null) {
				soldierPanel.addStat(k);
				throw new RuntimeException();
			}

			double statValue = getInt(sv);
			if (squared) {
				statValue *= statValue;
			}
			double bonusValue = getDouble(value);

			return statValue / 100d * bonusValue;
		}

		public Weapon getWeapon() {
			Weapon w = new Weapon(getInt(aimed.range), getInt(minRange), getInt(auto.range), getInt(snap.range),
					getInt(dropoff), getInt(dmg));
			return w;
		}

	}
	
	private static class SaveLoadPanel extends JPanel {
		final String folderName;
		final JTextField name;
		final JLabel error;
		
		public SaveLoadPanel(String folderName, Supplier<String> saveFunction, Consumer<Map<String, String>> loadFunction) {
			this.folderName = folderName;
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
			setLayout(new GridBagLayout());
			setMinimumSize(new Dimension(200, 100));
			
			name = addLabeledTextField("Name", 0, this);
			name.getDocument().addDocumentListener(new FileNameDocumentListener(name, folderName));
			
			JButton save = addComponent(this, new JButton("Save"), 0, 1);
			JButton load = addComponent(this, new JButton("Load"), 1, 1);

			error = addComponent(this, new JLabel(""), 0, 2);
			save.addActionListener(e ->executeSave(saveFunction, name, error));
			load.addActionListener(e ->executeLoad(loadFunction, name, error));
			
		}
		
		private void executeSave(Supplier<String> saveFunction, JTextField name, JLabel error) {
			try {
				String fileName = name.getText();
				File f = new File(folderName, fileName);
				System.out.println("saving as " + f.getAbsolutePath());
				List<String> lines = new ArrayList<>();
				lines.add(saveFunction.get());
				Files.write(f.toPath(), lines);
				name.setBackground(Color.GREEN);
				error.setText("");
			} catch (Exception e) {
				e.printStackTrace();
				name.setBackground(Color.RED);
				error.setText(e.getMessage());
			}
		}
		
		private static String readFile(String folder, String fileName) throws Exception {
			File f = new File(folder, fileName);
			System.out.println("loading as " + f.getAbsolutePath());
			List<String> lines = Files.readAllLines(f.toPath());
			StringBuilder str = new StringBuilder();
			for(String s : lines) {
				str.append(s);
				str.append("\n");
			}
			return str.toString();
		}
		
		void executeLoad(Consumer<Map<String, String>> fun, JTextField name, JLabel error) {
			try {
				String content = readFile(folderName, name.getText());
				fun.accept(SaveUtil.read(content));
				name.setBackground(Color.GREEN);
				error.setText("");
			} catch (Exception e) {
				e.printStackTrace();
				name.setBackground(Color.RED);
				error.setText(e.getMessage());
			}
		}
	}

	private static class FileNameDocumentListener implements DocumentListener {
		final JTextField field;
		final String folderName;
		
	    public FileNameDocumentListener(JTextField field, String folderName) {
			this.field = field;
			this.folderName = folderName;
		}

		@Override
	    public void insertUpdate(DocumentEvent e) {
	    	colorNameField(folderName, field);
	    }

	    @Override
	    public void removeUpdate(DocumentEvent e) {
	    	colorNameField(folderName, field);
	    }

	    @Override
	    public void changedUpdate(DocumentEvent e) {
	    	colorNameField(folderName, field);
	    }
	}
	
	private static void colorNameField(String folderName, JTextField field) {
		File folder = new File(folderName);
		if(!folder.exists()) {
			folder.mkdirs();
		}
		File f = new File(folder, field.getText());
		if(f.exists()) {
			field.setBackground(Color.GREEN);
		} else {
			field.setBackground(Color.YELLOW);
		}
	}
	private static class ColorCellRenderer extends JButton implements ListCellRenderer<Color> {
		private boolean s = false;

		public ColorCellRenderer() {
			setOpaque(true);
		}

		@Override
		public void setBackground(Color bg) {
			if (s) {
				super.setBackground(bg);
			}
		}

		public Component getListCellRendererComponent(JList<? extends Color> list, Color value, int index,
				boolean isSelected, boolean cellHasFocus) {
			s = true;
			setText(" ");
			setBackground((Color) value);
			s = false;
			return this;
		}
	}

	static boolean nullOrEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	private static class StatPanel extends JPanel implements ActionListener, Saveable {

		private Map<JTextField, JTextField> values = new HashMap<>();

		public StatPanel(String name) {
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
			setLayout(new GridBagLayout());
			JLabel s = new JLabel(name);
			GUI.addComponent(this, s, 0, 0);
			JButton add = new JButton("add field");
			add.addActionListener(this);
			GUI.addComponent(this, add, 0, 1);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			addField("", "");
		}

		public JTextField[] addField(String name, String value) {
			JTextField t[] = GUI.addComponent(name, value, this, values, values.size() + 2);
			revalidate();
			return t;
		}

		public JTextField getStat(String name) {
			JTextField sv = null;
			for (Map.Entry<JTextField, JTextField> e : values.entrySet()) {
				String sk = e.getKey().getText();
				if (nullOrEmpty(sk)) {
					continue;
				}
				if (sk.equalsIgnoreCase(name)) {
					sv = e.getValue();
					e.getValue().setBackground(Color.WHITE);
					break;
				}
			}
			return sv;
		}

		@Override
		public String save() {
			Map<String, String> result = new HashMap<>();
			for(Map.Entry<JTextField, JTextField> e : values.entrySet()) {
				String k = e.getKey().getText();
				String v = e.getValue().getText();
				if(!k.isEmpty() && !v.isEmpty()) {
					result.put(k, v);
				}
			}
			return SaveUtil.write(result);
		}

		@Override
		public void load(Map<String, String> values) {
			for(Map.Entry<JTextField, JTextField> e : this.values.entrySet()) {
				remove(e.getKey());
				remove(e.getValue());
			}
			this.values = new HashMap<>();
			for(Map.Entry<String, String> e : values.entrySet()) {
				addField(e.getKey(), e.getValue());
			}
			revalidate();
		}

		
	}

	private static class TargetPanel extends JPanel {
		private JTextField width;
		private JTextField height;

		public TargetPanel() {
			setMinimumSize(new Dimension(200, 80));
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
			setLayout(new GridBagLayout());
			addComponent(this, new JLabel("Target"), 0, 0);
			width = addLabeledTextField("Width (Voxels)", 1, this);
			width.setText("12");
			height = addLabeledTextField("Height (Voxels)", 2, this);
			height.setText("20");
		}

		public Target getTarget() {
			Target target = new Target();
			target.x = 160;
			target.z = 160;
			target.width = getInt(width);
			target.height = getInt(height);
			return target;
		}
	}

	private static int getInt(JTextField f) {
		String s = f.getText();
		try {
			int i = Integer.valueOf(s);
			f.setBackground(Color.WHITE);
			return i;
		} catch (Exception e) {
			f.setBackground(Color.RED);
			throw new RuntimeException();
		}
	}

	private static double getDouble(JTextField f) {
		String s = f.getText();
		try {
			double i = Double.valueOf(s);
			f.setBackground(Color.WHITE);
			return i;
		} catch (Exception e) {
			f.setBackground(Color.RED);
			throw new RuntimeException();
		}
	}

	private static class GeneralPanel extends JPanel {

		private JTextField shots;

		public GeneralPanel() {
			setMinimumSize(new Dimension(200, 80));
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
			setLayout(new GridBagLayout());
			addComponent(this, new JLabel("General Settings"), 0, 0);
			shots = addLabeledTextField("Simulation runs", 1, this);
			shots.setText("100000");

		}

	}

	private static class SoldierPanel extends JPanel implements ActionListener, Saveable{

		private JCheckBox kneeled;
		private StatPanel stats;
		private SaveLoadPanel slp;
		public SoldierPanel() {
			setMinimumSize(new Dimension(200, 200));
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
			setLayout(new GridBagLayout());
			JLabel s = new JLabel("Unit stats");
			GUI.addComponent(this, s, 0, 0);
			kneeled = new JCheckBox("Kneeled");
			GUI.addComponent(this, kneeled, 0, 1);
			stats = new StatPanel("stats");
			GUI.addComponent(this, stats, 0, 2);
			stats.addField("acc", "75")[0].setEnabled(false);
			stats.addField(TU, "100")[0].setEnabled(false);
			stats.addField("str", "60")[0].setEnabled(false);
			
			slp = new SaveLoadPanel("units", this::save, this::load);
			addComponent(this, slp, 0, 3, 2, 1);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			stats.addField("", "");
			revalidate();
		}

		public void addStat(String value) {
			JTextField[] k = stats.addField(value, "");
			k[1].setBackground(Color.red);
			revalidate();
		}

		@Override
		public String save() {
			Map<String, String> result = new HashMap<>();
			result.put("kneeled", "" + kneeled.isSelected());
			result.put("stats", stats.save());
			return SaveUtil.write(result);
		}

		@Override
		public void load(Map<String, String> values) {
			SaveUtil.setEnabled(kneeled, values.get("kneeled"));
			SaveUtil.load(stats, values.get("stats"));
			revalidate();
		}
		

	}

	private static JTextField[] addComponent(String name, String value, Container container,
			Map<JTextField, JTextField> values, int y) {
		JTextField label = new JTextField(12);
		label.setText(name);
		JTextField tf = new JTextField(12);
		tf.setText(value);
		GUI.addComponent(container, label, 0, y);
		GUI.addComponent(container, tf, 1, y);
		values.put(label, tf);
		return new JTextField[] { label, tf };
	}

	private static JTextField addLabeledTextField(String str, int gridy, Container container) {
		JLabel label = new JLabel(str);
		JTextField tf = new JTextField(8);
		addComponent(container, label, 0, gridy);
		addComponent(container, tf, 1, gridy);
		return tf;
	}

	private static final Insets insets = new Insets(0, 0, 0, 0);

	private static <T extends Component> T addComponent(Container container, T component, int gridx, int gridy) {
		return addComponent(container, component, gridx, gridy, 1, 1, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
	}

	private static <T extends Component> T addComponent(Container container, T component, int gridx, int gridy,
			int gridwidth, int gridheight) {
		return addComponent(container, component, gridx, gridy, gridwidth, gridheight, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH);
	}

	private static <T extends Component> T addComponent(Container container, T component, int gridx, int gridy,
			int gridwidth, int gridheight, int anchor, int fill) {
		GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, 1.0, 1.0, anchor, fill,
				insets, 0, 0);
		container.add(component, gbc);
		return component;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("calculate...");
		Position source = new Position();
		source.x = 160;
		source.y = 160;
		source.z = 160;

		List<Graph> graphs = new ArrayList<>();
		long t1 = System.currentTimeMillis();
		for(WeaponPanel w : weaponPanels) {
	
			Target target = targetPanel.getTarget();
	
			calcGraph(w.aimed, source, target, w, graphs);
			calcGraph(w.snap, source, target, w, graphs);
			calcGraph(w.auto, source, target, w, graphs);
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Took " + (t2-t1) + "ms");
		resultPanel.canvas.setGraphs(graphs);
	}
	
	private void processNoGui(NoGUI noGui) throws Exception{
		System.out.println("calculate...");

		Position source = new Position();
		source.x = 160;
		source.y = 160;
		source.z = 160;

		long t1 = System.currentTimeMillis();
		
		soldierPanel.slp.name.setText(noGui.soldierSave);
		soldierPanel.slp.executeLoad(soldierPanel::load, soldierPanel.slp.name, soldierPanel.slp.error);
		
		for(String s : noGui.weapons) {
	
			WeaponPanel w = weaponPanels.get(0);
			w.slp.name.setText(s);
			w.slp.executeLoad(w::load, w.slp.name, w.slp.error);
			
			Target target = targetPanel.getTarget();
	
			List<Graph> graphs = new ArrayList<>();
			
			calcGraph(w.aimed, source, target, w, graphs);
			persistGraph(graphs, noGui.soldierSave, s, "aimed");
			graphs = new ArrayList<>();
			
			calcGraph(w.snap, source, target, w, graphs);
			persistGraph(graphs, noGui.soldierSave, s, "snap");
			graphs = new ArrayList<>();
			
			calcGraph(w.auto, source, target, w, graphs);
			persistGraph(graphs, noGui.soldierSave, s, "auto");
			
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Took " + (t2-t1) + "ms");
		
		System.exit(0);
	}

	private void persistGraph(List<Graph> graphs, String soldierSave, String weaponName, String shotMode) throws Exception {
		if(graphs.isEmpty()) {
			return;
		}
		Graph g = graphs.get(0);
		File f = new File("graphs", soldierSave + "_" + weaponName + "_" + shotMode);
		if(!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		List<String> lines = new ArrayList<>();
		lines.add("#hitChance");
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for(Point p : g.getPoints()) {
			if(!first) {
				str.append(";");
			}
			first = false;
			str.append(p.hitChance);
		}
		lines.add(str.toString());
		lines.add("#dmg per turn");
		first = true;
		str = new StringBuilder();
		for(Point p : g.getPoints()) {
			if(!first) {
				str.append(";");
			}
			first = false;
			str.append(p.dmgPerTurn);
		}
		lines.add(str.toString());
		
		Files.write(f.toPath(), lines);
	}

	private void calcGraph(WeaponPanel.ShotSettings settings, Position source, Target t, WeaponPanel weaponPanel, List<Graph> list) {
		if(!settings.enabled.isSelected()) {
			return;
		}
		Weapon weapon = weaponPanel.getWeapon();
		
		double acc = weaponPanel.getSoldierAcc();
		double weaponDmg = (double) weaponPanel.getWeaponDmg();
		acc *= getPercent(settings.acc) / 100d;
		if (soldierPanel.kneeled.isSelected()) {
			acc *= getPercent(weaponPanel.kneelBonus) / 100d;
		}
		int simulationShots = getInt(generalPanel.shots);
		int shotTypeShots = getInt(settings.shots);
		
		int tu = getInt(soldierPanel.stats.getStat(TU));
		
		Color color = (Color)settings.color.getSelectedItem();
		ShotType shotType = settings.shotType;
		int tuForShot = settings.getTuForShot();
		
		List<GraphCallable> callables = new ArrayList<>();
		for (int i = 0; i <= 50; i++) {
			callables.add(new GraphCallable(i, t, source, weapon, acc, simulationShots, shotTypeShots, shotType, weaponDmg, tu, tuForShot));
		}
		List<Point> points = new ArrayList<>();
		try {
			List<Future<Point>> invokeAll = tpe.invokeAll(callables);
			for(Future<Point> f : invokeAll) {
				points.add(f.get());
			}
			Collections.sort(points, (p1, p2)-> Integer.compare(p1.distance, p2.distance));
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong", e);
		}
		
		Graph g = new Graph(points, color);
		list.add(g);
	}
	
	static class GraphCallable implements Callable<Point> {
		final int distance;
		final Target t;
		final Position source;
		final Weapon weapon;
		final double acc;
		final int simulationShots;
		final int shotTypeShots;
		final ShotType shotType;
		final double weaponDmg;
		final int tu;
		final int tuForShot;
		
		
		public GraphCallable(int distance, Target t, Position source, Weapon weapon, double acc, int simulationShots,
				int shotTypeShots, ShotType shotType, double weaponDmg, int tu, int tuForShot) {
			this.distance = distance;
			this.t = t;
			this.source = source;
			this.weapon = weapon;
			this.acc = acc;
			this.simulationShots = simulationShots;
			this.shotTypeShots = shotTypeShots;
			this.shotType = shotType;
			this.weaponDmg = weaponDmg;
			this.tu = tu;
			this.tuForShot = tuForShot;
		}


		@Override
		public Point call() throws Exception {
			Target target = new Target();
			target.height = t.height;
			target.width = t.width;
			target.x = t.x;
			target.y = source.y + (distance * Simulator.VOXEL_PER_TILE);
			target.z = t.z;

			Cache c = new Cache(CACHE_SIZE);
			double hitRatio = Simulator.simulateAcc(source, target, weapon, acc, simulationShots,
					shotTypeShots, shotType, c);

			int shotsPerTurn = tu / tuForShot;
			double dmg = weaponDmg * (hitRatio / 100d) * shotsPerTurn;
			return new Point(distance, hitRatio, dmg);
		}
	}

	public static final String TU = "tu";

	private static double getPercent(JTextField f) {
		String s = f.getText();
		try {
			if (s.endsWith("%")) {
				int i = Integer.valueOf(s.substring(0, s.length() - 1));
				f.setBackground(Color.WHITE);
				return i;
			}
		} catch (Exception e) {

		}
		f.setBackground(Color.RED);
		throw new RuntimeException();
	}

	static int CACHE_SIZE = 10000000;
	
	final static ExecutorService tpe = Executors.newCachedThreadPool();
	
	private static class NoGUI {
		final String soldierSave;
		final List<String> weapons;
		public NoGUI(String soldierSave, List<String> weapons) {
			this.soldierSave = soldierSave;
			this.weapons = weapons;
		}
		
	}
	
	private static void assume(String s, int i, String[] args) {
		if(!args[i].equals(s)) {
			throw new RuntimeException("Invalid option: " + args[i]);
		}
	}

	private static NoGUI readArgs(String[] args) {
		if(args==null || args.length==0) {
			return null;
		}
		assume("-nogui", 0, args);
		
		if(args.length<5) {
			System.out.println("Usage: -nogui -unit [unitSaveName] -weapon [weaponSaveName1] [weaponSaveName2] ...");
			throw new RuntimeException();
		}
		assume("-unit", 1, args);
		
		String soldierSave = args[2];
		
		assume("-weapon", 3, args);

		List<String> weapons = new ArrayList<>();
		for(int i = 4; i<args.length; i++) {
			File f = new File("weapons", args[i]);
			if(!f.exists()) {
				throw new RuntimeException(f.getAbsolutePath() + " not found!");
			}
			weapons.add(args[i]);
		}
		
		if(weapons.isEmpty()) {
			throw new RuntimeException("No Weapon save parameter!");
		}
		return new NoGUI(soldierSave, weapons);
	}
	
	public static void main(String[] args) throws Exception{
		
		NoGUI noGui = readArgs(args);
		
		JFrame frame = new JFrame();
		frame.setSize(1600, 700);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GUI gui = new GUI(frame);
		
		if(noGui!=null) {
			gui.processNoGui(noGui);
		} else {
			frame.setVisible(true);
			
			frame.add(gui);

			frame.revalidate();
		}


	}


}
