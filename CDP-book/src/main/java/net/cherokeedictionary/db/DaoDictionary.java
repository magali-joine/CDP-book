package net.cherokeedictionary.db;

import java.io.File;
import java.util.List;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import net.cherokeedictionary.model.SimpleDictionaryEntry;

public interface DaoDictionary {
	public String table_dictionary="LIKESPREADSHEETS";
	public DaoDictionary dao = new DBI(new H2Db(new File("output/tmp-db"))).onDemand(DaoDictionary.class);

	@RegisterMapper(MapperSimpleDictionaryEntry.class)
	@SqlQuery("SELECT id,"
			+ " SYLLABARYB as s1, VFIRSTPRESH as s2, VTHIRDPASTSYLLJ as s3,"
			+ " VTHIRDPRESSYLLL as s4, VSECONDIMPERSYLLN as s5, VTHIRDINFSYLLP as s6,"
			+ " NOUNADJPLURALSYLLF as s7,\n"
			+ "	ENTRYTONE as p1, VFIRSTPRESTONE as p2, VTHIRDPASTTONE as p3,"
			+ " VTHIRDPRESTONE as p4, VSECONDIMPERTONE as p5, VTHIRDINFTONE as p6,"
			+ " NOUNADJPLURALTONE as p7,"
			+ " DEFINITIOND as d\n"
			+ "FROM " + table_dictionary)
	public List<SimpleDictionaryEntry> getSimpleEntries();
}
