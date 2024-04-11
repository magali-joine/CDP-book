package net.cherokeedictionary.main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.cherokeelessons.chr.Syllabary;

public class FixAnimals {
	
	public FixAnimals() throws IOException {
		List<String> animals = FileUtils.readLines(new File("/home/muksihs/git/BoundPronouns/android/assets/text/animals-latin.txt"), "UTF-8");
		for (String animal: animals) {
			List<String> pieces = Arrays.asList(animal.split("-"));
			Collections.sort(pieces, (a,b)->b.length()-a.length());
			for (String piece: pieces) {
				if (piece==null||piece.length()==0) {
					continue;
				}
				String chr = Syllabary.lat2chr(piece);
				if (chr==null) {
					continue;
				}
				animal=animal.replace(piece, chr);
			}
			animal=animal.replace("-", "");
			System.out.println(animal);
			System.out.flush();
		}
	}
}
