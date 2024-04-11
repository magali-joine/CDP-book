package net.cherokeedictionary.model.entries;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class BodyPart extends LyxEntry {

	@Override
	public List<String> getSyllabary() {
		List<String> list = new ArrayList<>();
		return list;
	}

	@Override
	public String getLyxCode() {
		return sortKey();
	}

	private String _sortKey = null;

	@Override
	protected String sortKey() {
		if (StringUtils.isEmpty(_sortKey)) {
			StringBuilder sb = new StringBuilder();
			sb.append(" ");
			_sortKey = sb.toString();
			_sortKey = _sortKey.replaceAll(" +", " ");
			_sortKey = StringUtils.strip(_sortKey);
		}
		return _sortKey;
	}

	@Override
	public List<String> getPronunciations() {
		return null;
	}
}