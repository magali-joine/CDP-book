package net.cherokeedictionary.model.entries;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ConjunctionEntry extends LyxEntry {
	public DefinitionLine conjunction;
	public ExampleLine[] example = null;

	@Override
	public List<String> getSyllabary() {
		List<String> list = new ArrayList<>();
		list.add(conjunction.syllabary);
		return list;
	}

	@Override
	public List<String> getPronunciations() {
		List<String> list = new ArrayList<>();
		list.add(conjunction.pronounce);
		return list;
	}

	@Override
	public String getLyxCode() {
		StringBuilder sb = new StringBuilder();
		sb.append(lyxSyllabaryPronounceDefinition(id, conjunction,
				pos, definition, null));
		return sb.toString();
	}

	private String _sortKey = null;

	@Override
	protected String sortKey() {
		if (StringUtils.isEmpty(_sortKey)) {
			StringBuilder sb = new StringBuilder();
			sb.append(conjunction.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
			sb.append(" ");
			sb.append(conjunction.pronounce.replace("-", ""));
			_sortKey = sb.toString();
			_sortKey = _sortKey.replaceAll(" +", " ");
			_sortKey = StringUtils.strip(_sortKey);
		}
		return _sortKey;
	}
}