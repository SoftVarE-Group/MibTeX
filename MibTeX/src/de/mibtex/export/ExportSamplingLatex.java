package de.mibtex.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.mibtex.BibtexEntry;

public class ExportSamplingLatex extends ExportSampling {

	public ExportSamplingLatex(String path, String file) throws Exception {
		super(path, file);
		fileName = "sampling.tex";
	}

	public final static String[] TAG_CATEGORIES = { "Input Data", "Algorithm", "Coverage", "Evaluation",
			"Application" };

	public final static String KEYS = "ABW:ASE14,AKT+:GPCE16,ALL+:VACE17,AMK+:GPCE16,ATL+:SoSyM16,AGV:ICST15,HSMI:ICST14,CR:IJAST14,DPL+:VaMoS15,FKPV:CEC16,EB+:ITNG11,EBG:CAiSE12,FLS+:CIM17,FLV:SBES17,FV:SBES15,FLRE:SBST16,GCD:ESE11,HNA+:TR17,HLHE:VaMoS13,HPL:SBSE14,HPP+:TSE14,HPP+:SPLC13,JHF:MODELS11,MHF:SPLC12,JHF+:MODELS12,KBK:AOSD11,KBBK:RV10,KSS:VariComp13,LKA+:ESECFSE13,LHF+:CEC14,MGSH:SPLC13,MKR+:ICSE16,OMR:SPLC10,OZLG:SPLC11,POS+:SQJ12,PSK+:ICST10,RBR+:SPLC15,SCD:FASE12,TDS+:ATC14,TLD:OSR12";
	
	public List<String> keyList = new ArrayList<String>();

	public List<String> valueList = new ArrayList<String>();

	public final static String NEW_COLUMN = "&";

	public final static String NEW_ROW = "\\\\";

	void writeHeader(BufferedWriter out) throws IOException {
		out.append("\\newcolumntype{h}{>{\\columncolor{blue!10}}c}" + System.lineSeparator());
		out.append("\\begin{tabular}{");
		int numberOfColumns = numberOfColumns();
		out.append("r");
		for (int i = 1; i < numberOfColumns; i++) {
			out.append(i % 2 == 0 ? "c" : "h");
		}
		out.append("}" + System.lineSeparator());
		out.append("\\toprule" + System.lineSeparator());
		writeCategories(out);
		writeTags(out);
		out.append("\\midrule" + System.lineSeparator());
	}

	void writeFooter(BufferedWriter out) throws IOException {
		out.append("\\bottomrule" + System.lineSeparator());
		out.append("\\end{tabular}" + System.lineSeparator());
	}

	void writeCategories(BufferedWriter out) throws IOException {
		for (int category = 0; category < TAG_CATEGORIES.length; category++) {
			out.append(NEW_COLUMN);
			out.append("\\multicolumn{" + TAGS[category].length + "}{");
			out.append(category % 2 == 1 ? "c" : "h");
			out.append("}{" + TAG_CATEGORIES[category] + "}");
		}
		out.append(newRow());
	}

	void writeTags(BufferedWriter out) throws IOException {
		out.append(rotateText("Publication"));
		for (int category = 0; category < TAGS.length; category++) {
			for (int tag = 0; tag < TAGS[category].length; tag++) {
				out.append(NEW_COLUMN);
				out.append(rotateText(TAGS[category][tag]));
			}
		}
		out.append(newRow());
	}

	void writeEntries(BufferedWriter out) throws IOException {
		readPredefinedKeys();
		readEntries();
		for (int i = 0; i < valueList.size(); i++) {
			String value = valueList.get(i);
			out.append(value.isEmpty() ? defaultValue(i) : value);
			if (i % 5 == 4 && i < valueList.size()-1) {
				out.append("\\addlinespace[2pt]" + System.lineSeparator());
			}
		}
	}

	String defaultValue(int i) {
		return keyList.get(i) + NEW_ROW;
	}

	void readPredefinedKeys() {
		StringTokenizer tokenizer = new StringTokenizer(KEYS, ",");
		while (tokenizer.hasMoreTokens()) {
			String key = tokenizer.nextToken();
			keyList.add(key);
			valueList.add("");
		}
	}

	void readEntries() {
		for (BibtexEntry entry : entries.values()) {
			for (List<String> tags : entry.tagList.values()) {
				if (isEntryInScope(tags)) {
					readEntry(entry, tags);
				}
			}
		}
	}

	void readEntry(BibtexEntry entry, List<String> tags) {
		StringBuilder builder = new StringBuilder();
		builder.append("\\cite{" + entry.key + "}");
		for (int category = 0; category < TAGS.length; category++) {
			for (int tag = 0; tag < TAGS[category].length; tag++) {
				builder.append(NEW_COLUMN);
				builder.append(tags.contains(TAGS[category][tag]) ? "\\cellcolor{blue!50}" : "");
			}
		}
		builder.append(newRow());
		String key = entry.key;
		String value = builder.toString();
		if (keyList.contains(key)) {
			int i = keyList.lastIndexOf(key);
			if (valueList.get(i).isEmpty()) {
				valueList.set(i, value);
			}
			else {
				keyList.add(i+1, key);
				valueList.add(i+1, value);
			}
		}
		else {
			keyList.add(key);
			valueList.add(value);
		}
	}

	String rotateText(String text) {
		return "\\rotatebox{90}{" + text + "}";
	}

	String newRow() {
		return NEW_ROW + System.lineSeparator();
	}

	int numberOfColumns() {
		int number = 1;
		for (int category = 0; category < TAGS.length; category++) {
			number += TAGS[category].length;
		}
		return number;
	}

}
