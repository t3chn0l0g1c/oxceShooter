package sim;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class SaveUtil {
	
	public interface Saveable {
		public String save();
		public void load(Map<String, String> values);
	}

	public static final char ASSIGN = '=';
	public static final char SEPARATOR = '#';

	public static final char BRACKET_OPEN = '{';
	public static final char BRACKET_CLOSED = '}';

	public static boolean isGroup(String s) {
		return s != null && s.startsWith("{");
	}

	// {a=abc#b=cde#c={a=asd#b=adf}}
	public static Map<String, String> read(String input) {
		Map<String, String> map = new HashMap<String, String>();
		if (!input.startsWith(String.valueOf(BRACKET_OPEN))) {
			throw new RuntimeException("Invalid String: " + input);
		}
		input = input.substring(1).trim();
		StringBuilder current = new StringBuilder();
		String key = null;
		int nested = 0;
		for (char c : input.toCharArray()) {
			if (c == BRACKET_OPEN) {
				nested++;
			}
			if (c == BRACKET_CLOSED) {
				nested--;
				if(nested==-1) {
					continue;
				}
			}
			if(nested==0){
				if(c==ASSIGN) {
					key = current.toString();
					current = new StringBuilder();
					continue;
				}
				if(c==SEPARATOR) {
					map.put(key, current.toString());
					current = new StringBuilder();
					continue;
				}
			}
			current.append(c);
		}
		map.put(key, current.toString());
		if(map.containsKey(null) || nested!=-1) {
			throw new RuntimeException("Invalid String: " + input);
		}
		return map;
	}
	
	public static String write(Map<String, String> map) {
		StringBuilder str = new StringBuilder();
		str.append(BRACKET_OPEN);
		boolean first = true;
		for(Map.Entry<String, String> e : map.entrySet()) {
			if(!first) {
				str.append(SEPARATOR);
			}
			first = false;
			str.append(e.getKey());
			str.append(ASSIGN);
			str.append(e.getValue());
		}
		str.append(BRACKET_CLOSED);
		return str.toString();
	}
	
	public static void setText(JTextField field, String s) {
		if(s==null) {
			s = "";
		}
		field.setText(s);
	}
	
	public static void setEnabled(JCheckBox field, String s) {
		field.setSelected(s!=null && s.equals("true"));
	}
	
	public static void setIndex(JComboBox<?> c, String s) {
		if(s!=null && !s.isEmpty()) {
			c.setSelectedIndex(Integer.valueOf(s));
		} else {
			c.setSelectedIndex(0);
		}
	}
	
	public static void load(Saveable o, String s) {
		if(s!=null) {
			Map<String, String> m = read(s);
			o.load(m);
		}
	}
}
