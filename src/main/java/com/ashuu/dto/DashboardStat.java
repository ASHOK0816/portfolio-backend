package com.ashuu.dto;

public class DashboardStat {

	private String label;
	private String value; // ✅ use String

	// ✅ Constructor
	public DashboardStat(String label, String value) {
		this.label = label;
        this.value = value;
    }

	// ✅ Getters
	public String getLabel() {
		return label;
	}

	public String getValue() {
		return value;
	}

	// ✅ Setters (optional)
	public void setLabel(String label) {
		this.label = label;
	}

	public void setValue(String value) {
		this.value = value;
	}
}