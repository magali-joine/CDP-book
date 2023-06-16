#!/usr/bin/env bash
"""true" '''\'
set -e
eval "$(${CONDA_EXE:-conda} shell.bash hook)"
conda activate cdp-book
exec python "$0" "$@"
exit $?
''"""
import csv
import dataclasses
import itertools
import re
from copy import copy
from pathlib import Path


@dataclasses.dataclass
class RowData:
    definitiond: str = ""
    "Raw definition"

    partofspeechc: str = ""

    syllabaryb: str = ""
    "3rd person singular present (Syllabary)"
    entrytone: str = ""
    "3rd person singular present (pronunciation)"

    nounadjpluralsyllf: str = ""
    "Noun/Adjective Plural (Syllabary)"
    nounadjpluraltone: str = ""
    "Noun/Adjective Plural (pronunciation)"

    vfirstpresh: str = ""
    "1st person singular present (Syllabary)"
    vfirstprestone: str = ""
    "1st person singular present (pronunciation)"

    vthirdpastsyllj: str = ""
    "3rd person singular remote past (Syllabary)"
    vthirdpasttone: str = ""
    "3rd person singular remote past (pronunciation)"

    vthirdpressylll: str = ""
    "3rd person singular habitual (Syllabary)"
    vthirdprestone: str = ""
    "3rd person singular habitual (pronunciation)"

    vsecondimpersylln: str = ""
    "2nd person singular immediate (Syllabary)"
    vsecondimpertone: str = ""
    "2nd person singular immediate (pronunciation)"

    vthirdinfsyllp: str = ""
    "3rd person singular deverbal (Syllabary)"
    vthirdinftone: str = ""
    "3rd person singular deverbal (pronunciation)"

    source: str = ""
    "Reference source"

    @classmethod
    def from_dict(cls, row: dict[str, str]) -> "RowData":
        entry = RowData()
        for key in row.keys():
            if hasattr(entry, key):
                value = row[key]
                if not value.replace("-", "").strip():
                    continue
                setattr(entry, key, value)
        return entry


@dataclasses.dataclass
class WordEntry:
    syllabary: str = ""
    pronounce: str = ""

    def __bool__(self) -> bool:
        return bool(self.syllabary) or bool(self.pronounce)

    def needs_split(self) -> bool:
        if "," not in self.syllabary:
            return False
        if "," not in self.pronounce:
            return False
        return True

    def splits_count(self) -> int:
        if "," not in self.syllabary:
            return 0
        if "," not in self.pronounce:
            return 0
        return min(self.syllabary.count(","), self.pronounce.count(","))

    def splits(self) -> list["WordEntry"]:
        if not self.needs_split():
            return [self]
        splits: list[WordEntry] = list()
        while "," in self.syllabary and "," in self.pronounce:
            new_entry = WordEntry()
            new_entry.pronounce = self.pronounce[:self.pronounce.find(",")].strip()
            new_entry.syllabary = self.syllabary[:self.syllabary.find(",")].strip()
            splits.append(new_entry)
            self.syllabary = self.syllabary[self.syllabary.find(",")+1:].strip()
            self.pronounce = self.pronounce[self.pronounce.find(",")+1:].strip()
        splits.append(self)
        return splits

    def tabbed(self) -> str:
        return f"{self.syllabary}\t[{self.pronounce}]"


@dataclasses.dataclass
class DictionaryEntry:
    """
    Selected columns from spreadsheet dump of CherokeeDictionary.net DB
    """

    _data: RowData = dataclasses.field(default_factory=RowData)

    _comment: str = ""

    def has_entries(self) -> bool:
        if self.first_singular_present.pronounce:
            return True
        if self.second_singular_immediate.pronounce:
            return True
        if self.third_singular_present.pronounce:
            return True
        if self.third_plural_noun_adj.pronounce:
            return True
        if self.third_singular_remote_past.pronounce:
            return True

        if self.third_singular_deverbal.pronounce:
            return True
        return False

    def needs_cherokee_splits(self) -> bool:
        if "," in self.third_singular_present.pronounce:
            return True
        if "," in self.third_plural_noun_adj:
            return True

    @property
    def pos(self) -> str:
        if self.data.partofspeechc.lower().startswith("v"):
            return "v"
        if self.data.partofspeechc.lower().startswith("n"):
            return "n"
        if self.data.partofspeechc.lower().startswith("a"):
            return "a"
        if self.data.partofspeechc.lower().startswith("c"):
            return "c"
        if self.data.partofspeechc.lower().startswith("p"):
            return "p"
        if self.data.partofspeechc.lower().startswith("i"):
            return "i"
        raise RuntimeError(f"Unknown POS for {self.data}")

    @pos.setter
    def pos(self, pos: str) -> None:
        self.data.partofspeechc = pos

    @property
    def comment(self) -> str:
        return self._comment

    @comment.setter
    def comment(self, comment: str) -> None:
        self._comment = comment

    def glossary_lines(self) -> list[str]:
        lines: list[str] = list()
        if self.first_singular_present.pronounce:
            lines.append(f"{self.first_singular_present}")
        return lines

    @property
    def data(self) -> RowData:
        return self._data

    @data.setter
    def data(self, data: RowData) -> None:
        self._data = copy(data)

    def copy(self) -> "DictionaryEntry":
        dupe = copy(self)
        dupe.data = self.data
        return dupe

    @property
    def definition(self) -> str:
        """Raw definition"""
        return self.data.definitiond

    @definition.setter
    def definition(self, definition: str) -> None:
        self.data.definitiond = definition.strip()

    _3rd_sing_pres: WordEntry | None = None

    @property
    def third_singular_present(self) -> WordEntry:
        """3rd person singular present continuous"""
        if self._3rd_sing_pres is None:
            self._3rd_sing_pres = WordEntry(syllabary=self.data.syllabaryb, pronounce=self.data.entrytone)
        return self._3rd_sing_pres

    _3rd_plur_na: WordEntry | None = None

    @property
    def third_plural_noun_adj(self) -> WordEntry:
        """3rd person plural noun or adjective"""
        if self._3rd_plur_na is None:
            self._3rd_plur_na = WordEntry(syllabary=self.data.nounadjpluralsyllf, pronounce=self.data.nounadjpluraltone)
        return self._3rd_plur_na

    _1st_sing_pres: WordEntry | None = None

    @property
    def first_singular_present(self) -> WordEntry:
        """1st person singular present continuous"""
        if self._1st_sing_pres is None:
            self._1st_sing_pres = WordEntry(syllabary=self.data.vfirstpresh, pronounce=self.data.vfirstprestone)
        return self._1st_sing_pres

    _3rd_sing_rp: WordEntry | None = None

    @property
    def third_singular_remote_past(self) -> WordEntry:
        """3rd person singular remote past"""
        if self._3rd_sing_rp is None:
            self._3rd_sing_rp = WordEntry(syllabary=self.data.vthirdpastsyllj, pronounce=self.data.vthirdpasttone)
        return self._3rd_sing_rp

    _3rd_sing_hab: WordEntry | None = None

    @property
    def third_singular_habitual(self) -> WordEntry:
        """3rd person singular remote past"""
        if self._3rd_sing_hab is None:
            self._3rd_sing_hab = WordEntry(syllabary=self.data.vthirdpressylll, pronounce=self.data.vthirdprestone)
        return self._3rd_sing_hab

    _2nd_sing_imm: WordEntry | None = None

    @property
    def second_singular_immediate(self) -> WordEntry:
        """1st person singular immediate"""
        if self._2nd_sing_imm is None:
            self._2nd_sing_imm = WordEntry(syllabary=self.data.vsecondimpersylln, pronounce=self.data.vsecondimpertone)
        return self._2nd_sing_imm

    _3rd_sing_dv: WordEntry | None = None

    @property
    def third_singular_deverbal(self) -> WordEntry:
        """3rd person singular deverbal"""
        if self._3rd_sing_dv is None:
            self._3rd_sing_dv = WordEntry(syllabary=self.data.vsecondimpersylln, pronounce=self.data.vsecondimpertone)
        return self._3rd_sing_dv

    @property
    def source(self) -> str:
        """Reference source"""
        return self.data.source

    @classmethod
    def from_dict(cls, row: dict[str, str]) -> "DictionaryEntry":
        entry = DictionaryEntry()
        entry.data = RowData.from_dict(row)
        return entry


def remove_unwanted_entries(entries: list[DictionaryEntry]) -> list[DictionaryEntry]:
    entries = [entry for entry in entries if "-" not in entry.data.syllabaryb]
    entries = [entry for entry in entries if "(see gram" not in entry.data.definitiond.lower()]
    entries = [entry for entry in entries if "see " not in entry.data.definitiond.lower()]
    entries = [entry for entry in entries if entry.data.syllabaryb]
    return entries


def keep_only_sources(entries: list[DictionaryEntry], sources: list[str]) -> list[DictionaryEntry]:
    lsources: list[str] = [source.lower() for source in sources]
    return [entry for entry in entries if entry.data.source in lsources]


def split_word_entry(word_entry: WordEntry) -> list[WordEntry]:
    pronounce = word_entry.pronounce
    syllabary = word_entry.syllabary
    if "," not in syllabary and "," not in pronounce:
        return [word_entry]
    if "," not in pronounce or "," not in syllabary:
        raise RuntimeError(f"Mismatch between '{syllabary}' and '{pronounce}'")
    new_entries: list[WordEntry] = list()
    new_entry: WordEntry
    new_entry = WordEntry()
    new_entry.pronounce = pronounce[:pronounce.find(",")].strip()
    new_entry.syllabary = syllabary[:syllabary.find(",")].strip()
    new_entries.append(new_entry)
    new_entry = WordEntry()
    new_entry.pronounce = pronounce[pronounce.find(",")+1:].strip()
    new_entry.syllabary = syllabary[syllabary.find(",")+1:].strip()
    new_entries.append(new_entry)
    return new_entries


def reformat_definition(entry: DictionaryEntry) -> list[DictionaryEntry]:
    # right single quote to straight quote
    if "’" in entry.definition:
        entry.definition = entry.definition.replace("’", "'")

    if entry.definition.startswith("They're"):
        entry.definition = entry.definition.replace("They're", "They are")

    if entry.definition.startswith("they're"):
        entry.definition = entry.definition.replace("they're", "They are")

    if "it, them" in entry.definition:
        entry.definition = entry.definition.replace("it, them", "it or them")

    revised_entries: list[DictionaryEntry] = list()
    entry.definition = entry.definition.strip()
    lc_definition = entry.definition.strip().lower()

    if re.match("it \\(.*", lc_definition):
        ix = lc_definition.find("(")
        iy = lc_definition.find(")")
        entry.comment += " " + entry.definition[ix:iy+1]
        entry.definition = entry.definition[:ix] + entry.definition[iy+1:]
        entry.definition = re.sub("\\s\\s+", " ", entry.definition)
        return reformat_definition(entry)

    if re.match(".*\\d[ .]?.*", entry.definition):
        defs: list[str] = re.split("\\d[ .]?", entry.definition)
        for adef in defs:
            if not adef:
                continue
            dupe_entry = entry.copy()
            dupe_entry.definition = adef
            revised_entries.extend(reformat_definition(dupe_entry))
        return revised_entries

    if lc_definition.startswith("he, it's"):
        entry.definition = entry.definition[len("he, it's"):].strip()
        if "(he or it is)" not in entry.comment:
            entry.comment += f" (he or it is)"
        return reformat_definition(entry)

    if lc_definition.startswith("he, it is "):
        entry.definition = entry.definition[len("he, it is "):].strip()
        if "(he or it is)" not in entry.comment:
            entry.comment += f" (he or it is)"
        return reformat_definition(entry)

    if lc_definition.startswith("it's"):
        entry.definition = entry.definition[len("it's"):].strip()
        entry.comment += f" (it is)"
        return reformat_definition(entry)

    if lc_definition.startswith("it is being"):
        entry.definition = entry.definition[len("it is being"):].strip()
        entry.comment += f" (it is being)"
        return reformat_definition(entry)

    if lc_definition.startswith("it is"):
        entry.definition = entry.definition[len("it is"):].strip()
        entry.comment += f" (it is)"
        return reformat_definition(entry)

    if lc_definition.startswith("he's"):
        entry.definition = entry.definition[len("he's"):].strip()
        if "(he is)" not in entry.comment:
            entry.comment += f" (he is)"
        return reformat_definition(entry)

    if lc_definition.startswith("she's"):
        entry.definition = entry.definition[len("she's"):].strip()
        if "(she is)" not in entry.comment:
            entry.comment += f" (she is)"
        return reformat_definition(entry)

    if lc_definition.startswith("he/it is"):
        entry.definition = entry.definition[len("he/it is"):].strip()
        if "(he or it is)" not in entry.comment:
            entry.comment += f" (he or it is)"
        return reformat_definition(entry)

    if lc_definition.startswith("she/it is"):
        entry.definition = entry.definition[len("she/it is"):].strip()
        if "(she or it is)" not in entry.comment:
            entry.comment += f" (she or it is)"
        return reformat_definition(entry)

    if "him, it" in lc_definition:
        entry.definition = entry.definition.replace("him, it", "him or it")
        return reformat_definition(entry)

    if "it, him" in lc_definition:
        entry.definition = entry.definition.replace("it, him", "him or it")
        return reformat_definition(entry)

    if "her, it" in lc_definition:
        entry.definition = entry.definition.replace("her, it", "her or it")
        return reformat_definition(entry)

    if "it, her" in lc_definition:
        entry.definition = entry.definition.replace("it, her", "her or it")
        return reformat_definition(entry)

    if "his, her" in lc_definition:
        entry.definition = entry.definition.replace("his, her", "his or her")
        return reformat_definition(entry)

    if "her, his" in lc_definition:
        entry.definition = entry.definition.replace("her, his", "his or her")
        return reformat_definition(entry)

    if "him/it" in lc_definition:
        entry.definition = entry.definition.replace("him/it", "him or it")
        return reformat_definition(entry)

    if lc_definition.strip().endswith(","):
        entry.definition = entry.definition.strip()[:-1]
        return reformat_definition(entry)

    if lc_definition.startswith("he "):
        entry.definition = entry.definition[len("he "):].strip()
        if "(he)" not in entry.comment:
            entry.comment += f" (he)"
        return reformat_definition(entry)

    if lc_definition.startswith("he's"):
        entry.definition = entry.definition[len("he's"):].strip()
        if "(he is)" not in entry.comment:
            entry.comment += f" (he is)"
        return reformat_definition(entry)

    if lc_definition.startswith("his or her "):
        entry.definition = entry.definition[len("his or her "):].strip()
        if "(his or her)" not in entry.comment:
            entry.comment += f" (his or her)"
        return reformat_definition(entry)

    if lc_definition == "his or her":
        return [entry]

    if lc_definition.startswith("his "):
        entry.definition = entry.definition[len("his "):].strip()
        if "(his)" not in entry.comment:
            entry.comment += f" (his)"
        return reformat_definition(entry)

    if lc_definition.startswith("her "):
        entry.definition = entry.definition[len("her "):].strip()
        if "(her)" not in entry.comment:
            entry.comment += f" (her)"
        return reformat_definition(entry)

    if lc_definition.startswith("is "):
        entry.definition = entry.definition[len("is "):].strip()
        if "(is)" not in entry.comment:
            entry.comment += f" (is)"
        return reformat_definition(entry)

    if lc_definition.startswith("it "):
        entry.definition = entry.definition[len("it "):].strip()
        if "(it)" not in entry.comment:
            entry.comment += f" (it)"
        return reformat_definition(entry)

    if lc_definition.startswith("its "):
        entry.definition = entry.definition[len("its "):].strip()
        if "(his, hers, or its)" not in entry.comment:
            entry.comment += f" (his, hers, or its)"
        return reformat_definition(entry)

    if lc_definition.startswith("they are having "):
        entry.definition = entry.definition[len("they are having "):].strip()
        if "(they are having)" not in entry.comment:
            entry.comment += f" (they are having)"
        return reformat_definition(entry)

    if lc_definition.startswith("they are "):
        entry.definition = entry.definition[len("they are "):].strip()
        if "(they are)" not in entry.comment:
            entry.comment += f" (they are)"
        return reformat_definition(entry)

    if lc_definition.startswith("they're "):
        entry.definition = entry.definition[len("they're "):].strip()
        if "(they are)" not in entry.comment:
            entry.comment += f" (they are)"
        return reformat_definition(entry)

    if "(" in lc_definition:
        entry.comment += " " + entry.definition[entry.definition.find("("):].strip()
        entry.definition = entry.definition[:entry.definition.find("(")].strip()
        if not entry.definition:
            entry.definition = entry.comment[entry.comment.rfind(")")+1:].strip()
            entry.comment = entry.comment[:entry.comment.rfind(")")+1].strip()
        return reformat_definition(entry)

    definition = entry.definition

    if "," in definition:
        defs: list[str] = re.split(",", definition)
        for adef in defs:
            if not adef:
                continue
            dupe_entry = entry.copy()
            dupe_entry.definition = adef.strip()
            revised_entries.extend(reformat_definition(dupe_entry))
        return revised_entries

    if "/" in definition:
        defs: list[str] = re.split("/", definition)
        for adef in defs:
            if not adef:
                continue
            dupe_entry = entry.copy()
            dupe_entry.definition = adef
            revised_entries.extend(reformat_definition(dupe_entry))
        return revised_entries

    if ", he has" in definition or ",he has" in definition:
        defs: list[str] = re.split(", ?he", definition)
        for adef in defs:
            if not adef:
                continue
            dupe_entry = entry.copy()
            dupe_entry.definition = adef
            revised_entries.extend(reformat_definition(dupe_entry))
        return revised_entries

    if ", she has" in definition or ",she has" in definition:
        defs: list[str] = re.split(", ?she", definition)
        for adef in defs:
            if not adef:
                continue
            dupe_entry = entry.copy()
            dupe_entry.definition = adef
            revised_entries.extend(reformat_definition(dupe_entry))
        return revised_entries

    if " him," in definition and " him, it" not in definition and " him, her" not in definition:
        defs: list[str] = re.split(" him,", definition)
        for adef in defs:
            if not adef:
                continue
            dupe_entry = entry.copy()
            dupe_entry.definition = adef
            revised_entries.extend(reformat_definition(dupe_entry))
        return revised_entries

    if ", his " in definition or ",his " in definition:
        defs: list[str] = re.split(", ?his ", definition)
        for adef in defs:
            if not adef:
                continue
            dupe_entry = entry.copy()
            dupe_entry.definition = adef
            revised_entries.extend(reformat_definition(dupe_entry))
        return revised_entries

    if ", her " in definition or ",her " in definition:
        defs: list[str] = re.split(", ?her ", definition)
        for adef in defs:
            if not adef:
                continue
            dupe_entry = entry.copy()
            dupe_entry.definition = adef
            revised_entries.extend(reformat_definition(dupe_entry))
        return revised_entries

    if ", it's" in definition or ",it's" in definition:
        defs: list[str] = re.split(", ?it's", definition)
        for adef in defs:
            if not adef:
                continue
            dupe_entry = entry.copy()
            dupe_entry.definition = adef
            revised_entries.extend(reformat_definition(dupe_entry))
        return revised_entries

    if ", he's" in definition or ",he's" in definition:
        defs: list[str] = re.split(", ?he's", definition)
        for adef in defs:
            if not adef:
                continue
            dupe_entry = entry.copy()
            dupe_entry.definition = adef
            revised_entries.extend(reformat_definition(dupe_entry))
        return revised_entries

    if ", she's" in definition or ",she's" in definition:
        defs: list[str] = re.split(", ?she's", definition)
        for adef in defs:
            if not adef:
                continue
            dupe_entry = entry.copy()
            dupe_entry.definition = adef
            revised_entries.extend(reformat_definition(dupe_entry))
        return revised_entries

    return [entry]


def reformat_definitions(entries: list[DictionaryEntry]) -> list[DictionaryEntry]:
    revised_entries: list[DictionaryEntry] = list()

    for entry in entries:
        revised_entries.extend(reformat_definition(entry))

    return revised_entries


def split_cherokee_alts(entries: list[DictionaryEntry]) -> list[DictionaryEntry]:
    revised_entries: list[DictionaryEntry] = list()
    for entry in entries:
        revised_entries.extend(split_cherokee_alt(entry))
    return revised_entries


def split_cherokee_alt(entry: DictionaryEntry) -> list[DictionaryEntry]:
    revised_entries: list[DictionaryEntry] = list()

    if entry.first_singular_present.needs_split():
        wf = entry.first_singular_present
        syl = wf.syllabary
        pron = wf.pronounce

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = ("(an.) " + entry.comment).strip()
        new_entry.first_singular_present.syllabary = syl[:syl.find(",")].strip()
        new_entry.first_singular_present.pronounce = pron[:pron.find(",")].strip()
        revised_entries.append(new_entry)

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = ("(in.) " + entry.comment).strip()
        new_entry.first_singular_present.syllabary = syl[syl.find(",")+1:].strip()
        new_entry.first_singular_present.pronounce = pron[pron.find(",")+1:].strip()
        revised_entries.append(new_entry)

        wf.pronounce = ""
        wf.syllabary = ""

    if entry.second_singular_immediate.needs_split():
        wf = entry.second_singular_immediate
        syl = wf.syllabary
        pron = wf.pronounce

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = ("(in.) " + entry.comment).strip()
        new_entry.second_singular_immediate.syllabary = syl[:syl.find(",")].strip()
        new_entry.second_singular_immediate.pronounce = pron[:pron.find(",")].strip()
        revised_entries.append(new_entry)

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = ("(an.) " + entry.comment).strip()
        new_entry.second_singular_immediate.syllabary = syl[syl.find(",")+1:].strip()
        new_entry.second_singular_immediate.pronounce = pron[pron.find(",")+1:].strip()
        revised_entries.append(new_entry)

        wf.pronounce = ""
        wf.syllabary = ""

    if entry.third_singular_present.needs_split():
        wf = entry.third_singular_present
        syl = wf.syllabary
        pron = wf.pronounce

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = entry.comment.strip()
        new_entry.third_singular_present.syllabary = syl[:syl.find(",")].strip()
        new_entry.third_singular_present.pronounce = pron[:pron.find(",")].strip()
        revised_entries.append(new_entry)

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = entry.comment.strip()
        new_entry.third_singular_present.syllabary = syl[syl.find(",")+1:].strip()
        new_entry.third_singular_present.pronounce = pron[pron.find(",")+1:].strip()
        revised_entries.append(new_entry)

        wf.pronounce = ""
        wf.syllabary = ""

        if entry.third_singular_present:
            raise RuntimeError(f"{entry.third_singular_present}")

    if entry.third_singular_deverbal.needs_split():
        wf = entry.third_singular_deverbal
        syl = wf.syllabary
        pron = wf.pronounce

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = entry.comment.strip()
        new_entry.third_singular_deverbal.syllabary = syl[:syl.find(",")].strip()
        new_entry.third_singular_deverbal.pronounce = pron[:pron.find(",")].strip()
        revised_entries.append(new_entry)

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = entry.comment.strip()
        new_entry.third_singular_deverbal.syllabary = syl[syl.find(",")+1:].strip()
        new_entry.third_singular_deverbal.pronounce = pron[pron.find(",")+1:].strip()
        revised_entries.append(new_entry)

        wf.pronounce = ""
        wf.syllabary = ""

    if entry.third_singular_remote_past.needs_split():
        wf = entry.third_singular_remote_past
        syl = wf.syllabary
        pron = wf.pronounce

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = entry.comment.strip()
        new_entry.third_singular_remote_past.syllabary = syl[:syl.find(",")].strip()
        new_entry.third_singular_remote_past.pronounce = pron[:pron.find(",")].strip()
        revised_entries.append(new_entry)

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = entry.comment.strip()
        new_entry.third_singular_remote_past.syllabary = syl[syl.find(",")+1:].strip()
        new_entry.third_singular_remote_past.pronounce = pron[pron.find(",")+1:].strip()
        revised_entries.append(new_entry)

        wf.pronounce = ""
        wf.syllabary = ""

    if entry.third_plural_noun_adj.needs_split():
        wf = entry.third_plural_noun_adj
        syl = wf.syllabary
        pron = wf.pronounce

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = entry.comment.strip()
        new_entry.third_plural_noun_adj.syllabary = syl[:syl.find(",")].strip()
        new_entry.third_plural_noun_adj.pronounce = pron[:pron.find(",")].strip()
        revised_entries.append(new_entry)

        new_entry = DictionaryEntry()
        new_entry.definition = entry.definition
        new_entry.pos = entry.pos
        new_entry.comment = entry.comment.strip()
        new_entry.third_plural_noun_adj.syllabary = syl[syl.find(",")+1:].strip()
        new_entry.third_plural_noun_adj.pronounce = pron[pron.find(",")+1:].strip()
        revised_entries.append(new_entry)

        wf.pronounce = ""
        wf.syllabary = ""

    if entry.has_entries():
        revised_entries.append(entry)

    return revised_entries


def ced2mco(pronunciation: WordEntry) -> None:
    from chr_utils.chrutils import ascii_ced2mco
    if pronunciation:
        pronunciation.pronounce = ascii_ced2mco(pronunciation.pronounce)


def fix_pronunciations(entries: list[DictionaryEntry]) -> None:
    for entry in entries:
        ced2mco(entry.third_singular_present)
        ced2mco(entry.first_singular_present)
        ced2mco(entry.third_plural_noun_adj)
        ced2mco(entry.third_singular_remote_past)
        ced2mco(entry.third_singular_habitual)
        ced2mco(entry.second_singular_immediate)
        ced2mco(entry.third_singular_deverbal)


def main() -> None:
    work_path: Path = Path(__file__).parent
    csv_path: Path = work_path.joinpath("likespreadsheets_202306141826.csv")

    entries: list[DictionaryEntry] = list()
    with csv_path.open("r") as csv_input:
        reader = csv.DictReader(csv_input)
        row: dict[str, str]
        for row in reader:
            entry = DictionaryEntry.from_dict(row)
            entries.append(entry)

    entries = keep_only_sources(entries, ["ced"])

    entries = remove_unwanted_entries(entries)
    entries = reformat_definitions(entries)

    with Path("definitions-to-check.csv").open("w") as w:
        for entry in entries:
            if "," in entry.definition:
                w.write(entry.third_singular_present.syllabary)
                w.write("\t")
                w.write(entry.definition)
                w.write("\n")

    entries = split_cherokee_alts(entries)
    fix_pronunciations(entries)
    entries.sort(key=lambda x: x.definition.lower())
    glossary_path = Path("ced-glossary.txt")
    with glossary_path.open("w") as w:
        entry: DictionaryEntry
        for entry in entries:
            e = entry.third_singular_present
            if e:
                if entry.pos == "v":
                    w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment} (3rd/present continuous)".strip())
                elif entry.pos == "n":
                    w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment} (single noun)".strip())
                elif entry.pos == "a":
                    w.write(f"{entry.definition}\t{e.tabbed()} (adj/adv) {entry.comment}".strip())
                else:
                    w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment}".strip())
                w.write("\n")
            e = entry.third_plural_noun_adj
            if e:
                if entry.pos == "n":
                    w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment} (plural noun)".strip())
                elif entry.pos == "a":
                    w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment} (plural adj/adv)".strip())
                else:
                    w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment}".strip())
                w.write("\n")
            e = entry.first_singular_present
            if e:
                w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment} (1st/present continuous)".strip())
                w.write("\n")
            e = entry.third_singular_remote_past
            if e:
                w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment} (3rd/remote past)".strip())
                w.write("\n")
            e = entry.third_singular_habitual
            if e:
                w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment} (3rd/habitual)".strip())
                w.write("\n")
            e = entry.second_singular_immediate
            if e:
                if e.syllabary.strip().endswith("ᏍᏗ"):
                    w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment} (2nd/future progressive)".strip())
                else:
                    w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment} (2nd/immediate)".strip())
                w.write("\n")
            e = entry.third_singular_deverbal
            if e:
                w.write(f"{entry.definition}\t{e.tabbed()} {entry.comment} (3rd/deverbal)".strip())
                w.write("\n")


if __name__ == '__main__':
    main()
