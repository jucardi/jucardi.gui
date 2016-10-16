/**
 * @file						PropertyGrid.java

 * @author					juan.diaz
 * @date						08/03/2012
 * @copyright		  	Jucardi. All Rights Reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */
package com.jucardi.gui.forms;

import com.jucardi.gui.DisplayInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PropertyGrid extends JPanel
{
	// region Constants
	private static final long       serialVersionUID             = 9145338406554707501L;
	private static final double     COLUMN_SEPARATION            = 15.00;
	// endregion

	// region Fields
	private Object                  currentObject                   = null;
	private int                     columIndex                      = 0;
	private Map<String, JComponent> componentValueList              = new HashMap<String, JComponent>();
	private Map<String, JLabel>     displayLabelList                = new HashMap<String, JLabel>();
	private Map<String, Method>     setters                         = new HashMap<String, Method>();
	private Map<String, Method>     getters                         = new HashMap<String, Method>();
	private Map<String, Class>      propertyClass                   = new HashMap<String, Class>();
	private Map<String, String>     displayNames                    = new HashMap<String, String>();
	private Map<String, AccessMode> accessModeList                  = new HashMap<String, AccessMode>();
	private Map<String, Field>      fieldInfoList                   = new HashMap<String, Field>();
	private String                  firstColumnTitle                = null;
	private String                  secondColumnTitle               = null;
	private boolean                 isDisplayInfoAnnotationRequired = false;
	private boolean                 loadGettersAndSetters           = true;
	private boolean                 loadFields                      = false;
	private List<String>            propertiesThatStartWithIs       = new ArrayList<String>();
	// endregion

	// region Enums

	enum AccessMode
	{
		METHODS,
		FIELD
	}

	// endregion

	// region Getters and Setters

	/** Gets the title to be shown in the first column. */
	public String getFirstColumnTitle()
	{
		return firstColumnTitle;
	}

	/** Sets the title to be shown in the first column. */
	public void setFirstColumnTitle(String firstColumnTitle)
	{
		this.firstColumnTitle = firstColumnTitle;
	}

	/** Gets the title to be shown in the second column. */
	public String getSecondColumnTitle()
	{
		return secondColumnTitle;
	}

	/** Sets the title to be shown in the second column. */
	public void setSecondColumnTitle(String secondColumnTitle)
	{
		this.secondColumnTitle = secondColumnTitle;
	}

	/** Gets the current object. */
	public Object getCurrentObject()
	{
		return this.currentObject;
	}

	/** Sets the current object. */
	public void setCurrentObject(Object object)
	{
		this.currentObject = object;
		this.refresh();
	}

	/** Indicates whether the DisplayInfo annotation is required to load a Getters, Setters or Fields.
	 *
	 * @return 'true' if the DisplayInfo annotation is required; otherwise 'false'.
	 */
	public boolean isDisplayInfoAnnotationRequired()
	{
		return this.isDisplayInfoAnnotationRequired;
	}

	/** Sets whether the DisplayInfo annotation is required to load a Getters, Setters or Fields.
	 *
	 * @param value 'true' if the DisplayInfo annotation is required; otherwise 'false'.
	 */
	public void setDisplayInfoAnnotationRequired(boolean value)
	{
		this.isDisplayInfoAnnotationRequired = value;
	}

	/** Indicates whether the Getters and Setters in a class should be loaded.
	 *
	 * @return 'true' if the Getters and Setters should be loaded; otherwise 'false'
	 */
	public boolean getLoadGettersAndSetters()
	{
		return loadFields;
	}

	/** Sets whether the Getters and Setters  in a class should be loaded.
	 *
	 * @param loadFields 'true' if the Getters and Setters should be loaded; otherwise 'false'
	 */
	public void setLoadGettersAndSetters(boolean loadFields)
	{
		this.loadFields = loadFields;
	}

	/** Indicates whether the public fields in a class should be loaded.
	 *
	 * @return 'true' if the fields should be loaded; otherwise 'false'
	 */
	public boolean getLoadFields()
	{
		return loadFields;
	}

	/** Sets whether the public fields in a class should be loaded.
	 *
	 * @param loadFields 'true' if the fields should be loaded; otherwise 'false'
	 */
	public void setLoadFields(boolean loadFields)
	{
		this.loadFields = loadFields;
	}

	// endregion

	// region Constructor

	/** Create the panel. */
	public PropertyGrid()
	{
		this.initialize();
	}

	// endregion

	// region Public Methods

	/** Refreshes the visual display. */
	public void refresh()
	{
		this.reset();
		this.loadGettersAndSetters();
		this.setHeader();
	}

	/** Returns the properties count. */
	public int propertyCount()
	{
		return this.columIndex;
	}

	// endregion

	// region Methods

	/** Sets the background color. */
	public void setBackground(Color bg)
	{
		super.setBackground(bg);

		if (this.scrollPane != null)
			this.scrollPane.setBackground(bg);

		if (this.panel != null)
			this.panel.setBackground(bg);
	}

	/** Adds a text field to the given property */
	private void addTextfield(String propertyName, String displayName)
	{
		JLabel lblNewLabel = new JLabel(displayName);
		this.displayLabelList.put(propertyName, lblNewLabel);
		this.panel.add(lblNewLabel, String.format("cell 0 %d,alignx trailing", this.columIndex));

		JTextField textField = new JTextField();
		textField.setName(propertyName);
		this.componentValueList.put(propertyName, textField);
		this.panel.add(textField, String.format("cell 2 %d,growx", this.columIndex));

		DocumentListener listener = new PropertyDocumentListener(propertyName) {
			public void removeUpdate(DocumentEvent e)
			{
				textChanged(e, property);
			}

			public void insertUpdate(DocumentEvent e)
			{
				textChanged(e, property);
			}

			public void changedUpdate(DocumentEvent e)
			{
				textChanged(e, property);
			}
		};

		textField.getDocument().addDocumentListener(listener);
		this.columIndex++;
	}

	/** Adds a combo box to the given property */
	private void addBooleanField(String propertyName, String displayNamw)
	{
		JLabel lblNewLabel = new JLabel(displayNamw);
		this.displayLabelList.put(propertyName, lblNewLabel);
		this.panel.add(lblNewLabel, String.format("cell 0 %d,alignx trailing", this.columIndex));

		JComboBox comboBoxField = new JComboBox();
		comboBoxField.setName(propertyName);
		comboBoxField.addItem(Boolean.TRUE);
		comboBoxField.addItem(Boolean.FALSE);

		this.componentValueList.put(propertyName, comboBoxField);
		this.panel.add(comboBoxField, String.format("cell 2 %d,growx", this.columIndex));

		comboBoxField.addActionListener(new PropertyActionListener(propertyName) {
			public void actionPerformed(ActionEvent arg0)
			{
				comboBoxItemChanged(arg0, this.property);
			}
		});

		this.columIndex++;
	}

	/** Adds a combo box to the given property */
	private void addEnumField(String propertyName, String displayName, Class enumClass)
	{
		JLabel lblNewLabel = new JLabel(displayName);
		this.displayLabelList.put(propertyName, lblNewLabel);
		this.panel.add(lblNewLabel, String.format("cell 0 %d,alignx trailing", this.columIndex));

		JComboBox comboBoxField = new JComboBox();
		comboBoxField.setName(propertyName);
		for (Object enumValue : enumClass.getEnumConstants())
			comboBoxField.addItem(enumValue);

		this.componentValueList.put(propertyName, comboBoxField);
		this.panel.add(comboBoxField, String.format("cell 2 %d,growx", this.columIndex));

		comboBoxField.addActionListener(new PropertyActionListener(propertyName) {
			public void actionPerformed(ActionEvent arg0)
			{
				comboBoxItemChanged(arg0, this.property);
			}
		});

		this.columIndex++;
	}

	/** Resets the control. */
	private void reset()
	{
		this.componentValueList = new HashMap<String, JComponent>();
		this.displayLabelList   = new HashMap<String, JLabel>();
		this.setters            = new HashMap<String, Method>();
		this.getters            = new HashMap<String, Method>();
		this.propertyClass      = new HashMap<String, Class>();
		this.displayNames       = new HashMap<String, String>();
		this.columIndex = 0;
		this.scrollPane.remove(this.panel);
		this.panel = new JPanel();
		this.scrollPane.add(this.panel);
		this.scrollPane.setViewportView(this.panel);
	}

	/** Loads the Getters and Setters of the current object. */
	private void loadGettersAndSetters()
	{
		if (!this.loadGettersAndSetters)
			return;

		try
		{
			Class<? extends Object> c = this.currentObject.getClass();

			for (Method method : c.getMethods())
			{
				int modifiers = method.getModifiers();

				if ((modifiers & Modifier.PUBLIC) != Modifier.PUBLIC)
					continue;

				if (this.isDisplayInfoAnnotationRequired && !method.isAnnotationPresent(DisplayInfo.class))
					continue;

				DisplayInfo propertyInfo = method.getAnnotation(DisplayInfo.class);

				String propertyName = "";

				if (method.getName().startsWith("get") && method.getName().length() > 3)
				{
					propertyName = method.getName().replace("get", "");
					this.getters.put(propertyName, method);
					this.propertyClass.put(propertyName, method.getReturnType());
				}
				else if (method.getName().startsWith("is"))
				{
					propertyName = method.getName().replace("is", "");
					this.propertiesThatStartWithIs.add(propertyName);
					this.getters.put(propertyName, method);
					this.propertyClass.put(propertyName, method.getReturnType());
				}
				else if (method.getName().startsWith("set") && method.getName().length() > 3)
				{
					propertyName = method.getName().replace("set", "");
					this.setters.put(propertyName, method);
				}
				else if (method.getName().startsWith("setIs") && method.getName().length() > 5)
				{
					propertyName = method.getName().replace("setIs", "");
					this.propertiesThatStartWithIs.add(propertyName);
					this.setters.put(propertyName, method);
				}

				String displayName = propertyInfo != null && !this.isStringNullOrEmpty(propertyInfo.displayName()) ? propertyInfo.displayName() : propertyName;
				this.displayNames.put(propertyName, displayName);

				if (this.displayNames.containsKey(propertyName) && this.doesPropertyStartWithIs(propertyName) && displayName.equals(propertyName))
					this.displayNames.put(propertyName, String.format("is%s", displayName));
			}

			String rowString = "";

			for (int i = 0; i < getters.keySet().size(); i++)
				rowString = String.format("%s%s", rowString, "[]");

			this.panel.setLayout(new MigLayout("", String.format("[fill][%f][grow]", PropertyGrid.COLUMN_SEPARATION), rowString));

			for (String key : getters.keySet())
			{
				System.out.println(key);
				Class klass = this.propertyClass.get(key);

				if (klass == Boolean.class || klass == boolean.class)
					this.addBooleanField(key, this.displayNames.get(key));
				else if (klass.isEnum())
					this.addEnumField(key, this.displayNames.get(key), this.propertyClass.get(key));
				else if (klass == String.class || klass.isPrimitive())
					this.addTextfield(key, this.displayNames.get(key));

				this.accessModeList.put(key, AccessMode.METHODS);
				this.setVisualValue(key);
				this.verifySetter(key);
			}
		}
		catch (Throwable e)
		{
		}
	}

	/** Loads the public fields of the current object. */
	private void loadFields()
	{
		if (!this.loadFields)
			return;

		try
		{
			Class<? extends Object> c = this.currentObject.getClass();

			for (Field field : c.getFields())
			{
				int modifiers = field.getModifiers();

				if ((modifiers & Modifier.PUBLIC) != Modifier.PUBLIC)
					continue;

				if (this.isDisplayInfoAnnotationRequired && !field.isAnnotationPresent(DisplayInfo.class))
					continue;

				DisplayInfo propertyInfo = field.getAnnotation(DisplayInfo.class);

				if (field.getName().startsWith("is"))
					this.propertiesThatStartWithIs.add(field.getName().replace("is", ""));

				String propertyName = field.getName().replace("is", "");
				String displayName  = propertyInfo != null && !this.isStringNullOrEmpty(propertyInfo.displayName()) ? propertyInfo.displayName() : propertyName;

				if (this.accessModeList.containsKey(propertyName))
					continue;

				this.propertyClass.put(propertyName, field.getDeclaringClass());
				this.displayNames.put(propertyName, displayName);

				if (this.displayNames.containsKey(propertyName) && this.doesPropertyStartWithIs(propertyName) && !displayName.equals(propertyName))
					this.displayNames.put(propertyName, String.format("is%s", displayName));

				this.fieldInfoList.put(propertyName, field);
			}
		}
		catch (Exception e)
		{
		}
	}

	/** Sets the property grid header if enabled. */
	private void setHeader()
	{
		if (this.firstColumnTitle != null || this.secondColumnTitle != null)
		{
			JPanel headerPanel = new JPanel();
			headerPanel.setBackground(new Color(0, 0, 0, 35));
			headerPanel.setLayout(new MigLayout("", String.format("[2][%d][%f][grow]", this.getLargestJLabelWidth() + 2, PropertyGrid.COLUMN_SEPARATION), "[][][]"));

			JLabel firstColumTitleControl = new JLabel(this.firstColumnTitle != null ? this.firstColumnTitle : "");
			firstColumTitleControl.setFont(new Font(firstColumTitleControl.getFont().getFamily(), Font.BOLD, firstColumTitleControl.getFont().getSize()));
			firstColumTitleControl.setHorizontalAlignment(SwingConstants.CENTER);
			headerPanel.add(firstColumTitleControl, "cell 1 1");

			JLabel secondColumTitleControl = new JLabel(this.secondColumnTitle != null ? this.secondColumnTitle : "");
			secondColumTitleControl.setFont(new Font(secondColumTitleControl.getFont().getFamily(), Font.BOLD, secondColumTitleControl.getFont().getSize()));
			secondColumTitleControl.setHorizontalAlignment(SwingConstants.CENTER);
			headerPanel.add(secondColumTitleControl, "cell 3 1");

			JSeparator separator = new JSeparator();
			headerPanel.add(separator, BorderLayout.SOUTH);

			this.add(headerPanel, BorderLayout.NORTH);

			this.columIndex++;
		}
	}

	/** Gets the width of the largest JLabel in the left column. */
	private int getLargestJLabelWidth()
	{
		int largestPixWidth = 0;

		for (String propertyName : this.displayLabelList.keySet())
		{
			JLabel currentLabel = this.displayLabelList.get(propertyName);

			Font font = currentLabel.getFont();

			@SuppressWarnings("serial")
			FontMetrics metrics = new FontMetrics(font) {};
			Rectangle2D bounds = metrics.getStringBounds(currentLabel.getText(), null);
			int widthInPixels = (int) bounds.getWidth();

			if (largestPixWidth > widthInPixels)
				continue;

			largestPixWidth = widthInPixels;
		}

		return largestPixWidth;
	}

	/** Sets the value to the given property. */
	private void setValue(String property, Object value)
	{
		if (!setters.containsKey(property))
			return;

		Method method = this.setters.get(property);

		try
		{
			method.invoke(this.currentObject, value);
		}
		catch (Exception e)
		{
		}

		this.setVisualValue(property);

	}

	/** Gets the value from the given property */
	private Object getValue(String property)
	{
		if (!getters.containsKey(property))
			return null;

		Object value = null;

		try
		{
			value = getters.get(property).invoke(this.currentObject, (Object[]) null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		return value;
	}

	/** Sets the visual value to the given property. */
	private void setVisualValue(String property)
	{
		try
		{
			String value = this.getValue(property).toString();
			JComponent currentField = this.componentValueList.get(property);

			if (currentField.getClass() == JTextField.class)
				((JTextField) currentField).setText(value);
			else if (currentField.getClass() == JComboBox.class)
				((JComboBox) currentField).setSelectedItem(value);
		}
		catch (Exception e)
		{
		}
	}

	/** Verifies if the property has a setter and disables it's control if it does not. */
	private void verifySetter(String property)
	{
		if (this.setters.containsKey(property))
			return;

		this.componentValueList.get(property).setEnabled(false);
	}

	/** Occurs when a text box value is changed. */
	private void textChanged(DocumentEvent e, String propertySender)
	{
		Object value = null;
		JTextField currentField = (JTextField) this.componentValueList.get(propertySender);

		try
		{
			Class klass = this.propertyClass.get(propertySender);

			if (klass == int.class || klass == Integer.class)
				value = Integer.valueOf(currentField.getText());
			else if (klass == float.class || klass == Float.class)
				value = Float.valueOf(currentField.getText());
			else if (klass == double.class || klass == Double.class)
				value = Double.valueOf(currentField.getText());
			else if (klass == boolean.class || klass == Boolean.class)
				value = Boolean.valueOf(currentField.getText());
			else if (klass == String.class)
				value = currentField.getText();
			else if (klass.isEnum())
				value = Enum.valueOf(this.propertyClass.get(propertySender), currentField.getText());
		}
		catch (Exception ex)
		{
			value = null;
		}

		if (value == null)
		{
			currentField.setForeground(Color.RED);
			return;
		}

		currentField.setForeground(Color.BLACK);
		this.setValue(propertySender, value);
	}

	/** Occurs when a combo box item is changed. */
	private void comboBoxItemChanged(ActionEvent arg0, String propertySender)
	{
		JComboBox currentField = (JComboBox) this.componentValueList.get(propertySender);
		Object value = currentField.getSelectedItem();

		if (value == null)
		{
			currentField.setForeground(Color.RED);
			return;
		}

		currentField.setForeground(Color.BLACK);
		this.setValue(propertySender, value);
	}

	/** Verifies if a String is null or empty. */
	private boolean isStringNullOrEmpty(String value)
	{
		if (value == null)
			return true;

		if (value.isEmpty())
			return true;

		return false;
	}

	/** Indicates if a property name starts with 'is' */
	private boolean doesPropertyStartWithIs(String property)
	{
		for (String prop : this.propertiesThatStartWithIs)
		{
			if (prop.equalsIgnoreCase(property))
				return true;
		}

		return false;
	}
	// endregion

	// region Designer

	/** Initializes the visual state of this control. */
	private void initialize()
	{
		this.setLayout(new BorderLayout());
		this.scrollPane.setBorder(null);
		this.add(this.scrollPane, BorderLayout.CENTER);
		this.loadGettersAndSetters();
	}

	private JPanel      panel      = new JPanel();
	private JScrollPane scrollPane = new JScrollPane();

	// endregion
}
