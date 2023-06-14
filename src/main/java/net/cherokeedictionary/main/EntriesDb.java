package net.cherokeedictionary.main;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.cherokeedictionary.dao.Db;

public class EntriesDb {
	private final Db dbc;
	
	public EntriesDb(Db _dbc) {
		this.dbc=_dbc;
	}

	public List<DbEntry> getEntries() {
		int counter = 0;
		List<DbEntry> list = new ArrayList<>();
		try (Connection db = dbc.openConnection()) {
			Statement s = db.createStatement();
			ResultSet rs = s.executeQuery("select * from likespreadsheets");
			while (rs.next()) {
				DbEntry entry = new DbEntry();
				for (Field f : DbEntry.class.getFields()) {
					String simpleName = f.getType().getSimpleName();
					if (simpleName.equals("String")) {
						String name = f.getName();
						f.set(entry, rs.getString(name));
					}
				}
				entry.id = counter++;
				list.add(entry);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return list;
	}
	
	public void removeEntriesWithInvalidSyllabary(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			entry.syllabaryb=StringUtils.defaultString(entry.syllabaryb);
			if (!StringUtils.isEmpty(entry.syllabaryb.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.syllabaryb);
				ientry.remove();
				continue;
			}
			entry.nounadjpluralsyllf=StringUtils.defaultString(entry.nounadjpluralsyllf);
			if (!StringUtils.isEmpty(entry.nounadjpluralsyllf.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.nounadjpluralsyllf);
				ientry.remove();
				continue;
			}
			entry.vfirstpresh=StringUtils.defaultString(entry.vfirstpresh);
			if (!StringUtils.isEmpty(entry.vfirstpresh.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.vfirstpresh);
				ientry.remove();
				continue;
			}
			entry.vsecondimpersylln=StringUtils.defaultString(entry.vsecondimpersylln);
			if (!StringUtils.isEmpty(entry.vsecondimpersylln.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.vsecondimpersylln);
				ientry.remove();
				continue;
			}
			entry.vthirdinfsyllp=StringUtils.defaultString(entry.vthirdinfsyllp);
			if (!StringUtils.isEmpty(entry.vthirdinfsyllp.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.vthirdinfsyllp);
				ientry.remove();
				continue;
			}
			entry.vthirdpastsyllj=StringUtils.defaultString(entry.vthirdpastsyllj);
			if (!StringUtils.isEmpty(entry.vthirdpastsyllj.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.vthirdpastsyllj);
				ientry.remove();
				continue;
			}
			entry.vthirdpressylll=StringUtils.defaultString(entry.vthirdpressylll);
			if (!StringUtils.isEmpty(entry.vthirdpressylll.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.vthirdpressylll);
				ientry.remove();
				continue;
			}
		}
	}

	public void removeEntriesWithMissingPronunciations(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			if (StringUtils.isEmpty(entry.entrytone)) {
				App.err("Missing entrya: " + entry.entrya);
				ientry.remove();
				continue;
			}
			entry.nounadjpluraltone=StringUtils.defaultString(entry.nounadjpluraltone, "");
			entry.nounadjpluralsyllf=StringUtils.defaultString(entry.nounadjpluralsyllf, "");
			if (StringUtils.isEmpty(entry.nounadjpluraltone.replace("-", "")) != StringUtils
					.isEmpty(entry.nounadjpluralsyllf.replace("-", ""))) {
				App.err("Missing nounadjpluraltone or nounadjpluralsyllf: "
						+ entry.entrya + ", " + entry.nounadjpluraltone + "|"
						+ entry.nounadjpluralsyllf);
				ientry.remove();
				continue;
			}
			entry.vfirstprestone=StringUtils.defaultString(entry.vfirstprestone, "");
			entry.vfirstpresh=StringUtils.defaultString(entry.vfirstpresh, "");
			if (StringUtils.isBlank(entry.vfirstprestone.replace("-", "")) != StringUtils
					.isEmpty(entry.vfirstpresh.replace("-", ""))) {
				App.err("Missing vfirstprestone or vfirstpresh: "
						+ entry.entrya + ", " + entry.vfirstprestone + "|"
						+ entry.vfirstpresh);
				ientry.remove();
				continue;
			}
			entry.vsecondimpertone=StringUtils.defaultString(entry.vsecondimpertone, "");
			entry.vsecondimpersylln=StringUtils.defaultString(entry.vsecondimpersylln, "");
			if (StringUtils.isEmpty(entry.vsecondimpertone.replace("-", "")) != StringUtils
					.isEmpty(entry.vsecondimpersylln.replace("-", ""))) {
				App.err("Missing vsecondimpertone or vsecondimpersylln: "
						+ entry.entrya + ", " + entry.vsecondimpertone + "|"
						+ entry.vsecondimpersylln);
				ientry.remove();
				continue;
			}
			entry.vthirdpasttone=StringUtils.defaultString(entry.vthirdpasttone, "");
			entry.vthirdpastsyllj=StringUtils.defaultString(entry.vthirdpastsyllj, "");
			if (StringUtils.isEmpty(entry.vthirdpasttone.replace("-", "")) != StringUtils
					.isEmpty(entry.vthirdpastsyllj.replace("-", ""))) {
				App.err("Missing vthirdpasttone or vthirdpastsyllj: "
						+ entry.entrya + ", " + entry.vthirdpasttone + "|"
						+ entry.vthirdpastsyllj);
				ientry.remove();
				continue;
			}
			entry.vthirdprestone=StringUtils.defaultString(entry.vthirdprestone, "");
			entry.vthirdpressylll=StringUtils.defaultString(entry.vthirdpressylll, "");
			if (StringUtils.isEmpty(entry.vthirdprestone.replace("-", "")) != StringUtils
					.isEmpty(entry.vthirdpressylll.replace("-", ""))) {
				App.err("Missing vthirdprestone or vthirdpressylll: "
						+ entry.entrya + ", " + entry.vthirdprestone + "|"
						+ entry.vthirdpressylll);
				ientry.remove();
				continue;
			}
		}
	}

	/**
	 * We don't want "empty", "word parts", or
	 * "Cross references to the not-included Grammar"
	 * 
	 * @param entries
	 */
	public static void removeUnwantedEntries(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			if (entry.syllabaryb.contains("-")) {
				ientry.remove();
				continue;
			}
			if (entry.entrya.startsWith("-")) {
				ientry.remove();
				continue;
			}
			if (entry.entrya.endsWith("-")) {
				ientry.remove();
				continue;
			}
			if (entry.definitiond.contains("see Gram")) {
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.syllabaryb)) {
				App.err("No Syllabary: " + entry.entrya + " = "
						+ entry.definitiond);
				ientry.remove();
				continue;
			}
		}
	}
	
	public static void removeEntriesWithBogusDefinitions(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			if (entry.definitiond.startsWith("(see")) {
				App.err("Bad definition: " + entry.entrya + ": "
						+ entry.definitiond);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.definitiond)) {
				App.err("Empty definition: " + entry.entrya + ": "
						+ entry.syllabaryb);
				ientry.remove();
				continue;
			}
		}
	}
	
}
