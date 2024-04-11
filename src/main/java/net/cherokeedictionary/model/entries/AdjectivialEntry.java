package net.cherokeedictionary.model.entries;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class AdjectivialEntry extends LyxEntry {
	public DefinitionLine single_in;
	public DefinitionLine single_an;
	public DefinitionLine plural_in;
	public DefinitionLine plural_an;
	public ExampleLine[] example = null;

	@Override
	public List<String> getSyllabary() {
		List<String> list = new ArrayList<>();
		list.add(single_in.syllabary);
		if (!single_an.syllabary.isEmpty()) {
			list.add(single_an.syllabary);
		}
		if (!plural_in.syllabary.isEmpty()) {
			list.add(plural_in.syllabary);
		}
		if (!plural_an.syllabary.isEmpty()) {
			list.add(plural_an.syllabary);
		}
		return list;
	}

	@Override
	public List<String> getPronunciations() {
		List<String> list = new ArrayList<>();
		list.add(single_in.pronounce);
		if (!single_an.syllabary.isEmpty()) {
			list.add(single_an.pronounce);
		}
		if (!plural_in.syllabary.isEmpty()) {
			list.add(plural_in.pronounce);
		}
		if (!plural_an.syllabary.isEmpty()) {
			list.add(plural_an.pronounce);
		}
		return list;
	}

	@Override
	public String getLyxCode() {
		StringBuilder sb = new StringBuilder();
		sb.append(lyxSyllabaryPronounceDefinition(id, single_in, pos, definition, null));
		boolean addit = isOnlySyllabary(single_an.syllabary);
		addit |= isOnlySyllabary(plural_in.syllabary);
		addit |= isOnlySyllabary(plural_an.syllabary);
		if (addit) {
			sb.append("\\begin_deeper\n");
			if (isOnlySyllabary(single_an.syllabary)) {
				sb.append(lyxSyllabaryPronounce(single_an));
			}
			if (isOnlySyllabary(plural_in.syllabary)) {
				sb.append(lyxSyllabaryPronounce(plural_in));
			}
			if (isOnlySyllabary(plural_an.syllabary)) {
				sb.append(lyxSyllabaryPronounce(plural_an));
			}
			sb.append("\\end_deeper\n");
		}
		return sb.toString();
	}

	private String _sortKey = null;

	@Override
	protected String sortKey() {
		if (StringUtils.isEmpty(_sortKey)) {
			StringBuilder sb = new StringBuilder();
			sb.append(single_in.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
			sb.append(" ");
			sb.append(single_an.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
			sb.append(" ");
			sb.append(plural_in.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
			sb.append(" ");
			sb.append(plural_an.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
			sb.append(" ");
			sb.append(single_in.pronounce.replace("-", ""));
			sb.append(" ");
			sb.append(single_an.pronounce.replace("-", ""));
			sb.append(" ");
			sb.append(plural_in.pronounce.replace("-", ""));
			sb.append(" ");
			sb.append(plural_an.pronounce.replace("-", ""));
			_sortKey = sb.toString();
			_sortKey = _sortKey.replaceAll(" +", " ");
			_sortKey = StringUtils.strip(_sortKey);
		}
		return _sortKey;
	}
}