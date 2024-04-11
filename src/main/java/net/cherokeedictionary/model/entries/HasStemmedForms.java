package net.cherokeedictionary.model.entries;

import java.util.List;

import net.cherokeedictionary.shared.StemEntry;

public interface HasStemmedForms {

	/**
	 * Additional entries "normalized" to help expose vowels on word roots.
	 * 
	 * @return
	 */
	public List<StemEntry> getStems();
}