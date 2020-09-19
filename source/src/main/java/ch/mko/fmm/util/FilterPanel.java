package main.java.ch.mko.fmm.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.stream.IntStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

public class FilterPanel<Type> extends JPanel {

	private static final long serialVersionUID = -8501324799915120232L;

	private final JCheckBox m_useFilter = new JCheckBox();
	private final JComboBox<Type> m_filter = new JComboBox<>();

	public FilterPanel(Type[] items) {
		this(items, null);
	}
	
	public FilterPanel(Type[] items, String[] texts) {
		setLayout(new BorderLayout());
		add(m_useFilter, BorderLayout.WEST);
		add(m_filter, BorderLayout.CENTER);
		m_useFilter.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				m_filter.setEnabled(m_useFilter.isSelected());
			}
		});
		m_filter.setModel(new DefaultComboBoxModel<>(items));
		if (texts != null) {
			m_filter.setRenderer(new DefaultListCellRenderer() {

				private static final long serialVersionUID = 432447790327294104L;

				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					DefaultListCellRenderer cell = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					
					int selectedIndex = IntStream.range(0, items.length)
							.filter(i -> items[i] == value)
							.findFirst()
							.orElse(-1);
					if (selectedIndex != -1) {
						cell.setText(texts[selectedIndex]);	
					}
					
					return cell;
				}
			});	
		}
		setSelectedItem(null);
	}
	
	public void addActionListener(ActionListener actionListener) {
		m_useFilter.addActionListener(actionListener);
		m_filter.addActionListener(actionListener);
	}
	
	public void setSelectedItem(Type item) {
		m_useFilter.setSelected(item != null);
		m_filter.setEnabled(m_useFilter.isSelected());
		if (item != null) {
			m_filter.setSelectedItem(item);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Type getSelectedItem() {
		return m_useFilter.isSelected() ? (Type) m_filter.getSelectedItem() : null;
	}
	
	public boolean include(Type item) {
		Type selectedItem = getSelectedItem();
		return selectedItem == null || selectedItem.equals(item);
	}
}
