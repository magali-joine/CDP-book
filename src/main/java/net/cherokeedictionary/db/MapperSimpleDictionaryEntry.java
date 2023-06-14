package net.cherokeedictionary.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import net.cherokeedictionary.model.SimpleDictionaryEntry;

public class MapperSimpleDictionaryEntry implements ResultSetMapper<SimpleDictionaryEntry>{
	@Override
	public SimpleDictionaryEntry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		SimpleDictionaryEntry e = new SimpleDictionaryEntry();
		//syllabary fields
		e.syllabary.add(r.getString("s1"));
		e.syllabary.add(r.getString("s2"));
		e.syllabary.add(r.getString("s3"));
		e.syllabary.add(r.getString("s4"));
		e.syllabary.add(r.getString("s5"));
		e.syllabary.add(r.getString("s6"));
		e.syllabary.add(r.getString("s7"));
		//pronounce fields
		e.pronunciations.add(r.getString("p1"));
		e.pronunciations.add(r.getString("p2"));
		e.pronunciations.add(r.getString("p3"));
		e.pronunciations.add(r.getString("p4"));
		e.pronunciations.add(r.getString("p5"));
		e.pronunciations.add(r.getString("p6"));
		e.pronunciations.add(r.getString("p7"));
		//other fields
		e.id=r.getInt("id");
		e.definition=r.getString("d");
		e.type=null;
		return e;
	}
	
}
