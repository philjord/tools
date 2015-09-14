package com.gg.slider;

import java.awt.Dimension;

public class SidebarAnimation extends Animation{

	private SidebarSection sideBarSection;
	
	public SidebarAnimation(SidebarSection sidebarSection, int durationMs) {
		
		super(durationMs);
		this.sideBarSection = sidebarSection;
	}

	public void starting () {
		sideBarSection.contentPane.setVisible(true);
	}
	
	protected void render(int value2) {
		
		//System.out.println("render with value : " + value);
		
		sideBarSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, value2));
		
		sideBarSection.contentPane.setVisible(true);

		sideBarSection.revalidate();
	}

	public void stopped () {
		
		sideBarSection.contentPane.setVisible(true);
		sideBarSection.revalidate();
		//sideBarSection.printDimensions();
	}
}
