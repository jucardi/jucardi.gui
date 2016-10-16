/**
 * @file						PictureBox.java
 * @author					juan.diaz
 * @date						08/03/2012
 * @copyright		  	Jucardi. All Rights Reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */
package com.jucardi.gui.forms;

import com.jucardi.gui.forms.DisplayInfo;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JLabel;


public class PictureBox extends JPanel
{
	// region Fields

	private static final long serialVersionUID = 7603624253977899404L;
	private JLabel pictureArea = new JLabel();

	private Image image;
	private Object tag;
	private SizeMode sizeMode = SizeMode.NORMAL;

	// endregion

	// region Constructor

	public PictureBox()
	{
		this.setLayout(null);
		this.pictureArea.setLocation(0, 0);
		this.add(pictureArea);
	}

	// endregion

	// region Properties

	/** Gets the value of the 'Image' property */
	public Image getImage()
	{
		return this.image;
	}

	/** Sets the value of the 'Image' property */
	public void setImage(Image value)
	{
		this.image = value;
		this.update();
	}

	/** Gets the value of the 'SizeMode' property */
	@DisplayInfo(displayName = "SizeMode", description = "")
	public SizeMode getSizeMode()
	{
		return this.sizeMode;
	}

	/** Sets the value of the 'SizeMode' property */
	@DisplayInfo(displayName = "SizeMode", description = "")
	public void setSizeMode(SizeMode value)
	{
		this.sizeMode = value;
		this.update();
	}

	/** Gets the object tagged to this control. */
	public Object getTag()
	{
		return this.tag;
	}

	/** Sets an object tagged to this control. */
	public void setTag(Object value)
	{
		this.tag = value;
	}

	// endregion

	// region Methods

	/** Updates the image. */
	private void update()
	{
		if (this.image == null)
		{
			this.pictureArea.setIcon(null);
			return;
		}


		Image img = this.proccessImage(this.image);
		this.pictureArea.setSize(img.getWidth(null), img.getHeight(null));
		this.pictureArea.setIcon(new ImageIcon(img));

		switch (this.sizeMode)
		{
			case ZOOM:
			case CENTER:
				this.pictureArea.setLocation((this.getWidth() - img.getWidth(null)) / 2, (this.getHeight() - img.getHeight(null)) / 2);
				break;
			case AUTOSIZE:
				this.setSize(img.getWidth(null), img.getHeight(null));
			case NORMAL:
			case STRETCH:
				this.pictureArea.setLocation(1, 1);
				break;
		}
	}

	/** Processes the given image to get a preview image based on the control settings. */
	private Image proccessImage(Image img)
	{
		int width = 0;
		int height = 0;

		switch (this.sizeMode)
		{
			case NORMAL:
			case CENTER:
			case AUTOSIZE:
				return this.image;
			case STRETCH:
				width = this.getWidth();
				height = this.getHeight();
				break;
			case ZOOM:
				double imgWHRatio = (double) img.getWidth(null) / (double) img.getHeight(null);
				double pboxWHRatio = (double) this.getWidth() / (double) this.getHeight();

				if (imgWHRatio >= pboxWHRatio)
				{
					width = this.getWidth();
					height = (int) ((double) img.getHeight(null) * ((double) this.getWidth()) / (double) img.getWidth(null));
				}
				else if (imgWHRatio < pboxWHRatio)
				{
					height = this.getHeight();
					width = (int) ((double) img.getWidth(null) * ((double) this.getHeight()) / (double) img.getHeight(null));
				}

				break;
		}

		return img.getScaledInstance(width - 2, height - 2, Image.SCALE_SMOOTH);
	}

	// endregion
}
