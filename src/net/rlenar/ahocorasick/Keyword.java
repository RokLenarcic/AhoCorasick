package net.rlenar.ahocorasick;

class Keyword {
	Keyword alsoContains;
	final String word;

	Keyword(final String word) {
		this.word = word;
	}

	@Override
	public String toString() {
		return word + (alsoContains == null ? "" : " " + alsoContains);
	}
}